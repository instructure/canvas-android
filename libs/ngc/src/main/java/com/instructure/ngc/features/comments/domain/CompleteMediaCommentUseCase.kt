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

import android.webkit.MimeTypeMap
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import java.io.File
import java.util.Date
import javax.inject.Inject

/**
 * Resolves a SUCCEEDED media (audio/video) upload: deletes the optimistic pending row so it isn't
 * rendered alongside the completed comment below (they would otherwise share an id, duplicating a
 * LazyColumn key), then builds the media [SubmissionCommentItem] for the just-uploaded file.
 */
class CompleteMediaCommentUseCase @Inject constructor(
    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao,
    private val apiPrefs: ApiPrefs,
) : BaseUseCase<CompleteMediaCommentUseCase.Params, SubmissionCommentItem>() {

    data class Params(val pendingId: Long, val file: File)

    override suspend fun execute(params: Params): SubmissionCommentItem {
        pendingSubmissionCommentDao.deleteById(params.pendingId)

        return SubmissionCommentItem(
            id = params.pendingId.toString(),
            authorName = apiPrefs.user?.name.orEmpty(),
            authorId = apiPrefs.user?.id?.toString().orEmpty(),
            authorAvatarUrl = apiPrefs.user?.avatarUrl.orEmpty(),
            createdAt = Date(),
            isOwnComment = true,
            mediaObject = SubmissionCommentMedia(
                id = params.pendingId.toString(),
                mediaDownloadUrl = params.file.path,
                title = null,
                mediaType = if (params.file.extension == "mp4") SubmissionCommentMediaType.VIDEO else SubmissionCommentMediaType.AUDIO,
                thumbnailUrl = null,
                contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(params.file.extension)
            ),
            isPending = false
        )
    }
}
