/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.offline.sync

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Handler
import android.os.Looper
import android.util.Size
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.apis.DownloadState
import com.instructure.canvasapi2.apis.FileDownloadAPI
import com.instructure.canvasapi2.apis.LaunchDefinitionsAPI
import com.instructure.canvasapi2.apis.StudioApi
import com.instructure.canvasapi2.apis.saveFile
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.LaunchDefinition
import com.instructure.canvasapi2.models.StudioCaption
import com.instructure.canvasapi2.models.StudioLoginSession
import com.instructure.canvasapi2.models.StudioMediaMetadata
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.offline.daos.StudioMediaProgressDao
import com.instructure.pandautils.room.offline.entities.StudioMediaProgressEntity
import com.instructure.pandautils.utils.poll
import com.instructure.pandautils.views.CanvasWebView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL


class StudioSync(
    private val context: Context,
    private val launchDefinitionsApi: LaunchDefinitionsAPI.LaunchDefinitionsInterface,
    private val apiPrefs: ApiPrefs,
    private val studioApi: StudioApi,
    private val studioMediaProgressDao: StudioMediaProgressDao,
    private val fileDownloadApi: FileDownloadAPI,
    private val firebaseCrashlytics: FirebaseCrashlytics
) {

    private var dummyProgressId: Long? = null

    public suspend fun getStudioMetadata(courseIds: Set<Long>): List<StudioMediaMetadata> {
        val studioSession = authenticateStudio() ?: return emptyList()
        val metadata = getAllVideosMetaData(courseIds, studioSession)
        // Create dummy progress so that when all the files are synced and the studio video sync haven't started we won't show the progress as completed.
        if (metadata.isNotEmpty()) {
            dummyProgressId = createAndInsertProgress(StudioMediaMetadata(-1, "", "", "", 0, emptyList(), ""))
        }
        return metadata
    }

    public suspend fun syncStudioVideos(allVideosMetadata: List<StudioMediaMetadata>, mediaIdsToSync: Set<String>) {
        val videosToSync = allVideosMetadata.filter { mediaIdsToSync.contains(it.ltiLaunchId) }
        val videosNeeded = cleanupAndCheckExistingVideos(videosToSync)
        downloadVideos(videosNeeded)
        dummyProgressId?.let {updateProgress(it, 100, ProgressState.COMPLETED) }
    }

    private suspend fun authenticateStudio(): StudioLoginSession? {
        val launchDefinitions = launchDefinitionsApi.getLaunchDefinitions(RestParams(isForceReadFromNetwork = true, shouldRefreshToken = false)).dataOrNull.orEmpty()
        val studioLaunchDefinition = launchDefinitions.firstOrNull {
            it.domain == LaunchDefinition.STUDIO_DOMAIN
        } ?: return null

        val studioUrl = "${apiPrefs.fullDomain}/api/v1/accounts/self/external_tools/sessionless_launch?url=${studioLaunchDefinition.url}"
        val studioLti = launchDefinitionsApi.getLtiFromAuthenticationUrl(studioUrl, RestParams(isForceReadFromNetwork = true, shouldRefreshToken = false)).dataOrNull ?: return null

        return studioLti.url?.let {
            val webView = withTimeoutOrNull(10000) { loadUrlIntoHeadlessWebView(context, it) }
            if (webView == null) return null

            // Get base url for Studio api calls
            val url = URL(studioLaunchDefinition.url)
            val baseUrl = "${url.protocol}://${url.host}"

            poll(block = {
                val token = webView.evaluateJavascriptSuspend("sessionStorage.getItem('token')")
                val userId = webView.evaluateJavascriptSuspend("sessionStorage.getItem('userId')")
                StudioLoginSession(userId, token, baseUrl)
            }, validate = {
                it.userId != "null" && it.token != "null"
            })
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun loadUrlIntoHeadlessWebView(context: Context, url: String): WebView = suspendCancellableCoroutine { continuation ->
        Handler(Looper.getMainLooper()).post {
            val webView = CanvasWebView(context)
            webView.settings.javaScriptEnabled = true
            webView.loadUrl(url)
            val webViewClient = webView.webViewClient
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (url?.contains("login") == true) {
                        webView.webViewClient = webViewClient
                        continuation.resume(webView, null)
                    }
                }
            }
        }
    }

    private suspend fun getAllVideosMetaData(
        courseIds: Set<Long>,
        studioSession: StudioLoginSession
    ) = coroutineScope {
        courseIds.map { courseId ->
            async {
                studioApi.getStudioMediaMetadata(
                    "${studioSession.baseUrl}/api/public/v1/courses/$courseId/media",
                    RestParams(isForceReadFromNetwork = true, shouldIgnoreToken = true, shouldRefreshToken = false),
                    "Bearer ${studioSession.accessToken}"
                ).dataOrNull.orEmpty()
            }
        }.awaitAll()
    }.flatten().distinctBy { it.id }

    private suspend fun downloadVideos(videos: List<StudioMediaMetadata>) {
        val syncData = mutableListOf<StudioVideoSyncData>()

        videos.forEach {
            val progressId = createAndInsertProgress(it)
            syncData.add(StudioVideoSyncData(progressId, it.ltiLaunchId, it.url, it.captions, it.mimeType))
        }

        val chunks = syncData.chunked(6)

        coroutineScope {
            chunks.forEach { chunk ->
                chunk.map {
                    async { downloadFile(it) }
                }.awaitAll()
            }
        }
    }

    private suspend fun createAndInsertProgress(studioVideoMetadata: StudioMediaMetadata): Long {
        val progress = StudioMediaProgressEntity(
            studioVideoMetadata.ltiLaunchId,
            0,
            0,
            ProgressState.IN_PROGRESS
        )

        val rowId = studioMediaProgressDao.insert(progress)
        return studioMediaProgressDao.findByRowId(rowId)?.id ?: -1L
    }

    private fun cleanupAndCheckExistingVideos(videos: List<StudioMediaMetadata>): List<StudioMediaMetadata> {
        val studioDir = getStudioDir()
        val existingVideoFolders = studioDir.listFiles()?.toList()?.filterNotNull() ?: emptyList()

        val downloadedVideosLaunchIds = mutableListOf<String>()
        existingVideoFolders.forEach { folder ->
            if (folder.listFiles()?.isNotEmpty() == true) {
                downloadedVideosLaunchIds.add(folder.name)
            }
            if (videos.none { it.ltiLaunchId == folder.name }) {
                folder.deleteRecursively()
            }
        }

        return videos.filter { !downloadedVideosLaunchIds.contains(it.ltiLaunchId) }
    }

    private suspend fun downloadFile(fileSyncData: StudioVideoSyncData) {
        var downloadedFile = getDownloadFile(fileSyncData.ltiLaunchId)

        try {
            val downloadResult = fileDownloadApi.downloadFile(
                fileSyncData.fileUrl,
                RestParams(shouldIgnoreToken = true, isForceReadFromNetwork = true, shouldRefreshToken = false)
            )

            downloadResult
                .dataOrThrow
                .saveFile(downloadedFile)
                .collect {
                    when (it) {
                        is DownloadState.InProgress -> {
                            updateProgress(fileSyncData.progressId, it.progress, ProgressState.IN_PROGRESS, it.totalBytes)
                        }

                        is DownloadState.Success -> {
                            if (downloadedFile.name.startsWith("temp_")) {
                                downloadedFile = rewriteOriginalFile(downloadedFile)
                            }
                            if (fileSyncData.mimeType.contains("video")) {
                                createThumbnail(downloadedFile)
                            }
                            saveCaptions(fileSyncData.captions, downloadedFile)
                            updateProgress(fileSyncData.progressId, 100, ProgressState.COMPLETED)
                        }

                        is DownloadState.Failure -> {
                            throw it.throwable
                        }
                    }
                }
        } catch (e: Exception) {
            downloadedFile.delete()
            updateProgress(fileSyncData.progressId, 0, ProgressState.ERROR)
            firebaseCrashlytics.recordException(e)
        }
    }

    private fun getDownloadFile(ltiLaunchId: String): File {
        val studioDir = getStudioDir()
        val videoDir = File(studioDir, ltiLaunchId)
        if (!videoDir.exists()) {
            videoDir.mkdir()
        }

        var downloadFile = File(videoDir, "${ltiLaunchId}.mp4")
        if (downloadFile.exists()) {
            downloadFile = File(videoDir, "temp_${ltiLaunchId}.mp4")
        }
        return downloadFile
    }

    private fun getStudioDir(): File {
        val userFilesDir = File(context.filesDir, apiPrefs.user?.id.toString())
        if (!userFilesDir.exists()) {
            userFilesDir.mkdir()
        }

        val studioDir = File(userFilesDir, "studio")
        if (!studioDir.exists()) {
            studioDir.mkdir()
        }

        return studioDir
    }

    private fun createThumbnail(downloadedFile: File) {
        var thumbnailBitmap: Bitmap? = null
        try {
            thumbnailBitmap = ThumbnailUtils.createVideoThumbnail(downloadedFile, Size(512, 512), null)
            val thumbnail = File(downloadedFile.parentFile, "poster.jpg")
            if (thumbnail.exists()) {
                thumbnail.delete()
            }
            FileOutputStream(thumbnail).use { out ->
                thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: IOException) {
            firebaseCrashlytics.recordException(e)
        } finally {
            thumbnailBitmap?.recycle()
        }
    }

    private fun saveCaptions(captions: List<StudioCaption>, downloadedFile: File) {
        val captionsRegex = Regex("(?<=\\d{2}:\\d{2}:\\d{2}),(?=\\d{3})")
        val videoDir = downloadedFile.parentFile ?: return
        captions.forEach { caption ->
            val captionFile = File(videoDir, "${caption.srcLang}.vtt")
            if (captionFile.exists()) {
                captionFile.delete()
            }

            try {
                val correctedCaption = "WEBVTT\n\n${caption.data}".replace(captionsRegex, ".")
                FileOutputStream(captionFile).use { out ->
                    out.write(correctedCaption.toByteArray())
                }
            } catch (e: IOException) {
                firebaseCrashlytics.recordException(e)
            }
        }
    }

    private suspend fun updateProgress(progressId: Long, progress: Int, progressState: ProgressState, fileSize: Long? = null) {
        var newProgress = studioMediaProgressDao.findById(progressId)?.copy(progress = progress, progressState = progressState)
        if (fileSize != null) {
            newProgress = newProgress?.copy(fileSize = fileSize)
        }
        newProgress?.let { studioMediaProgressDao.update(it) }
    }

    private fun rewriteOriginalFile(tempFile: File): File {
        val dir = tempFile.parentFile ?: return tempFile
        val originalFile = File(dir, tempFile.name.substringAfter("temp_"))
        originalFile.delete()
        tempFile.renameTo(originalFile)
        return originalFile
    }
}

data class StudioVideoSyncData(
    val progressId: Long,
    val ltiLaunchId: String,
    val fileUrl: String,
    val captions: List<StudioCaption> = emptyList(),
    val mimeType: String
)

@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun WebView.evaluateJavascriptSuspend(script: String): String = suspendCancellableCoroutine { continuation ->
    Handler(Looper.getMainLooper()).post {
        this.evaluateJavascript(script) { result ->
            continuation.resume(result, null)
        }
    }
}