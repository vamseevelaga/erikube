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
                echo 'Checkout rel/1.1.x code...'
                checkout([$class: 'GitSCM', branches: [[name: '*/rel/1.1.x']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '00979d43-f305-4af8-b874-ec20d3e2beec', url: 'ssh://gerrit.ericsson.se:29418/erikube/erikube.git']]])
            }
        }
        stage('Trigger all daily testing') {
            steps {
                parallel(
                        'Daily VMware Release 1.1.x': {
                            build job: 'vmware-rel-1.1.x'
                        },
                        'Daily VMware HA Release 1.1.x': {
                            build job: 'daily-vmware-ha-rel-1.1.x'
                        },
                        'E2C Deploy Release 1.1.x': {
                            build job: 'daily-e2c-deploy-rel-1.1.x'
                        },
                        'E2C Upgrade Release 1.1.x': {
                            build job: 'daily-e2c-upgrade-rel-1.1.x'
                        }
                )
            }
        }
        stage('Promote RC to artifactory') {
            steps {
                build job: 'promote-rc-1.1.x'
            }
        }
    }
    post {
        always {
            echo 'Publish daily release info'
            sh '$CICD_DIR/daily_release/daily_release.sh'
            archiveArtifacts 'artifact.properties'
        }
    }
}
