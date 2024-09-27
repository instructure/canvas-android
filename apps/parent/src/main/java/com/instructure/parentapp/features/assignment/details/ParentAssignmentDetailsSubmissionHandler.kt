package com.instructure.parentapp.features.assignment.details

import android.content.Context
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsSubmissionHandler
import java.io.File

class ParentAssignmentDetailsSubmissionHandler: AssignmentDetailsSubmissionHandler {
    override var isUploading: Boolean = false
    override var lastSubmission: Submission? = null
    override var lastSubmissionIsDraft: Boolean = false
    override var lastSubmissionEntry: String? = null

    override fun addAssignmentSubmissionObserver() = Unit

    override fun removeAssignmentSubmissionObserver() = Unit

    override fun uploadAudioSubmission(context: Context?, file: File?) = Unit

    override fun getVideoUri(fragment: FragmentActivity): Uri? = null

    override suspend fun getStudioLTITool(assignment: Assignment, courseId: Long?): LTITool? = null
}