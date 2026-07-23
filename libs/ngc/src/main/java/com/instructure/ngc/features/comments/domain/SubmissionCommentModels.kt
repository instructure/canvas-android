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

import java.util.Date

/**
 * A single submission comment, ready to be rendered by the reusable comments component.
 *
 * [isAnonymous] is true when the author's profile is hidden from the viewer (the server returns no
 * [authorId] but a placeholder name in [authorName], e.g. "Student 11234"). [isUnread] mirrors the
 * per-comment GraphQL `read` flag for comments authored by someone other than the current user.
 */
data class SubmissionCommentItem(
    val id: String = "",
    val authorName: String = "",
    val authorId: String = "",
    val authorAvatarUrl: String? = null,
    val content: String = "",
    val createdAt: Date? = null,
    val isOwnComment: Boolean = false,
    val isAnonymous: Boolean = false,
    val isUnread: Boolean = false,
    val attachments: List<SubmissionCommentAttachment> = emptyList(),
    val mediaObject: SubmissionCommentMedia? = null,
    val isPending: Boolean = false,
    val isFailed: Boolean = false,
)

data class SubmissionCommentAttachment(
    val id: String = "",
    val title: String = "",
    val displayName: String = "",
    val contentType: String = "",
    val size: String = "",
    val url: String = "",
    val thumbnailUrl: String? = null,
    val createdAt: Date? = null,
)

data class SubmissionCommentMedia(
    val id: String,
    val mediaDownloadUrl: String?,
    val title: String?,
    val mediaType: SubmissionCommentMediaType?,
    val thumbnailUrl: String?,
    val contentType: String?,
)

enum class SubmissionCommentMediaType {
    AUDIO,
    VIDEO
}

data class SubmissionCommentsResult(
    val comments: List<SubmissionCommentItem>,
    val submissionId: Long?,
)

data class CreatedSubmissionComment(
    val id: String,
    val createdAt: Date?,
)

data class PendingSubmissionCommentsData(
    val draftText: String?,
    val pendingComments: List<SubmissionCommentItem>,
)

data class PendingCommentRetryData(
    val comment: String?,
    val workerId: String?,
    val filePaths: List<String>?,
    val filePath: String?,
)
