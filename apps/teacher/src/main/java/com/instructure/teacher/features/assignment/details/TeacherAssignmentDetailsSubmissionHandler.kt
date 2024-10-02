package com.instructure.teacher.features.assignment.details

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsSubmissionHandler
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsViewData
import kotlinx.coroutines.CoroutineScope
import java.io.File

class TeacherAssignmentDetailsSubmissionHandler: AssignmentDetailsSubmissionHandler {
    override var isUploading: Boolean = false
    override var lastSubmissionAssignmentId: Long? = null
    override var lastSubmissionSubmissionType: String? = null
    override var lastSubmissionIsDraft: Boolean = false
    override var lastSubmissionEntry: String? = null
    override fun addAssignmentSubmissionObserver(
        assignmentId: Long,
        userId: Long,
        resources: Resources,
        coroutineScope: CoroutineScope,
        data: MutableLiveData<AssignmentDetailsViewData>,
        refreshAssignment: () -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun removeAssignmentSubmissionObserver() {
        TODO("Not yet implemented")
    }

    override fun uploadAudioSubmission(
        context: Context?,
        course: Course?,
        assignment: Assignment?,
        file: File?
    ) {
        TODO("Not yet implemented")
    }

    override fun getVideoUri(fragment: FragmentActivity): Uri? {
        TODO("Not yet implemented")
    }

    override suspend fun getStudioLTITool(assignment: Assignment, courseId: Long?): LTITool? {
        TODO("Not yet implemented")
    }
}