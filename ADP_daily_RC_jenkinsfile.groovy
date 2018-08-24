//Dummy pipe line script



import hudson.model.*
pipeline {
    agent any
    options {
        timestamps()
        skipStagesAfterUnstable()
        timeout(time: 4, unit: 'HOURS')
    }
    git url: "$GIT_REPO_URL", branch: "$GIT_BRANCH"
    echo env.GIT_REVISION
    stages {
        stage('Trigger all daily testing') {
            steps {
                parallel(
                        'Daily VMware Release 1.3.0': {
                            build job: 'vmware-rel-1.3.0'
                        }, 
                        'Daily VMware HA Release 1.3.0': {
                            build job: 'vmware-ha-rel-1.3.0'
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
