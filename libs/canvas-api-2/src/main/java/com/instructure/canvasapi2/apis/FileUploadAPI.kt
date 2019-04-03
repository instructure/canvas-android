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
package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.FileUploadParams
import com.instructure.canvasapi2.models.StorageQuotaExceededError
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import java.io.File


internal object FileUploadAPI {

    private interface FileUploadInterface {
        @POST("{context}/files")
        fun getUploadParams(
                @Path("context", encoded = true) uploadContext: String,
                @Query("name") fileName: String,
                @Query("size") fileSize: Long,
                @Query("content_type") contentType: String,
                @Query("parent_folder_id") parentId: Long?,
                @Query("parent_folder_path") parentPath: String?,
                @Query("on_duplicate") actionOnDuplicate: String
        ): Call<FileUploadParams>

        @Multipart
        @POST
        fun uploadFile(
                @Url url: String,
                @PartMap uploadParams: Map<String, @JvmSuppressWildcards RequestBody>,
                @Part file: MultipartBody.Part
        ): Call<Attachment>
    }

    @Suppress("LiftReturnOrAssignment")
    fun getUploadParams(
            uploadContext: String,
            fileName: String,
            fileSize: Long,
            contentType: String,
            parentId: Long?,
            parentPath: String?,
            adapter: RestBuilder,
            params: RestParams,
            renameOnDuplicate: Boolean
    ): FileUploadParams? {
        val response: Response<FileUploadParams>?
        try {
            val renameStrategy = if (renameOnDuplicate) "rename" else "overwrite"
            response = adapter
                    .build(FileUploadInterface::class.java, params)
                    .getUploadParams(
                            uploadContext,
                            fileName,
                            fileSize,
                            contentType,
                            parentId,
                            parentPath,
                            renameStrategy
                    ).execute()
            // check to see if we've hit the file quota limit
            if (response?.code() == 400) {
                EventBus.getDefault().post(StorageQuotaExceededError())
            }
            val uploadParams = response.body()
            /* At least one API endpoint (quizzes) returns a list instead of a single object, so we
            attempt to return the first item in the list or else fall back to the object itself */
            return uploadParams?.list?.firstOrNull() ?: uploadParams
        } catch (ignore: Exception) {
            return null
        }
    }

    fun uploadSynchronous(
            uploadParams: FileUploadParams,
            file: File,
            adapter: RestBuilder,
            params: RestParams
    ): Attachment? {
        val requestFile = RequestBody.create(MediaType.parse("application/octet-stream"), file)
        val requestFilePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
        return try {
            adapter.buildNoRetry(FileUploadInterface::class.java, params)
                    .uploadFile(
                            uploadParams.uploadUrl.orEmpty(),
                            uploadParams.getPlainTextUploadParams(),
                            requestFilePart
                    ).execute().body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
