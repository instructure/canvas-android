package com.instructure.student.mobius.assignmentDetails.submission.url

import android.content.Context
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.student.mobius.assignmentDetails.submission.url.ui.UrlSubmissionUploadViewState
import com.instructure.student.mobius.common.ui.Presenter


object UrlSubmissionUploadPresenter : Presenter<UrlSubmissionUploadModel, UrlSubmissionUploadViewState> {
    override fun present(model: UrlSubmissionUploadModel, context: Context): UrlSubmissionUploadViewState {
        return UrlSubmissionUploadViewState(ApiPrefs.fullDomain, model.initialUrl,  submitEnabled = model.isSubmittable)
    }
}