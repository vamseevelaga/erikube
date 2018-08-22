pipeline {
    agent any
    options {
        timestamps()
        skipStagesAfterUnstable()
        timeout(time: 4, unit: 'HOURS')
    }
    environment {
        REPO_DIR = "$WORKSPACE"
        CICD_DIR = "cicd"
    }
    stages {
        stage('Check out code') {
            steps {
                echo 'Initial cleanup and checkout...'
                sh 'sudo chown -R ${USER}:${USER} .'
                deleteDir()
                echo 'Checkout rel/1.3.0 code...'
                checkout([$class: 'GitSCM', branches: [[name: '*/rel/1.3.0']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/vamseevelaga/erikube.git']]])
            }
        }
        stage('Trigger all daily testing') {
            steps {
                parallel(
                        'Daily VMware Release 1.3.0': {
                            build job: 'vmware-rel-1.3.0'
                        },
                        'Daily VMware HA Release 1.3.0': {
                            build job: 'daily-vmware-ha-rel-1.3.0'
                        },
                        'E2C Deploy Release 1.3.0': {
                            build job: 'daily-e2c-deploy-rel-1.3.0'
                        },
                        'E2C Upgrade Release 1.1.x': {
                            build job: 'daily-e2c-upgrade-rel-1.3.0'
                        }
                )
            }
        }
    }
    post {
        always {
            echo 'Publish daily release info'
        }
    }
}
