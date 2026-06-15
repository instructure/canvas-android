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
package com.instructure.horizon.offline.sync

import android.content.Context
import android.webkit.MimeTypeMap
import com.instructure.canvasapi2.apis.DownloadState
import com.instructure.canvasapi2.apis.FileDownloadAPI
import com.instructure.canvasapi2.apis.saveFile
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.database.dao.HorizonLocalImageDao
import com.instructure.horizon.database.entity.HorizonLocalImageEntity
import com.instructure.horizon.database.entity.IMAGE_SCOPE_ACCOUNT
import com.instructure.horizon.offline.FileSyncPaths
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.Date
import javax.inject.Inject

class ImageSyncer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiPrefs: ApiPrefs,
    private val fileDownloadApi: FileDownloadAPI,
    private val localImageDao: HorizonLocalImageDao,
) {
    suspend fun syncImages(imageUrls: Set<String>, courseId: Long? = null) = withContext(Dispatchers.IO) {
        val scope = courseId ?: IMAGE_SCOPE_ACCOUNT
        val nonBlank = imageUrls.filter { it.isNotBlank() }
        if (nonBlank.isEmpty()) return@withContext

        nonBlank.chunked(PARALLEL_DOWNLOAD_BATCH).forEach { chunk ->
            coroutineScope {
                chunk.map { url -> async { syncImage(url, scope) } }.awaitAll()
            }
        }
    }

    private suspend fun syncImage(url: String, scope: Long) {
        try {
            val existing = localImageDao.findByUrl(url)
            if (existing != null && File(existing.localPath).exists()) {
                recordReference(url, existing.localPath, scope, existing.createdDate)
                return
            }
            val destFile = planDestFile(url) ?: return
            downloadImage(url, destFile, scope)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Image download failure is non-fatal
        }
    }

    private fun planDestFile(url: String): File? {
        val userId = apiPrefs.user?.id ?: return null
        val dir = FileSyncPaths.imagesDir(context.filesDir, userId).also { it.mkdirs() }
        val ext = MimeTypeMap.getFileExtensionFromUrl(url).orEmpty()
        val digest = MessageDigest.getInstance("SHA-1").digest(url.toByteArray()).take(8)
        val baseName = digest.joinToString("") { "%02x".format(it) }
        return File(dir, if (ext.isEmpty()) baseName else "$baseName.$ext")
    }

    private suspend fun downloadImage(url: String, destFile: File, scope: Long) {
        val body = fileDownloadApi.downloadFile(
            url,
            RestParams(shouldIgnoreToken = true, shouldLoginOnTokenError = false)
        ).dataOrNull ?: return

        body.saveFile(destFile).collect { state ->
            when (state) {
                is DownloadState.Success -> recordReference(url, destFile.absolutePath, scope, Date())
                is DownloadState.Failure -> if (destFile.exists()) destFile.delete()
                else -> {}
            }
        }
    }

    private suspend fun recordReference(url: String, localPath: String, scope: Long, createdDate: Date) {
        localImageDao.insert(
            HorizonLocalImageEntity(
                url = url,
                localPath = localPath,
                createdDate = createdDate,
                courseId = scope,
            )
        )
    }

    companion object {
        private const val PARALLEL_DOWNLOAD_BATCH = 6
    }
}
