/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.features.offline.sync

import android.content.Context
import android.net.Uri
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File

class HtmlParser(
    private var localFileDao: LocalFileDao,
    private val apiPrefs: ApiPrefs,
    private val fileFolderDao: FileFolderDao,
    @ApplicationContext private val context: Context,
    private val fileSyncSettingsDao: FileSyncSettingsDao,
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface
) {

    private val imageRegex = Regex("<img[^>]*src=\"([^\"]*)\"[^>]*>")
    private val fileLinkRegex = Regex("<a[^>]*class=\"instructure_file_link[^>]*href=\"([^\"]*)\"[^>]*>")
    private val internalFileRegex = Regex(".*${apiPrefs.domain}.*files/(\\d+)")

    suspend fun createHtmlStringWithLocalFiles(html: String?, courseId: Long): HtmlParsingResult {
        if (html == null) return HtmlParsingResult(null, emptySet(), emptySet())

        val imageParsingResult = parseAndReplaceImageTags(html, courseId)
        val filesFromFileLinks = findFileIdsToSync(imageParsingResult.htmlWithLocalFileLinks ?: html)

        return imageParsingResult.copy(internalFileIds = imageParsingResult.internalFileIds + filesFromFileLinks)
    }

    private suspend fun parseAndReplaceImageTags(originalHtml: String, courseId: Long): HtmlParsingResult {
        var resultHtml: String = originalHtml
        val internalFileIds = mutableSetOf<Long>()
        val externalFileUrls = mutableSetOf<String>()

        val matches = imageRegex.findAll(resultHtml)
        matches.forEach { match ->
            val imageUrl = match.groupValues[1]
            val fileId = internalFileRegex.find(imageUrl)?.groupValues?.get(1)?.toLongOrNull()
            if (fileId != null) {
                val (newHtml, shouldSyncFile) = replaceInternalFileUrl(resultHtml, courseId, fileId, imageUrl)
                resultHtml = newHtml
                if (shouldSyncFile) internalFileIds.add(fileId)
            } else {
                val fileUri = Uri.parse(imageUrl)
                val fileName = fileUri.lastPathSegment
                if (fileName != null && fileUri.scheme == "https") { // We don't allow cleartext traffic in the app.
                    resultHtml = resultHtml.replace(imageUrl, "file://${createLocalFilePathForExternalFile(fileName, courseId)}")
                    externalFileUrls.add(imageUrl)
                }
            }
        }

        return HtmlParsingResult(resultHtml, internalFileIds, externalFileUrls)
    }

    private suspend fun replaceInternalFileUrl(html: String, courseId: Long, fileId: Long, imageUrl: String): Pair<String, Boolean> {
        var resultHtml = html
        var shouldSyncFile = false

        val filePath = localFileDao.findById(fileId)?.path
        if (!filePath.isNullOrEmpty()) {
            resultHtml = resultHtml.replace(imageUrl, "file://$filePath")
        } else {
            resultHtml = resultHtml.replace(imageUrl, "file://${createLocalFilePath(fileId, courseId)}")
            if (fileSyncSettingsDao.findById(fileId) == null) {
                shouldSyncFile = true
            }
        }

        return Pair(resultHtml, shouldSyncFile)
    }

    private suspend fun createLocalFilePath(fileId: Long, courseId: Long): String {
        var fileName = fileFolderDao.findById(fileId)?.displayName.orEmpty()
        if (fileName.isEmpty()) {
            val file = fileFolderApi.getCourseFile(courseId, fileId, RestParams(isForceReadFromNetwork = false)).dataOrNull
            fileName = file?.displayName.orEmpty()
        }
        val fileNameWithId = if (fileName.isNotEmpty()) "${fileId}_$fileName" else "$fileId"
        val dir = File(context.filesDir, apiPrefs.user?.id.toString())

        val downloadedFile = File(dir, fileNameWithId)
        return downloadedFile.absolutePath
    }

    private suspend fun createLocalFilePathForExternalFile(fileName: String, courseId: Long): String {
        val dir = File(context.filesDir, "${apiPrefs.user?.id.toString()}/external_$courseId")

        val downloadedFile = File(dir, fileName)
        return downloadedFile.absolutePath
    }

    private suspend fun findFileIdsToSync(html: String): Set<Long> {
        val internalFileIds = mutableSetOf<Long>()

        val fileMatches = fileLinkRegex.findAll(html)
        fileMatches.forEach { match ->
            val fileUrl = match.groupValues[1]
            val fileId = internalFileRegex.find(fileUrl)?.groupValues?.get(1)?.toLongOrNull()
            if (fileId != null) {
                if (fileSyncSettingsDao.findById(fileId) == null) {
                    internalFileIds.add(fileId)
                }
            }
        }

        return internalFileIds
    }
}

data class HtmlParsingResult(
    val htmlWithLocalFileLinks: String?,
    val internalFileIds: Set<Long>,
    val externalFileUrls: Set<String>
)