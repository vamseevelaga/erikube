//Dummy pipe line script



import hudson.model.*
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
    <!-- CHANGE SET -->
<% changeSet = build.changeSet
if (changeSet != null) {
hadChanges = false %>
<h2>Changes</h2>
<ul>
<% changeSet.each { cs ->
hadChanges = true
aUser = cs.author %>
<li>Commit <b>${cs.revision}</b> by <b><%= aUser != null ? aUser.displayName :      it.author.displayName %>:</b> (${cs.msg})
<ul>
<% cs.affectedFiles.each { %>
<li class="change-${it.editType.name}"><b>${it.editType.name}</b>: ${it.path}                              </li> <%  } %> </ul>   </li> <%  }

 if (!hadChanges) { %>  
  <li>No Changes !!</li>
 <%  } %>   </ul> <% } %>
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
