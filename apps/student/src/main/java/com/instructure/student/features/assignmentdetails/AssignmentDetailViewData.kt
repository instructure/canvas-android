package com.instructure.student.features.assignmentdetails

import androidx.annotation.ColorRes
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.canvasapi2.models.*
import com.instructure.student.features.assignmentdetails.gradecellview.GradeCellViewData

data class AssignmentDetailViewData(
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
    @Bindable var attempts: List<AssignmentDetailAttemptItemViewModel> = emptyList(),
    @Bindable var selectedGradeCellViewData: GradeCellViewData? = GradeCellViewData(GradeCellViewData.State.EMPTY),
    val dueDate: String = "",
    val submissionTypes: String = "",
    val allowedFileTypes: String = "",
    val description: String = "",
    val descriptionLabelText: String = "",
    val quizDetails: QuizViewViewData? = null,
    val attemptsViewData: AttemptsViewData? = null,
    @Bindable var hasDraft: Boolean = false
) : BaseObservable() {
    val firstAttemptOrNull = attempts.firstOrNull()
    val noDescriptionVisible = description.isEmpty() && !fullLocked
}

data class AssignmentDetailAttemptViewData(
    val title: String,
    val date: String,
    val submission: Submission? = null,
    val isUploading: Boolean = false,
    val isFailed: Boolean = false
)

data class QuizViewViewData(val questionCount: String, val timeLimit: String, val allowedAttempts: String)

data class AttemptsViewData(val allowedAttempts: String, val usedAttempts: String)

sealed class AssignmentDetailAction {
    data class ShowToast(val message: String) : AssignmentDetailAction()
    data class NavigateToLtiScreen(val url: String) : AssignmentDetailAction()
    data class NavigateToSubmissionScreen(val isObserver: Boolean) : AssignmentDetailAction()
    data class NavigateToQuizScreen(val quiz: Quiz) : AssignmentDetailAction()
    data class NavigateToDiscussionScreen(val discussionTopicHeaderId: Long, val course: Course) : AssignmentDetailAction()
    data class NavigateByUrl(val url: String) : AssignmentDetailAction()
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
}
