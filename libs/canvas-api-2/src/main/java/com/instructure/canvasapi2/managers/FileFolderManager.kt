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

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.ExhaustiveListCallback
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.weave.apiAsync
import retrofit2.Response
import java.util.*

object FileFolderManager {

    fun getFileFolderFromURL(url: String, forceNetwork: Boolean, callback: StatusCallback<FileFolder>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        FileFolderAPI.getFileFolderFromURL(adapter, url, callback, params)
    }

    fun getFileFolderFromUrlAsync(
        url: String,
        forceNetwork: Boolean
    ) = apiAsync<FileFolder> { getFileFolderFromURL(url, forceNetwork, it) }

    fun getFileFolderFromURLSynchronous(url: String): FileFolder? {
        val adapter = RestBuilder(object : StatusCallback<String>() {}) // Empty StatusCallback
        val params = RestParams()
        return FileFolderAPI.getFileFolderFromURLSynchronous(adapter, url, params)
    }

    fun getRootFolderForContext(
        canvasContext: CanvasContext,
        forceNetwork: Boolean,
        callback: StatusCallback<FileFolder>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(canvasContext = canvasContext, isForceReadFromNetwork = forceNetwork)
        FileFolderAPI.getRootFolderForContext(adapter, canvasContext, callback, params)
    }

    fun getFolder(folderId: Long, forceNetwork: Boolean, callback: StatusCallback<FileFolder>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        FileFolderAPI.getFolder(folderId, adapter, params, callback)
    }

    fun getCourseFile(courseId: Long, fileId: Long, forceNetwork: Boolean, callback: StatusCallback<FileFolder>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        FileFolderAPI.getCourseFile(courseId, fileId, adapter, params, callback)
    }

    fun getUserFile(fileId: Long, forceNetwork: Boolean, callback: StatusCallback<FileFolder>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        FileFolderAPI.getUserFile(fileId, adapter, params, callback)
    }

    fun getFirstPageFolders(folderId: Long, forceNetwork: Boolean, callback: StatusCallback<List<FileFolder>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        FileFolderAPI.getFirstPageFolders(adapter, folderId, callback, params)
    }

    fun getFirstPageFiles(folderId: Long, forceNetwork: Boolean, callback: StatusCallback<List<FileFolder>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        FileFolderAPI.getFirstPageFiles(adapter, folderId, callback, params)
    }

    fun getNextPageFilesFolder(url: String, forceNetwork: Boolean, callback: StatusCallback<List<FileFolder>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        FileFolderAPI.getNextPageFilesFolder(adapter, url, callback, params)
    }

    fun deleteFile(fileId: Long, callback: StatusCallback<FileFolder>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        FileFolderAPI.deleteFile(adapter, fileId, callback, params)
    }

    fun getAllFoldersRoot(
        canvasContext: CanvasContext,
        forceNetwork: Boolean,
        callback: StatusCallback<List<FileFolder>>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(
            canvasContext = canvasContext,
            usePerPageQueryParam = true,
            isForceReadFromNetwork = forceNetwork
        )
        FileFolderAPI.getRootFolderForContext(adapter, canvasContext, object : StatusCallback<FileFolder>() {
            override fun onResponse(response: Response<FileFolder>, linkHeaders: LinkHeaders, type: ApiType) {
                getAllFolders(response.body()!!.id, forceNetwork, callback)
            }
        }, params)
    }

    fun getAllFolders(folderId: Long, forceNetwork: Boolean, callback: StatusCallback<List<FileFolder>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<FileFolder>(callback) {
            override fun getNextPage(callback: StatusCallback<List<FileFolder>>, nextUrl: String, isCached: Boolean) {
                FileFolderAPI.getNextPageFilesFolder(adapter, nextUrl, callback, params)
            }
        }
        FileFolderAPI.getFirstPageFolders(adapter, folderId, depaginatedCallback, params)
    }

    fun getAllFilesRoot(
        canvasContext: CanvasContext,
        forceNetwork: Boolean,
        callback: StatusCallback<List<FileFolder>>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(
            canvasContext = canvasContext,
            usePerPageQueryParam = true,
            isForceReadFromNetwork = forceNetwork
        )
        FileFolderAPI.getRootFolderForContext(adapter, canvasContext, object : StatusCallback<FileFolder>() {
            override fun onResponse(response: Response<FileFolder>, linkHeaders: LinkHeaders, type: ApiType) {
                getAllFiles(response.body()!!.id, forceNetwork, callback)
            }
        }, params)
    }

    fun getAllFiles(folderId: Long, forceNetwork: Boolean, callback: StatusCallback<List<FileFolder>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<FileFolder>(callback) {
            override fun getNextPage(callback: StatusCallback<List<FileFolder>>, nextUrl: String, isCached: Boolean) {
                FileFolderAPI.getNextPageFilesFolder(adapter, nextUrl, callback, params)
            }
        }
        FileFolderAPI.getFirstPageFiles(adapter, folderId, depaginatedCallback, params)
    }

    fun searchFiles(
        query: String,
        canvasContext: CanvasContext,
        forceNetwork: Boolean,
        callback: StatusCallback<List<FileFolder>>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<FileFolder>(callback) {
            override fun getNextPage(callback: StatusCallback<List<FileFolder>>, nextUrl: String, isCached: Boolean) {
                FileFolderAPI.getNextPageFilesFolder(adapter, nextUrl, callback, params)
            }
        }
        FileFolderAPI.searchFiles(adapter, query, canvasContext, depaginatedCallback, params)
    }

    fun createFolder(folderId: Long, folder: CreateFolder, callback: StatusCallback<FileFolder>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        FileFolderAPI.createFolder(folderId, folder, adapter, callback, params)
    }

    fun deleteFolder(folderId: Long, callback: StatusCallback<FileFolder>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        FileFolderAPI.deleteFolder(folderId, adapter, callback, params)
    }

    fun updateFolder(folderId: Long, updateFileFolder: UpdateFileFolder, callback: StatusCallback<FileFolder>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        FileFolderAPI.updateFolder(folderId, updateFileFolder, adapter, callback, params)
    }

    fun updateUsageRights(courseId: Long, formParams: Map<String, Any>, callback: StatusCallback<UsageRights>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        FileFolderAPI.updateUsageRights(courseId, formParams, adapter, callback, params)
    }

    fun updateFile(fileId: Long, updateFileFolder: UpdateFileFolder, callback: StatusCallback<FileFolder>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        FileFolderAPI.updateFile(fileId, updateFileFolder, adapter, callback, params)
    }

    fun getCourseFileLicenses(courseId: Long, callback: StatusCallback<ArrayList<License>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        FileFolderAPI.getCourseFileLicenses(courseId, adapter, callback, params)
    }

    fun getCourseFileLicensesAsync(courseId: Long) = apiAsync<ArrayList<License>> { getCourseFileLicenses(courseId, it) }

    fun getAvatarFileToken(fileNumber: String, callback: StatusCallback<FileFolder>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        FileFolderAPI.getAvatarFileToken(fileNumber, adapter, params, callback)
    }
}
