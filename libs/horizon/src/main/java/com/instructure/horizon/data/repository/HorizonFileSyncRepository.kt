package com.instructure.horizon.data.repository

import android.content.Context
import android.net.Uri
import com.instructure.canvasapi2.apis.DownloadState
import com.instructure.canvasapi2.apis.FileDownloadAPI
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.apis.saveFile
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.database.dao.HorizonFileFolderDao
import com.instructure.horizon.database.dao.HorizonLocalFileDao
import com.instructure.horizon.database.entity.HorizonFileFolderEntity
import com.instructure.horizon.database.entity.HorizonLocalFileEntity
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.features.offline.sync.HtmlParsingResult
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.util.Date
import javax.inject.Inject

class HorizonFileSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileDownloadApi: FileDownloadAPI,
    private val localFileDao: HorizonLocalFileDao,
    private val fileFolderDao: HorizonFileFolderDao,
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface,
    private val apiPrefs: ApiPrefs,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun syncHtmlFiles(courseId: Long, parsingResult: HtmlParsingResult) {
        val alreadyDownloadedIds = localFileDao.findByCourseId(courseId).map { it.id }.toSet()
        val internalFileIdsToSync = parsingResult.internalFileIds.filterNot { alreadyDownloadedIds.contains(it) }

        coroutineScope {
            internalFileIdsToSync.chunked(6).forEach { chunk ->
                chunk.map { fileId -> async { downloadInternalFile(fileId, courseId) } }.awaitAll()
            }
        }

        coroutineScope {
            parsingResult.externalFileUrls.toList().chunked(6).forEach { chunk ->
                chunk.map { url -> async { downloadExternalFile(url, courseId) } }.awaitAll()
            }
        }
    }

    private suspend fun downloadInternalFile(fileId: Long, courseId: Long) {
        val fileInfo = resolveInternalFileInfo(fileId, courseId) ?: return
        val dir = File(context.filesDir, apiPrefs.user?.id.toString()).also { it.mkdirs() }
        val destFile = File(dir, "${fileId}_${fileInfo.displayName}")

        if (destFile.exists()) return

        downloadToFile(fileInfo.url, destFile, shouldIgnoreToken = false) {
            localFileDao.insert(
                HorizonLocalFileEntity(
                    fileId,
                    courseId,
                    Date(),
                    destFile.absolutePath
                )
            )
        }
    }

    private suspend fun resolveInternalFileInfo(fileId: Long, courseId: Long): HorizonFileFolderEntity? {
        fileFolderDao.findById(fileId)?.let { return it }

        val file = fileFolderApi.getCourseFile(
            courseId, fileId,
            RestParams(isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
        ).dataOrNull ?: return null

        val url = file.url ?: return null
        val displayName = file.displayName ?: return null

        val entity = HorizonFileFolderEntity(fileId, url, displayName)
        fileFolderDao.insert(entity)
        return entity
    }

    private suspend fun downloadExternalFile(url: String, courseId: Long) {
        val fileName = Uri.parse(url).lastPathSegment ?: return
        val dir = File(context.filesDir, "${apiPrefs.user?.id}/external_$courseId").also { it.mkdirs() }
        val destFile = File(dir, fileName)

        if (destFile.exists()) return

        downloadToFile(url, destFile, shouldIgnoreToken = true) {}
    }

    private suspend fun downloadToFile(url: String, destFile: File, shouldIgnoreToken: Boolean, onSuccess: suspend () -> Unit) {
        val body = fileDownloadApi.downloadFile(
            url,
            RestParams(shouldIgnoreToken = shouldIgnoreToken, shouldLoginOnTokenError = false)
        ).dataOrNull ?: return

        body.saveFile(destFile).collect { state ->
            when (state) {
                is DownloadState.Success -> onSuccess()
                is DownloadState.Failure -> destFile.delete()
                else -> {}
            }
        }
    }

    override suspend fun sync() {
        TODO("Not yet implemented — will sync all/selected course files")
    }
}