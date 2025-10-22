package com.instructure.pandautils.features.assignments.details

import android.text.Spanned
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptItemViewModel
import com.instructure.pandautils.features.assignments.details.gradecellview.GradeCellViewData
import com.instructure.pandautils.features.assignments.details.itemviewmodels.ReminderItemViewModel
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.utils.ThemedColor

data class AssignmentDetailsViewData(
    val courseColor: ThemedColor,
    @ColorInt val submissionAndRubricLabelColor: Int,
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
    @Bindable var selectedGradeCellViewData: GradeCellViewData? = GradeCellViewData(courseColor, submissionAndRubricLabelColor, GradeCellViewData.State.EMPTY),
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

data class ReminderViewData(val id: Long, val text: String)

sealed class AssignmentDetailAction {
    data class ShowToast(val message: String) : AssignmentDetailAction()
    data class NavigateToSendMessage(val options: InboxComposeOptions) : AssignmentDetailAction()
    data class NavigateToLtiScreen(val url: String) : AssignmentDetailAction()
    data class NavigateToSubmissionScreen(
        val isObserver: Boolean,
        val selectedSubmissionAttempt: Long?,
        val assignmentUrl: String?,
        val isAssignmentEnhancementEnabled: Boolean,
        val isQuiz: Boolean
    ) : AssignmentDetailAction()
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
    data class NavigateToLtiLaunchScreen(val title: String, val ltiTool: LTITool?, val openInternally: Boolean) : AssignmentDetailAction()
    data class ShowMediaDialog(val assignment: Assignment) : AssignmentDetailAction()
    data class ShowSubmitDialog(val assignment: Assignment, val studioLTITool: LTITool?) : AssignmentDetailAction()
    data class NavigateToUploadStatusScreen(val submissionId: Long) : AssignmentDetailAction()
    data class OnDiscussionHeaderAttachmentClicked(val attachments: List<RemoteFile>) : AssignmentDetailAction()
    data object ShowReminderDialog : AssignmentDetailAction()
    data class ShowDeleteReminderConfirmationDialog(val reminderId: Long) : AssignmentDetailAction()
}
