/**
 * Automation build for game server (application, worker)
 */
def _game = 'bpc'
def _git_url = 'https://git.sixthgearstudios.com/mc/bpc/bpc-server'
def _builds = ['develop/debug', 'develop/p1', 'develop/s1', 'staging/p1', 'staging/s1', 'release/p1']
def _aws_region = 'us-west-2'
def _ecr_region = 'us-west-2'
def _ecr_repo = '636583053830.dkr.ecr.us-west-2.amazonaws.com'
def _ecr_repo_prefix = _ecr_repo + '/bpc-'
def _url_patterns = [develop: 'https://api-%s.dev.bpc.alleylabs.com', staging: 'https://api-%s.staging.bpc.alleylabs.com', release: 'https://api-%s.bpc.alleylabs.com']
def _slack_channels = [develop: 'bpc-dev-builds', staging: 'bpc-staging-builds', release: 'bpc-live-builds']

/**
 * Jenkins Declarative Pipeline
 */
pipeline {
    agent { label 'server' }

    // build option
    parameters {
        choice name: 'PARAM_BRANCH', choices: _builds, description: 'Build branch'
        booleanParam name: 'PARAM_ECS', defaultValue: false, description: 'Skip reload ecs'
    }

    // constants
    environment {
        GIT_URL = "${_git_url}"
        GIT_CREDENTIALS = 'git_credentials'
        AWS_CREDENTIALS = 'aws_credentials'
        AWS_ROLE = credentials('aws_role_arn')
    }

    options {
        gitLabConnection('server')

        // use lock resource: https://issues.jenkins-ci.org/browse/JENKINS-50176
        // or use milestone: https://issues.jenkins-ci.org/browse/JENKINS-48510
        disableConcurrentBuilds()
    }

    // accept trigger from gitlab push for build branches
    triggers {
        gitlab(triggerOnPush: true, secretToken: '5a331911d9e9d9e1d4f63530222152bb',
                branchFilterType: 'NameBasedFilter', includeBranchesSpec: "${_builds.join(',')}")
    }

    stages {
        // prepare env
        stage('Prepare') {
            steps {
                script {
                    // build environment and version
                    def buildBranch = env.gitlabBranch ? env.gitlabBranch : params.PARAM_BRANCH
                    env._BUILD_BRANCH = buildBranch
                    env._BUILD_ENV = buildBranch.split('/')[0]
                    env._BUILD_VERSION = buildBranch.split('/')[1]
                    // reload ecs
                    if (params.PARAM_ECS) {
                        env._RELOAD_ECS = '0'
                    } else {
                        env._RELOAD_ECS = '1'
                    }
                    env._API_URL = String.format(_url_patterns.get(env._BUILD_ENV), env._BUILD_VERSION)
                    // aws session
                    env._AWS_SESSION = sh(returnStdout: true, script: 'echo `whoami`-`date +%Y%m%d`').trim()
                    // ecr
                    env._ECR_REPO = _ecr_repo_prefix + env._BUILD_ENV
                    // ecs services
                    env._AWS_REGION = _aws_region
                    env._ECS_CLUSTER = "${_game}-${env._BUILD_ENV}-${env._BUILD_VERSION}-cluster"
                    // slack
                    env._SLACK_CHANNEL = _slack_channels.get(env._BUILD_ENV)

                    currentBuild.displayName = "Build Server ${env._BUILD_ENV} ${env._BUILD_VERSION}"
                    currentBuild.description = "Build game server for ${env._BUILD_ENV} ${env._BUILD_VERSION}"
                }
            }
        }

        stage('Checkout source code') {
            steps {
                deleteDir()
                checkout scm: [
                        $class                           : 'GitSCM',
                        userRemoteConfigs                : [[credentialsId: env.GIT_CREDENTIALS, url: env.GIT_URL]],
                        branches                         : [[name: env._BUILD_BRANCH]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions                       : [
                                [$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: true, recursiveSubmodules: true, trackingSubmodules: false],
                                [$class: 'CleanBeforeCheckout'],
                                [$class: 'CloneOption', depth: 0, noTags: false, reference: '', shallow: false]
                        ],
                        submoduleCfg                     : [],
                ], changelog: false, poll: false

                script {
                    env._CODE_HASH = sh(returnStdout: true, script: 'git rev-parse --short=8 HEAD').trim()
                    env._COMMIT_AUTHOR = sh(returnStdout: true, script: 'git log -1 --pretty=%aN').trim()
                    env._COMMIT_SUBJECT = sh(returnStdout: true, script: 'git log -1 --pretty=%s').trim()
                }
            }
        }

        stage('Build source') {
            steps {
                catchError {
                    sh './gradlew buildAll'
                }
            }
        }

        stage('Build Docker image') {
            environment {
                ENV_ECR = "${env._ECR_REPO}"
                ENV_VERSION = "${env._BUILD_ENV}_${env._BUILD_VERSION}"
            }
            steps {
                sh "docker-compose -f docker/ecs_docker-compose.yml build --build-arg code_hash=${env._CODE_HASH}"
            }
        }

//        stage('Perform integration test') {
//            when { equals expected: 'release', actual: env._BUILD_ENV }
//            steps {
//                sh "./gradlew unitTest -Pargs=${ENV}"
//                junit 'build/test-results/unitTest/*.xml'
//            }
//        }

        stage('Confirm update') {
            when { equals expected: 'release', actual: env._BUILD_ENV }
            options { timeout(time: 1, unit: 'HOURS') }
            steps {
                input message: 'Confirm build RELEASE?'
            }
            post {
                success { echo 'approved' }
                unsuccessful { echo 'Build not approved' }
            }
        }

        stage("Push Docker image") {
            environment {
                ENV_ECR = "${env._ECR_REPO}"
                ENV_VERSION = "${env._BUILD_ENV}_${env._BUILD_VERSION}"
            }
            steps {
                withAWS(region: env._AWS_REGION, credentials: env.AWS_CREDENTIALS, role: env.AWS_ROLE, duration: 3600, roleSessionName: env._AWS_SESSION) {
                    sh "aws ecr get-login-password --region ${_ecr_region}" +
                            "| docker login --username AWS --password-stdin ${_ecr_repo}"
                }
                sh 'docker-compose -f docker/ecs_docker-compose.yml push'
            }
        }

        stage("Reload ECS") {
            when { equals expected: '1', actual: env._RELOAD_ECS }
            steps {
                withAWS(region: env._AWS_REGION, credentials: env.AWS_CREDENTIALS, role: env.AWS_ROLE, duration: 3600, roleSessionName: env._AWS_SESSION) {
                    script {
                        def ecsServices = ['worker', 'application']
                        for (String ecsService : ecsServices) {
                            sh "aws ecs update-service --cluster ${env._ECS_CLUSTER} --service ${ecsService} --force-new-deployment"
                        }
                        for (String ecsService : ecsServices) {
                            sh "aws ecs wait services-stable --cluster ${env._ECS_CLUSTER} --service ${ecsService}"
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                if (env._RELOAD_ECS == '1' && !env._SLACK_CHANNEL.isEmpty()) {
                    def msgAttachments = [
                            [
                                    mrkdwn_in: ['text'],
                                    fallback : "Server deployed on ${env._BUILD_ENV}",
                                    color    : '#36a64f',
                                    pretext  : "Server deployed on <${env._API_URL}|${env._BUILD_ENV}>",
                                    text     : "*Build:* ${env._BUILD_ENV} ${env._BUILD_VERSION}\n" +
                                            "*Build Number:* <${env.BUILD_URL}|#${env.BUILD_NUMBER}>\n" +
                                            "*Code hash:* <${_git_url}/commit/${env._CODE_HASH}|${env._CODE_HASH}>\n" +
                                            "*Git Author:* ${env._COMMIT_AUTHOR}",
                                    "fields" : [
                                            [title: 'Git Commit', value: "${env._COMMIT_SUBJECT}"]
                                    ]
                            ]
                    ]
                    slackSend(botUser: true, teamDomain: 'scorepuzzle', tokenCredentialId: 'slack-ci-token', channel: env._SLACK_CHANNEL, attachments: msgAttachments)
                }
            }
        }
        failure {
            script {
                if (env._RELOAD_ECS == '1' && !env._SLACK_CHANNEL.isEmpty()) {
                    def msgAttachments = [
                            [
                                    mrkdwn_in: ['text'],
                                    fallback : "Server deployment failure on ${env._BUILD_ENV}",
                                    color    : '#c91e1e',
                                    pretext  : "Server deployment failure on ${env._BUILD_ENV}",
                                    text     : "*Build:* ${env._BUILD_ENV} ${env._BUILD_VERSION}\n" +
                                            "*Build Number:* <${env.BUILD_URL}|#${env.BUILD_NUMBER}>\n" +
                                            "*Code hash:* <${_git_url}/commit/${env._CODE_HASH}|${env._CODE_HASH}>\n" +
                                            "*Git Author:* ${env._COMMIT_AUTHOR}",
                                    "fields" : [
                                            [title: 'Git Commit', value: "${env._COMMIT_SUBJECT}"]
                                    ]
                            ]
                    ]
                    slackSend(botUser: true, teamDomain: 'scorepuzzle', tokenCredentialId: 'slack-ci-token', channel: env._SLACK_CHANNEL, attachments: msgAttachments)
                }
            }
        }
    }
}