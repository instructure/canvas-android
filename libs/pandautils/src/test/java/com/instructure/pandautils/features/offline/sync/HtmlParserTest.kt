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
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.entities.FileSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.LocalFileEntity
import com.instructure.pandautils.utils.FilePrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class HtmlParserTest {

    private var localFileDao: LocalFileDao = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val fileFolderDao: FileFolderDao = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val fileSyncSettingsDao: FileSyncSettingsDao = mockk(relaxed = true)
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface = mockk(relaxed = true)

    private val htmlParser = HtmlParser(localFileDao, apiPrefs, fileFolderDao, context, fileSyncSettingsDao, fileFolderApi)

    @Before
    fun setup() {
        every { context.filesDir } returns File("/files")
        every { apiPrefs.domain } returns "https://mobiledev.instructure.com"
        every { apiPrefs.user!!.id } returns 1L

        mockkStatic(Uri::class)
        every { Uri.parse(any()) } answers {
            val url = it.invocation.args.first() as String
            mockk<Uri>() {
                every { lastPathSegment } returns url.split("/").last()
                every { scheme } returns "https"
            }
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Return empty result when html is null`() = runTest {
        val result = htmlParser.createHtmlStringWithLocalFiles(null, 1L)

        assertNull(result.htmlWithLocalFileLinks)
        assertTrue(result.internalFileIds.isEmpty())
        assertTrue(result.externalFileUrls.isEmpty())
    }

    @Test
    fun `Return empty result with input html when no match is found`() = runTest {
        val html = "<p>This is an Assignment. You can tell when you look at it from the Calendar or from the Modules page because it has the Canvas Assignments Icon displayed next to it: &nbsp;<i class=\"icon-assignment\"></i>&nbsp;</p>\n" +
            "<hr />"

        val result = htmlParser.createHtmlStringWithLocalFiles(html, 1L)
        assertEquals(html, result.htmlWithLocalFileLinks)
        assertTrue(result.internalFileIds.isEmpty())
        assertTrue(result.externalFileUrls.isEmpty())
    }

    @Test
    fun `Return html with local file links and do not sync file when local file already exists`() = runTest {
        val html = "<p>This is an Assignment. You can tell when you look at it from the Calendar or from the Modules page because it has the Canvas Assignments Icon displayed next to it: &nbsp;<i class=\"icon-assignment\"></i>&nbsp;</p>\n" +
            "<hr />" +
            "<img src=\"https://mobiledev.instructure.com/files/123456/download\" alt=\"\" />"

        coEvery { localFileDao.findById(123456) } returns LocalFileEntity(123456, 1L, Date(), "/files/1/123456_filename.jpg")

        val result = htmlParser.createHtmlStringWithLocalFiles(html, 1L)
        val expectedHtml = "<p>This is an Assignment. You can tell when you look at it from the Calendar or from the Modules page because it has the Canvas Assignments Icon displayed next to it: &nbsp;<i class=\"icon-assignment\"></i>&nbsp;</p>\n" +
            "<hr />" +
            "<img src=\"file:///files/1/123456_filename.jpg\" alt=\"\" />"

        assertEquals(expectedHtml, result.htmlWithLocalFileLinks)
        assertEquals(0, result.internalFileIds.size)
        assertEquals(0, result.externalFileUrls.size)
    }

    @Test
    fun `Return html with local file links and sync file when local file doesn't exist`() = runTest {
        val html = "<p>This is an Assignment. You can tell when you look at it from the Calendar or from the Modules page because it has the Canvas Assignments Icon displayed next to it: &nbsp;<i class=\"icon-assignment\"></i>&nbsp;</p>\n" +
            "<hr />" +
            "<img src=\"https://mobiledev.instructure.com/files/123456/download\" alt=\"\" />"

        coEvery { fileFolderApi.getCourseFile(1L, 123456, any()) } returns DataResult.Success(
            FileFolder(id = 123456, displayName = "filenameFromNetwork.jpg")
        )

        coEvery { fileSyncSettingsDao.findById(123456) } returns null

        val result = htmlParser.createHtmlStringWithLocalFiles(html, 1L)
        val expectedHtml = "<p>This is an Assignment. You can tell when you look at it from the Calendar or from the Modules page because it has the Canvas Assignments Icon displayed next to it: &nbsp;<i class=\"icon-assignment\"></i>&nbsp;</p>\n" +
            "<hr />" +
            "<img src=\"file:///files/1/123456_filenameFromNetwork.jpg\" alt=\"\" />"

        assertEquals(expectedHtml, result.htmlWithLocalFileLinks)
        assertEquals(1, result.internalFileIds.size)
        assertTrue(result.internalFileIds.contains(123456))
        assertEquals(0, result.externalFileUrls.size)
    }

    @Test
    fun `Return html with local file links and sync for external files`() = runTest {
        val html = "<p>This is an Assignment. You can tell when you look at it from the Calendar or from the Modules page because it has the Canvas Assignments Icon displayed next to it: &nbsp;<i class=\"icon-assignment\"></i>&nbsp;</p>\n" +
            "<hr />" +
            "<img src=\"https://live.staticflickr.com/65535/53057996255_0c9e5e5a68_z.jpg\" alt=\"\" />"

        val result = htmlParser.createHtmlStringWithLocalFiles(html, 1L)
        val expectedHtml = "<p>This is an Assignment. You can tell when you look at it from the Calendar or from the Modules page because it has the Canvas Assignments Icon displayed next to it: &nbsp;<i class=\"icon-assignment\"></i>&nbsp;</p>\n" +
            "<hr />" +
            "<img src=\"file:///files/1/external_1/53057996255_0c9e5e5a68_z.jpg\" alt=\"\" />"

        assertEquals(expectedHtml, result.htmlWithLocalFileLinks)
        assertEquals(0, result.internalFileIds.size)
        assertEquals(1, result.externalFileUrls.size)
        assertTrue(result.externalFileUrls.contains("https://live.staticflickr.com/65535/53057996255_0c9e5e5a68_z.jpg"))
    }

    @Test
    fun `Test parsing with multiple external and internal files`() = runTest {
        val html = "<p>External file:</p>\n" +
            "<p><a title=\"Silhouettes\" href=\"https://www.flickr.com/photos/197758383@N07/52993992415/in/dateposted-public/\" data-flickr-embed=\"true\"><img src=\"https://live.staticflickr.com/65535/52993992415_0bd9344aba_z.jpg\" alt=\"Silhouettes\" width=\"640\" height=\"425\" /></a></p>\n" +
            "<p>Internal image:</p>\n" +
            "<p><img id=\"123456\" src=\"https://mobiledev.instructure.com/courses/1/files/123456/preview\" alt=\"image1.jpg\" /></p>\n" +
            "<p>An other external:</p>\n" +
            "<p><img src=\"https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg\" alt=\"Alt text\" /></p>\n" +
            "<p>Internal public:</p>\n" +
            "<p><img id=\"789\" src=\"https://mobiledev.instructure.com/courses/1/files/789/preview\" alt=\"image2.png\" /></p>"

        coEvery { fileFolderApi.getCourseFile(1L, 123456, any()) } returns DataResult.Success(
            FileFolder(id = 123456)
        )
        coEvery { fileSyncSettingsDao.findById(123456) } returns null
        coEvery { localFileDao.findById(789) } returns LocalFileEntity(789, 1L, Date(), "/files/1/789_image2.png")

        val result = htmlParser.createHtmlStringWithLocalFiles(html, 1L)

        val expectedHtml = "<p>External file:</p>\n" +
            "<p><a title=\"Silhouettes\" href=\"https://www.flickr.com/photos/197758383@N07/52993992415/in/dateposted-public/\" data-flickr-embed=\"true\"><img src=\"file:///files/1/external_1/52993992415_0bd9344aba_z.jpg\" alt=\"Silhouettes\" width=\"640\" height=\"425\" /></a></p>\n" +
            "<p>Internal image:</p>\n" +
            "<p><img id=\"123456\" src=\"file:///files/1/123456\" alt=\"image1.jpg\" /></p>\n" +
            "<p>An other external:</p>\n" +
            "<p><img src=\"file:///files/1/external_1/maxresdefault.jpg\" alt=\"Alt text\" /></p>\n" +
            "<p>Internal public:</p>\n" +
            "<p><img id=\"789\" src=\"file:///files/1/789_image2.png\" alt=\"image2.png\" /></p>"

        assertEquals(expectedHtml, result.htmlWithLocalFileLinks)

        assertEquals(1, result.internalFileIds.size)
        assertTrue(result.internalFileIds.contains(123456)) // We only have this one here because the other internal file is already synced

        assertEquals(2, result.externalFileUrls.size)
        assertTrue(result.externalFileUrls.contains("https://live.staticflickr.com/65535/52993992415_0bd9344aba_z.jpg"))
        assertTrue(result.externalFileUrls.contains("https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg"))
    }

    @Test
    fun `Add internal file ids from file links only when the file is not synced already`() = runTest {
        val html = "<p>File already synced</p>\n" +
            "<p><a class=\"instructure_file_link\" href=\"https://tamaskozmer.instructure.com/courses/1L/files/678?wrap=1\" target=\"_blank\" rel=\"noopener\">Group One Final Project -1.key</a></p>\n" +
            "<p>&nbsp;</p>\n" +
            "<p>File not synced:</p>\n" +
            "<p><a class=\"instructure_file_link instructure_scribd_file inline_disabled\" href=\"https://mobiledev.instructure.com/courses/1L/files/1234?wrap=1\" target=\"_blank\" rel=\"noopener\" data-api-endpoint=\"https://mobiledev.instructure.com/api/v1/courses/1L/files/1234\" data-api-returntype=\"File\">file.pdf</a></p>"

        coEvery { fileSyncSettingsDao.findById(1234) } returns FileSyncSettingsEntity(1234, "name", 1L, "")
        coEvery { fileSyncSettingsDao.findById(678) } returns null

        val result = htmlParser.createHtmlStringWithLocalFiles(html, 1L)

        assertEquals(1, result.internalFileIds.size)
        assertEquals(678, result.internalFileIds.first())
        assertEquals(0, result.externalFileUrls.size)
    }
}