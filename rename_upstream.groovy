//Dummy pipe line script



import hudson.model.*
pipeline {
    agent any
    options {
        timestamps()
        skipStagesAfterUnstable()
        timeout(time: 4, unit: 'HOURS')
    }
    stages {
       
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
