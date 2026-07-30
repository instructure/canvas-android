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

import com.instructure.canvasapi2.models.postmodels.CommentSendStatus
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import javax.inject.Inject

/**
 * Posts a pending text comment via [CreateSubmissionCommentUseCase] and reconciles the local pending row:
 * on success deletes it and returns the sent [SubmissionCommentItem]; on failure marks it ERROR and
 * rethrows so the caller can leave it visible as a retryable/failed comment.
 */
class SendTextCommentUseCase @Inject constructor(
    private val createSubmissionCommentUseCase: CreateSubmissionCommentUseCase,
    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao,
    private val apiPrefs: ApiPrefs,
) : BaseUseCase<SendTextCommentUseCase.Params, SubmissionCommentItem>() {

    data class Params(
        val pendingId: Long,
        val submissionId: Long?,
        val comment: String,
        val attemptId: Long?,
    )

    override suspend fun execute(params: Params): SubmissionCommentItem {
        try {
            val created = createSubmissionCommentUseCase(
                CreateSubmissionCommentUseCase.Params(
                    submissionId = params.submissionId ?: throw IllegalStateException("Submission ID is null"),
                    comment = params.comment,
                    attempt = params.attemptId?.toInt()
                )
            )
            pendingSubmissionCommentDao.deleteById(params.pendingId)
            return SubmissionCommentItem(
                id = created.id,
                authorName = apiPrefs.user?.name.orEmpty(),
                authorId = apiPrefs.user?.id?.toString().orEmpty(),
                authorAvatarUrl = apiPrefs.user?.avatarUrl.orEmpty(),
                content = params.comment,
                createdAt = created.createdAt,
                isOwnComment = true,
                isPending = false
            )
        } catch (e: Throwable) {
            pendingSubmissionCommentDao.findById(params.pendingId)?.let {
                it.status = CommentSendStatus.ERROR.toString()
                pendingSubmissionCommentDao.update(it)
            }
            throw e
        }
    }
}
