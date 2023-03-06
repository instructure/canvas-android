package com.instructure.student.features.assignmentdetails

import android.text.Spanned
import androidx.annotation.ColorRes
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.canvasapi2.models.*
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptItemViewModel
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.student.features.assignmentdetails.gradecellview.GradeCellViewData

data class AssignmentDetailsViewData(
    val courseColor: ThemedColor,
    val assignmentName: String,
    val points: String,
    val submissionStatusText: String,
    val submissionStatusIcon: Int,
    @ColorRes val submissionStatusTint: Int,
    val submissionStatusVisible: Boolean,
    val fullLocked: Boolean = false,
    val lockedMessage: String = "",
    val submitButtonText: String = "",
    val submitEnabled: Boolean = false,
    val submitVisible: Boolean = false,
    @Bindable var attempts: List<AssignmentDetailsAttemptItemViewModel> = emptyList(),
    @Bindable var selectedGradeCellViewData: GradeCellViewData? = GradeCellViewData(courseColor, GradeCellViewData.State.EMPTY),
    val dueDate: String = "",
    val submissionTypes: String = "",
    val allowedFileTypes: String = "",
    val description: String = "",
    val descriptionLabelText: String = "",
    val discussionHeaderViewData: DiscussionHeaderViewData? = null,
    val quizDetails: QuizViewViewData? = null,
    val attemptsViewData: AttemptsViewData? = null,
    @Bindable var hasDraft: Boolean = false
) : BaseObservable() {
    val firstAttemptOrNull = attempts.firstOrNull()
    val noDescriptionVisible = description.isEmpty() && !fullLocked
}

data class QuizViewViewData(val questionCount: String, val timeLimit: String, val allowedAttempts: String)

data class AttemptsViewData(val allowedAttempts: String, val usedAttempts: String)

data class DiscussionHeaderViewData(
    val authorAvatarUrl: String,
    val authorName: String,
    val authorNameWithPronouns: Spanned,
    val authoredDate: String,
    val attachmentIconVisible: Boolean,
    val onAttachmentClicked: () -> Unit
)

sealed class AssignmentDetailAction {
    data class ShowToast(val message: String) : AssignmentDetailAction()
    data class NavigateToLtiScreen(val url: String) : AssignmentDetailAction()
    data class NavigateToSubmissionScreen(val isObserver: Boolean, val selectedSubmissionAttempt: Long?) : AssignmentDetailAction()
    data class NavigateToQuizScreen(val quiz: Quiz) : AssignmentDetailAction()
    data class NavigateToDiscussionScreen(val discussionTopicHeaderId: Long, val course: Course) : AssignmentDetailAction()
    data class NavigateToUploadScreen(val assignment: Assignment) : AssignmentDetailAction()
    data class NavigateToTextEntryScreen(
        val assignmentName: String?,
        val submittedText: String? = null,
        val isFailure: Boolean = false
    ) : AssignmentDetailAction()

    data class NavigateToUrlSubmissionScreen(
        val assignmentName: String?,
        val submittedUrl: String? = null,
        val isFailure: Boolean = false
    ) : AssignmentDetailAction()

    data class NavigateToAnnotationSubmissionScreen(val assignment: Assignment) : AssignmentDetailAction()
    data class NavigateToLtiLaunchScreen(val title: String, val ltiTool: LTITool?) : AssignmentDetailAction()
    data class ShowMediaDialog(val assignment: Assignment) : AssignmentDetailAction()
    data class ShowSubmitDialog(val assignment: Assignment, val studioLTITool: LTITool?) : AssignmentDetailAction()
    data class NavigateToUploadStatusScreen(val submissionId: Long) : AssignmentDetailAction()
    data class OnDiscussionHeaderAttachmentClicked(val attachments: List<RemoteFile>) : AssignmentDetailAction()
}
