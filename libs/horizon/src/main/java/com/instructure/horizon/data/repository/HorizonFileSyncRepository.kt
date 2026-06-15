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
import com.instructure.horizon.database.dao.HorizonFileSyncPlanDao
import com.instructure.horizon.database.dao.HorizonLocalFileDao
import com.instructure.horizon.database.entity.HorizonFileFolderEntity
import com.instructure.horizon.database.entity.HorizonFileSyncPlanEntity
import com.instructure.horizon.database.entity.HorizonLocalFileEntity
import com.instructure.horizon.offline.FileSyncPaths
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.horizon.offline.sync.HorizonProgressState
import com.instructure.horizon.util.sanitizeAsFilename
import com.instructure.pandautils.features.offline.sync.HtmlParsingResult
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class HorizonFileSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileDownloadApi: FileDownloadAPI,
    private val localFileDao: HorizonLocalFileDao,
    private val fileFolderDao: HorizonFileFolderDao,
    private val fileSyncPlanDao: HorizonFileSyncPlanDao,
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface,
    private val apiPrefs: ApiPrefs,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun downloadFile(fileId: Long, courseId: Long, expectedSize: Long? = null, onProgress: suspend (Int) -> Unit = {}) {
        downloadInternalFile(fileId, courseId, expectedSize, onProgress)
    }

    suspend fun downloadFileByUrl(
        fileId: Long,
        courseId: Long,
        url: String,
        displayName: String,
        expectedSize: Long? = null,
        onProgress: suspend (Int) -> Unit = {}
    ) {
        if (localFileDao.findById(fileId) != null) return
        val userId = apiPrefs.user?.id ?: return
        val dir = FileSyncPaths.userDir(context.filesDir, userId).also { it.mkdirs() }
        val destFile = File(dir, "${fileId}_${displayName.sanitizeAsFilename()}")

        if (destFile.exists()) return

        downloadToFile(url, destFile, shouldIgnoreToken = false, expectedSize = expectedSize, onProgress = onProgress) {
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

    suspend fun syncHtmlFiles(courseId: Long, parsingResult: HtmlParsingResult) = withContext(Dispatchers.IO) {
        val alreadyDownloadedIds = localFileDao.findByCourseId(courseId).map { it.id }.toSet()
        val internalFileIdsToSync = parsingResult.internalFileIds.filterNot { alreadyDownloadedIds.contains(it) }

        val existingPlanIds = fileSyncPlanDao.findByCourseId(courseId).map { it.fileId }.toSet()
        val internalFileSizeById = mutableMapOf<Long, Long>()
        for (fileId in internalFileIdsToSync) {
            if (fileId in existingPlanIds) continue
            val fileInfo = resolveInternalFileInfo(fileId, courseId)
            val fileSize = fileInfo?.size ?: 0L
            internalFileSizeById[fileId] = fileSize
            fileSyncPlanDao.upsert(
                HorizonFileSyncPlanEntity(
                    fileId = fileId,
                    courseId = courseId,
                    fileName = fileInfo?.displayName ?: "file_$fileId",
                    fileSize = fileSize,
                    state = HorizonProgressState.PENDING,
                    isAdditionalFile = true,
                )
            )
        }

        coroutineScope {
            internalFileIdsToSync.chunked(6).forEach { chunk ->
                chunk.map { fileId ->
                    async {
                        try {
                            fileSyncPlanDao.updateProgress(fileId, courseId, 0, HorizonProgressState.IN_PROGRESS)
                            val expectedSize = internalFileSizeById[fileId]?.takeIf { it > 0 }
                            downloadInternalFile(fileId, courseId, expectedSize) { percent ->
                                fileSyncPlanDao.updateProgress(fileId, courseId, percent, HorizonProgressState.IN_PROGRESS)
                            }
                            fileSyncPlanDao.updateProgress(fileId, courseId, 100, HorizonProgressState.COMPLETED)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            fileSyncPlanDao.updateProgress(fileId, courseId, 0, HorizonProgressState.ERROR)
                        }
                    }
                }.awaitAll()
            }
        }

        coroutineScope {
            parsingResult.externalFileUrls.toList().chunked(6).forEach { chunk ->
                chunk.map { url -> async { downloadExternalFile(url, courseId) } }.awaitAll()
            }
        }
    }

    private suspend fun downloadInternalFile(fileId: Long, courseId: Long, expectedSize: Long? = null, onProgress: suspend (Int) -> Unit = {}) {
        val fileInfo = resolveInternalFileInfo(fileId, courseId) ?: return
        val userId = apiPrefs.user?.id ?: return
        val dir = FileSyncPaths.userDir(context.filesDir, userId).also { it.mkdirs() }
        val destFile = File(dir, "${fileId}_${fileInfo.displayName.sanitizeAsFilename()}")

        if (destFile.exists()) return

        downloadToFile(fileInfo.url, destFile, shouldIgnoreToken = false, expectedSize = expectedSize, onProgress = onProgress) {
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

        val entity = HorizonFileFolderEntity(
            id = fileId,
            courseId = courseId,
            url = url,
            displayName = displayName,
            size = file.size,
            contentType = file.contentType,
            thumbnailUrl = file.thumbnailUrl,
        )
        fileFolderDao.insert(entity)
        return entity
    }

    private suspend fun downloadExternalFile(url: String, courseId: Long) {
        val fileName = Uri.parse(url).lastPathSegment?.sanitizeAsFilename() ?: return
        val userId = apiPrefs.user?.id ?: return
        val dir = FileSyncPaths.externalUrlDir(context.filesDir, userId, courseId).also { it.mkdirs() }
        val destFile = File(dir, fileName)

        if (destFile.exists()) return

        val pseudoId = externalUrlPseudoId(url)
        val existing = fileSyncPlanDao.findByCourseId(courseId).firstOrNull { it.fileId == pseudoId }
        if (existing == null) {
            fileSyncPlanDao.upsert(
                HorizonFileSyncPlanEntity(
                    fileId = pseudoId,
                    courseId = courseId,
                    fileName = fileName,
                    fileSize = 0L,
                    state = HorizonProgressState.PENDING,
                    isAdditionalFile = true,
                )
            )
        }

        try {
            fileSyncPlanDao.updateProgress(pseudoId, courseId, 0, HorizonProgressState.IN_PROGRESS)
            downloadToFile(
                url = url,
                destFile = destFile,
                shouldIgnoreToken = true,
                onProgress = { percent ->
                    fileSyncPlanDao.updateProgress(pseudoId, courseId, percent, HorizonProgressState.IN_PROGRESS)
                },
                onTotalBytesKnown = { totalBytes ->
                    fileSyncPlanDao.updateFileSize(pseudoId, courseId, totalBytes)
                },
            ) {}
            fileSyncPlanDao.updateProgress(pseudoId, courseId, 100, HorizonProgressState.COMPLETED)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            fileSyncPlanDao.updateProgress(pseudoId, courseId, 0, HorizonProgressState.ERROR)
        }
    }

    // Negative so it cannot collide with real (positive) Canvas file IDs.
    private fun externalUrlPseudoId(url: String): Long {
        val bits = UUID.nameUUIDFromBytes(url.toByteArray()).leastSignificantBits
        return if (bits < 0) bits else -1L - bits
    }

    private suspend fun downloadToFile(
        url: String,
        destFile: File,
        shouldIgnoreToken: Boolean,
        expectedSize: Long? = null,
        onProgress: suspend (Int) -> Unit = {},
        onTotalBytesKnown: suspend (Long) -> Unit = {},
        onSuccess: suspend () -> Unit,
    ) {
        val body = fileDownloadApi.downloadFile(
            url,
            RestParams(shouldIgnoreToken = shouldIgnoreToken, shouldLoginOnTokenError = false)
        ).dataOrNull ?: return

        val tmpFile = File(destFile.parentFile, "${destFile.name}.tmp")
        if (tmpFile.exists()) tmpFile.delete()

        var succeeded = false
        var totalBytesReported = false
        try {
            body.saveFile(tmpFile).collect { state ->
                when (state) {
                    is DownloadState.Success -> {
                        if (expectedSize != null && expectedSize > 0 && tmpFile.length() != expectedSize) {
                            tmpFile.delete()
                            throw SizeMismatchException(expectedSize, tmpFile.length())
                        }
                        fsync(tmpFile)
                        if (!tmpFile.renameTo(destFile)) {
                            tmpFile.copyTo(destFile, overwrite = true)
                            tmpFile.delete()
                        }
                        succeeded = true
                        onSuccess()
                    }
                    is DownloadState.Failure -> tmpFile.delete()
                    is DownloadState.InProgress -> {
                        if (!totalBytesReported && state.totalBytes > 0) {
                            totalBytesReported = true
                            onTotalBytesKnown(state.totalBytes)
                        }
                        onProgress(state.progress)
                    }
                }
            }
        } finally {
            if (!succeeded && tmpFile.exists()) tmpFile.delete()
        }
    }

    private fun fsync(file: File) {
        try {
            RandomAccessFile(file, "r").use { it.fd.sync() }
        } catch (_: Exception) {
            // Best-effort flush; rename below still happens.
        }
    }
}

class SizeMismatchException(
    val expected: Long,
    val actual: Long,
) : RuntimeException("Downloaded file size mismatch: expected $expected, got $actual")