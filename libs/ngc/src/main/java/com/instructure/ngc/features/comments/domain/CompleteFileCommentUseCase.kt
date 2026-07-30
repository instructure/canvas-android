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

import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.room.appdatabase.daos.AttachmentDao
import com.instructure.pandautils.room.appdatabase.daos.AuthorDao
import com.instructure.pandautils.room.appdatabase.daos.MediaCommentDao
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.daos.SubmissionCommentDao
import com.instructure.pandautils.utils.humanReadableByteCount
import javax.inject.Inject

/**
 * Resolves a SUCCEEDED file-upload worker: deletes the optimistic pending row for the worker, loads the
 * cached [com.instructure.pandautils.room.appdatabase.model.SubmissionCommentWithAttachments] the upload
 * worker wrote, maps it to a [SubmissionCommentItem], then cleans up the now-redundant cached
 * author/media/attachment/comment rows. Returns null (leaving the pending row deleted but nothing else to
 * do) when no matching pending row or cached comment is found.
 */
class CompleteFileCommentUseCase @Inject constructor(
    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao,
    private val submissionCommentDao: SubmissionCommentDao,
    private val authorDao: AuthorDao,
    private val mediaCommentDao: MediaCommentDao,
    private val attachmentDao: AttachmentDao,
    private val apiPrefs: ApiPrefs,
) : BaseUseCase<CompleteFileCommentUseCase.Params, SubmissionCommentItem?>() {

    data class Params(val workerId: String, val submissionCommentId: Long)

    override suspend fun execute(params: Params): SubmissionCommentItem? {
        val pending = pendingSubmissionCommentDao.findByWorkerId(params.workerId) ?: return null
        pendingSubmissionCommentDao.delete(pending)

        val submissionComment = submissionCommentDao.findById(params.submissionCommentId) ?: return null

        val item = SubmissionCommentItem(
            id = submissionComment.submissionComment.id.toString(),
            authorName = submissionComment.author?.displayName ?: apiPrefs.user?.name.orEmpty(),
            authorId = submissionComment.author?.id?.toString() ?: apiPrefs.user?.id?.toString().orEmpty(),
            authorAvatarUrl = submissionComment.author?.avatarImageUrl ?: apiPrefs.user?.avatarUrl,
            content = submissionComment.submissionComment.comment.orEmpty(),
            createdAt = submissionComment.submissionComment.createdAt,
            isOwnComment = true,
            attachments = submissionComment.attachments?.map { attachment ->
                SubmissionCommentAttachment(
                    id = attachment.id.toString(),
                    url = attachment.url.orEmpty(),
                    thumbnailUrl = attachment.thumbnailUrl,
                    createdAt = attachment.createdAt,
                    title = attachment.displayName ?: attachment.filename.orEmpty(),
                    displayName = attachment.displayName.orEmpty(),
                    contentType = attachment.contentType.orEmpty(),
                    size = attachment.size.humanReadableByteCount()
                )
            } ?: emptyList(),
            isPending = false
        )

        submissionComment.author?.let { authorDao.delete(it) }
        submissionComment.mediaComment?.let { mediaCommentDao.delete(it) }
        submissionComment.attachments?.let { attachmentDao.deleteAll(it) }
        submissionCommentDao.delete(submissionComment.submissionComment)

        return item
    }
}
