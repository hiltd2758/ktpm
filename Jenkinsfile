pipeline {
    agent any

    tools {
        maven 'Maven 3.9'
        jdk 'JDK 21'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                dir('web') {
                    sh 'mvn -B verify'
                }
            }
            post {
                always {
                    junit 'web/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('web') {
                    withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
                        sh '''
                            mvn -B sonar:sonar \
                                -Dsonar.projectKey=hiltd2758_ktpm \
                                -Dsonar.organization=hiltd2758 \
                                -Dsonar.host.url=https://sonarcloud.io \
                                -Dsonar.token=$SONAR_TOKEN \
                                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                        '''
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                dir('web') {
                    sh 'docker build -t e-health-care:${BUILD_NUMBER} .'
                }
            }
        }

        stage('Run Container') {
            steps {
                sh '''
                    docker stop e-health-care || true
                    docker rm e-health-care || true
                    docker run -d \
                        --name e-health-care \
                        --link mysql-ehealth:mysql-ehealth \
                        -p 8081:8080 \
                        -e SPRING_DATASOURCE_URL="jdbc:mysql://mysql-ehealth:3306/e_health" \
                        -e SPRING_DATASOURCE_USERNAME="root" \
                        -e SPRING_DATASOURCE_PASSWORD="root" \
                        e-health-care:${BUILD_NUMBER}
                '''
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}