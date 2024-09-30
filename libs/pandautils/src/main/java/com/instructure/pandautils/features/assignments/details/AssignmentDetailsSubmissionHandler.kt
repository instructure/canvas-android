package com.instructure.pandautils.features.assignments.details

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import kotlinx.coroutines.CoroutineScope
import java.io.File

interface AssignmentDetailsSubmissionHandler {
    var isUploading: Boolean
    var lastSubmissionAssignmentId: Long?
    var lastSubmissionSubmissionType: String?
    var lastSubmissionIsDraft: Boolean
    var lastSubmissionEntry: String?

    fun addAssignmentSubmissionObserver(assignmentId: Long, userId: Long, resources: Resources, coroutineScope: CoroutineScope, data: MutableLiveData<AssignmentDetailsViewData>, refreshAssignment: () -> Unit)

    fun removeAssignmentSubmissionObserver()

    fun uploadAudioSubmission(context: Context?, course: Course?, assignment: Assignment?, file: File?)

    fun getVideoUri(fragment: FragmentActivity): Uri?

    suspend fun getStudioLTITool(assignment: Assignment, courseId: Long?): LTITool?
}