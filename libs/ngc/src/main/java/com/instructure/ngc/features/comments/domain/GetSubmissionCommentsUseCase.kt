/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.ngc.features.comments.domain

import com.instructure.canvasapi2.SubmissionCommentsQuery
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.data.repository.submissioncomments.SubmissionCommentsRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

/**
 * Loads the submission comments for a given student/assignment via GraphQL and maps them into the
 * [SubmissionCommentItem]s the reusable comments component renders. Handles own-vs-other detection,
 * per-comment anonymity, unread state and attempt filtering.
 */
class GetSubmissionCommentsUseCase @Inject constructor(
    private val submissionCommentsRepository: SubmissionCommentsRepository,
    private val apiPrefs: ApiPrefs,
) : BaseUseCase<GetSubmissionCommentsUseCase.Params, SubmissionCommentsResult>() {

    data class Params(
        val courseId: Long,
        val studentId: Long,
        val assignmentId: Long,
        val selectedAttemptId: Long? = null,
        val forceNetwork: Boolean = false,
    )

    override suspend fun execute(params: Params): SubmissionCommentsResult {
        val response = submissionCommentsRepository.getSubmissionComments(
            userId = params.studentId,
            assignmentId = params.assignmentId,
            forceNetwork = params.forceNetwork
        )

        val submissionId = response.data.submission?._id?.toLongOrNull()

        val filteredComments = response.comments
            .filter { matchesAttempt(it.attempt.toLong(), params.selectedAttemptId) }

        // The GraphQL comment query returns unverified attachment URLs, which can't be previewed or
        // downloaded on their own. Resolve them to the verifier-authenticated URLs from the REST
        // submission, keyed by the numeric attachment id (falling back to the GraphQL URL if absent).
        // Only fetched when a visible comment actually has an attachment, to avoid a needless request.
        val hasAttachments = filteredComments.any { !it.attachments.isNullOrEmpty() }
        val verifierUrls = if (hasAttachments) {
            getVerifierUrls(params.courseId, params.assignmentId, params.studentId)
        } else {
            emptyMap()
        }

        val comments = filteredComments.map { node -> node.toCommentItem(verifierUrls) }

        return SubmissionCommentsResult(comments = comments, submissionId = submissionId)
    }

    private suspend fun getVerifierUrls(courseId: Long, assignmentId: Long, studentId: Long): Map<Long, String> {
        val submission = submissionCommentsRepository.getSingleSubmission(courseId, assignmentId, studentId)
        return submission?.submissionComments
            ?.flatMap { it.attachments }
            ?.mapNotNull { attachment -> attachment.url?.let { attachment.id to it } }
            ?.toMap()
            .orEmpty()
    }

    // When no attempt is selected we show all comments; otherwise only comments for that attempt.
    // Legacy comments recorded against attempt 0 are shown alongside the first attempt.
    private fun matchesAttempt(commentAttempt: Long, selectedAttemptId: Long?): Boolean {
        if (selectedAttemptId == null) return true
        return commentAttempt == selectedAttemptId || (commentAttempt == 0L && selectedAttemptId == 1L)
    }

    private fun SubmissionCommentsQuery.Node1.toCommentItem(verifierUrls: Map<Long, String>): SubmissionCommentItem {
        val authorId = author?._id.orEmpty()
        val currentUserId = apiPrefs.user?.id?.toString()
        val isOwnComment = authorId.isNotEmpty() && authorId == currentUserId
        // The author is anonymous when the server hides the real user but provides a visible name.
        val isAnonymous = !isOwnComment && authorId.isEmpty() && !authorVisibleName.isNullOrEmpty()

        val authorName = when {
            isOwnComment -> author?.name.orEmpty()
            !author?.name.isNullOrEmpty() -> author?.name.orEmpty()
            else -> authorVisibleName.orEmpty()
        }

        return SubmissionCommentItem(
            id = _id,
            authorName = authorName,
            authorId = authorId,
            authorAvatarUrl = author?.avatarUrl.takeUnless { isAnonymous },
            content = comment.orEmpty(),
            createdAt = createdAt,
            isOwnComment = isOwnComment,
            isAnonymous = isAnonymous,
            isUnread = !read && !isOwnComment,
            attachments = attachments.map { it.toAttachment(verifierUrls) },
            mediaObject = mediaObject?.toMedia(),
        )
    }

    private fun SubmissionCommentsQuery.Attachment.toAttachment(verifierUrls: Map<Long, String>): SubmissionCommentAttachment {
        val verifierUrl = _id.toLongOrNull()?.let { verifierUrls[it] }
        return SubmissionCommentAttachment(
            id = id,
            title = title.orEmpty(),
            displayName = displayName.orEmpty(),
            contentType = contentType.orEmpty(),
            size = size.orEmpty(),
            url = verifierUrl ?: url.orEmpty(),
            thumbnailUrl = thumbnailUrl,
            createdAt = createdAt,
        )
    }

    private fun SubmissionCommentsQuery.MediaObject.toMedia(): SubmissionCommentMedia {
        val mediaSource = mediaSources?.firstOrNull()
        return SubmissionCommentMedia(
            id = _id,
            mediaDownloadUrl = mediaSource?.url,
            title = title,
            // TODO Use the mediaType field once EVAL-5640 fixes the query to return it reliably.
            mediaType = if (title?.endsWith(".mp4") == true) {
                SubmissionCommentMediaType.VIDEO
            } else {
                SubmissionCommentMediaType.AUDIO
            },
            thumbnailUrl = thumbnailUrl,
            contentType = mediaSource?.contentType,
        )
    }
}
