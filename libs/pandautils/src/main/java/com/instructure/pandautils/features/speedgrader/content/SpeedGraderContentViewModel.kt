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

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.ui.util.fastMap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.SubmissionContentQuery
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Assignment.SubmissionType
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.QuizSubmission
import com.instructure.canvasapi2.utils.validOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeedGraderContentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SpeedGraderContentRepository
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

        val content = getContent(submission)
        _uiState.update { it.copy(content = content, assigneeId = (submission.submission?.groupId ?: submission.submission?.userId)?.toLong()) }
    }

    private suspend fun getContent(submissionData: SubmissionContentQuery.Data): GradeableContent {
        val submission = submissionData.submission
        return when {
            SubmissionType.NONE.apiString in (submission?.assignment?.submissionTypes
                ?: emptyList()).fastMap { it.rawValue } -> NoneContent

            SubmissionType.ON_PAPER.apiString in (submission?.assignment?.submissionTypes
                ?: emptyList()).fastMap { it.rawValue } -> OnPaperContent

            submission?.submissionType == null -> NoSubmissionContent
//            assignment.getState(submission) == AssignmentUtils2.ASSIGNMENT_STATE_MISSING ||
//                    assignment.getState(submission) == AssignmentUtils2.ASSIGNMENT_STATE_GRADED_MISSING -> NoSubmissionContent
            else -> when (Assignment.getSubmissionTypeFromAPIString(submission.submissionType?.rawValue!!)) {

                // LTI submission
                SubmissionType.BASIC_LTI_LAUNCH -> ExternalToolContent(
                    submission.previewUrl.validOrNull()
                        ?: submission.assignment?.htmlUrl.validOrNull().orEmpty()
                )

                // Text submission
                SubmissionType.ONLINE_TEXT_ENTRY -> TextContent(submission.body ?: "")

                // Media submission
                SubmissionType.MEDIA_RECORDING -> submission.mediaObject?.let {
                    MediaContent(
                        uri = Uri.parse(it.mediaDownloadUrl),
                        contentType = it.mediaType?.rawValue ?: "",
                        displayName = it.title
                    )
                } ?: UnsupportedContent

                // File uploads
                SubmissionType.ONLINE_UPLOAD -> submission.attachments?.get(0)?.let {
                    getAttachmentContent(it, submission.assignment?.courseId?.toLong(), (submission.groupId ?: submission.userId)?.toLong())
                } ?: UnsupportedContent

                // URL Submission
                SubmissionType.ONLINE_URL -> UrlContent(
                    submission.url!!,
                    submission.attachments?.firstOrNull()?.url
                )

                // Quiz Submission
                SubmissionType.ONLINE_QUIZ -> handleQuizSubmissionType(submission)

                // Discussion Submission
                SubmissionType.DISCUSSION_TOPIC -> DiscussionContent(submission.previewUrl)

                SubmissionType.STUDENT_ANNOTATION -> {
                    StudentAnnotationContent(submission.id.toLong(), submission.attempt.toLong())
                }

                // Unsupported type
                else -> UnsupportedContent
            }
        }
    }

    private suspend fun getAttachmentContent(attachment: SubmissionContentQuery.Attachment1, courseId: Long?, assigneeId: Long?): GradeableContent {
        // TODO remove; We need this now, because the GraphQL query doesn't return file verifiers.
        val submission = courseId?.let {
            repository.getSingleSubmission(courseId, assignmentId, studentId)
        }

        val thumbnailUrl = submission?.attachments?.firstOrNull()?.thumbnailUrl ?: attachment.thumbnailUrl.orEmpty()
        val url = submission?.attachments?.firstOrNull()?.url ?: attachment.url.orEmpty()
        val previewUrl = submission?.attachments?.firstOrNull()?.previewUrl ?: attachment.submissionPreviewUrl.orEmpty()

        var type = attachment.contentType ?: return OtherAttachmentContent(
            Attachment(
                contentType = attachment.contentType,
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
                attachment.contentType!!
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

    private fun handleQuizSubmissionType(submission: SubmissionContentQuery.Submission): GradeableContent {
        val assignment = submission.assignment ?: return UnsupportedContent
        return if (submission.assignment?.anonymousGrading == true) {
            AnonymousSubmissionContent
        } else {
            QuizContent(
                assignment.courseId!!.toLong(),
                assignmentId,
                studentId,
                submission.previewUrl.orEmpty(),
                QuizSubmission.parseWorkflowState(submission.state.rawValue) == QuizSubmission.WorkflowState.PENDING_REVIEW
            )
        }
    }

    companion object {
        const val ASSIGNMENT_ID_KEY = "assignmentId"
        const val STUDENT_ID_KEY = "submissionId"
    }
}