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
                    // sh 'cd frontend'
                    // sh 'npm install'
                    // sh 'npm run build'
                    // def frontendCacheKey = 'frontend-build'
                    // withCache(frontendCacheKey) {
                    dir('frontend') {
                        // sh 'id'
                        sh 'npm install'
                        sh 'npm run build'
                    }
                    // }
                }
            }
        }
    }
}
