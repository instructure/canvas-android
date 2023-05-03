/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.emeritus.student.test.util

import com.emeritus.student.R
import com.emeritus.student.util.FileUtils
import org.junit.Assert
import org.junit.Test

class FileUtilsTest : Assert() {

    // region content type
    @Test
    fun `getFileIcon with image contentType results in image icon`() {
        assertEquals(R.drawable.ic_image, FileUtils.getFileIcon("filename.jpg", "image"))
    }

    @Test
    fun `getFileIcon with image star contentType results in image icon`() {
        assertEquals(R.drawable.ic_image, FileUtils.getFileIcon("filename.jpg", "image/*"))
    }

    @Test
    fun `getFileIcon with video star contentType results in media icon`() {
        assertEquals(R.drawable.ic_media, FileUtils.getFileIcon("filename.mp4", "video/*"))
    }

    @Test
    fun `getFileIcon with video contentType results in media icon`() {
        assertEquals(R.drawable.ic_media, FileUtils.getFileIcon("filename.mp4", "video"))
    }

    @Test
    fun `getFileIcon with audio contentType results in audio icon`() {
        assertEquals(R.drawable.ic_audio, FileUtils.getFileIcon("filename.mp3", "audio"))
    }

    @Test
    fun `getFileIcon with audio star contentType results in audio icon`() {
        assertEquals(R.drawable.ic_audio, FileUtils.getFileIcon("filename.mp3", "audio/*"))
    }
    // endregion content type

    // region doc extensions
    @Test
    fun `getFileIcon with doc extension results in doc icon`() {
        assertEquals(R.drawable.ic_document, FileUtils.getFileIcon("filename.doc", ""))
    }
    @Test
    fun `getFileIcon with docx extension results in doc icon`() {
        assertEquals(R.drawable.ic_document, FileUtils.getFileIcon("filename.docx", ""))
    }
    @Test
    fun `getFileIcon with txt extension results in doc icon`() {
        assertEquals(R.drawable.ic_document, FileUtils.getFileIcon("filename.txt", ""))
    }
    @Test
    fun `getFileIcon with rtf extension results in doc icon`() {
        assertEquals(R.drawable.ic_document, FileUtils.getFileIcon("filename.rtf", ""))
    }
    @Test
    fun `getFileIcon with pdf extension results in doc icon`() {
        assertEquals(R.drawable.ic_document, FileUtils.getFileIcon("filename.pdf", ""))
    }
    @Test
    fun `getFileIcon with xls extension results in doc icon`() {
        assertEquals(R.drawable.ic_document, FileUtils.getFileIcon("filename.xls", ""))
    }
    // endregion doc extensions

    // region compressed/etc extensions
    @Test
    fun `getFileIcon with zip extension results in attachment icon`() {
        assertEquals(R.drawable.ic_attachment, FileUtils.getFileIcon("filename.zip", ""))
    }
    @Test
    fun `getFileIcon with tar extension results in attachment icon`() {
        assertEquals(R.drawable.ic_attachment, FileUtils.getFileIcon("filename.tar", ""))
    }
    @Test
    fun `getFileIcon with 7z extension results in attachment icon`() {
        assertEquals(R.drawable.ic_attachment, FileUtils.getFileIcon("filename.7z", ""))
    }
    @Test
    fun `getFileIcon with apk extension results in attachment icon`() {
        assertEquals(R.drawable.ic_attachment, FileUtils.getFileIcon("filename.apk", ""))
    }
    @Test
    fun `getFileIcon with jar extension results in attachment icon`() {
        assertEquals(R.drawable.ic_attachment, FileUtils.getFileIcon("filename.jar", ""))
    }
    @Test
    fun `getFileIcon with rar extension results in attachment icon`() {
        assertEquals(R.drawable.ic_attachment, FileUtils.getFileIcon("filename.rar", ""))
    }
    // endregion compressed/etc extensions

    @Test
    fun `getFileIcon with empty filename results in attachment icon`() {
        assertEquals(R.drawable.ic_attachment, FileUtils.getFileIcon("", ""))
    }

    @Test
    fun `getFileIcon with empty filename and contentType results in attachment icon`() {
        assertEquals(R.drawable.ic_attachment, FileUtils.getFileIcon("", ""))
    }
}
