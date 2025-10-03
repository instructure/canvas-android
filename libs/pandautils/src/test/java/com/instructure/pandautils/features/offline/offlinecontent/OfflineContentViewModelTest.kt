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
import androidx.lifecycle.SavedStateHandle
import com.google.android.material.checkbox.MaterialCheckBox
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.OfflineAnalyticsManager
import com.instructure.pandautils.features.offline.offlinecontent.itemviewmodels.EmptyCourseContentViewModel
import com.instructure.pandautils.features.offline.sync.OfflineSyncHelper
import com.instructure.pandautils.features.offline.sync.settings.SyncFrequency
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.FileSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.SyncSettingsEntity
import com.instructure.pandautils.room.offline.model.CourseSyncSettingsWithFiles
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.StorageUtils
import com.instructure.pandautils.utils.orDefault
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.LifecycleTestOwner
import org.junit.Test

@ExperimentalCoroutinesApi
class OfflineContentViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val offlineContentRepository: OfflineContentRepository = mockk(relaxed = true)
    private val storageUtils: StorageUtils = mockk(relaxed = true)
    private val offlineSyncHelper: OfflineSyncHelper = mockk(relaxed = true)
    private val offlineAnalyticsManager: OfflineAnalyticsManager = mockk(relaxed = true)

    private val lifecycleTestOwner = LifecycleTestOwner()

    private lateinit var viewModel: OfflineContentViewModel

    @Before
    fun setUp() {

        mockkStatic(Formatter::class)
        every { Formatter.formatShortFileSize(context, any()) } answers { "${arg<Long>(1) / 1000} kb" }
        every { savedStateHandle.get<Course>(Const.CANVAS_CONTEXT) } returns null

        coEvery { offlineContentRepository.findCourseSyncSettings(any()) } answers {
            val course = arg<Course>(0)
            CourseSyncSettingsWithFiles(
                CourseSyncSettingsEntity(
                    courseId = course.id,
                    courseName = "Course",
                    fullContentSync = false
                ),
                emptyList()
            )

        }
    }

    @Test
    fun `Post empty state when course list is empty`() {
        createViewModel()

        Assert.assertEquals(
            ViewState.Empty(
                R.string.offline_content_empty_title,
                R.string.offline_content_empty_message,
                R.drawable.ic_panda_space
            ),
            viewModel.state.value
        )
    }

    @Test
    fun `Add empty course item when course tab list is empty`() {
        coEvery { offlineContentRepository.getCourses() } returns listOf(Course(1L))

        createViewModel()

        Assert.assertTrue(viewModel.data.value?.courseItems?.first()?.items?.first() is EmptyCourseContentViewModel)
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
        mockStorageInfoData()

        val expected = StorageInfo(
            10,
            20,
            "Used 2000 kb of 10000 kb",
            "Storage Info: 2000 kb of 10000 kb used, Other Apps 10%, Canvas Student 10%, Remaining 80%"
        )

        Assert.assertEquals(expected, viewModel.data.value?.storageInfo)
    }

    @Test
    fun `Storage info calculates on toggle selection`() {
        mockStorageInfoData()

        val expected = StorageInfo(
            10,
            33,
            "Used 3300 kb of 10000 kb",
            "Storage Info: 3300 kb of 10000 kb used, Other Apps 10%, Canvas Student 23%, Remaining 67%"
        )

        viewModel.toggleSelection()

        Assert.assertEquals(expected, viewModel.data.value?.storageInfo)
    }

    @Test
    fun `Storage info calculates on course selection`() {
        mockStorageInfoData()

        val expected = StorageInfo(
            10,
            29,
            "Used 2900 kb of 10000 kb",
            "Storage Info: 2900 kb of 10000 kb used, Other Apps 10%, Canvas Student 19%, Remaining 71%"
        )

        viewModel.data.value?.courseItems?.first()?.apply {
            onCheckedChanged.invoke(true, this)
        }

        Assert.assertEquals(expected, viewModel.data.value?.storageInfo)
    }

    @Test
    fun `Storage info calculates on tab selection`() {
        mockStorageInfoData()

        val expected = StorageInfo(
            10,
            21,
            "Used 2100 kb of 10000 kb",
            "Storage Info: 2100 kb of 10000 kb used, Other Apps 10%, Canvas Student 11%, Remaining 79%"
        )

        viewModel.data.value?.courseItems?.first()?.data?.tabs?.first()?.apply {
            onCheckedChanged.invoke(true, this)
        }

        Assert.assertEquals(expected, viewModel.data.value?.storageInfo)
    }

    @Test
    fun `Storage info calculates on file selection`() {
        mockStorageInfoData()

        val expected = StorageInfo(
            10,
            22,
            "Used 2200 kb of 10000 kb",
            "Storage Info: 2200 kb of 10000 kb used, Other Apps 10%, Canvas Student 12%, Remaining 78%"
        )

        viewModel.data.value?.courseItems?.first()?.data?.tabs?.last()?.data?.files?.first()?.apply {
            onCheckedChanged.invoke(true, this)
        }

        Assert.assertEquals(expected, viewModel.data.value?.storageInfo)
    }

    @Test
    fun `Course list maps correctly`() {
        mockkCourseViewModels()

        createViewModel()

        Assert.assertEquals(2, viewModel.data.value?.courseItems?.size)
        Assert.assertEquals(arrayListOf(1L, 2L), viewModel.data.value?.courseItems?.map { it.courseId })
        val firstCourseTabs = viewModel.data.value?.courseItems?.first()?.data?.tabs
        Assert.assertEquals(
            arrayListOf("Pages", "Syllabus", "Assignments", "Grades", "Files"),
            firstCourseTabs?.map { it.data.title })
        Assert.assertTrue(firstCourseTabs?.first()?.data?.files.isNullOrEmpty())
        Assert.assertEquals(
            arrayListOf("File 1", "File 2"),
            firstCourseTabs?.last()?.data?.files?.map { it.data.title })
        Assert.assertEquals("~900 kb", viewModel.data.value?.courseItems?.first()?.data?.size)
    }

    @Test
    fun `Selection toggles correctly`() {
        mockkCourseViewModels()

        createViewModel()

        Assert.assertEquals(0, viewModel.data.value?.selectedCount)

        viewModel.toggleSelection()

        Assert.assertEquals(10, viewModel.data.value?.selectedCount)
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

        Assert.assertTrue(viewModel.data.value?.courseItems?.first()?.data?.checkedState() == MaterialCheckBox.STATE_INDETERMINATE)
        Assert.assertTrue(viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.FILES_ID }?.data?.checkedState() == MaterialCheckBox.STATE_INDETERMINATE)
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
    fun `Should show discard dialog`() {
        mockkCourseViewModels()
        createViewModel()

        Assert.assertEquals(false, viewModel.shouldShowDiscardDialog())

        viewModel.data.value?.courseItems?.first()?.apply {
            onCheckedChanged.invoke(true, this)
        }

        Assert.assertEquals(true, viewModel.shouldShowDiscardDialog())
    }

    @Test
    fun `Show dialog on sync click`() {
        every { context.getString(R.string.offline_content_sync_dialog_title) } returns "Title"
        every { context.getString(R.string.offline_content_sync_dialog_message, any()) } answers { "Message ${secondArg<Array<Any>>().first()}" }
        every { context.getString(R.string.offline_content_sync_dialog_positive) } returns "Sync"

        mockkCourseViewModels()
        createViewModel()

        viewModel.data.value?.courseItems?.first()?.apply {
            onCheckedChanged.invoke(true, this)
        }

        viewModel.onSyncClicked()

        Assert.assertTrue(viewModel.events.value?.getContentIfNotHandled() is OfflineContentAction.Dialog)
        val dialog = viewModel.events.value?.peekContent() as OfflineContentAction.Dialog
        Assert.assertEquals("Title", dialog.title)
        Assert.assertEquals("Message 900 kb", dialog.message)
        Assert.assertEquals("Sync", dialog.positive)
    }

    @Test
    fun `Show wifi only dialog on sync click`() {
        every { context.getString(R.string.offline_content_sync_dialog_title) } returns "Title"
        every {
            context.getString(
                R.string.offline_content_sync_dialog_message_wifi_only,
                any()
            )
        } answers { "Wifi only message ${secondArg<Array<Any>>().first()}" }
        every { context.getString(R.string.offline_content_sync_dialog_positive) } returns "Sync"
        coEvery { offlineContentRepository.getSyncSettings() } returns SyncSettingsEntity(1L, true, SyncFrequency.DAILY, true)

        mockkCourseViewModels()
        createViewModel()

        viewModel.data.value?.courseItems?.first()?.apply {
            onCheckedChanged.invoke(true, this)
        }

        viewModel.onSyncClicked()

        Assert.assertTrue(viewModel.events.value?.getContentIfNotHandled() is OfflineContentAction.Dialog)
        val dialog = viewModel.events.value?.peekContent() as OfflineContentAction.Dialog
        Assert.assertEquals("Title", dialog.title)
        Assert.assertEquals("Wifi only message 900 kb", dialog.message)
        Assert.assertEquals("Sync", dialog.positive)
    }

    @Test
    fun `Announce sync started and move back on sync`() {
        createViewModel()

        val events = mutableListOf<Event<OfflineContentAction>>()
        viewModel.events.observeForever {
            events.add(it)
        }

        viewModel.onSyncClicked()

        (viewModel.events.value?.getContentIfNotHandled() as OfflineContentAction.Dialog).positiveCallback.invoke()

        Assert.assertEquals(
            listOf(OfflineContentAction.AnnounceSyncStarted, OfflineContentAction.Back),
            events.takeLast(2).map { it.peekContent() }
        )
    }

    @Test
    fun `Start sync`() {
        createViewModel()

        viewModel.onSyncClicked()

        (viewModel.events.value?.getContentIfNotHandled() as OfflineContentAction.Dialog).positiveCallback.invoke()

        coVerify {
            offlineSyncHelper.syncCourses(any())
            offlineAnalyticsManager.reportOfflineSyncStarted()
        }
    }

    @Test
    fun `Save state on sync`() {
        mockkCourseViewModels()

        createViewModel()

        val tabs = CourseSyncSettingsEntity.TABS.associateWith { true }
        val expected = CourseSyncSettingsEntity(1L, "Course", true, tabs, true)
        val expectedFiles =
            listOf(FileSyncSettingsEntity(1L, "File 1", 1L, null), FileSyncSettingsEntity(2L, "File 2", 1L, null))

        viewModel.data.value?.courseItems?.first()?.apply {
            onCheckedChanged.invoke(true, this)
        }

        viewModel.onSyncClicked()

        (viewModel.events.value?.getContentIfNotHandled() as OfflineContentAction.Dialog).positiveCallback.invoke()

        coVerify(exactly = 1) { offlineContentRepository.updateCourseSyncSettings(1L, expected, expectedFiles) }
    }

    @Test
    fun `Syllabus check changed`() {
        mockkCourseViewModels()

        createViewModel()

        viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.SYLLABUS_ID }?.apply {
            onCheckedChanged.invoke(true, this)
        }

        Assert.assertTrue(viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.SYLLABUS_ID }?.data?.checkedState() == MaterialCheckBox.STATE_CHECKED)
        Assert.assertTrue(viewModel.data.value?.courseItems?.first()?.data?.checkedState() == MaterialCheckBox.STATE_INDETERMINATE)
    }

    @Test
    fun `Pages check changed`() {
        mockkCourseViewModels()

        createViewModel()

        viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.PAGES_ID }?.apply {
            onCheckedChanged.invoke(true, this)
        }

        Assert.assertTrue(viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.PAGES_ID }?.data?.checkedState() == MaterialCheckBox.STATE_CHECKED)
        Assert.assertTrue(viewModel.data.value?.courseItems?.first()?.data?.checkedState() == MaterialCheckBox.STATE_INDETERMINATE)
    }

    @Test
    fun `Assignments check changed`() {
        mockkCourseViewModels()

        createViewModel()

        viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.ASSIGNMENTS_ID }?.apply {
            onCheckedChanged.invoke(true, this)
        }

        Assert.assertTrue(viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.ASSIGNMENTS_ID }?.data?.checkedState() == MaterialCheckBox.STATE_CHECKED)
        Assert.assertTrue(viewModel.data.value?.courseItems?.first()?.data?.checkedState() == MaterialCheckBox.STATE_INDETERMINATE)
    }

    @Test
    fun `Grades check changed`() {
        mockkCourseViewModels()

        createViewModel()

        viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.GRADES_ID }?.apply {
            onCheckedChanged.invoke(true, this)
        }

        Assert.assertTrue(viewModel.data.value?.courseItems?.first()?.data?.tabs?.find { it.tabId == Tab.GRADES_ID }?.data?.checkedState() == MaterialCheckBox.STATE_CHECKED)
        Assert.assertTrue(viewModel.data.value?.courseItems?.first()?.data?.checkedState() == MaterialCheckBox.STATE_INDETERMINATE)
    }

    private fun createViewModel() {
        viewModel = OfflineContentViewModel(
            savedStateHandle,
            context,
            offlineContentRepository,
            storageUtils,
            offlineSyncHelper,
            offlineAnalyticsManager
        )

        viewModel.state.observe(lifecycleTestOwner.lifecycleOwner) {}
        viewModel.events.observe(lifecycleTestOwner.lifecycleOwner) {}
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner) {}
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
        val files = listOf(FileFolder(id = 1, displayName = "File 1", size = 200000L), FileFolder(id = 2, displayName = "File 2", size = 300000L))

        coEvery { offlineContentRepository.getCourses() } returns courses
        coEvery { offlineContentRepository.getCourseFiles(1) } returns files
    }

    private fun mockStorageInfoData() {
        mockkCourseViewModels()
        every { storageUtils.getTotalSpace() } returns 10000000L
        every { storageUtils.getFreeSpace() } returns 8000000L
        every { storageUtils.getAppSize() } returns 1000000L
        every {
            context.getString(R.string.offline_content_storage_info, any(), any())
        } answers {
            "Used ${secondArg<Array<Any>>().first()} of ${secondArg<Array<Any>>().last()}"
        }
        every {
            context.getString(R.string.offline_content_storage_info_a11y_description_canvas, any(), any(), any(), any(), any())
        } answers {
            "Storage Info: ${secondArg<Array<Any>>()[0]} of ${secondArg<Array<Any>>()[1]} used, Other Apps ${secondArg<Array<Any>>()[2]}%, Canvas Student ${secondArg<Array<Any>>()[3]}%, Remaining ${secondArg<Array<Any>>()[4]}%"
        }

        createViewModel()
    }
}
