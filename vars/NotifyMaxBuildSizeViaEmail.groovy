def call(Map pipelineParams) {
    pipeline {
        agent {
            label 'master'
        }
        environment {
            EMAIL_NOTIFY=0
        }
        
        stages {
            stage('Build Size Check') {
                when {
                    expression {
                        EMAIL_NOTIFY = sh(
                            returnStdout: true, 
                            script: '''
                                    JOB_PATH=${JOB_NAME//\\//\\/jobs\\/}
                                    BUILD_PATH=${HOME}/jobs/${JOB_PATH}/builds/${BUILD_ID}
                                    # du -d 0 -m ${BUILD_PATH} | cut -d'/' -f1  | tr -d \'[:space:]\'
                                    du -d 0 -m /var/lib/jenkins/jobs/Fantasia | cut -d'/' -f1  | tr -d \'[:space:]\'
                                    
                                    '''
                        ).trim() 

                        return (EMAIL_NOTIFY >= pipelineParams.MAX_BUILD_SIZE_IN_MB)
                    }
                }
                steps {
                    // sh 'printenv'
                    echo "Build size exceeded max size of ${pipelineParams.MAX_BUILD_SIZE_IN_MB} MB set for this job"
                    wrap([$class: 'BuildUser']) { //requires jenkins plugin ‘user build vars’ to be enabled
                        emailext( //requires jenkins plugin ‘extension e-mail’ to be enabled
                            body: "Hi ${BUILD_USER_FIRST_NAME},\n\nYour Jenkins job ${env.JOB_NAME} with build ${env.BUILD_NUMBER}\n has exceeded max build size of ${env.MAX_BUILD_SIZE_IN_MB} MB set in ${env.JOB_URL}.",
                            recipientProviders: [[$class: 'RequesterRecipientProvider']],
                            subject: "Jenkins Build size limit exceeded for Job ${env.JOB_NAME}"
                        ) 
                    }
                }
             
            } 
        }
    }
}