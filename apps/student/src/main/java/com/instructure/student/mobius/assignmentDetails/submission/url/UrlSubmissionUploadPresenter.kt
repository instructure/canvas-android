package com.instructure.student.mobius.assignmentDetails.submission.url

import android.content.Context
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submission.url.ui.UrlSubmissionUploadViewState
import com.instructure.student.mobius.common.ui.Presenter


object UrlSubmissionUploadPresenter : Presenter<UrlSubmissionUploadModel, UrlSubmissionUploadViewState> {
    override fun present(model: UrlSubmissionUploadModel, context: Context): UrlSubmissionUploadViewState {
        var message = ""

        if (model.urlError == MalformedUrlError.CLEARTEXT) {
            if (model.isFailure) {
                message += getFailedMessage(context) + "\n"
            }
            message += context.getString(R.string.submissionUrlClearTextError)
        } else if (model.isFailure) {
            message = getFailedMessage(context)
        }

        return UrlSubmissionUploadViewState(
            ApiPrefs.fullDomain,
            model.initialUrl,
            submitEnabled = model.isSubmittable,
            isFailure = model.isFailure || model.urlError == MalformedUrlError.CLEARTEXT,
            failureText = message
        )
    }

    private fun getFailedMessage(context: Context) = context.getString(R.string.textSubmissionFailureMessage)
}
