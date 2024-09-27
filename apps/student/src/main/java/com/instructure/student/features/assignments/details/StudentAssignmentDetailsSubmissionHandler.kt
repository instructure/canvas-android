package com.instructure.student.features.assignments.details

import android.content.Context
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsSubmissionHandler
import com.instructure.pandautils.utils.toast
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.uploadAudioRecording
import com.instructure.student.mobius.common.ui.SubmissionHelper
import com.instructure.student.util.getStudioLTITool
import java.io.File

class StudentAssignmentDetailsSubmissionHandler(
    private val submissionHelper: SubmissionHelper
) : AssignmentDetailsSubmissionHandler {
    override var isUploading: Boolean = false
    override var lastSubmission: Submission? = null
    override var lastSubmissionIsDraft: Boolean = false
    override var lastSubmissionEntry: String? = null


    override fun addAssignmentSubmissionObserver() = Unit

    override fun removeAssignmentSubmissionObserver() = Unit

    override fun uploadAudioSubmission(context: Context?, course: Course?, assignment: Assignment?, file: File?) {
        if (context != null && file != null && assignment != null && course != null) {
            uploadAudioRecording(submissionHelper, file, assignment, course)
        } else {
            context?.let {
                context.toast(context.getString(R.string.audioRecordingError))
            }
        }
    }

    override fun getVideoUri(fragment: FragmentActivity): Uri? = null

    override suspend fun getStudioLTITool(assignment: Assignment, courseId: Long?): LTITool? {
        return if (assignment.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_UPLOAD)) {
            courseId?.getStudioLTITool()?.dataOrNull
        } else null
    }
}