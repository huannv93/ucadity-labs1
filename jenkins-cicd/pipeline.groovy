pipeline {
    agent any
    
    stages {

        stage('Checkout') {
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

        stage('Build Frontend') {
            agent {
                docker { image 'circleci/node:13.8.0' }
            }
            steps {

                script {
                    checkout scm: [
                        $class: 'GitSCM',
                        branches: [[name: '*/main']],
                        userRemoteConfigs: [[url: 'https://github.com/huannv93/ucadity-labs1.git']]
                    ]
                    // sh 'cd frontend'
                    // sh 'npm install'
                    // sh 'npm run build'
                    // def frontendCacheKey = 'frontend-build'
                    // withCache(frontendCacheKey) {
                    dir('frontend') {
                        sh 'sudo chown -R 992:992 "/.npm"'
                        sh 'sudo npm install'
                        sh 'sudo npm run build'
                    }
                    // }
                }
            }
        }
    }
}