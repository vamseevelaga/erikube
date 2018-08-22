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
        def release_version = "${GERRIT_REFSPEC}".split('/')
    }
    stages {
        stage('Trigger all daily testing') {
            steps { 
                parallel(
                    println "JOB_NAME = " + System.getenv('GERRIT_REFSPEC');
                        "Daily VMware Release data.get(3)": {
                            build job: "vmware-${data.get(2)}-${data.get(3)}"
                        },
                        'Daily VMware HA Release data.get(3)': {
                            build job: 'daily-vmware-ha-${data.get(2)}-${data.get(3)}'
                        },
                        'E2C Deploy Release ${data.get(3)}': {
                            build job: 'daily-e2c-deploy-${data.get(2)}-${data.get(3)}'
                        }, 
                        'E2C Upgrade Release ${data.get(3)}': { 
                            build job: 'daily-e2c-upgrade-${data.get(2)}-${data.get}(3)'
                       }
                )
            }
        }
        stage('Promote RC to artifactory') {
            steps {
                build job: 'promote-rc-1.3.0'
            }
        }
    }
    post {
        always {
            echo 'Publish daily release info'
        }
    }
}
