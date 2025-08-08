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
package com.instructure.pandautils.features.speedgrader.content

import android.content.res.Resources
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.ui.util.fastMap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.SubmissionContentQuery
import com.instructure.canvasapi2.fragment.SubmissionFields
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Assignment.SubmissionType
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.QuizSubmission
import com.instructure.canvasapi2.type.SubmissionState
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.pandautils.R
import com.instructure.pandautils.features.grades.SubmissionStateLabel
import com.instructure.pandautils.features.speedgrader.SpeedGraderSelectedAttemptHolder
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SpeedGraderContentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SpeedGraderContentRepository,
    private val resources: Resources,
    private val speedGraderSelectedAttemptHolder: SpeedGraderSelectedAttemptHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpeedGraderContentUiState())
    val uiState = _uiState.asStateFlow()

    private val assignmentId: Long = savedStateHandle.get<Long>(ASSIGNMENT_ID_KEY) ?: -1L
    private val studentId: Long = savedStateHandle.get<Long>(STUDENT_ID_KEY) ?: -1L

    init {
        viewModelScope.launch {
            fetchData()
        }
    }

    private suspend fun fetchData() {
        val submission = repository.getSubmission(assignmentId, studentId)
        val submissionFields = submission.submission?.submissionFields

        val groupSubmission = submissionFields?.groupId != null && !submissionFields.assignment?.gradeGroupStudentsIndividually.orDefault()

        val assignee = if (groupSubmission) {
            val group = submissionFields?.assignment?.groupSet?.groups?.find { it._id == submissionFields.groupId }
            Assignee(
                id = group?._id?.toLongOrNull(),
                name = group?.name,
            )
        } else {
            Assignee(
                id = submission.submission?.userId?.toLongOrNull(),
                name = submissionFields?.user?.name,
                avatarUrl = submissionFields?.user?.avatarUrl
            )
        }

        val submissionHistory = submission.submission?.submissionHistoriesConnection?.edges
            .orEmpty()
            .sortedByDescending {
                it?.node?.submissionFields?.attempt
            }

        val attempts = mapAttempts(submissionHistory)

        speedGraderSelectedAttemptHolder.setSelectedAttemptId(studentId, attempts.firstOrNull()?.id)

        val attachments = mapAttachments(submissionHistory.firstOrNull()?.node)

        val initialContent = getContent(
            submission,
            submissionHistory.firstOrNull()?.node?.submissionFields?.attempt,
            attachments.firstOrNull()?.id
        )

        val anonymousGrading = submissionFields?.assignment?.anonymousGrading ?: false

        _uiState.update { state ->
            state.copy(
                content = initialContent,
                assigneeId = assignee.id,
                userName = if (anonymousGrading) resources.getString(R.string.anonymousGradingStudentLabel) else assignee.name,
                userUrl = if (!anonymousGrading) assignee.avatarUrl else null,
                submissionState = getSubmissionStateLabel(submissionFields?.state),
                dueDate = submissionFields?.assignment?.dueAt,
                attachmentSelectorUiState = SelectorUiState(
                    items = attachments,
                    selectedItemId = attachments.firstOrNull()?.id,
                    onItemSelected = { onAttachmentSelected(submission, it) }
                ),
                attemptSelectorUiState = SelectorUiState(
                    items = attempts,
                    selectedItemId = attempts.firstOrNull()?.id,
                    onItemSelected = { onAttemptSelected(submission, submissionHistory, it) }
                ),
                anonymous = anonymousGrading,
                group = groupSubmission
            )
        }
    }

    private fun getSubmissionStateLabel(submissionState: SubmissionState?): SubmissionStateLabel {
        return when (submissionState) {
            SubmissionState.submitted -> SubmissionStateLabel.SUBMITTED
            SubmissionState.unsubmitted -> SubmissionStateLabel.NOT_SUBMITTED
            SubmissionState.graded -> SubmissionStateLabel.GRADED
            SubmissionState.ungraded -> SubmissionStateLabel.SUBMITTED
            SubmissionState.pending_review -> SubmissionStateLabel.SUBMITTED
            else -> SubmissionStateLabel.NONE
        }
    }

    private suspend fun getContent(
        submissionData: SubmissionContentQuery.Data,
        selectedAttempt: Int?,
        selectedAttachmentId: Long?
    ): GradeableContent {
        val submission = submissionData.submission
        val submissionFields = submissionData.submission?.submissionHistoriesConnection?.edges?.find {
            it?.node?.submissionFields?.attempt == selectedAttempt
        }?.node?.submissionFields

        return when {
            SubmissionType.NONE.apiString in (submissionFields?.assignment?.submissionTypes
                ?: emptyList()).fastMap { it.rawValue } -> NoneContent

            SubmissionType.ON_PAPER.apiString in (submissionFields?.assignment?.submissionTypes
                ?: emptyList()).fastMap { it.rawValue } -> OnPaperContent

            submissionFields?.submissionType == null -> NoSubmissionContent
            else -> when (Assignment.getSubmissionTypeFromAPIString(submissionFields.submissionType?.rawValue.orEmpty())) {

                // LTI submission
                SubmissionType.BASIC_LTI_LAUNCH -> ExternalToolContent(
                    submissionFields.previewUrl.validOrNull()
                        ?: submissionFields.assignment?.htmlUrl.validOrNull().orEmpty()
                )

                // Text submission
                SubmissionType.ONLINE_TEXT_ENTRY -> TextContent(submissionFields.body ?: "")

                // Media submission
                SubmissionType.MEDIA_RECORDING -> submissionFields.mediaObject?.let {
                    MediaContent(
                        uri = Uri.parse(it.mediaSources?.firstOrNull()?.url.orEmpty()),
                        contentType = it.mediaType?.rawValue,
                        displayName = it.title
                    )
                } ?: UnsupportedContent

                // File uploads
                SubmissionType.ONLINE_UPLOAD -> submissionFields.attachments?.find {
                    it._id.toLong() == selectedAttachmentId
                }?.let {
                    getAttachmentContent(
                        it,
                        submissionFields.assignment?.courseId?.toLong(),
                        (submissionFields.groupId ?: submission?.userId)?.toLong(),
                        selectedAttempt?.toLong()
                    )
                } ?: UnsupportedContent

                // URL Submission
                SubmissionType.ONLINE_URL -> UrlContent(
                    submissionFields.url.orEmpty(),
                    submissionFields.attachments?.firstOrNull()?.url
                )

                // Quiz Submission
                SubmissionType.ONLINE_QUIZ -> handleQuizSubmissionType(submissionFields)

                // Discussion Submission
                SubmissionType.DISCUSSION_TOPIC -> DiscussionContent(submissionFields.previewUrl?.replace("&show_full_discussion_immediately=true", ""))

                SubmissionType.STUDENT_ANNOTATION -> {
                    try {
                        val canvaDocSession = repository.createCanvaDocSession(submission?._id.orEmpty(), submissionFields.attempt.toString())
                        PdfContent(
                            canvaDocSession.canvadocsSessionUrl.orEmpty(),
                            submissionFields.assignment?.courseId?.toLong(),
                            (submissionFields.groupId ?: submission?.userId)?.toLong()
                        )
                    } catch (e: Exception) {
                        UnsupportedContent
                    }
                }

                // Unsupported type
                else -> UnsupportedContent
            }
        }
    }

    private suspend fun getAttachmentContent(
        attachment: SubmissionFields.Attachment,
        courseId: Long?,
        assigneeId: Long?,
        attemptId: Long?
    ): GradeableContent {
        // TODO remove; We need this now, because the GraphQL query doesn't return file verifiers.
        val submission = courseId?.let {
            repository.getSingleSubmission(courseId, assignmentId, studentId)
        }?.submissionHistory?.find {
            it?.attempt == attemptId
        }

        val attachmentWithVerifiers = submission?.attachments?.firstOrNull { it.id == attachment._id.toLongOrNull() }
        val thumbnailUrl = attachmentWithVerifiers?.thumbnailUrl ?: attachment.thumbnailUrl.orEmpty()
        val url = attachmentWithVerifiers?.url ?: attachment.url.orEmpty()
        val previewUrl = attachmentWithVerifiers?.previewUrl ?: attachment.submissionPreviewUrl.orEmpty()

        var type = attachment.contentType ?: return OtherAttachmentContent(
            Attachment(
                createdAt = attachment.createdAt,
                displayName = attachment.displayName,
                thumbnailUrl = thumbnailUrl,
                url = url
            )
        )
        if (type == "*/*") {
            val fileExtension = attachment.title?.substringAfterLast(".") ?: ""
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)
                ?: MimeTypeMap.getFileExtensionFromUrl(url)
                        ?: type
        }
        return when {
            type == "application/pdf" || previewUrl.contains("canvadoc") -> {
                if (previewUrl.contains("canvadoc")) {
                    PdfContent(previewUrl, courseId, assigneeId)
                } else {
                    PdfContent(url, courseId, assigneeId)
                }
            }

            type.startsWith("audio") || type.startsWith("video") -> with(
                attachment
            ) {
                MediaContent(
                    uri = Uri.parse(url),
                    thumbnailUrl = thumbnailUrl,
                    contentType = contentType,
                    displayName = displayName
                )
            }

            type.startsWith("image") -> ImageContent(
                url,
                attachment.contentType.orEmpty(),
            )

            else -> OtherAttachmentContent(
                Attachment(
                    contentType = attachment.contentType,
                    createdAt = attachment.createdAt,
                    displayName = attachment.displayName,
                    thumbnailUrl = thumbnailUrl,
                    url = url
                )
            )
        }
    }

    private fun handleQuizSubmissionType(submission: SubmissionFields): GradeableContent {
        val assignment = submission.assignment ?: return UnsupportedContent
        return if (submission.assignment?.anonymousGrading == true) {
            AnonymousSubmissionContent
        } else {
            QuizContent(
                assignment.courseId?.toLongOrNull() ?: -1L,
                assignmentId,
                studentId,
                submission.previewUrl.orEmpty(),
                QuizSubmission.parseWorkflowState(submission.state.rawValue) == QuizSubmission.WorkflowState.PENDING_REVIEW
            )
        }
    }

    private fun getFormattedAttemptDate(date: Date): String {
        val datePart = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(date)
        val timePart = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(date)
        return "$datePart, $timePart"
    }

    private fun mapAttempts(submissionHistory: List<SubmissionContentQuery.Edge?>) = submissionHistory.mapNotNull { edge ->
        edge?.node?.submissionFields?.let { attempt ->
            SelectorItem(
                attempt.attempt.toLong(),
                resources.getString(R.string.attempt, attempt.attempt),
                attempt.submittedAt?.let { getFormattedAttemptDate(it) }
            )
        }
    }

    private fun onAttemptSelected(
        submission: SubmissionContentQuery.Data,
        submissionHistory: List<SubmissionContentQuery.Edge?>,
        attemptId: Long
    ) = viewModelScope.launch {
        val selectedSubmission = submissionHistory.find { it?.node?.submissionFields?.attempt == attemptId.toInt() }
        val newAttachments = mapAttachments(selectedSubmission?.node)
        val newContent = getContent(submission, attemptId.toInt(), newAttachments.firstOrNull()?.id)
        _uiState.update {
            it.copy(
                content = newContent,
                attemptSelectorUiState = it.attemptSelectorUiState.copy(selectedItemId = attemptId),
                attachmentSelectorUiState = it.attachmentSelectorUiState.copy(
                    items = newAttachments,
                    selectedItemId = newAttachments.firstOrNull()?.id
                )
            )
        }
        speedGraderSelectedAttemptHolder.setSelectedAttemptId(studentId, attemptId)
    }

    private fun mapAttachments(submissionNode: SubmissionContentQuery.Node?) = submissionNode?.submissionFields?.attachments
        .orEmpty()
        .map { SelectorItem(it._id.toLong(), it.displayName.orEmpty()) }

    private fun onAttachmentSelected(
        submission: SubmissionContentQuery.Data,
        attachmentId: Long
    ) = viewModelScope.launch {
        val newContent = getContent(submission, uiState.value.attemptSelectorUiState.selectedItemId?.toInt(), attachmentId)
        _uiState.update {
            it.copy(
                content = newContent,
                attachmentSelectorUiState = it.attachmentSelectorUiState.copy(selectedItemId = attachmentId)
            )
        }
    }

    companion object {
        const val ASSIGNMENT_ID_KEY = "assignmentId"
        const val STUDENT_ID_KEY = "submissionId"
    }
}

data class Assignee(
    val id: Long? = null,
    val name: String? = null,
    val avatarUrl: String? = null
)
