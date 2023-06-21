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

package com.instructure.pandautils.features.offline.offlinecontent

import android.content.Context
import android.text.format.Formatter
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import com.google.android.material.checkbox.MaterialCheckBox
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.sync.OfflineSyncHelper
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.FileSyncSettingsEntity
import com.instructure.pandautils.room.offline.model.CourseSyncSettingsWithFiles
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.StorageUtils
import com.instructure.pandautils.utils.orDefault
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.*

@ExperimentalCoroutinesApi
class OfflineContentViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val offlineContentRepository: OfflineContentRepository = mockk(relaxed = true)
    private val storageUtils: StorageUtils = mockk(relaxed = true)
    private val offlineSyncHelper: OfflineSyncHelper = mockk(relaxed = true)

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: OfflineContentViewModel

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        mockkStatic(Formatter::class)
        every { Formatter.formatShortFileSize(context, any()) } returns ""
        every { savedStateHandle.get<Course>(Const.CANVAS_CONTEXT) } returns null

        coEvery { offlineContentRepository.findCourseSyncSettings(any()) } answers {
            val courseId = arg<Long>(0)
            CourseSyncSettingsWithFiles(
                CourseSyncSettingsEntity(
                    courseId = courseId,
                    fullContentSync = false,
                    assignments = false,
                    pages = false,
                    grades = false,
                    syllabus = false,
                    conferences = false,
                    fullFileSync = false
                ),
                emptyList()
            )

        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()

        unmockkAll()
    }

    @Test
    fun `Post error state when fetching fails`() {
        coEvery { offlineContentRepository.getCourses() } throws Exception()
        every { context.getString(R.string.offline_content_loading_error) } returns "Error"

        createViewModel()

        Assert.assertEquals(ViewState.Error("Error"), viewModel.state.value)
    }

    @Test
    fun `Post success state when fetching was successful`() {
        coEvery { offlineContentRepository.getCourses() } returns listOf(Course(id = 1))

        createViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
    }

    @Test
    fun `Post success state when fetching by specific course id was successful`() {
        val course = Course(id = 1)
        every { savedStateHandle.get<Course>(Const.CANVAS_CONTEXT) } returns course
        coEvery { offlineContentRepository.getCourse(any()) } returns course

        createViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
    }

    @Test
    fun `Storage info maps correctly`() {
        coEvery { offlineContentRepository.getCourses() } returns emptyList()
        every { storageUtils.getTotalSpace() } returns 100L
        every { storageUtils.getFreeSpace() } returns 80L
        every { storageUtils.getAppSize() } returns 10L
        every { context.getString(R.string.offline_content_storage_info, any(), any()) } returns "Used 20 GB of 100 GB"

        createViewModel()

        val expected = StorageInfo(10, 20, "Used 20 GB of 100 GB")

        Assert.assertEquals(expected, viewModel.data.value?.storageInfo)
    }

    @Test
    fun `Course list maps correctly`() {
        mockkCourseViewModels()

        createViewModel()

        Assert.assertEquals(2, viewModel.data.value?.courseItems?.size)
        Assert.assertEquals(arrayListOf(1L, 2L), viewModel.data.value?.courseItems?.map { it.courseId })
        val firstCourseTabs = viewModel.data.value?.courseItems?.first()?.data?.tabs
        Assert.assertEquals(arrayListOf("Pages", "Syllabus", "Assignments", "Grades", "Files"), firstCourseTabs?.map { it.data.title })
        Assert.assertTrue(firstCourseTabs?.first()?.data?.files.isNullOrEmpty())
        Assert.assertEquals(
            arrayListOf("File 1", "File 2"),
            firstCourseTabs?.last()?.data?.files?.map { it.data.title })
    }

    @Test
    fun `Selection toggles correctly`() {
        mockkCourseViewModels()

        createViewModel()

        Assert.assertEquals(0, viewModel.data.value?.selectedCount)

        viewModel.toggleSelection()

        Assert.assertEquals(12, viewModel.data.value?.selectedCount)
    }

    @Test
    fun `Course checked change selects and deselect all pages and files`() {
        mockkCourseViewModels()

        createViewModel()

        Assert.assertEquals(0, viewModel.data.value?.selectedCount)

        viewModel.data.value?.courseItems?.first()?.apply {
            onCheckedChanged.invoke(true, this)
        }

        Assert.assertEquals(6, viewModel.data.value?.selectedCount)
        Assert.assertTrue(viewModel.data.value?.courseItems?.first()?.data?.checkedState() == MaterialCheckBox.STATE_CHECKED)
        Assert.assertTrue(viewModel.data.value?.courseItems?.first()?.data?.tabs?.last()?.data?.synced.orDefault())
        Assert.assertTrue(viewModel.data.value?.courseItems?.first()?.data?.tabs?.last()?.data?.files?.first()?.data?.checked.orDefault())

        viewModel.data.value?.courseItems?.first()?.apply {
            onCheckedChanged.invoke(false, this)
        }

        Assert.assertEquals(0, viewModel.data.value?.selectedCount)
        Assert.assertFalse(viewModel.data.value?.courseItems?.first()?.data?.checkedState() == MaterialCheckBox.STATE_CHECKED)
        Assert.assertFalse(viewModel.data.value?.courseItems?.first()?.data?.tabs?.last()?.data?.synced.orDefault())
        Assert.assertFalse(viewModel.data.value?.courseItems?.first()?.data?.tabs?.last()?.data?.files?.first()?.data?.checked.orDefault())
    }

    @Test
    fun `Tab checked change affects courses and files correctly`() {
        mockkCourseViewModels()

        createViewModel()

        Assert.assertEquals(0, viewModel.data.value?.selectedCount)

        viewModel.data.value?.courseItems?.first()?.apply {
            onCheckedChanged.invoke(true, this)
        }

        viewModel.data.value?.courseItems?.first()?.data?.tabs?.last()?.apply {
            onCheckedChanged.invoke(false, this)
        }

        Assert.assertEquals(4, viewModel.data.value?.selectedCount)
        Assert.assertFalse(viewModel.data.value?.courseItems?.first()?.data?.checkedState() == MaterialCheckBox.STATE_CHECKED)
        Assert.assertFalse(viewModel.data.value?.courseItems?.first()?.data?.tabs?.last()?.data?.files?.first()?.data?.checked.orDefault())
    }

    @Test
    fun `File checked change affects courses and tabs correctly`() {
        mockkCourseViewModels()

        createViewModel()

        Assert.assertEquals(0, viewModel.data.value?.selectedCount)

        viewModel.data.value?.courseItems?.first()?.apply {
            onCheckedChanged.invoke(true, this)
        }

        viewModel.data.value?.courseItems?.first()?.data?.tabs?.last()?.data?.files?.first()?.apply {
            onCheckedChanged.invoke(false, this)
        }

        Assert.assertEquals(5, viewModel.data.value?.selectedCount)
        Assert.assertFalse(viewModel.data.value?.courseItems?.first()?.data?.checkedState() == MaterialCheckBox.STATE_CHECKED)
        Assert.assertFalse(viewModel.data.value?.courseItems?.first()?.data?.tabs?.last()?.data?.synced.orDefault())
    }

    @Test
    fun `Selecting not all files sets file checkbox to indeterminate`() {
        mockkCourseViewModels()

        createViewModel()

        viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.FILES_ID }?.data?.files?.first()
            ?.apply {
                onCheckedChanged.invoke(true, this)
            }

        assert(viewModel.data.value?.courseItems?.first()?.data?.checkedState() == MaterialCheckBox.STATE_INDETERMINATE)
        assert(viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.FILES_ID }?.data?.checkedState() == MaterialCheckBox.STATE_INDETERMINATE)
    }

    @Test
    fun `Refresh updates list`() {
        coEvery { offlineContentRepository.getCourses() } returns emptyList()

        createViewModel()

        Assert.assertEquals(0, viewModel.data.value?.courseItems?.size)

        coEvery { offlineContentRepository.getCourses() } returns listOf(Course(id = 1))

        viewModel.onRefresh()

        Assert.assertEquals(1, viewModel.data.value?.courseItems?.size)
    }

    @Test
    fun `Move back on sync click`() {
        createViewModel()

        viewModel.events.observe(lifecycleOwner) {}

        viewModel.onSyncClicked()

        assert(viewModel.events.value?.getContentIfNotHandled() is OfflineContentAction.Back)
    }

    @Test
    fun `Start sync`() {
        createViewModel()

        viewModel.events.observe(lifecycleOwner) {}

        viewModel.onSyncClicked()

        coVerify {
            offlineSyncHelper.syncCourses(any())
        }
    }

    @Test
    fun `Save state on sync`() {
        mockkCourseViewModels()

        createViewModel()

        val expected = CourseSyncSettingsEntity(
            courseId = 1L,
            fullContentSync = true,
            assignments = true,
            pages = true,
            grades = true,
            syllabus = true,
            conferences = true,
            fullFileSync = true
        )
        val expectedFiles = listOf(FileSyncSettingsEntity(1L, 1L, null), FileSyncSettingsEntity(2L, 1L, null))

        viewModel.data.value?.courseItems?.first()?.apply {
            onCheckedChanged.invoke(true, this)
        }

        viewModel.onSyncClicked()

        coVerify(exactly = 1) { offlineContentRepository.updateCourseSyncSettings(1L, expected, expectedFiles) }
    }

    @Test
    fun `Syllabus check changed`() {
        mockkCourseViewModels()

        createViewModel()

        viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.SYLLABUS_ID }?.apply {
            onCheckedChanged.invoke(true, this)
        }

        assert(viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.SYLLABUS_ID }?.data?.checkedState() == MaterialCheckBox.STATE_CHECKED)
        assert(viewModel.data.value?.courseItems?.first()?.data?.checkedState() == MaterialCheckBox.STATE_INDETERMINATE)
    }

    @Test
    fun `Pages check changed`() {
        mockkCourseViewModels()

        createViewModel()

        viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.PAGES_ID }?.apply {
            onCheckedChanged.invoke(true, this)
        }

        assert(viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.PAGES_ID }?.data?.checkedState() == MaterialCheckBox.STATE_CHECKED)
        assert(viewModel.data.value?.courseItems?.first()?.data?.checkedState() == MaterialCheckBox.STATE_INDETERMINATE)
    }

    @Test
    fun `Assignments check changed`() {
        mockkCourseViewModels()

        createViewModel()

        viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.ASSIGNMENTS_ID }?.apply {
            onCheckedChanged.invoke(true, this)
        }

        assert(viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.ASSIGNMENTS_ID }?.data?.checkedState() == MaterialCheckBox.STATE_CHECKED)
        assert(viewModel.data.value?.courseItems?.first()?.data?.checkedState() == MaterialCheckBox.STATE_INDETERMINATE)
    }

    @Test
    fun `Grades check changed`() {
        mockkCourseViewModels()

        createViewModel()

        viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.GRADES_ID }?.apply {
            onCheckedChanged.invoke(true, this)
        }

        assert(viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.GRADES_ID }?.data?.checkedState() == MaterialCheckBox.STATE_CHECKED)
        assert(viewModel.data.value?.courseItems?.first()?.data?.checkedState() == MaterialCheckBox.STATE_INDETERMINATE)
    }

    private fun createViewModel() {
        viewModel = OfflineContentViewModel(
            savedStateHandle,
            context,
            offlineContentRepository,
            storageUtils,
            offlineSyncHelper
        )
        viewModel.data.observe(lifecycleOwner) {}
    }

    private fun mockkCourseViewModels() {
        val tabs = listOf(
            Tab(tabId = "pages", label = "Pages"),
            Tab(tabId = "syllabus", label = "Syllabus"),
            Tab(tabId = "assignments", label = "Assignments"),
            Tab(tabId = "grades", label = "Grades"),
            Tab(tabId = "files", label = "Files"),
        )
        val courses = listOf(Course(id = 1, tabs = tabs), Course(id = 2, tabs = tabs))
        val files = listOf(FileFolder(id = 1, displayName = "File 1"), FileFolder(id = 2, displayName = "File 2"))

        coEvery { offlineContentRepository.getCourses() } returns courses
        coEvery { offlineContentRepository.getCourseFiles(any()) } returns files
    }
}