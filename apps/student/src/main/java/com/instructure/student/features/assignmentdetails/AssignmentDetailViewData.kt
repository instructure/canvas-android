package com.instructure.student.features.assignmentdetails

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.canvasapi2.models.*
import com.instructure.student.features.assignmentdetails.gradecellview.GradeCellViewData

data class AssignmentDetailViewData(
    val assignment: Assignment,
    val assignmentName: String,
    val points: String,
    val submissionStatusText: String,
    val submissionStatusIcon: Int,
    val submissionStatusTint: Int,
    val fullLocked: Boolean = false,
    val lockedMessage: String = "",
    val submitButtonText: String = "",
    val submitEnabled: Boolean = false,
    val submitVisible: Boolean = false,
    val attempts: List<AssignmentDetailAttemptItemViewModel> = emptyList(),
    @Bindable var selectedGradeCellViewData: GradeCellViewData? = GradeCellViewData(GradeCellViewData.State.EMPTY),
    val dueDate: String = "",
    val submissionTypes: String = "",
    val allowedFileTypes: String = "",
    val description: String = "",
    val ltiTool: LTITool? = null,
    val descriptionLabelText: String = "",
    val quizDetails: QuizViewViewData? = null,
    val attemptsViewData: AttemptsViewData? = null
) : BaseObservable() {
    val firstAttemptOrNull = attempts.firstOrNull()
}

data class AssignmentDetailAttemptViewData(
    val title: String,
    val date: String,
    val submission: Submission
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
    data class ShowSubmitDialog(val assignment: Assignment) : AssignmentDetailAction()
}
