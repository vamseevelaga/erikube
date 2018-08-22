/*
*renames the top upstream run to have a more usefull name
* if run from Gerrit, add build number and Gerrit info + shorthash
* if run by a user from jenkins display build number name + shorthash
* if run by a trigger from jenkins display build number run by SCM + shorthash  
*/
def build = Thread.currentThread().executable
assert build
def env=build.getEnvironment()
def build_num =env.get('BUILD_ID')

def gerritRevision = env.get('GERRIT_PATCHSET_REVISION')
def gitShort=""
if (gerritRevision) {
    gitShort = gerritRevision[0..7]
}else{
    gitShort = env['GIT_COMMIT'][0..7]
}
def name = env.get('GERRIT_CHANGE_OWNER_NAME')

try {
    if (name) {
        def subject = env.get('GERRIT_CHANGE_SUBJECT')
        println "Build display name is set to #${build_num} ${name}: ${subject} ${gitShort}"
        build.displayName = "#${build_num} ${name}: ${subject}: ${gitShort} "
    }else{
        def user = env.get('BUILD_USER')
        if (user){
            println "Build display name is set to #${build_num} ${user} Triggered: ${gitShort}"
            build.displayName = "#${build_num} ${user} Trigered: ${gitShort} "
        }else{
            println "Build display name is set to #${build_num} SCM Triggered: ${gitShort}"
         build.displayName = "#${build_num} SCM Trigered: ${gitShort} "

        }
         println "Build display name is set to #${build_num} ${user} Triggered: ${gitShort}"

         build.displayName = "#${build_num} ${user} Trigered: ${gitShort} "
    }
} catch (MissingPropertyException e) {
    println "Error: $e"
}
