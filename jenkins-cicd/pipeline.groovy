pipeline {
    agent any
    
    stages {
        stage('Build Frontend') {
            agent {
                docker { image 'circleci/node:13.8.0' }
            }
            steps {
                checkout()
                script {
                    def frontendCacheKey = 'frontend-build'
                    withCache(frontendCacheKey) {
                        dir('frontend') {
                            sh 'npm install'
                            sh 'npm run build'
                        }
                    }
                }
            }
        }
    }
}