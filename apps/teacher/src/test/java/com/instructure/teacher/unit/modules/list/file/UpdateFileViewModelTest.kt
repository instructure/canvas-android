/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 *
 */

package com.instructure.teacher.unit.modules.list.file

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.UpdateFileFolder
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.FileFolderUpdatedEvent
import com.instructure.teacher.features.modules.list.ui.file.FileAvailability
import com.instructure.teacher.features.modules.list.ui.file.FileVisibility
import com.instructure.teacher.features.modules.list.ui.file.UpdateFileEvent
import com.instructure.teacher.features.modules.list.ui.file.UpdateFileViewModel
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.LifecycleTestOwner
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.greenrobot.eventbus.EventBus
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@ExperimentalCoroutinesApi
class UpdateFileViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val lifecycleTestOwner = LifecycleTestOwner()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val fileApi: FileFolderAPI.FilesFoldersInterface = mockk(relaxed = true)
    private val eventBus: EventBus = mockk(relaxed = true)

    private lateinit var viewModel: UpdateFileViewModel

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val timeFormat = SimpleDateFormat("HH:mm")

    @Before
    fun setUp() {

        mockkObject(DateHelper)
        val dateCaptor = slot<Date>()
        every { DateHelper.getFormattedDate(any(), capture(dateCaptor)) } answers {
            dateFormat.format(dateCaptor.captured)
        }
        val timeCaptor = slot<Date>()
        every { DateHelper.getFormattedTime(any(), capture(timeCaptor)) } answers {
            timeFormat.format(timeCaptor.captured)
        }
    }

    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `loadData sets correct state`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.PUBLIC.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)

        val contentDetails = ModuleContentDetails(hidden = false, locked = false)

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        assertEquals(ViewState.Success, viewModel.state.value)
    }

    @Test
    fun `Error during file fetching sets visibility to Inherit`() {
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Fail()

        val contentDetails = ModuleContentDetails(hidden = false, locked = false)

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        assertEquals(FileVisibility.INHERIT, viewModel.data.value?.selectedVisibility)
        assert(viewModel.state.value is ViewState.Success)
    }

    @Test
    fun `Published file maps correctly`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.PUBLIC.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)

        val contentDetails = ModuleContentDetails(hidden = false, locked = false)

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        assertEquals(FileAvailability.PUBLISHED, viewModel.data.value?.selectedAvailability)
        assert(viewModel.state.value is ViewState.Success)
    }

    @Test
    fun `Unpublished file maps correctly`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.PUBLIC.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)

        val contentDetails = ModuleContentDetails(hidden = false, locked = true)

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        assertEquals(FileAvailability.UNPUBLISHED, viewModel.data.value?.selectedAvailability)
    }

    @Test
    fun `Hidden file maps correctly`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.PUBLIC.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)

        val contentDetails = ModuleContentDetails(hidden = true, locked = false)

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        assertEquals(FileAvailability.HIDDEN, viewModel.data.value?.selectedAvailability)
    }

    @Test
    fun `Scheduled file maps correctly`() {
        val calendar = Calendar.getInstance()
        val unlockDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val lockDate = calendar.time

        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.PUBLIC.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)

        val contentDetails = ModuleContentDetails(
            hidden = false,
            locked = false,
            lockAt = lockDate.toApiString(),
            unlockAt = unlockDate.toApiString()
        )

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        assertEquals(FileAvailability.SCHEDULED, viewModel.data.value?.selectedAvailability)
        assertEquals(dateFormat.format(lockDate), viewModel.data.value?.lockAtDateString)
        assertEquals(timeFormat.format(lockDate), viewModel.data.value?.lockAtTimeString)
        assertEquals(dateFormat.format(unlockDate), viewModel.data.value?.unlockAtDateString)
        assertEquals(timeFormat.format(unlockDate), viewModel.data.value?.unlockAtTimeString)
    }

    @Test
    fun `Inherit visibility maps correctly`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.INHERIT.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)

        val contentDetails = ModuleContentDetails(hidden = false, locked = false)

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        assertEquals(FileVisibility.INHERIT, viewModel.data.value?.selectedVisibility)
    }

    @Test
    fun `Context visibility maps correctly`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.CONTEXT.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)

        val contentDetails = ModuleContentDetails(hidden = false, locked = false)

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        assertEquals(FileVisibility.CONTEXT, viewModel.data.value?.selectedVisibility)
    }

    @Test
    fun `Institution visibility maps correctly`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.INSTITUTION.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)

        val contentDetails = ModuleContentDetails(hidden = false, locked = false)

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        assertEquals(FileVisibility.INSTITUTION, viewModel.data.value?.selectedVisibility)
    }

    @Test
    fun `Public visibility maps correctly`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.PUBLIC.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)

        val contentDetails = ModuleContentDetails(hidden = false, locked = false)

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        assertEquals(FileVisibility.PUBLIC, viewModel.data.value?.selectedVisibility)
    }

    @Test
    fun `Availability change updates data`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.PUBLIC.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)

        val contentDetails = ModuleContentDetails(hidden = false, locked = false)

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        viewModel.onAvailabilityChanged(FileAvailability.HIDDEN)

        assertEquals(FileAvailability.HIDDEN, viewModel.data.value?.selectedAvailability)
    }

    @Test
    fun `Visibility change updates data`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.PUBLIC.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)

        val contentDetails = ModuleContentDetails(hidden = false, locked = false)

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        viewModel.onVisibilityChanged(FileVisibility.CONTEXT)

        assertEquals(FileVisibility.CONTEXT, viewModel.data.value?.selectedVisibility)
    }

    @Test
    fun `Close emits correct event`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.PUBLIC.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)

        val contentDetails = ModuleContentDetails(hidden = false, locked = false)

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        viewModel = createViewModel()
        viewModel.close()

        assertEquals(UpdateFileEvent.Close, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Update lock at date`() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)

        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.PUBLIC.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)
        val contentDetails = ModuleContentDetails(hidden = false, locked = false, lockAt = Date().toApiString())

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        viewModel.updateLockAt()
        val event = viewModel.events.value?.getContentIfNotHandled()
        assert(event is UpdateFileEvent.ShowDatePicker)
        (event as UpdateFileEvent.ShowDatePicker).callback(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        assertEquals(dateFormat.format(calendar.time), viewModel.data.value?.lockAtDateString)
        assertEquals(timeFormat.format(calendar.time), viewModel.data.value?.lockAtTimeString)
    }

    @Test
    fun `Update lock at time`() {
        val calendar = Calendar.getInstance()

        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.PUBLIC.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)
        val contentDetails = ModuleContentDetails(hidden = false, locked = false, lockAt = calendar.time.toApiString())

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        viewModel.updateLockTime()
        val event = viewModel.events.value?.getContentIfNotHandled()
        assert(event is UpdateFileEvent.ShowTimePicker)

        calendar.add(Calendar.HOUR_OF_DAY, 1)
        calendar.add(Calendar.MINUTE, 1)
        (event as UpdateFileEvent.ShowTimePicker).callback(
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )

        assertEquals(dateFormat.format(calendar.time), viewModel.data.value?.lockAtDateString)
        assertEquals(timeFormat.format(calendar.time), viewModel.data.value?.lockAtTimeString)
    }

    @Test
    fun `Update unlock at date`() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)

        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.PUBLIC.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)
        val contentDetails = ModuleContentDetails(hidden = false, locked = false, unlockAt = Date().toApiString())

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        viewModel.updateUnlockAt()
        val event = viewModel.events.value?.getContentIfNotHandled()
        assert(event is UpdateFileEvent.ShowDatePicker)
        (event as UpdateFileEvent.ShowDatePicker).callback(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        assertEquals(dateFormat.format(calendar.time), viewModel.data.value?.unlockAtDateString)
        assertEquals(timeFormat.format(calendar.time), viewModel.data.value?.unlockAtTimeString)
    }

    @Test
    fun `Update unlock at time`() {
        val calendar = Calendar.getInstance()

        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.PUBLIC.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)
        val contentDetails =
            ModuleContentDetails(hidden = false, locked = false, unlockAt = calendar.time.toApiString())

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        viewModel.updateUnlockTime()
        val event = viewModel.events.value?.getContentIfNotHandled()
        assert(event is UpdateFileEvent.ShowTimePicker)

        calendar.add(Calendar.HOUR_OF_DAY, 1)
        calendar.add(Calendar.MINUTE, 1)
        (event as UpdateFileEvent.ShowTimePicker).callback(
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )

        assertEquals(dateFormat.format(calendar.time), viewModel.data.value?.unlockAtDateString)
        assertEquals(timeFormat.format(calendar.time), viewModel.data.value?.unlockAtTimeString)
    }

    @Test
    fun `Clear lock time`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.PUBLIC.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)
        val contentDetails = ModuleContentDetails(hidden = false, locked = false, lockAt = Date().toApiString())

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        viewModel.clearLockDate()

        assertNull(viewModel.data.value?.lockAt)
        assertNull(viewModel.data.value?.lockAtDateString)
        assertNull(viewModel.data.value?.lockAtTimeString)
    }

    @Test
    fun `Clear unlock time`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.PUBLIC.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)
        val contentDetails = ModuleContentDetails(hidden = false, locked = false, unlockAt = Date().toApiString())

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        viewModel.clearUnlockDate()

        assertNull(viewModel.data.value?.unlockAt)
        assertNull(viewModel.data.value?.unlockAtDateString)
        assertNull(viewModel.data.value?.unlockAtTimeString)
    }

    @Test
    fun `Hiding file calls api with correct params`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.PUBLIC.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)
        coEvery { fileApi.updateFile(any(), any(), any()) } returns DataResult.Success(file)
        val contentDetails = ModuleContentDetails(hidden = false, locked = false, unlockAt = Date().toApiString())

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        viewModel.onAvailabilityChanged(FileAvailability.HIDDEN)

        viewModel.update()

        coVerify {
            fileApi.updateFile(
                1L,
                UpdateFileFolder(
                    hidden = true,
                    unlockAt = "",
                    lockAt = "",
                    locked = false,
                    visibilityLevel = FileVisibility.PUBLIC.name.lowercase()
                ),
                any()
            )
        }
    }

    @Test
    fun `Unpublishing file calls api with correct params`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.PUBLIC.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)
        coEvery { fileApi.updateFile(any(), any(), any()) } returns DataResult.Success(file)
        val contentDetails = ModuleContentDetails(hidden = false, locked = false, unlockAt = Date().toApiString())

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        viewModel.onAvailabilityChanged(FileAvailability.UNPUBLISHED)

        viewModel.update()

        coVerify {
            fileApi.updateFile(
                1L,
                UpdateFileFolder(
                    hidden = false,
                    unlockAt = "",
                    lockAt = "",
                    locked = true,
                    visibilityLevel = FileVisibility.PUBLIC.name.lowercase()
                ),
                any()
            )
        }
    }

    @Test
    fun `Publishing file calls api with correct params`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.CONTEXT.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)
        coEvery { fileApi.updateFile(any(), any(), any()) } returns DataResult.Success(file)
        val contentDetails = ModuleContentDetails(hidden = false, locked = true, unlockAt = Date().toApiString())

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        viewModel.onAvailabilityChanged(FileAvailability.PUBLISHED)

        viewModel.update()

        coVerify {
            fileApi.updateFile(
                1L,
                UpdateFileFolder(
                    hidden = false,
                    unlockAt = "",
                    lockAt = "",
                    locked = false,
                    visibilityLevel = FileVisibility.CONTEXT.name.lowercase()
                ),
                any()
            )
        }
    }

    @Test
    fun `Updating file emits correct events`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.CONTEXT.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)
        coEvery { fileApi.updateFile(any(), any(), any()) } returns DataResult.Success(file)

        val contentDetails = ModuleContentDetails(hidden = false, locked = true, unlockAt = Date().toApiString())

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        viewModel.onAvailabilityChanged(FileAvailability.PUBLISHED)

        viewModel.update()

        val event = viewModel.events.value?.getContentIfNotHandled()
        assert(event is UpdateFileEvent.Close)
        verify {
            eventBus.post(any<FileFolderUpdatedEvent>())
        }
    }

    @Test
    fun `Update error sets correct view state`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.CONTEXT.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)
        coEvery { fileApi.updateFile(any(), any(), any()) } returns DataResult.Fail()

        val contentDetails = ModuleContentDetails(hidden = false, locked = true, unlockAt = Date().toApiString())

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        viewModel.onAvailabilityChanged(FileAvailability.PUBLISHED)

        viewModel.update()

        assert(viewModel.state.value is ViewState.Error)
    }

    @Test
    fun `minDate is set if unlockDate is not null`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.CONTEXT.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)
        val contentDetails = ModuleContentDetails(hidden = false, locked = true, unlockAt = Date().toApiString())

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        viewModel.updateLockAt()

        val event = viewModel.events.value?.getContentIfNotHandled()
        assert(event is UpdateFileEvent.ShowDatePicker)
        assertEquals(contentDetails.unlockDate, (event as UpdateFileEvent.ShowDatePicker).minDate)
    }

    @Test
    fun `maxDate is set if lockDate is not null`() {
        val file = FileFolder(id = 1L, visibilityLevel = FileVisibility.CONTEXT.name.lowercase())
        coEvery { fileApi.getFile(any(), any()) } returns DataResult.Success(file)
        val contentDetails = ModuleContentDetails(hidden = false, locked = true, unlockAt = Date().toApiString())

        every { savedStateHandle.get<Long>("contentId") } returns 1L
        every { savedStateHandle.get<ModuleContentDetails>("contentDetails") } returns contentDetails

        viewModel = createViewModel()

        viewModel.updateUnlockAt()

        val event = viewModel.events.value?.getContentIfNotHandled()
        assert(event is UpdateFileEvent.ShowDatePicker)
        assertEquals(contentDetails.lockDate, (event as UpdateFileEvent.ShowDatePicker).maxDate)
    }

    private fun createViewModel() = UpdateFileViewModel(savedStateHandle, context, fileApi, eventBus)
}
