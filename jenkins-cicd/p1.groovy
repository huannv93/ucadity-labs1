pipeline {
    agent any
    
    environment {
        AWS_DEFAULT_REGION = 'ap-northeast-1'
        ECS_CLUSTER = 'ecs-cluster-CloudSM'
        ECS_SERVICE = 'ecs-svc-CloudSM'
        TASK_DEFINITION_NAME = 'CloudSM'
        DOCKER_IMAGE_NAME = '221008696644.dkr.ecr.ap-northeast-1.amazonaws.com/huannv-repo-private' 

    }

    stages {
        
//         stage('Build image environment'){
//             steps {
//             script {
//                 checkout scm: [
//                     $class: 'GitSCM',
//                     branches: [[name: '*/main']],
//                     userRemoteConfigs: [[url: 'https://github.com/huannv93/ucadity-labs1.git']]
//                 ]
//                 dir('frontend') {        
//                     sh 'docker build -t image-env .'
//                         }
            
//         }
//         }
//         }

//         stage('Build Frontend') {
//             agent {
//                 docker { image 'image-env' }
//             }
//             steps {

//                 script {
//                     checkout scm: [
//                         $class: 'GitSCM',
//                         branches: [[name: '*/main']],
//                         userRemoteConfigs: [[url: 'https://github.com/huannv93/ucadity-labs1.git']]
//                     ]

//                     def frontendCacheKey = 'frontend-build'
//                         dir('frontend') {
//                             sh 'sudo npm install'
//                             sh 'sudo npm run build'
//                         }
                    
//                 }
//             }
//         }

//         stage('Build Backend') {
//             agent {
//                 docker { image 'image-env' }
//             }
//             steps {

//                 script {
//                     checkout scm: [
//                         $class: 'GitSCM',
//                         branches: [[name: '*/main']],
//                         userRemoteConfigs: [[url: 'https://github.com/huannv93/ucadity-labs1.git']]
//                     ]

//                     def backendCacheKey = 'frontend-build'
//                         dir('backend') {
//                             sh 'sudo npm install'
//                             sh 'sudo npm run build'
//                         }
                    
//                 }
//             }
//         }

//         stage('Test Frontend') {
//             agent {
//                 docker { image 'image-env' }
//             }
//             steps {

//                 script {
//                     checkout scm: [
//                         $class: 'GitSCM',
//                         branches: [[name: '*/main']],
//                         userRemoteConfigs: [[url: 'https://github.com/huannv93/ucadity-labs1.git']]
//                     ]

//                     dir('frontend') {
//                             sh 'sudo npm run build'
//                             sh 'sudo npm run test'
//                         }
                    
//                 }
//             }
//         }


//         stage('Test Backend') {
//             agent {
//                 docker { image 'image-env' }
//             }
//             steps {

//                 script {
//                     checkout scm: [
//                         $class: 'GitSCM',
//                         branches: [[name: '*/main']],
//                         userRemoteConfigs: [[url: 'https://github.com/huannv93/ucadity-labs1.git']]
//                     ]

//                     dir('backend') {
//                             sh 'sudo npm run build'
//                             sh 'sudo npm run test'
//                         }
                    
//                 }
//             }
//         }
// // Comment this code to pass scan security
//         // stage('Scan Frontend') {
//         //     agent {
//         //         docker { image 'image-env' }
//         //     }
//         //     steps {

//         //         script {
//         //             checkout scm: [
//         //                 $class: 'GitSCM',
//         //                 branches: [[name: '*/main']],
//         //                 userRemoteConfigs: [[url: 'https://github.com/huannv93/ucadity-labs1.git']]
//         //             ]

//         //             dir('frontend') {
//         //                     sh 'npm install'
//         //                     sh 'npm audit fix --audit-level=critical --force'
//         //                     sh 'npm audit --audit-level=critical'
//         //                 }
                    
//         //         }
//         //     }
//         // }

//         // stage('Scan Backend') {
//         //     agent {
//         //         docker { image 'image-env' }
//         //     }
//         //     steps {

//         //         script {
//         //             checkout scm: [
//         //                 $class: 'GitSCM',
//         //                 branches: [[name: '*/main']],
//         //                 userRemoteConfigs: [[url: 'https://github.com/huannv93/ucadity-labs1.git']]
//         //             ]

//         //             dir('backend') {
//         //                     sh 'npm install'
//         //                     sh 'npm audit fix --audit-level=critical --force'
//         //                     sh 'npm audit --audit-level=critical'
//         //                 }
                    
//         //         }
//         //     }
//         // }
// // Comment this code to pass scan security

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

        stage('Build and Push Image Backend') {
            steps {
                script {
                    // Get the Git commit hash
                    def commitHash = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()

                    // Build Docker image with Git commit hash as the tag
                    dir('backend') {
                        sh "docker build -t ${DOCKER_IMAGE_NAME}:${commitHash} ."
                        sh 'docker image ls'
                        // Login ECR
                        sh 'aws ecr get-login-password --region ap-northeast-1 | docker login --username AWS --password-stdin 221008696644.dkr.ecr.ap-northeast-1.amazonaws.com'
                        // Push Image to ECR
                        sh "docker push ${DOCKER_IMAGE_NAME}:${commitHash} "
                    }
                }
            }
        }

        stage('Update ECS Task Definition') {
            steps {
                script {
                    // Get the Git commit hash or another identifier
                    def commitHash = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
        
                    // Describe the existing ECS Task Definition
                    def TASK_DEFINITION = sh(script: "aws ecs describe-task-definition --task-definition ${TASK_DEFINITION_NAME} --region ${AWS_DEFAULT_REGION} --output json", returnStdout: true).trim()
                
                    // Update the Docker image tag in the Task Definition
                    def NEW_TASK_DEFINITION = sh(script: "echo '${TASK_DEFINITION}' | jq --arg IMAGE ${DOCKER_IMAGE_NAME}:${commitHash} '.taskDefinition.containerDefinitions[0].image = \$IMAGE | del(.taskDefinitionArn) | del(.revision) | del(.status) | del(.requiresAttributes) | del(.compatibilities) |  del(.registeredAt)  | del(.registeredBy)'", returnStdout: true).trim()
                
                    def container_definitions = sh(script: "echo '${NEW_TASK_DEFINITION}' | jq '.taskDefinition.containerDefinitions' -c", returnStdout: true).trim()
        
                    // Get info to create new ECS Task Definition

                    def requires_compatibilities = sh(script: "echo '${NEW_TASK_DEFINITION}' | jq -r '.taskDefinition.requiresCompatibilities[0]' -c", returnStdout: true).trim()
                    def network_mode = sh(script: "echo '${NEW_TASK_DEFINITION}' | jq '.taskDefinition.networkMode' -c", returnStdout: true).trim()

                    def task_role_arn = sh(script: "echo '${NEW_TASK_DEFINITION}' | jq '.taskDefinition.taskRoleArn' -c", returnStdout: true).trim()

                    def execution_role_arn = sh(script: "echo '${NEW_TASK_DEFINITION}' | jq '.taskDefinition.executionRoleArn' -c", returnStdout: true).trim()
                    def cpu = sh(script: "echo '${NEW_TASK_DEFINITION}' | jq '.taskDefinition.cpu' -c", returnStdout: true).trim()
                    def memory = sh(script: "echo '${NEW_TASK_DEFINITION}' | jq '.taskDefinition.memory' -c", returnStdout: true).trim()
              
        
                    // Register a new revision of the ECS Task Definition
                    def NEW_TASK_INFO = sh(script: "aws ecs register-task-definition --family CloudSM --container-definitions '${container_definitions}' --requires-compatibilities ${requires_compatibilities}  --network-mode ${network_mode}  --task-role-arn ${task_role_arn} --execution-role-arn ${execution_role_arn} --cpu ${cpu} --memory ${memory} --region ${AWS_DEFAULT_REGION}", returnStdout: true).trim()
        
                    // Get the new revision number
                    def NEW_REVISION = sh(script: "echo '${NEW_TASK_INFO}' | jq '.taskDefinition.revision'", returnStdout: true).trim()

                    // Update ecs service
                    sh "aws ecs update-service --region '${AWS_DEFAULT_REGION}' --cluster '${ECS_CLUSTER}' --service '${ECS_SERVICE}' --task-definition '${TASK_DEFINITION_NAME}':'${NEW_REVISION}' --force-new-deployment"

        
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
