def call(Map pipelineParams) {
    
    EMAIL_NOTIFY = sh(
        returnStdout: true, 
        script: '''
                JOB_PATH=${JOB_NAME//\\//\\/jobs\\/}
                BUILD_PATH=${HOME}/jobs/${JOB_PATH}/builds/${BUILD_ID}
                du -d 0 -m ${BUILD_PATH} | cut -d'/' -f1  | tr -d \'[:space:]\'
                '''
        ).trim() 

    if (EMAIL_NOTIFY >= pipelineParams.MAX_BUILD_SIZE_IN_MB) {
               
        echo "Build size exceeded max size of " + pipelineParams.MAX_BUILD_SIZE_IN_MB + " MB set for this job"
        wrap([$class: 'BuildUser']) { //requires jenkins plugin ‘user build vars’ to be enabled
            emailext( //requires jenkins plugin ‘extension e-mail’ to be enabled
                body: "Hi ${BUILD_USER_FIRST_NAME},\n\nYour Jenkins job ${env.JOB_NAME} with build ${env.BUILD_NUMBER}\nhas exceeded max build size of " + pipelineParams.MAX_BUILD_SIZE_IN_MB + " MB set in \n${env.JOB_URL}.\n\nThank you,\nTeam CSE",
                recipientProviders: [[$class: 'RequesterRecipientProvider']],
                subject: "Jenkins Build size limit exceeded for Job ${env.JOB_NAME}"
            ) 
        }
    }
}