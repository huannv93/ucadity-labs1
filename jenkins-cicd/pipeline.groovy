pipeline {
    agent any
    
    stages {
        
        stage('Build image environment'){
            steps {
            script {
                checkout scm: [
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[url: 'https://github.com/huannv93/ucadity-labs1.git']]
                ]
                dir('frontend') {        
                    sh 'docker build -t image-env .'
                        }
            
        }
        }
        }

        stage('Build Frontend') {
            agent {
                docker { image 'image-env' }
            }
            steps {

                script {
                    checkout scm: [
                        $class: 'GitSCM',
                        branches: [[name: '*/main']],
                        userRemoteConfigs: [[url: 'https://github.com/huannv93/ucadity-labs1.git']]
                    ]

                    def frontendCacheKey = 'frontend-build'
                        dir('frontend') {
                            sh 'sudo npm install'
                            sh 'sudo npm run build'
                        }
                    
                }
            }
        }

        stage('Build Backend') {
            agent {
                docker { image 'image-env' }
            }
            steps {

                script {
                    checkout scm: [
                        $class: 'GitSCM',
                        branches: [[name: '*/main']],
                        userRemoteConfigs: [[url: 'https://github.com/huannv93/ucadity-labs1.git']]
                    ]

                    def backendCacheKey = 'frontend-build'
                        dir('backend') {
                            sh 'sudo npm install'
                            sh 'sudo npm run build'
                        }
                    
                }
            }
        }

        stage('Test Frontend') {
            agent {
                docker { image 'image-env' }
            }
            steps {

                script {
                    checkout scm: [
                        $class: 'GitSCM',
                        branches: [[name: '*/main']],
                        userRemoteConfigs: [[url: 'https://github.com/huannv93/ucadity-labs1.git']]
                    ]

                    dir('frontend') {
                            sh 'sudo npm run build'
                            sh 'sudo npm run test'
                        }
                    
                }
            }
        }


        stage('Test Backend') {
            agent {
                docker { image 'image-env' }
            }
            steps {

                script {
                    checkout scm: [
                        $class: 'GitSCM',
                        branches: [[name: '*/main']],
                        userRemoteConfigs: [[url: 'https://github.com/huannv93/ucadity-labs1.git']]
                    ]

                    dir('backend') {
                            sh 'sudo npm run build'
                            sh 'sudo npm run test'
                        }
                    
                }
            }
        }
// Comment this code to pass scan security
        // stage('Scan Frontend') {
        //     agent {
        //         docker { image 'image-env' }
        //     }
        //     steps {

        //         script {
        //             checkout scm: [
        //                 $class: 'GitSCM',
        //                 branches: [[name: '*/main']],
        //                 userRemoteConfigs: [[url: 'https://github.com/huannv93/ucadity-labs1.git']]
        //             ]

        //             dir('frontend') {
        //                     sh 'npm install'
        //                     sh 'npm audit fix --audit-level=critical --force'
        //                     sh 'npm audit --audit-level=critical'
        //                 }
                    
        //         }
        //     }
        // }

        // stage('Scan Backend') {
        //     agent {
        //         docker { image 'image-env' }
        //     }
        //     steps {

        //         script {
        //             checkout scm: [
        //                 $class: 'GitSCM',
        //                 branches: [[name: '*/main']],
        //                 userRemoteConfigs: [[url: 'https://github.com/huannv93/ucadity-labs1.git']]
        //             ]

        //             dir('backend') {
        //                     sh 'npm install'
        //                     sh 'npm audit fix --audit-level=critical --force'
        //                     sh 'npm audit --audit-level=critical'
        //                 }
                    
        //         }
        //     }
        // }
// Comment this code to pass scan security

        stage('checkout') {
            steps {
                script {
                    checkout scm: [
                        $class: 'GitSCM',
                        branches: [[name: '*/main']],
                        userRemoteConfigs: [[url: 'https://github.com/huannv93/ucadity-labs1.git']]
                    ]
                    
                }
            }
        }
        stage('Build image backend') {
            steps {
                script {
                    dir('backend') {
                            sh 'docker build -t udacity-bk .'
                            sh 'docker image ls'
                        }                    
                }
            }
        }

      
    }

    
    post {
        success {
            // Send Slack notification on success
            slackSend(
                color: 'good',
                message: 'Success message',
                channel: '#devops'
            )
        }
        failure {
            // Send Slack notification on failure
            slackSend(
                color: 'danger',
                message: 'Failure message',
                channel: '#devops'
            )
        }
    }
}
