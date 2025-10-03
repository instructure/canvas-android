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

package com.instructure.teacher.features.files.details

import android.content.res.Resources
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.License
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.color
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.LifecycleTestOwner
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FileDetailsViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private lateinit var viewModel: FileDetailsViewModel

    private val lifecycleTestOwner = LifecycleTestOwner()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val repository: FileDetailsRepository = mockk(relaxed = true)
    private val mimeTypeMap: MimeTypeMap = mockk(relaxed = true)

    private val course = Course(1L)

    @Before
    fun setup() {

        mockkStatic(URLUtil::class)
        every { URLUtil.isNetworkUrl(any()) } returns true

        ContextKeeper.appContext = mockk(relaxed = true)

        every { savedStateHandle.get<Course>(Const.CANVAS_CONTEXT) } returns course
        every { savedStateHandle.get<String>(Const.FILE_URL) } returns "https://file.url"
    }

    fun teardown() {
        unmockkAll()
    }

    private fun createViewModel() {
        viewModel = FileDetailsViewModel(savedStateHandle, resources, repository, mimeTypeMap)
    }

    private fun getEditableFile(fileFolder: FileFolder) = EditableFile(
        file = fileFolder,
        usageRights = false,
        licenses = emptyList(),
        courseColor = course.color,
        canvasContext = course,
        iconRes = R.drawable.ic_document
    )

    @Test
    fun `Load error`() {
        val expected = "An unexpected error occurred."

        every { resources.getString(R.string.errorOccurred) } returns expected

        coEvery { repository.getFileFolderFromURL(any()) } throws IllegalStateException()

        createViewModel()

        Assert.assertEquals(ViewState.Error(expected), viewModel.state.value)
        Assert.assertEquals(expected, (viewModel.state.value as? ViewState.Error)?.errorMessage)
    }

    @Test
    fun `Load success with licences`() {
        val fileFolder = FileFolder(url = "url", displayName = "displayName", contentType = "type", thumbnailUrl = "thumbnailUrl")
        val licence = License(id = "id", name = "licence", url = "https://licence.url")
        val expected = FileViewData.Other(
            fileFolder.url!!,
            fileFolder.displayName!!,
            fileFolder.contentType!!,
            fileFolder.thumbnailUrl!!,
            getEditableFile(fileFolder).copy(usageRights = true, licenses = listOf(licence))
        )

        coEvery { repository.getFileFolderFromURL(any()) } returns fileFolder

        coEvery { repository.getCourseFeatures(1L) } returns listOf("usage_rights_required")

        coEvery { repository.getCourseFileLicences(1L) } returns listOf(licence)

        createViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(expected, viewModel.data.value?.fileData)
    }

    @Test
    fun `Load success - pdf`() {
        val fileFolder = FileFolder(url = "url", contentType = "application/pdf")
        val expected = FileViewData.Pdf(
            fileFolder.url!!,
            getEditableFile(fileFolder)
        )

        coEvery { repository.getFileFolderFromURL(any()) } returns fileFolder

        coEvery { repository.getCourseFeatures(1L) } returns emptyList()

        createViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(expected, viewModel.data.value?.fileData)
    }

    @Test
    fun `Load success - video`() {
        val fileFolder = FileFolder(url = "url", contentType = "video", thumbnailUrl = "thumbnailUrl", displayName = "displayName")
        val expected = FileViewData.Media(
            fileFolder.url!!,
            fileFolder.thumbnailUrl!!,
            fileFolder.contentType!!,
            fileFolder.displayName!!,
            getEditableFile(fileFolder)
        )

        coEvery { repository.getFileFolderFromURL(any()) } returns fileFolder

        coEvery { repository.getCourseFeatures(1L) } returns emptyList()

        createViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(expected, viewModel.data.value?.fileData)
    }

    @Test
    fun `Load success - audio`() {
        val fileFolder = FileFolder(url = "url", contentType = "audio", thumbnailUrl = "thumbnailUrl", displayName = "displayName")
        val expected = FileViewData.Media(
            fileFolder.url!!,
            fileFolder.thumbnailUrl!!,
            fileFolder.contentType!!,
            fileFolder.displayName!!,
            getEditableFile(fileFolder)
        )

        coEvery { repository.getFileFolderFromURL(any()) } returns fileFolder

        coEvery { repository.getCourseFeatures(1L) } returns emptyList()

        createViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(expected, viewModel.data.value?.fileData)
    }

    @Test
    fun `Load success - image`() {
        val fileFolder = FileFolder(url = "url", contentType = "image", displayName = "displayName")
        val expected = FileViewData.Image(
            fileFolder.displayName!!,
            fileFolder.url!!,
            fileFolder.contentType!!,
            getEditableFile(fileFolder)
        )

        coEvery { repository.getFileFolderFromURL(any()) } returns fileFolder

        coEvery { repository.getCourseFeatures(1L) } returns emptyList()

        createViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(expected, viewModel.data.value?.fileData)
    }

    @Test
    fun `Load success - content type html`() {
        val fileFolder = FileFolder(url = "url", contentType = "text/html", displayName = "displayName")
        val expected = FileViewData.Html(
            fileFolder.url!!,
            fileFolder.displayName!!,
            getEditableFile(fileFolder)
        )

        coEvery { repository.getFileFolderFromURL(any()) } returns fileFolder

        coEvery { repository.getCourseFeatures(1L) } returns emptyList()

        createViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(expected, viewModel.data.value?.fileData)
    }

    @Test
    fun `Load success - extension htm`() {
        val fileFolder = FileFolder(url = "url", displayName = "displayName", name = "name.htm")
        val expected = FileViewData.Html(
            fileFolder.url!!,
            fileFolder.displayName!!,
            getEditableFile(fileFolder)
        )

        coEvery { repository.getFileFolderFromURL(any()) } returns fileFolder

        coEvery { repository.getCourseFeatures(1L) } returns emptyList()

        createViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(expected, viewModel.data.value?.fileData)
    }

    @Test
    fun `Load success - extension html`() {
        val fileFolder = FileFolder(url = "url", displayName = "displayName", name = "name.html")
        val expected = FileViewData.Html(
            fileFolder.url!!,
            fileFolder.displayName!!,
            getEditableFile(fileFolder)
        )

        coEvery { repository.getFileFolderFromURL(any()) } returns fileFolder

        coEvery { repository.getCourseFeatures(1L) } returns emptyList()

        createViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(expected, viewModel.data.value?.fileData)
    }
}
