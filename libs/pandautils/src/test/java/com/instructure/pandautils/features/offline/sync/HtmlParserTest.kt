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
import com.instructure.canvasapi2.models.StudioCaption
import com.instructure.canvasapi2.models.StudioMediaMetadata
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.entities.FileSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.LocalFileEntity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.Date

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

    @Test
    fun `Return html with with replaced studio iframes and studio media ids that need to be synced`() = runTest {
        val html = """
            <p>Studio Embed Below</p>
            <p><iframe class="lti-embed" style="width: 720px; height: 420px; display: inline-block;" title="bookmarks"
             src="https://test.instructure.com/courses/1/external_tools/%3Fcustom_arc_launch_type%3Dbare_embed%26custom_arc_media_id%3D123456%26custom_arc_start_at%3D0"
              width="720" height="420"> </iframe></p>
              <p>Video with captions</p>
            <p><iframe class="lti-embed" style="width: 720px; height: 420px; display: inline-block;" title="bookmarks"
             src="https://test.instructure.com/courses/1/external_tools/%3Fcustom_arc_launch_type%3Dbare_embed%26custom_arc_media_id%3D789%26custom_arc_start_at%3D0"
              width="720" height="420"> </iframe></p>
        """.trimIndent()

        val studioMetaData = listOf(
            StudioMediaMetadata(1, "123456", "title", "audio/mp4", 1000, emptyList(), "https://studio/media/123456"),
            StudioMediaMetadata(2, "789", "title", "video/mp4", 1000, listOf(
                StudioCaption("en", "caption", "English"),
                StudioCaption("es", "caption", "Spanish")
            ), "https://studio/media/789")
        )

        val result = htmlParser.createHtmlStringWithLocalFiles(html, 1L, studioMetaData)
        val expectedHtml = """
        <p>Studio Embed Below</p>
        <p><video controls playsinline preload="auto" poster="file:///files/1/studio/123456/poster.jpg">
          <source src="file:///files/1/studio/123456/123456.mp4" type="video/mp4" />

        </video></p>
          <p>Video with captions</p>
          <p><video controls playsinline preload="auto" poster="file:///files/1/studio/789/poster.jpg">
          <source src="file:///files/1/studio/789/789.mp4" type="video/mp4" />
          <track kind="captions" src="file:///files/1/studio/789/en.vtt" srclang="en" />
          <track kind="captions" src="file:///files/1/studio/789/es.vtt" srclang="es" />

        </video></p>
        """.trimIndent().filterNot { it.isWhitespace() }

        val expectedStudioMediaIds = setOf("123456", "789")
        assertEquals(expectedStudioMediaIds, result.studioMediaIds)
        assertEquals(expectedHtml, result.htmlWithLocalFileLinks?.filterNot { it.isWhitespace() })
    }

    @Test
    fun `Return html with replaced studio iframes from external tools retrieve format`() = runTest {
        val html = """
            <p>Studio video from external tools</p>
            <iframe class="lti-embed" style="width: 800px; height: 880px;" title="Canvas 101" src="https://viktoriagaramvolgyi.instructure.com/courses/67278/external_tools/retrieve?display=borderless&amp;url=https%3A%2F%2Fviktoriagaramvolgyi.instructuremedia.com%2Flti%2Flaunch%3Fcustom_arc_launch_type%3Dembed%26custom_arc_media_id%3D9bc6499d-1caf-4713-bc10-537706fa962b-435%26custom_arc_start_at%3D0&amp;com_instructure_course_canvas_resource_type=wiki_page.body&amp;com_instructure_course_canvas_resource_id=131217" width="800" height="880" loading="lazy" allowfullscreen="allowfullscreen" webkitallowfullscreen="webkitallowfullscreen" mozallowfullscreen="mozallowfullscreen" allow="geolocation *; microphone *; camera *; midi *; encrypted-media *; autoplay *; clipboard-write *; display-capture *" data-studio-resizable="false" data-studio-tray-enabled="false" data-studio-convertible-to-link="true"></iframe>
        """.trimIndent()

        val studioMetaData = listOf(
            StudioMediaMetadata(1, "9bc6499d-1caf-4713-bc10-537706fa962b-435", "Canvas 101", "video/mp4", 1000, emptyList(), "https://studio/media/9bc6499d-1caf-4713-bc10-537706fa962b-435")
        )

        val result = htmlParser.createHtmlStringWithLocalFiles(html, 1L, studioMetaData)
        val expectedHtml = """
        <p>Studio video from external tools</p>
        <video controls playsinline preload="auto" poster="file:///files/1/studio/9bc6499d-1caf-4713-bc10-537706fa962b-435/poster.jpg">
          <source src="file:///files/1/studio/9bc6499d-1caf-4713-bc10-537706fa962b-435/9bc6499d-1caf-4713-bc10-537706fa962b-435.mp4" type="video/mp4" />

        </video>
        """.trimIndent().filterNot { it.isWhitespace() }

        val expectedStudioMediaIds = setOf("9bc6499d-1caf-4713-bc10-537706fa962b-435")
        assertEquals(expectedStudioMediaIds, result.studioMediaIds)
        assertEquals(expectedHtml, result.htmlWithLocalFileLinks?.filterNot { it.isWhitespace() })
    }

    @Test
    fun `Return html with replaced studio iframes from external tools retrieve format with captions`() = runTest {
        val html = """
            <p>Studio video with captions</p>
            <iframe class="lti-embed" style="width: 800px; height: 880px;" title="Canvas Video" src="https://viktoriagaramvolgyi.instructure.com/courses/67278/external_tools/retrieve?display=borderless&amp;url=https%3A%2F%2Fviktoriagaramvolgyi.instructuremedia.com%2Flti%2Flaunch%3Fcustom_arc_launch_type%3Dembed%26custom_arc_media_id%3Dabc-123-def%26custom_arc_start_at%3D0&amp;com_instructure_course_canvas_resource_type=wiki_page.body&amp;com_instructure_course_canvas_resource_id=131217" width="800" height="880" allowfullscreen="allowfullscreen"></iframe>
        """.trimIndent()

        val studioMetaData = listOf(
            StudioMediaMetadata(1, "abc-123-def", "Canvas Video", "application/dash+xml", 2000, listOf(
                StudioCaption("en", "English captions", "English"),
                StudioCaption("fr", "French captions", "French")
            ), "https://studio/media/abc-123-def")
        )

        val result = htmlParser.createHtmlStringWithLocalFiles(html, 1L, studioMetaData)
        val expectedHtml = """
        <p>Studio video with captions</p>
        <video controls playsinline preload="auto" poster="file:///files/1/studio/abc-123-def/poster.jpg">
          <source src="file:///files/1/studio/abc-123-def/abc-123-def.mp4" type="video/mp4" />
          <track kind="captions" src="file:///files/1/studio/abc-123-def/en.vtt" srclang="en" />
          <track kind="captions" src="file:///files/1/studio/abc-123-def/fr.vtt" srclang="fr" />

        </video>
        """.trimIndent().filterNot { it.isWhitespace() }

        val expectedStudioMediaIds = setOf("abc-123-def")
        assertEquals(expectedStudioMediaIds, result.studioMediaIds)
        assertEquals(expectedHtml, result.htmlWithLocalFileLinks?.filterNot { it.isWhitespace() })
    }

    @Test
    fun `Return html with both old and new iframe formats in same html`() = runTest {
        val html = """
            <p>Old format video</p>
            <iframe class="lti-embed" style="width: 720px; height: 420px;" title="Old Format"
             src="https://test.instructure.com/courses/1/external_tools/%3Fcustom_arc_launch_type%3Dbare_embed%26custom_arc_media_id%3Dold-format-123%26custom_arc_start_at%3D0"
              width="720" height="420"> </iframe>
            <p>New format video</p>
            <iframe class="lti-embed" style="width: 800px; height: 880px;" title="New Format" src="https://viktoriagaramvolgyi.instructure.com/courses/67278/external_tools/retrieve?display=borderless&amp;url=https%3A%2F%2Fviktoriagaramvolgyi.instructuremedia.com%2Flti%2Flaunch%3Fcustom_arc_launch_type%3Dembed%26custom_arc_media_id%3Dnew-format-456%26custom_arc_start_at%3D0" width="800" height="880" allowfullscreen="allowfullscreen"></iframe>
        """.trimIndent()

        val studioMetaData = listOf(
            StudioMediaMetadata(1, "old-format-123", "Old Format", "video/mp4", 1000, emptyList(), "https://studio/media/old-format-123"),
            StudioMediaMetadata(2, "new-format-456", "New Format", "video/mp4", 1500, emptyList(), "https://studio/media/new-format-456")
        )

        val result = htmlParser.createHtmlStringWithLocalFiles(html, 1L, studioMetaData)
        val expectedHtml = """
        <p>Old format video</p>
        <video controls playsinline preload="auto" poster="file:///files/1/studio/old-format-123/poster.jpg">
          <source src="file:///files/1/studio/old-format-123/old-format-123.mp4" type="video/mp4" />

        </video>
        <p>New format video</p>
        <video controls playsinline preload="auto" poster="file:///files/1/studio/new-format-456/poster.jpg">
          <source src="file:///files/1/studio/new-format-456/new-format-456.mp4" type="video/mp4" />

        </video>
        """.trimIndent().filterNot { it.isWhitespace() }

        val expectedStudioMediaIds = setOf("old-format-123", "new-format-456")
        assertEquals(expectedStudioMediaIds, result.studioMediaIds)
        assertEquals(expectedHtml, result.htmlWithLocalFileLinks?.filterNot { it.isWhitespace() })
    }

    @Test
    fun `Return html with studio iframe and verify mimeType is overridden to video mp4`() = runTest {
        val html = """
            <p>DASH format video</p>
            <iframe class="lti-embed" style="width: 800px; height: 880px;" title="DASH Video" src="https://test.instructure.com/courses/67278/external_tools/retrieve?display=borderless&amp;url=https%3A%2F%2Ftest.instructuremedia.com%2Flti%2Flaunch%3Fcustom_arc_launch_type%3Dembed%26custom_arc_media_id%3Ddash-format-789%26custom_arc_start_at%3D0" width="800" height="880" allowfullscreen="allowfullscreen"></iframe>
        """.trimIndent()

        val studioMetaData = listOf(
            StudioMediaMetadata(1, "dash-format-789", "DASH Video", "application/dash+xml", 3000, emptyList(), "https://studio/media/dash-format-789")
        )

        val result = htmlParser.createHtmlStringWithLocalFiles(html, 1L, studioMetaData)
        val expectedHtml = """
        <p>DASH format video</p>
        <video controls playsinline preload="auto" poster="file:///files/1/studio/dash-format-789/poster.jpg">
          <source src="file:///files/1/studio/dash-format-789/dash-format-789.mp4" type="video/mp4" />

        </video>
        """.trimIndent().filterNot { it.isWhitespace() }

        val expectedStudioMediaIds = setOf("dash-format-789")
        assertEquals(expectedStudioMediaIds, result.studioMediaIds)
        assertEquals(expectedHtml, result.htmlWithLocalFileLinks?.filterNot { it.isWhitespace() })
        assertTrue(result.htmlWithLocalFileLinks?.contains("type=\"video/mp4\"") == true)
    }

    @Test
    fun `Return html with studio iframe without metadata`() = runTest {
        val html = """
            <p>Video without metadata</p>
            <iframe class="lti-embed" style="width: 800px; height: 880px;" title="Unknown Video" src="https://test.instructure.com/courses/67278/external_tools/retrieve?display=borderless&amp;url=https%3A%2F%2Ftest.instructuremedia.com%2Flti%2Flaunch%3Fcustom_arc_launch_type%3Dembed%26custom_arc_media_id%3Dunknown-video-id%26custom_arc_start_at%3D0" width="800" height="880" allowfullscreen="allowfullscreen"></iframe>
        """.trimIndent()

        val studioMetaData = emptyList<StudioMediaMetadata>()

        val result = htmlParser.createHtmlStringWithLocalFiles(html, 1L, studioMetaData)
        val expectedHtml = """
        <p>Video without metadata</p>
        <video controls playsinline preload="auto" poster="file:///files/1/studio/unknown-video-id/poster.jpg">
          <source src="file:///files/1/studio/unknown-video-id/unknown-video-id.mp4" type="video/mp4" />

        </video>
        """.trimIndent().filterNot { it.isWhitespace() }

        val expectedStudioMediaIds = setOf("unknown-video-id")
        assertEquals(expectedStudioMediaIds, result.studioMediaIds)
        assertEquals(expectedHtml, result.htmlWithLocalFileLinks?.filterNot { it.isWhitespace() })
    }

    @Test
    fun `Return html with mixed content - images, files, and studio videos`() = runTest {
        val html = """
            <p>Internal image:</p>
            <img src="https://mobiledev.instructure.com/files/123456/download" alt="Internal" />
            <p>External image:</p>
            <img src="https://example.com/image.jpg" alt="External" />
            <p>File link:</p>
            <a class="instructure_file_link" href="https://mobiledev.instructure.com/courses/1L/files/789?wrap=1">document.pdf</a>
            <p>Studio video old format:</p>
            <iframe class="lti-embed" src="https://test.instructure.com/courses/1/external_tools/%3Fcustom_arc_launch_type%3Dbare_embed%26custom_arc_media_id%3Dvideo-old%26custom_arc_start_at%3D0" width="720" height="420"> </iframe>
            <p>Studio video new format:</p>
            <iframe class="lti-embed" src="https://test.instructure.com/courses/67278/external_tools/retrieve?display=borderless&amp;url=https%3A%2F%2Ftest.instructuremedia.com%2Flti%2Flaunch%3Fcustom_arc_launch_type%3Dembed%26custom_arc_media_id%3Dvideo-new%26custom_arc_start_at%3D0" width="800" height="880"></iframe>
        """.trimIndent()

        coEvery { localFileDao.findById(123456) } returns LocalFileEntity(123456, 1L, Date(), "/files/1/123456_internal.jpg")
        coEvery { fileSyncSettingsDao.findById(789) } returns null

        val studioMetaData = listOf(
            StudioMediaMetadata(1, "video-old", "Old Video", "video/mp4", 1000, emptyList(), "https://studio/media/video-old"),
            StudioMediaMetadata(2, "video-new", "New Video", "application/dash+xml", 2000, listOf(
                StudioCaption("en", "English", "English")
            ), "https://studio/media/video-new")
        )

        val result = htmlParser.createHtmlStringWithLocalFiles(html, 1L, studioMetaData)

        assertEquals(1, result.internalFileIds.size)
        assertTrue(result.internalFileIds.contains(789))
        assertEquals(1, result.externalFileUrls.size)
        assertTrue(result.externalFileUrls.contains("https://example.com/image.jpg"))
        assertEquals(2, result.studioMediaIds.size)
        assertTrue(result.studioMediaIds.containsAll(setOf("video-old", "video-new")))

        assertTrue(result.htmlWithLocalFileLinks?.contains("file:///files/1/123456_internal.jpg") == true)
        assertTrue(result.htmlWithLocalFileLinks?.contains("file:///files/1/external_1/image.jpg") == true)
        assertTrue(result.htmlWithLocalFileLinks?.contains("video-old.mp4") == true)
        assertTrue(result.htmlWithLocalFileLinks?.contains("video-new.mp4") == true)
        assertTrue(result.htmlWithLocalFileLinks?.contains("en.vtt") == true)
    }
}