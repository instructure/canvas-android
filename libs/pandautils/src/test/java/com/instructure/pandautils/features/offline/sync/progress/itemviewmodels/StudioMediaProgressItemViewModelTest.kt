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
 */
package com.instructure.pandautils.features.offline.sync.progress.itemviewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.progress.StudioMediaProgressViewData
import com.instructure.pandautils.room.offline.daos.StudioMediaProgressDao
import com.instructure.pandautils.room.offline.entities.StudioMediaProgressEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import com.instructure.testutils.ViewModelTestRule
import org.junit.Test

class StudioMediaProgressItemViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val studioMediaProgressDao: StudioMediaProgressDao = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private lateinit var studioMediaProgressItemViewModel: StudioMediaProgressItemViewModel

    @Before
    fun setup() {
        mockkObject(NumberHelper)
        val captor = slot<Long>()
        every { NumberHelper.readableFileSize(any<Context>(), capture(captor)) } answers {
            "${captor.captured} bytes"
        }
    }

    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `Don't set item to visible if there are no progress entities`() {
        val liveData = MutableLiveData<List<StudioMediaProgressEntity>>(emptyList())

        every { studioMediaProgressDao.findAllLiveData() } returns liveData

        studioMediaProgressItemViewModel = createItemViewModel()

        liveData.postValue(emptyList())

        assertFalse(studioMediaProgressItemViewModel.data.visible)
    }

    @Test
    fun `Update state with in progress entities`() {
        val liveData = MutableLiveData<List<StudioMediaProgressEntity>>(emptyList())

        every { studioMediaProgressDao.findAllLiveData() } returns liveData

        studioMediaProgressItemViewModel = createItemViewModel()
        assertFalse(studioMediaProgressItemViewModel.data.visible)

        liveData.postValue(listOf(
            StudioMediaProgressEntity("1", 0, 100, ProgressState.IN_PROGRESS),
            StudioMediaProgressEntity("2", 0, 100, ProgressState.IN_PROGRESS)
        ))

        assertTrue(studioMediaProgressItemViewModel.data.visible)
        assertEquals(ProgressState.IN_PROGRESS, studioMediaProgressItemViewModel.data.state)
        assertEquals("200 bytes", studioMediaProgressItemViewModel.data.totalSize)
    }

    @Test
    fun `Update state with error, when one progress item has error progress`() {
        val liveData = MutableLiveData<List<StudioMediaProgressEntity>>(emptyList())

        every { studioMediaProgressDao.findAllLiveData() } returns liveData

        studioMediaProgressItemViewModel = createItemViewModel()
        assertFalse(studioMediaProgressItemViewModel.data.visible)

        liveData.postValue(listOf(
            StudioMediaProgressEntity("1", 0, 100, ProgressState.IN_PROGRESS),
            StudioMediaProgressEntity("2", 0, 100, ProgressState.ERROR)
        ))

        assertTrue(studioMediaProgressItemViewModel.data.visible)
        assertEquals(ProgressState.ERROR, studioMediaProgressItemViewModel.data.state)
        assertEquals("200 bytes", studioMediaProgressItemViewModel.data.totalSize)
    }

    @Test
    fun `Update state with completed, when all progress items are completed`() {
        val liveData = MutableLiveData<List<StudioMediaProgressEntity>>(emptyList())

        every { studioMediaProgressDao.findAllLiveData() } returns liveData

        studioMediaProgressItemViewModel = createItemViewModel()
        assertFalse(studioMediaProgressItemViewModel.data.visible)

        liveData.postValue(listOf(
            StudioMediaProgressEntity("1", 100, 100, ProgressState.COMPLETED),
            StudioMediaProgressEntity("2", 100, 100, ProgressState.COMPLETED)
        ))

        assertTrue(studioMediaProgressItemViewModel.data.visible)
        assertEquals(ProgressState.COMPLETED, studioMediaProgressItemViewModel.data.state)
        assertEquals("200 bytes", studioMediaProgressItemViewModel.data.totalSize)
    }

    private fun createItemViewModel(): StudioMediaProgressItemViewModel {
        return StudioMediaProgressItemViewModel(
            data = StudioMediaProgressViewData(),
            studioMediaProgressDao = studioMediaProgressDao,
            context = context)
    }
}
