def GERRIT_REFSPEC = env.GERRIT_REFSPEC
def release_value = GERRIT_REFSPEC.split('/')
def release_version = release_value[3]

def build_job(job_name,latest_commit_id) {
  last_build_status = sh(returnStdout: true, script: "curl -s https://fem005-eiffel018.rnd.ki.sw.ericsson.se:8443/jenkins/job/$job_name/lastBuild/api/xml| grep -io 'result.*/result' | sed -e 's/<.*//g' -e 's/result//g' -e 's/>//g'").trim()
  job_commit_id = sh(returnStdout: true, script: "curl -s https://fem005-eiffel018.rnd.ki.sw.ericsson.se:8443/jenkins/job/$job_name/lastBuild/console | grep -i 'checking out' | sed 's/.*Revision //' | cut -d' ' -f1").trim()
  if (!job_commit_id) {
    echo "Release version for $job_name is not appropriate"
  }
  if ( last_build_status == "SUCCESS" && latest_commit_id == job_commit_id ) {
      echo "Daily $job_name is already build with latest commit. skipping..."
  }
  else {
      echo "building the job $job_name-$release_version"
      build job: '$job_name-$release_version'
  }
}
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
                echo 'Checkout rel/$release_version code...'
                checkout([$class: 'GitSCM', branches: [[name: '*/rel/$release_version']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '00979d43-f305-4af8-b874-ec20d3e2beec', url: 'ssh://gerrit.ericsson.se:29418/erikube/erikube.git']]])
                 script {
                       latest_commit_id = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                      }
                echo "Latest commit HASH on master: $latest_commit_id"
            }
        }
        stage('Trigger all daily testing') {
            steps {
                parallel(
                        'Daily VMware Release $release_version': {
                            def job_name="vmware-rel-$release_version"
                            build_job(release_version,job_name",latest_commit_id)
                        },
                        'Daily VMware HA Release $release_version': {
                            def job_name="vmware-ha-rel-$release_version"
                            build_job(release_version,job_name,latest_commit_id)
                        },
                        'E2C Deploy Release $release_version': {
                            def job_name="daily-e2c-deploy-rel-$release_version"
                            build_job(release_version,job_name,latest_commit_id)
                        },
                        'E2C Upgrade Release $release_version': {
                            def job_name="daily-e2c-upgrade-rel-$release_version"
                            build_job(release_version,job_name,latest_commit_id)
                      }
                )
            }
        }
    }
}
