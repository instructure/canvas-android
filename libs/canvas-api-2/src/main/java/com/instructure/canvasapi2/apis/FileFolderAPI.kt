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

import androidx.annotation.WorkerThread
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.Call
import retrofit2.http.*
import java.io.IOException
import java.util.*


object FileFolderAPI {

    interface FilesFoldersInterface {

        @GET("self/folders/root")
        fun getRootUserFolder(): Call<FileFolder>

        @GET("{contextId}/folders/root")
        fun getRootFolderForContext(@Path("contextId") contextId: Long): Call<FileFolder>

        @GET("{contextType}/{contextId}/folders/root")
        suspend fun getRootFolderForContext(
            @Path("contextId") contextId: Long,
            @Path("contextType") contextType: String,
            @Tag params: RestParams
        ): DataResult<FileFolder>

        @GET("folders/{folderId}")
        fun getFolder(@Path("folderId") folderId: Long): Call<FileFolder>

        @GET("folders/{folderId}")
        suspend fun getFolder(@Path("folderId") folderId: Long, @Tag restParams: RestParams): DataResult<FileFolder>

        @GET("courses/{courseId}/files/{folderId}")
        fun getCourseFile(@Path("courseId") contextId: Long, @Path("folderId") folderId: Long): Call<FileFolder>

        @GET("courses/{courseId}/files/{fileId}")
        suspend fun getCourseFile(@Path("courseId") contextId: Long, @Path("fileId") folderId: Long, @Tag params: RestParams): DataResult<FileFolder>

        @GET("users/self/files/{folderId}")
        fun getUserFile(@Path("folderId") folderId: Long): Call<FileFolder>

        @GET("users/self/files/{folderId}")
        suspend fun getUserFile(@Path("folderId") folderId: Long, @Tag params: RestParams): DataResult<FileFolder>

        @GET("folders/{folderId}/folders")
        fun getFirstPageFolders(@Path("folderId") folderId: Long): Call<List<FileFolder>>

        @GET("folders/{folderId}/folders")
        suspend fun getFirstPageFolders(@Path("folderId") folderId: Long, @Tag params: RestParams): DataResult<List<FileFolder>>

        @GET("folders/{folderId}/files?include[]=usage_rights")
        fun getFirstPageFiles(@Path("folderId") folderId: Long): Call<List<FileFolder>>

        @GET("folders/{folderId}/files?include[]=usage_rights")
        suspend fun getFirstPageFiles(@Path("folderId") folderId: Long, @Tag params: RestParams): DataResult<List<FileFolder>>

        @GET("{fileUrl}")
        fun getFileFolderFromURL(@Path(value = "fileUrl", encoded = true) fileURL: String): Call<FileFolder>

        @GET("{fileUrl}")
        suspend fun getFileFolderFromURL(@Path(value = "fileUrl", encoded = true) fileURL: String, @Tag params: RestParams): DataResult<FileFolder>

        @GET
        fun getNextPageFileFoldersList(@Url nextURL: String): Call<List<FileFolder>>

        @GET
        suspend fun getNextPageFileFoldersList(@Url nextURL: String, @Tag params: RestParams): DataResult<List<FileFolder>>

        @GET("{canvasContext}/files")
        fun searchFiles(@Path(value = "canvasContext", encoded = true) contextPath: String, @Query("search_term") query: String): Call<List<FileFolder>>

        @GET("{canvasContext}/files")
        suspend fun searchFiles(@Path(value = "canvasContext", encoded = true) contextPath: String, @Query("search_term") query: String, @Tag params: RestParams): DataResult<List<FileFolder>>

        @DELETE("files/{fileId}")
        fun deleteFile(@Path("fileId") fileId: Long): Call<FileFolder>

        @PUT("files/{fileId}?include[]=usage_rights")
        fun updateFile(@Path("fileId") fileId: Long, @Body updateFileFolder: UpdateFileFolder): Call<FileFolder>

        @PUT("files/{fileId}?include[]=usage_rights")
        suspend fun updateFile(@Path("fileId") fileId: Long, @Body updateFileFolder: UpdateFileFolder, @Tag params: RestParams): DataResult<FileFolder>

        @POST("folders/{folderId}/folders")
        fun createFolder(@Path("folderId") folderId: Long, @Body newFolderName: CreateFolder): Call<FileFolder>

        @DELETE("folders/{folderId}?force=true")
        fun deleteFolder(@Path("folderId") folderId: Long): Call<FileFolder>

        @PUT("folders/{folderId}")
        fun updateFolder(@Path("folderId") folderId: Long, @Body updateFileFolder: UpdateFileFolder): Call<FileFolder>

        @FormUrlEncoded
        @PUT("courses/{courseId}/usage_rights")
        fun updateUsageRights(@Path("courseId") courseId: Long, @FieldMap params: Map<String, @JvmSuppressWildcards Any>): Call<UsageRights>

        @GET("courses/{courseId}/content_licenses")
        fun getCourseFileLicenses(@Path("courseId") courseId: Long): Call<ArrayList<License>>

        @GET("courses/{courseId}/content_licenses")
        suspend fun getCourseFileLicenses(@Path("courseId") courseId: Long, @Tag restParams: RestParams): DataResult<List<License>>

        @GET("files/{fileNumber}?include=avatar")
        fun getAvatarFileToken(@Path("fileNumber") fileNumber: String): Call<FileFolder>

        @GET("files/{fileId}")
        suspend fun getFile(@Path("fileId") fileId: Long, @Tag params: RestParams): DataResult<FileFolder>
    }

    fun getFileFolderFromURL(adapter: RestBuilder, url: String, callback: StatusCallback<FileFolder>, params: RestParams) {
        if (APIHelper.paramIsNull(callback, url)) return
        // TODO: add pagination
        callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).getFileFolderFromURL(url)).enqueue(callback)
    }

    @WorkerThread
    fun getFileFolderFromURLSynchronous(adapter: RestBuilder, url: String, params: RestParams): FileFolder? {
        if (APIHelper.paramIsNull(url)) return null
        //TODO: add pagination
        try {
            return adapter.build(FilesFoldersInterface::class.java, params).getFileFolderFromURL(url).execute().body()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    fun getRootFolderForContext(adapter: RestBuilder, canvasContext: CanvasContext, callback: StatusCallback<FileFolder>, params: RestParams) {
        if (APIHelper.paramIsNull(callback)) return
        if (canvasContext.type == CanvasContext.Type.USER) {
            callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).getRootUserFolder()).enqueue(callback)
        } else {
            callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).getRootFolderForContext(canvasContext.id)).enqueue(callback)
        }
    }

    fun getFolder(folderId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<FileFolder>) {
        callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).getFolder(folderId)).enqueue(callback)
    }

    fun getCourseFile(courseId: Long, folderId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<FileFolder>) {
        callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).getCourseFile(courseId, folderId)).enqueue(callback)
    }

    fun getUserFile(folderId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<FileFolder>) {
        callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).getUserFile(folderId)).enqueue(callback)
    }

    fun getFirstPageFolders(adapter: RestBuilder, folderId: Long, callback: StatusCallback<List<FileFolder>>, params: RestParams) {
        if (APIHelper.paramIsNull(callback)) return
        callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).getFirstPageFolders(folderId)).enqueue(callback)
    }

    fun getFirstPageFiles(adapter: RestBuilder, folderId: Long, callback: StatusCallback<List<FileFolder>>, params: RestParams) {
        if (APIHelper.paramIsNull(callback)) return
        callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).getFirstPageFiles(folderId)).enqueue(callback)
    }

    fun getNextPageFilesFolder(adapter: RestBuilder, nextUrl: String, callback: StatusCallback<List<FileFolder>>, params: RestParams) {
        if (APIHelper.paramIsNull(callback, nextUrl)) return
        callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).getNextPageFileFoldersList(nextUrl)).enqueue(callback)
    }

    fun searchFiles(adapter: RestBuilder, query: String, canvasContext: CanvasContext, callback: StatusCallback<List<FileFolder>>, params: RestParams) {
        val contextPath = canvasContext.toAPIString().substring(1) // Drop leading slash
        callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).searchFiles(contextPath, query)).enqueue(callback)
    }

    fun deleteFile(adapter: RestBuilder, fileId: Long, callback: StatusCallback<FileFolder>, params: RestParams) {
        if (APIHelper.paramIsNull(callback)) return
        callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).deleteFile(fileId)).enqueue(callback)
    }

    fun updateFile(fileId: Long, updateFileFolder: UpdateFileFolder, adapter: RestBuilder, callback: StatusCallback<FileFolder>, params: RestParams) {
        if (APIHelper.paramIsNull(callback)) return
        callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).updateFile(fileId, updateFileFolder)).enqueue(callback)
    }

    fun createFolder(folderId: Long, folder: CreateFolder, adapter: RestBuilder, callback: StatusCallback<FileFolder>, params: RestParams) {
        if (APIHelper.paramIsNull(callback)) return
        callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).createFolder(folderId, folder)).enqueue(callback)
    }

    fun deleteFolder(folderId: Long, adapter: RestBuilder, callback: StatusCallback<FileFolder>, params: RestParams) {
        if (APIHelper.paramIsNull(callback)) return
        callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).deleteFolder(folderId)).enqueue(callback)
    }

    fun updateFolder(folderId: Long, updateFileFolder: UpdateFileFolder, adapter: RestBuilder, callback: StatusCallback<FileFolder>, params: RestParams) {
        if (APIHelper.paramIsNull(callback)) return
        callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).updateFolder(folderId, updateFileFolder)).enqueue(callback)
    }

    fun updateUsageRights(courseId: Long, formParams: Map<String, Any>, adapter: RestBuilder, callback: StatusCallback<UsageRights>, params: RestParams) {
        if (APIHelper.paramIsNull(callback)) return
        callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).updateUsageRights(courseId, formParams)).enqueue(callback)
    }

    fun getCourseFileLicenses(courseId: Long, adapter: RestBuilder, callback: StatusCallback<ArrayList<License>>, params: RestParams) {
        if (APIHelper.paramIsNull(callback)) return
        callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).getCourseFileLicenses(courseId)).enqueue(callback)
    }

    fun getAvatarFileToken(fileNumber: String, adapter: RestBuilder, params: RestParams, callback: StatusCallback<FileFolder>) {
        if (APIHelper.paramIsNull(callback)) return
        callback.addCall(adapter.build(FilesFoldersInterface::class.java, params).getAvatarFileToken(fileNumber)).enqueue(callback)
    }
}
