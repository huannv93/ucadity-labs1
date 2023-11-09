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
        
                    echo "Container Definitions: ${TASK_DEFINITION}"
        
                    // Update the Docker image tag in the Task Definition
                    def NEW_TASK_DEFINITION = sh(script: "echo '${TASK_DEFINITION}' | jq --arg IMAGE ${DOCKER_IMAGE_NAME}:${commitHash} '.taskDefinition.containerDefinitions[0].image = \$IMAGE | del(.taskDefinitionArn) | del(.revision) | del(.status) | del(.requiresAttributes) | del(.compatibilities) |  del(.registeredAt)  | del(.registeredBy)'", returnStdout: true).trim()
        
                    echo "Container Definitions: ${NEW_TASK_DEFINITION}"
        
                    def container_definitions = sh(script: "echo '${NEW_TASK_DEFINITION}' | jq '.taskDefinition.containerDefinitions' -c", returnStdout: true).trim()
        
                    echo "Container Definitions: ${container_definitions}"
        
                    // Register a new revision of the ECS Task Definition
                    def NEW_TASK_INFO = sh(script: "aws ecs register-task-definition --family CloudSM --container-definitions '${container_definitions}' --requires-compatibilities FARGATE  --network-mode awsvpc  --task-role-arn arn:aws:iam::221008696644:role/ecs-task-role --execution-role-arn arn:aws:iam::221008696644:role/ecs-task-role --cpu 1024 --memory 2048 --region ${AWS_DEFAULT_REGION}", returnStdout: true).trim()
        
                    
                    // Get the new revision number
                    def NEW_REVISION = sh(script: "echo '${NEW_TASK_INFO}' | jq '.taskDefinition.revision'", returnStdout: true).trim()
            
                    echo "Container Definitions: ${NEW_REVISION}"
        
        
                    // Update the ECS service with the new Task Definition revision
                    sh "aws ecs update-service --region ${AWS_DEFAULT_REGION} --cluster ${ECS_CLUSTER} --service ${SERVICE_NAME} --task-definition ${TASK_DEFINITION_NAME}:${NEW_REVISION}"
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
