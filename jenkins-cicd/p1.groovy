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

        stage('Build Backend') {
            agent {
                docker { image 'circleci/node:13.8.0' }
            }
            steps {
                checkout()
                script {
                    def backendCacheKey = 'backend-build'
                    withCache(backendCacheKey) {
                        dir('backend') {
                            sh 'npm install'
                            sh 'npm run build'
                        }
                    }
                }
            }
        }

        stage('Test Frontend') {
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
                            sh 'npm run test'
                        }
                    }
                }
            }
            dependencies {
                stage('Build Frontend')
            }
        }

        stage('Test Backend') {
            agent {
                docker { image 'circleci/node:13.8.0' }
            }
            steps {
                checkout()
                script {
                    def backendCacheKey = 'backend-build'
                    withCache(backendCacheKey) {
                        dir('backend') {
                            sh 'npm install'
                            sh 'npm run test'
                        }
                    }
                }
            }
            dependencies {
                stage('Build Backend')
            }
        }

        stage('Scan Backend') {
            agent {
                docker { image 'circleci/node:13.8.0' }
            }
            steps {
                checkout()
                script {
                    def backendCacheKey = 'backend-build'
                    withCache(backendCacheKey) {
                        dir('backend') {
                            sh 'npm install'
                            sh 'npm audit fix --audit-level=critical --force'
                            sh 'npm audit --audit-level=critical'
                        }
                    }
                }
            }
            dependencies {
                stage('Build Backend')
            }
        }

        stage('Scan Frontend') {
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
                            sh 'npm audit fix --audit-level=critical --force'
                            sh 'npm audit --audit-level=critical'
                        }
                    }
                }
            }
            dependencies {
                stage('Build Frontend')
            }
        }
    }
}

def withCache(cacheKey, Closure body) {
    checkout([$class: 'GitSCM'])
    def sha = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
    def cacheName = "${cacheKey}-${sha}"
    dir(cacheName) {
        if (fileExists("cacheMarker")) {
            echo "Using cache for ${cacheKey}"
        } else {
            echo "Cache not found for ${cacheKey}"
            deleteDir()
            sh 'mkdir -p cacheMarker'
            body()
        }
    }
}




  build-image:
    docker:
      - image: docker:17.05.0-ce-git
    steps:
      - checkout
      - setup_remote_docker
      - run: 
          name: Git Commit ID
          command: | 
            export commitID=$(git rev-parse --short HEAD)
            echo ${commitID}
      - run:
          name: Install dependencies
          command: |
            apk add --no-cache \
              py-pip=9.0.0-r1
            pip install \
              docker-compose==1.12.0 \
              awscli==1.11.76