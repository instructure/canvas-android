package com.instructure.pandautils.features.assignments.details

import android.content.Context
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Submission
import java.io.File

interface AssignmentDetailsSubmissionHandler {
    var isUploading: Boolean
    var lastSubmission: Submission?
    var lastSubmissionIsDraft: Boolean
    var lastSubmissionEntry: String?

    fun addAssignmentSubmissionObserver()

    fun removeAssignmentSubmissionObserver()

    fun uploadAudioSubmission(context: Context?, file: File?)

    fun getVideoUri(fragment: FragmentActivity): Uri?

    suspend fun getStudioLTITool(assignment: Assignment, courseId: Long?): LTITool?
}