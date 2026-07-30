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
package com.instructure.ngc.features.assignmentdetails

import android.webkit.URLUtil
import com.instructure.canvasapi2.models.Assignment.SubmissionType
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.MediaComment
import com.instructure.canvasapi2.models.Submission
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NgcSubmissionPreviewMapperTest {

    @Before
    fun setUp() {
        // Constructing an Attachment eagerly evaluates its `isLocalFile` (URLUtil.isNetworkUrl),
        // an Android framework method that isn't available in plain JUnit; stub it so the mapper's
        // attachment handling can be exercised.
        mockkStatic(URLUtil::class)
        every { URLUtil.isNetworkUrl(any()) } returns true
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun submission(type: SubmissionType, block: Submission.() -> Submission = { this }) =
        Submission(submissionType = type.apiString).block()

    private fun attachment(contentType: String, name: String = "file", url: String = "https://host/$name") =
        Attachment(contentType = contentType, filename = name, displayName = name, url = url)

    @Test
    fun `null submission has no preview`() {
        assertEquals(NgcSubmissionPreviewPlan.NoPreview, mapSubmissionPreviewPlan(null, baseUrl = null))
    }

    @Test
    fun `quiz, external tool and discussion have no preview`() {
        listOf(
            SubmissionType.ONLINE_QUIZ,
            SubmissionType.EXTERNAL_TOOL,
            SubmissionType.DISCUSSION_TOPIC,
        ).forEach { type ->
            assertEquals(NgcSubmissionPreviewPlan.NoPreview, mapSubmissionPreviewPlan(submission(type), baseUrl = null))
        }
    }

    @Test
    fun `student annotation maps to an annotated pdf by submission id and attempt`() {
        val plan = mapSubmissionPreviewPlan(
            submission(SubmissionType.STUDENT_ANNOTATION) { copy(id = 42L, attempt = 3L) },
            baseUrl = null,
        )
        assertEquals(NgcSubmissionPreviewPlan.AnnotatedPdf(submissionId = 42L, attempt = 3L), plan)
    }

    @Test
    fun `text entry maps to the submission body and base url`() {
        val plan = mapSubmissionPreviewPlan(
            submission(SubmissionType.ONLINE_TEXT_ENTRY) { copy(body = "<p>Hi</p>") },
            baseUrl = "https://host",
        )
        assertEquals(NgcSubmissionPreviewPlan.TextEntry("<p>Hi</p>", "https://host"), plan)
    }

    @Test
    fun `url submission maps to the submitted url, or no preview when blank`() {
        val withUrl = mapSubmissionPreviewPlan(
            submission(SubmissionType.ONLINE_URL) { copy(url = "https://example.com") },
            baseUrl = null,
        )
        assertEquals(NgcSubmissionPreviewPlan.Url("https://example.com"), withUrl)

        val blank = mapSubmissionPreviewPlan(submission(SubmissionType.ONLINE_URL) { copy(url = "") }, baseUrl = null)
        assertEquals(NgcSubmissionPreviewPlan.NoPreview, blank)
    }

    @Test
    fun `upload previews the first file and reports the rest as additional`() {
        val plan = mapSubmissionPreviewPlan(
            submission(SubmissionType.ONLINE_UPLOAD) {
                copy(
                    attachments = arrayListOf(
                        attachment("image/png", "photo.png"),
                        attachment("application/pdf", "doc.pdf"),
                        attachment("application/zip", "archive.zip"),
                    )
                )
            },
            baseUrl = null,
        )
        assertEquals(NgcSubmissionPreviewPlan.Image("https://host/photo.png", "photo.png", additionalFileCount = 2), plan)
    }

    @Test
    fun `upload lists files when the first file is not previewable, even if a later one is`() {
        val plan = mapSubmissionPreviewPlan(
            submission(SubmissionType.ONLINE_UPLOAD) {
                copy(
                    attachments = arrayListOf(
                        attachment("application/zip", "archive.zip"),
                        attachment("image/png", "photo.png"),
                    )
                )
            },
            baseUrl = null,
        )
        assertEquals(NgcSubmissionPreviewPlan.NoFilePreview(listOf("archive.zip", "photo.png")), plan)
    }

    @Test
    fun `single previewable upload reports no additional files`() {
        val pdf = mapSubmissionPreviewPlan(
            submission(SubmissionType.ONLINE_UPLOAD) { copy(attachments = arrayListOf(attachment("application/pdf", "doc.pdf"))) },
            baseUrl = null,
        )
        assertEquals(NgcSubmissionPreviewPlan.Pdf("https://host/doc.pdf", additionalFileCount = 0), pdf)

        val text = mapSubmissionPreviewPlan(
            submission(SubmissionType.ONLINE_UPLOAD) { copy(attachments = arrayListOf(attachment("text/plain", "notes.txt"))) },
            baseUrl = null,
        )
        assertEquals(NgcSubmissionPreviewPlan.TextFile("https://host/notes.txt", "text/plain", additionalFileCount = 0), text)
    }

    @Test
    fun `upload with no renderable attachment lists the file names`() {
        val plan = mapSubmissionPreviewPlan(
            submission(SubmissionType.ONLINE_UPLOAD) {
                copy(
                    attachments = arrayListOf(
                        attachment("application/zip", "archive.zip"),
                        attachment("application/octet-stream", "data.bin"),
                    )
                )
            },
            baseUrl = null,
        )
        assertEquals(NgcSubmissionPreviewPlan.NoFilePreview(listOf("archive.zip", "data.bin")), plan)
    }

    @Test
    fun `media recording uses the media comment when present`() {
        val plan = mapSubmissionPreviewPlan(
            submission(SubmissionType.MEDIA_RECORDING) {
                copy(mediaComment = MediaComment(url = "https://host/clip.mp4", displayName = "clip", contentType = "video/mp4"))
            },
            baseUrl = null,
        )
        assertEquals(
            NgcSubmissionPreviewPlan.Media("https://host/clip.mp4", "", "video/mp4", "clip"),
            plan,
        )
    }

    @Test
    fun `media recording falls back to a media attachment when there is no media comment`() {
        val plan = mapSubmissionPreviewPlan(
            submission(SubmissionType.MEDIA_RECORDING) {
                copy(attachments = arrayListOf(attachment("video/mp4", "clip.mp4")))
            },
            baseUrl = null,
        )
        assertEquals(
            NgcSubmissionPreviewPlan.Media("https://host/clip.mp4", "", "video/mp4", "clip.mp4"),
            plan,
        )
    }
}
