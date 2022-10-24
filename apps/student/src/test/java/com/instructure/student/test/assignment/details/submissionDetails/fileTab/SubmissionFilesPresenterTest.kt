/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 *
 */
package com.instructure.student.test.assignment.details.submissionDetails.fileTab

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.backgroundColor
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFileData
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesModel
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesPresenter
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files.SubmissionFilesViewState
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubmissionFilesPresenterTest : Assert() {

    private val testCourse = Course(id = 123L, name = "Test Course")
    private lateinit var context: Context
    private lateinit var baseModel: SubmissionFilesModel
    private lateinit var baseData: SubmissionFileData
    private lateinit var baseAttachment: Attachment

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        baseModel = SubmissionFilesModel(
            canvasContext = testCourse,
            files = emptyList(),
            selectedFileId = 0
        )
        baseAttachment = Attachment(
            id = 123L,
            displayName = "File 123"
        )
        baseData = SubmissionFileData(
            id = 123L,
            name = "File 123",
            icon = 0,
            thumbnailUrl = null,
            isSelected = false,
            iconColor = baseModel.canvasContext.backgroundColor,
            selectionColor = testCourse.backgroundColor
        )
    }

    @Test
    fun `Returns empty state when there are no files`() {
        val expectedState = SubmissionFilesViewState.Empty
        val model = baseModel.copy()
        val actualState = SubmissionFilesPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct data for PDF files`() {
        val attachment = baseAttachment.copy(contentType = "application/pdf")
        val model = baseModel.copy(files = listOf(attachment))
        val expectedState = SubmissionFilesViewState.FileList(
            listOf(baseData.copy(icon = R.drawable.ic_pdf))
        )
        val actualState = SubmissionFilesPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct data for PowerPoint files`() {
        val attachment = baseAttachment.copy(
            contentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        )
        val model = baseModel.copy(files = listOf(attachment))
        val expectedState = SubmissionFilesViewState.FileList(
            listOf(baseData.copy(icon = R.drawable.ic_ppt))
        )
        val actualState = SubmissionFilesPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct data for spreadsheet files`() {
        val attachment = baseAttachment.copy(
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
        val model = baseModel.copy(files = listOf(attachment))
        val expectedState = SubmissionFilesViewState.FileList(
            listOf(baseData.copy(icon = R.drawable.ic_spreadsheet))
        )
        val actualState = SubmissionFilesPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct data for Word files`() {
        val attachment = baseAttachment.copy(
            contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
        val model = baseModel.copy(files = listOf(attachment))
        val expectedState = SubmissionFilesViewState.FileList(
            listOf(baseData.copy(icon = R.drawable.ic_word_doc))
        )
        val actualState = SubmissionFilesPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct data for Zip files`() {
        val attachment = baseAttachment.copy(
            contentType = "application/zip"
        )
        val model = baseModel.copy(files = listOf(attachment))
        val expectedState = SubmissionFilesViewState.FileList(
            listOf(baseData.copy(icon = R.drawable.ic_zip))
        )
        val actualState = SubmissionFilesPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct data for jpg files`() {
        val attachment = baseAttachment.copy(
            contentType = "image/jpeg",
            thumbnailUrl = "fake"
        )
        val model = baseModel.copy(files = listOf(attachment))
        val expectedState = SubmissionFilesViewState.FileList(
            listOf(baseData.copy(icon = R.drawable.ic_image, thumbnailUrl = "fake"))
        )
        val actualState = SubmissionFilesPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct data for png files`() {
        val attachment = baseAttachment.copy(
            contentType = "image/png",
            thumbnailUrl = "fake"
        )
        val model = baseModel.copy(files = listOf(attachment))
        val expectedState = SubmissionFilesViewState.FileList(
            listOf(baseData.copy(icon = R.drawable.ic_image, thumbnailUrl = "fake"))
        )
        val actualState = SubmissionFilesPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct data for jpg files without thumbnail`() {
        val attachment = baseAttachment.copy(contentType = "image/png")
        val model = baseModel.copy(files = listOf(attachment))
        val expectedState = SubmissionFilesViewState.FileList(
            listOf(baseData.copy(icon = R.drawable.ic_image))
        )
        val actualState = SubmissionFilesPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct data for unknown file type`() {
        val attachment = baseAttachment.copy(contentType = "abc123")
        val model = baseModel.copy(files = listOf(attachment))
        val expectedState = SubmissionFilesViewState.FileList(
            listOf(baseData.copy(icon = R.drawable.ic_document))
        )
        val actualState = SubmissionFilesPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns correct data for selected file`() {
        val attachment = baseAttachment.copy(contentType = "application/pdf")
        val model = baseModel.copy(files = listOf(attachment), selectedFileId = attachment.id)
        val expectedState = SubmissionFilesViewState.FileList(
            listOf(baseData.copy(icon = R.drawable.ic_pdf, isSelected = true))
        )
        val actualState = SubmissionFilesPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }
}
