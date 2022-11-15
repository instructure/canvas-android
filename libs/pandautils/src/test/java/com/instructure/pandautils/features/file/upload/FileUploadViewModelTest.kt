/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.file.upload

import android.content.res.Resources
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.work.WorkManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.R
import com.instructure.pandautils.room.daos.FileUploadInputDao
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FileUploadViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = TestCoroutineDispatcher()

    private val resources: Resources = mockk(relaxed = true)
    private val fileUploadUtilsHelper: FileUploadUtilsHelper = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private val fileUploadInputDao: FileUploadInputDao = mockk(relaxed = true)

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        every { fileUploadUtilsHelper.getFileMimeType(any()) } returns "file"
        every { fileUploadUtilsHelper.getFileNameWithDefault(any()) } returns "file"

        setupStrings()
    }

    @Test
    fun `Take Photo action`() {
        val viewModel = createViewModel()
        viewModel.events.observe(lifecycleOwner) {}

        viewModel.onCameraClicked()

        assertEquals(FileUploadAction.TakePhoto, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Pick Photo action`() {
        val viewModel = createViewModel()
        viewModel.events.observe(lifecycleOwner) {}

        viewModel.onGalleryClicked()

        assertEquals(FileUploadAction.PickMultipleImage, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Pick File action`() {
        val viewModel = createViewModel()
        viewModel.events.observe(lifecycleOwner) {}

        viewModel.onFilesClicked()

        assertEquals(FileUploadAction.PickMultipleFile, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Allowed extensions set up correctly`() {
        val viewModel = createViewModel()
        val course = createCourse(1L, "Course 1")
        val assignment = createAssignment(1L, "Assignment 1", 1L, listOf("pdf", "mp4", "docx"))
        viewModel.setData(assignment, arrayListOf(), FileUploadType.ASSIGNMENT, course, -1L, -1L, -1, -1L, -1L)

        viewModel.data.observe(lifecycleOwner) {}

        assertEquals("pdf,mp4,docx", viewModel.data.value?.allowedExtensions)
    }

    @Test
    fun `Add file with allowed extension`() {
        val uri: Uri = mockk(relaxed = true)
        val viewModel = createViewModel()
        val course = createCourse(1L, "Course 1")
        val assignment = createAssignment(1L, "Assignment 1", 1L, listOf("pdf"))
        viewModel.setData(assignment, arrayListOf(), FileUploadType.ASSIGNMENT, course, -1L, -1L, -1, -1L, -1L)

        every { fileUploadUtilsHelper.getFileSubmitObjectFromInputStream(any(), any(), any()) } returns createSubmitObject("test.pdf")

        viewModel.data.observe(lifecycleOwner) {}

        viewModel.addFile(uri)

        assertEquals(1, viewModel.data.value?.files?.size)
        assertEquals("test.pdf", viewModel.data.value?.files?.get(0)?.data?.fileName)
    }

    @Test
    fun `Add file with not allowed extension`() {
        val uri: Uri = mockk(relaxed = true)
        val viewModel = createViewModel()
        val course = createCourse(1L, "Course 1")
        val assignment = createAssignment(1L, "Assignment 1", 1L, listOf("pdf"))
        viewModel.setData(assignment, arrayListOf(), FileUploadType.ASSIGNMENT, course, -1L, -1L, -1, -1L, -1L)

        every { fileUploadUtilsHelper.getFileSubmitObjectFromInputStream(any(), any(), any()) } returns createSubmitObject("test.doc")

        viewModel.data.observe(lifecycleOwner) {}
        viewModel.events.observe(lifecycleOwner) {}

        viewModel.addFile(uri)

        assertEquals(0, viewModel.data.value?.files?.size)
        assertEquals(FileUploadAction.ShowToast("The selected file type is not allowed."), viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Cannot submit if no file has be selected`() {
        val viewModel = createViewModel()
        val course = createCourse(1L, "Course 1")
        val assignment = createAssignment(1L, "Assignment 1", 1L)
        viewModel.setData(assignment, arrayListOf(), FileUploadType.ASSIGNMENT, course, -1L, -1L, -1, -1L, -1L)

        viewModel.uploadFiles()

        assertEquals(FileUploadAction.ShowToast("You haven't selected any files."), viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Cannot submit if file submission is not allowed`() {
        val uri: Uri = mockk(relaxed = true)
        val viewModel = createViewModel()
        val course = createCourse(1L, "Course 1")
        val assignment = createAssignment(1L, "Assignment 1", 1L, submissionTypes = listOf("online_text_entry"))
        viewModel.setData(assignment, arrayListOf(), FileUploadType.ASSIGNMENT, course, -1L, -1L, -1, -1L, -1L)

        viewModel.addFile(uri)
        viewModel.uploadFiles()

        assertEquals(FileUploadAction.ShowToast("You can't upload files to the selected assignment."), viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Submit files with allowed extensions`() {
        val uri: Uri = mockk(relaxed = true)
        val viewModel = createViewModel()
        val course = createCourse(1L, "Course 1")
        val assignment = createAssignment(1L, "Assignment 1", 1L, listOf("pdf"))
        val submitObject = createSubmitObject("test.pdf")

        every { fileUploadUtilsHelper.getFileSubmitObjectFromInputStream(any(), any(), any()) } returns submitObject

        viewModel.setData(assignment, arrayListOf(), FileUploadType.ASSIGNMENT, course, -1L, -1L, -1, -1L, -1L)

        viewModel.data.observe(lifecycleOwner) {}
        viewModel.events.observe(lifecycleOwner) {}

        viewModel.addFile(uri)
        viewModel.uploadFiles()

        assert(viewModel.events.value?.getContentIfNotHandled() is FileUploadAction.UploadStarted)
    }

    @Test
    fun `Only single image can be picked as discussion attachment`() {
        val viewModel = createViewModel()
        val course = createCourse(1L, "Course 1")
        viewModel.setData(null, arrayListOf(), FileUploadType.DISCUSSION, course, -1L, -1L, -1, -1L, -1L)

        viewModel.onGalleryClicked()

        assertEquals(FileUploadAction.PickImage, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Only single file can be picked as discussion attachment`() {
        val viewModel = createViewModel()
        val course = createCourse(1L, "Course 1")
        viewModel.setData(null, arrayListOf(), FileUploadType.DISCUSSION, course, -1L, -1L, -1, -1L, -1L)

        viewModel.onFilesClicked()

        assertEquals(FileUploadAction.PickFile, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Only single image can be picked as quiz attachment`() {
        val viewModel = createViewModel()
        val course = createCourse(1L, "Course 1")
        viewModel.setData(null, arrayListOf(), FileUploadType.QUIZ, course, -1L, -1L, -1, -1L, -1L)

        viewModel.onGalleryClicked()

        assertEquals(FileUploadAction.PickImage, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Only single file can be picked as quiz attachment`() {
        val viewModel = createViewModel()
        val course = createCourse(1L, "Course 1")
        viewModel.setData(null, arrayListOf(), FileUploadType.QUIZ, course, -1L, -1L, -1, -1L, -1L)

        viewModel.onFilesClicked()

        assertEquals(FileUploadAction.PickFile, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Error when trying to add more files to quiz attachments`() {
        val uri: Uri = mockk(relaxed = true)
        val viewModel = createViewModel()
        val course = createCourse(1L, "Course 1")
        val assignment = createAssignment(1L, "Assignment 1", 1L, listOf("pdf"))
        val submitObject = createSubmitObject("test.pdf")

        every { fileUploadUtilsHelper.getFileSubmitObjectFromInputStream(any(), any(), any()) } returns submitObject

        viewModel.setData(assignment, arrayListOf(uri), FileUploadType.QUIZ, course, -1L, -1L, -1, -1L, -1L)

        viewModel.onFilesClicked()
        assertEquals(FileUploadAction.ShowToast("This submission only accepts one file upload"), viewModel.events.value?.getContentIfNotHandled())

        viewModel.onCameraClicked()
        assertEquals(FileUploadAction.ShowToast("This submission only accepts one file upload"), viewModel.events.value?.getContentIfNotHandled())

        viewModel.onGalleryClicked()
        assertEquals(FileUploadAction.ShowToast("This submission only accepts one file upload"), viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Error when trying to add more files to discussion attachments`() {
        val uri: Uri = mockk(relaxed = true)
        val viewModel = createViewModel()
        val course = createCourse(1L, "Course 1")
        val assignment = createAssignment(1L, "Assignment 1", 1L, listOf("pdf"))
        val submitObject = createSubmitObject("test.pdf")

        every { fileUploadUtilsHelper.getFileSubmitObjectFromInputStream(any(), any(), any()) } returns submitObject

        viewModel.setData(assignment, arrayListOf(uri), FileUploadType.DISCUSSION, course, -1L, -1L, -1, -1L, -1L)

        viewModel.onFilesClicked()
        assertEquals(FileUploadAction.ShowToast("This submission only accepts one file upload"), viewModel.events.value?.getContentIfNotHandled())

        viewModel.onCameraClicked()
        assertEquals(FileUploadAction.ShowToast("This submission only accepts one file upload"), viewModel.events.value?.getContentIfNotHandled())

        viewModel.onGalleryClicked()
        assertEquals(FileUploadAction.ShowToast("This submission only accepts one file upload"), viewModel.events.value?.getContentIfNotHandled())
    }

    private fun setupStrings() {
        every { resources.getString(R.string.extensionNotAllowed) } returns "The selected file type is not allowed."
        every { resources.getString(R.string.noFilesUploaded) } returns "You haven't selected any files."
        every { resources.getString(R.string.fileUploadNotSupported) } returns "You can't upload files to the selected assignment."
        every { resources.getString(R.string.oneFileOnly) } returns "This submission only accepts one file upload"
    }

    private fun createCourse(id: Long, name: String): Course {
        return Course(id, name)
    }

    private fun createAssignment(id: Long, name: String, courseId: Long, allowedExtensions: List<String> = emptyList(), submissionTypes: List<String> = listOf("online_upload")): Assignment {
        return Assignment(id = id, name = name, courseId = courseId, allowedExtensions = allowedExtensions, submissionTypesRaw = submissionTypes)
    }

    private fun createSubmitObject(fileName: String, fileSize: Long = 1024L): FileSubmitObject {
        return FileSubmitObject(fileName, fileSize, "file", "/$fileName")
    }

    private fun createViewModel(): FileUploadDialogViewModel {
        return FileUploadDialogViewModel(fileUploadUtilsHelper, resources, workManager, fileUploadInputDao)
    }
}