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
import com.instructure.canvasapi2.apis.DownloadState
import com.instructure.canvasapi2.apis.FileDownloadAPI
import com.instructure.canvasapi2.apis.saveFile
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.database.dao.HorizonLocalImageDao
import com.instructure.horizon.database.entity.HorizonLocalImageEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import javax.inject.Inject

class ImageSyncer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiPrefs: ApiPrefs,
    private val fileDownloadApi: FileDownloadAPI,
    private val localImageDao: HorizonLocalImageDao,
) {
    suspend fun syncImages(imageUrls: Set<String>) = withContext(Dispatchers.IO) {
        val nonBlank = imageUrls.filter { it.isNotBlank() }
        if (nonBlank.isEmpty()) return@withContext

        val existing = localImageDao.findByUrls(nonBlank)
            .filter { File(it.localPath).exists() }
            .map { it.url }
            .toSet()
        val toDownload = nonBlank.filter { it !in existing }

        toDownload.chunked(6).forEach { chunk ->
            coroutineScope {
                chunk.map { url -> async { downloadImage(url) } }.awaitAll()
            }
        }
    }

    private suspend fun downloadImage(url: String) {
        try {
            val userId = apiPrefs.user?.id ?: return
            val dir = File(context.filesDir, "$userId/images").also { it.mkdirs() }
            val ext = url.substringAfterLast('.', "jpg")
                .substringBefore('?')
                .take(4)
                .ifEmpty { "jpg" }
            val destFile = File(dir, "${url.hashCode()}.$ext")

            if (destFile.exists()) return

            val body = fileDownloadApi.downloadFile(
                url,
                RestParams(shouldIgnoreToken = true, shouldLoginOnTokenError = false)
            ).dataOrNull ?: return

            body.saveFile(destFile).collect { state ->
                when (state) {
                    is DownloadState.Success -> {
                        localImageDao.insert(
                            HorizonLocalImageEntity(
                                url = url,
                                localPath = destFile.absolutePath,
                                createdDate = Date(),
                            )
                        )
                    }
                    is DownloadState.Failure -> destFile.delete()
                    else -> {}
                }
            }
        } catch (_: Exception) {
            // Image download failure is non-fatal
        }
    }
}
