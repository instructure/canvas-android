/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.features.assignments.details

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptViewData
import com.instructure.pandautils.features.assignments.details.gradecellview.GradeCellViewData
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.reminder.ReminderViewState
import com.instructure.pandautils.utils.ThemedColor

data class AssignmentDetailsUiState(
    val screenState: AssignmentDetailsScreenState = AssignmentDetailsScreenState.Loading,
    val isRefreshing: Boolean = false,
    val showAudioRecording: Boolean = false,
    val errorMessage: String = "",
    val courseColor: ThemedColor? = null,
    val assignmentName: String = "",
    val points: String = "",
    val submissionStatusText: String = "",
    @DrawableRes val submissionStatusIcon: Int = 0,
    @ColorRes val submissionStatusTint: Int = 0,
    val submissionStatusVisible: Boolean = false,
    val hasDraft: Boolean = false,
    val attempts: List<AssignmentDetailsAttemptViewData> = emptyList(),
    val selectedAttemptPosition: Int = 0,
    val gradeCell: GradeCellViewData? = null,
    val checkpoints: List<DiscussionCheckpointViewState> = emptyList(),
    val reminders: List<ReminderViewState> = emptyList(),
    val fullLocked: Boolean = false,
    val lockedMessage: String = "",
    val submissionTypes: String = "",
    val allowedFileTypes: String = "",
    val quizDetails: QuizViewViewData? = null,
    val attemptsViewData: AttemptsViewData? = null,
    val descriptionLabelText: String = "",
    val description: String = "",
    val noDescriptionVisible: Boolean = false,
    val discussionHeader: DiscussionHeaderUiState? = null,
    val baseUrl: String? = null,
    val submitButtonText: String = "",
    val submitEnabled: Boolean = false,
    val submitVisible: Boolean = false,
    val actions: AssignmentDetailsActions = AssignmentDetailsActions(),
    // Navigation — one destination at a time.
    val navigation: AssignmentDetailsNavigation? = null,
    val onNavigationHandled: () -> Unit = {},
    // Independent one-off effects (may co-occur), each consumed via its own callback.
    val toastMessage: String? = null,
    val onToastShown: () -> Unit = {},
    val mediaDialogAssignment: Assignment? = null,
    val onMediaDialogShown: () -> Unit = {},
    val submitDialog: SubmitDialogUiState? = null,
    val onSubmitDialogShown: () -> Unit = {},
    val createReminder: CreateReminderUiState? = null,
    val onCreateReminderShown: () -> Unit = {},
    val deleteReminderId: Long? = null,
    val onDeleteReminderShown: () -> Unit = {}
)

data class QuizViewViewData(val questionCount: String, val timeLimit: String, val allowedAttempts: String)

data class AttemptsViewData(val allowedAttempts: String, val usedAttempts: String)

data class SubmitDialogUiState(val assignment: Assignment, val studioLTITool: LTITool?)

data class CreateReminderUiState(val tag: String?)

/**
 * UI → ViewModel callbacks, wired to private ViewModel functions in the [AssignmentDetailsUiState]
 * the ViewModel emits. Grouped into their own data class since there are many of them.
 */
data class AssignmentDetailsActions(
    val onRefresh: () -> Unit = {},
    val onSubmitClick: () -> Unit = {},
    val onGradeCellClick: () -> Unit = {},
    val onAttemptSelected: (Int) -> Unit = {},
    val onDraftClick: () -> Unit = {},
    val onLtiButtonPressed: (String) -> Unit = {},
    val onDiscussionAttachmentClick: () -> Unit = {},
    val onAddReminderClick: (String?) -> Unit = {},
    val onRemoveReminderClick: (Long) -> Unit = {}
)

sealed class AssignmentDetailsScreenState {
    data object Loading : AssignmentDetailsScreenState()
    data object Content : AssignmentDetailsScreenState()
    data class Error(val message: String) : AssignmentDetailsScreenState()
}

@Immutable
data class DiscussionHeaderUiState(
    val authorAvatarUrl: String,
    val authorName: String,
    val authorPronouns: String,
    val authoredDate: String,
    val attachmentIconVisible: Boolean
)

@Immutable
data class MessageFabUiState(
    @DrawableRes val iconRes: Int,
    val contentDescription: String,
    val onClick: () -> Unit
)

/**
 * One-off navigation destinations. The ViewModel exposes the current pending destination as the
 * [AssignmentDetailsUiState.navigation] state field; the host (Fragment) consumes it and then calls
 * [AssignmentDetailsUiState.onNavigationHandled] to clear it.
 */
sealed class AssignmentDetailsNavigation {
    data class SendMessage(val options: InboxComposeOptions) : AssignmentDetailsNavigation()
    data class LtiScreen(val url: String) : AssignmentDetailsNavigation()
    data class SubmissionScreen(
        val isObserver: Boolean,
        val selectedSubmissionAttempt: Long?,
        val assignmentUrl: String?,
        val isAssignmentEnhancementEnabled: Boolean,
        val isQuiz: Boolean
    ) : AssignmentDetailsNavigation()

    data class QuizScreen(val quiz: Quiz) : AssignmentDetailsNavigation()
    data class DiscussionScreen(val discussionTopicHeaderId: Long, val course: Course) : AssignmentDetailsNavigation()
    data class UploadScreen(val assignment: Assignment) : AssignmentDetailsNavigation()
    data class TextEntryScreen(
        val assignmentName: String?,
        val submittedText: String? = null,
        val isFailure: Boolean = false
    ) : AssignmentDetailsNavigation()

    data class UrlSubmissionScreen(
        val assignmentName: String?,
        val submittedUrl: String? = null,
        val isFailure: Boolean = false
    ) : AssignmentDetailsNavigation()

    data class AnnotationSubmissionScreen(val assignment: Assignment) : AssignmentDetailsNavigation()
    data class LtiLaunchScreen(val title: String, val ltiTool: LTITool?, val openInternally: Boolean) : AssignmentDetailsNavigation()
    data class UploadStatusScreen(val submissionId: Long) : AssignmentDetailsNavigation()
    data class DiscussionAttachment(val attachments: List<RemoteFile>) : AssignmentDetailsNavigation()
}
