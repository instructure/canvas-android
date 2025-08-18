/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.apis.FileUploadAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Avatar
import com.instructure.canvasapi2.models.AvatarWrapper
import com.instructure.canvasapi2.models.FileUploadParams
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.ProgressRequestUpdateListener
import java.io.File

object FileUploadManager {

    private fun performUpload(
        file: File,
        uploadParams: FileUploadParams,
        onProgress: ProgressRequestUpdateListener? = null,
        restParams: RestParams = RestParams()
    ): DataResult<Attachment> {
        val adapter = RestBuilder()
        val attachment = FileUploadAPI.uploadSynchronous(uploadParams, file, adapter, restParams, onProgress)
        return if (attachment != null) DataResult.Success(attachment) else DataResult.Fail()
    }

    fun uploadFile(
        config: FileUploadConfig,
        onProgress: ProgressRequestUpdateListener? = null,
        fileUploadRestParams: RestParams = RestParams()
    ): DataResult<Attachment> {
        return FileUploadAPI.getUploadParams(config)
            .then { performUpload(File(config.filePath), it, onProgress, fileUploadRestParams) }
    }

    fun uploadAvatarSynchronous(imageName: String, size: Long, contentType: String, path: String): AvatarWrapper {
        val config = FileUploadConfig(
            "users/self",
            imageName,
            path,
            size,
            contentType,
            parentFolderPath = "profile pictures"
        )
        val uploadParams = FileUploadAPI.getUploadParams(config).dataOrNull ?: return AvatarWrapper(AvatarWrapper.ERROR_UNKNOWN)
        if (uploadParams.message == "file size exceeds quota") return AvatarWrapper(AvatarWrapper.ERROR_QUOTA_EXCEEDED)
        val attachment = performUpload(File(path), uploadParams).dataOrNull ?: return AvatarWrapper(AvatarWrapper.ERROR_UNKNOWN)
        return AvatarWrapper(AvatarWrapper.ERROR_NONE, Avatar().apply {
            url = attachment.url
            displayName = attachment.displayName
            type = attachment.contentType
        })
    }
}

data class FileUploadConfig(
    val uploadContext: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val contentType: String,
    val parentFolderId: Long? = null,
    val parentFolderPath: String? = null,
    val renameOnDuplicate: Boolean = true
) {

    init {
        if (parentFolderId != null && parentFolderPath != null) {
            throw IllegalArgumentException("Specifying both parentFolderId and parentFolderPath is not allowed")
        }
    }

    companion object {

        fun forUser(
            fso: FileSubmitObject,
            parentFolderId: Long? = null,
            parentFolderPath: String? = null
        ) = fromSubmitObject(fso, "users/self", parentFolderId, parentFolderPath)

        fun forSubmission(
            fso: FileSubmitObject,
            courseId: Long,
            assignmentId: Long
        ) = fromSubmitObject(fso, "courses/$courseId/assignments/$assignmentId/submissions/self")

        fun forSubmissionComment(
            fso: FileSubmitObject,
            courseId: Long,
            assignmentId: Long
        ) = fromSubmitObject(fso, "courses/$courseId/assignments/$assignmentId/submissions/self/comments")

        fun forSubmissionCommentFromTeacher(
            fso: FileSubmitObject,
            courseId: Long,
            assignmentId: Long,
            assigneeId: Long
        ) = fromSubmitObject(fso, "courses/$courseId/assignments/$assignmentId/submissions/$assigneeId/comments")

        fun forCourse(
            fso: FileSubmitObject,
            courseId: Long,
            parentFolderId: Long? = null
        ) = fromSubmitObject(fso, "courses/$courseId", parentFolderId)

        fun forQuiz(
            fso: FileSubmitObject,
            courseId: Long,
            quizId: Long
        ) = fromSubmitObject(fso, "courses/$courseId/quizzes/$quizId/submissions/self")

        fun forGroup(
            fso: FileSubmitObject,
            groupId: Long,
            parentFolderId: Long? = null
        ) = fromSubmitObject(fso, "groups/$groupId", parentFolderId)

        fun fromSubmitObject(
            fso: FileSubmitObject,
            context: String,
            parentFolderId: Long? = null,
            parentFolderPath: String? = null,
            renameOnDuplicate: Boolean = true
        ) = FileUploadConfig(
            uploadContext = context,
            fileName = fso.name,
            filePath = fso.fullPath,
            fileSize = fso.size,
            contentType = fso.contentType,
            parentFolderId = parentFolderId,
            parentFolderPath = parentFolderPath,
            renameOnDuplicate = renameOnDuplicate
        )

    }
}
