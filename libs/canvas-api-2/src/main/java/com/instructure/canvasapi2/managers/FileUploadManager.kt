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
import java.io.File

object FileUploadManager {

    @JvmStatic
    private fun getUploadParamsSynchronous(
        uploadContext: String,
        fileName: String,
        fileSize: Long,
        contentType: String,
        parentId: Long?,
        parentPath: String?,
        renameOnDuplicate: Boolean
    ): FileUploadParams? {
        val adapter = RestBuilder()
        val params = RestParams()
        return FileUploadAPI.getUploadParams(
            uploadContext,
            fileName,
            fileSize,
            contentType,
            parentId,
            parentPath,
            adapter,
            params,
            renameOnDuplicate
        )
    }

    @JvmStatic
    private fun performUploadSynchronous(file: File, uploadParams: FileUploadParams, fileIndex: Int? = null, dbSubmissionId: Long? = null): Attachment? {
        val adapter = RestBuilder()
        val params = RestParams(shouldIgnoreToken = true)
        return FileUploadAPI.uploadSynchronous(uploadParams, file, adapter, params, fileIndex, dbSubmissionId)
    }

    /**
     * @param dbSubmissionId - An optional parameter to specify submission id to get progress updates
     */
    @JvmStatic
    fun uploadFileSynchronous(uploadContext: UploadContextProvider, config: FileUploadConfig, fileIndex: Int? = null, dbSubmissionId: Long? = null): Attachment? {
        if (config.parentFolderId != null && config.parentFolderPath != null) {
            throw IllegalArgumentException("Specifying both the parent folder ID and parent folder path is disallowed.")
        }
        return getUploadParamsSynchronous(
            uploadContext.getUploadContext,
            config.fileName,
            config.fileSize,
            config.contentType,
            config.parentFolderId,
            config.parentFolderPath,
            config.renameOnDuplicate
        )?.let { performUploadSynchronous(File(config.filePath), it, fileIndex, dbSubmissionId) }
    }

    @JvmStatic
    fun uploadAvatarSynchronous(imageName: String, size: Long, contentType: String, path: String): AvatarWrapper {
        val uploadParams = getUploadParamsSynchronous(
            UserUploadContext().getUploadContext,
            imageName,
            size,
            contentType,
            null,
            "profile pictures",
            true
        ) ?: return AvatarWrapper(AvatarWrapper.ERROR_UNKNOWN)
        if (uploadParams.message == "file size exceeds quota") return AvatarWrapper(AvatarWrapper.ERROR_QUOTA_EXCEEDED)
        val attachment = performUploadSynchronous(File(path), uploadParams) ?: return AvatarWrapper(AvatarWrapper.ERROR_UNKNOWN)
        return AvatarWrapper(AvatarWrapper.ERROR_NONE, Avatar().apply {
            url = attachment.url
            displayName = attachment.displayName
            type = attachment.contentType
        })
    }
}

interface UploadContextProvider {
    val getUploadContext: String
}

class UserUploadContext @JvmOverloads constructor(
    override val getUploadContext: String = "users/self"
) : UploadContextProvider

class SubmissionUploadContext @JvmOverloads constructor(
    val courseId: Long,
    val assignmentId: Long,
    override val getUploadContext: String = "courses/$courseId/assignments/$assignmentId/submissions/self"
) : UploadContextProvider

class SubmissionCommentUploadContext @JvmOverloads constructor(
    val courseId: Long,
    val assignmentId: Long,
    override val getUploadContext: String = "courses/$courseId/assignments/$assignmentId/submissions/self/comments"
) : UploadContextProvider

class CourseUploadContext @JvmOverloads constructor(
    val courseId: Long,
    override val getUploadContext: String = "courses/$courseId"
) : UploadContextProvider

class QuizUploadContext @JvmOverloads constructor(
    val courseId: Long,
    val quizId: Long,
    override val getUploadContext: String = "courses/$courseId/quizzes/$quizId/submissions/self"
) : UploadContextProvider

class GroupUploadContext @JvmOverloads constructor(
    val groupId: Long,
    override val getUploadContext: String = "groups/$groupId"
) : UploadContextProvider

data class FileUploadConfig(
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val contentType: String
) {
    var parentFolderId: Long? = null
    var parentFolderPath: String? = null
    var renameOnDuplicate: Boolean = true
}
