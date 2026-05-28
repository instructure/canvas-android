/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.moduleitemsequence

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.horizon.domain.usecase.GetAssignmentDetailsUseCase
import com.instructure.horizon.domain.usecase.GetEntityLastSyncedAtUseCase
import com.instructure.horizon.domain.usecase.GetLastSyncedAtUseCase
import com.instructure.horizon.domain.usecase.GetModuleItemSequenceUseCase
import com.instructure.horizon.domain.usecase.GetModuleItemUseCase
import com.instructure.horizon.domain.usecase.GetModulesWithItemsUseCase
import com.instructure.horizon.domain.usecase.HasUnreadCommentsUseCase
import com.instructure.horizon.domain.usecase.MarkModuleItemAsDoneUseCase
import com.instructure.horizon.domain.usecase.MarkModuleItemAsNotDoneUseCase
import com.instructure.horizon.domain.usecase.MarkModuleItemAsReadUseCase
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.horizon.features.dashboard.DashboardEventHandler
import com.instructure.horizon.features.learn.LearnEventHandler
import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCardStateMapper
import com.instructure.horizon.navigation.MainNavigationRoute
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ModuleItemSequenceViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val getModuleItemSequenceUseCase: GetModuleItemSequenceUseCase = mockk(relaxed = true)
    private val getModulesWithItemsUseCase: GetModulesWithItemsUseCase = mockk(relaxed = true)
    private val getModuleItemUseCase: GetModuleItemUseCase = mockk(relaxed = true)
    private val markModuleItemAsDoneUseCase: MarkModuleItemAsDoneUseCase = mockk(relaxed = true)
    private val markModuleItemAsNotDoneUseCase: MarkModuleItemAsNotDoneUseCase = mockk(relaxed = true)
    private val markModuleItemAsReadUseCase: MarkModuleItemAsReadUseCase = mockk(relaxed = true)
    private val hasUnreadCommentsUseCase: HasUnreadCommentsUseCase = mockk(relaxed = true)
    private val getAssignmentDetailsUseCase: GetAssignmentDetailsUseCase = mockk(relaxed = true)
    private val moduleItemCardStateMapper: ModuleItemCardStateMapper = mockk(relaxed = true)
    private val aiAssistContextProvider: AiAssistContextProvider = mockk(relaxed = true)
    private val getEntityLastSyncedAtUseCase: GetEntityLastSyncedAtUseCase = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val getLastSyncedAtUseCase: GetLastSyncedAtUseCase = mockk(relaxed = true)
    private val dashboardEventHandler: DashboardEventHandler = DashboardEventHandler()
    private val learnEventHandler: LearnEventHandler = LearnEventHandler()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)

    private val courseId = 1L
    private val moduleItemId = 100L

    private val testModuleItem = ModuleItem(
        id = moduleItemId,
        title = "Test Item",
        moduleId = 1L,
        type = "Assignment"
    )

    private val testModule = ModuleObject(
        id = 1L,
        name = "Test Module",
        items = listOf(testModuleItem)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("androidx.navigation.SavedStateHandleKt")
        every { savedStateHandle.toRoute<MainNavigationRoute.ModuleItemSequence>() } returns MainNavigationRoute.ModuleItemSequence(
            courseId = courseId,
            moduleItemId = moduleItemId,
            moduleItemAssetType = null,
            moduleItemAssetId = null
        )
        coEvery { getModuleItemSequenceUseCase(any()) } returns ModuleItemSequence()
        coEvery { getModulesWithItemsUseCase(any()) } returns listOf(testModule)
        coEvery { getModuleItemUseCase(any()) } returns testModuleItem
        coEvery { getAssignmentDetailsUseCase(any()) } returns Assignment(id = 1L)
        coEvery { hasUnreadCommentsUseCase(any()) } returns false
        coEvery { moduleItemCardStateMapper.mapModuleItemToCardState(any(), any()) } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test data loads with moduleItemId`() = runTest {
        val viewModel = getViewModel(savedStateHandle)

        assertFalse(viewModel.uiState.value.loadingState.isLoading)
        coVerify { getModulesWithItemsUseCase(match { it.courseId == courseId }) }
    }

    @Test
    fun `Test module items are loaded`() = runTest {
        val viewModel = getViewModel(savedStateHandle)

        assertNotNull(viewModel.uiState.value)
        coVerify { getModulesWithItemsUseCase(match { it.courseId == courseId && it.forceRefresh }) }
    }

    @Test
    fun `Test failed data load sets error state`() = runTest {
        coEvery { getModulesWithItemsUseCase(any()) } throws Exception("Network error")

        val viewModel = getViewModel(savedStateHandle)

        assertFalse(viewModel.uiState.value.loadingState.isLoading)
    }

    @Test
    fun `Test assignment is fetched for module item`() = runTest {
        coEvery { getAssignmentDetailsUseCase(any()) } returns Assignment(id = 123L, name = "Test Assignment")

        val viewModel = getViewModel(savedStateHandle)

        coVerify { getAssignmentDetailsUseCase(any()) }
    }

    @Test
    fun `Test unread comments check is performed`() = runTest {
        coEvery { getAssignmentDetailsUseCase(any()) } returns Assignment(id = 123L)

        val viewModel = getViewModel(savedStateHandle)

        coVerify { hasUnreadCommentsUseCase(match { it.assignmentId == 123L }) }
    }

    private fun getViewModel(savedStateHandle: SavedStateHandle): ModuleItemSequenceViewModel {
        return ModuleItemSequenceViewModel(
            context,
            getModuleItemSequenceUseCase,
            getModulesWithItemsUseCase,
            getModuleItemUseCase,
            markModuleItemAsDoneUseCase,
            markModuleItemAsNotDoneUseCase,
            markModuleItemAsReadUseCase,
            hasUnreadCommentsUseCase,
            getAssignmentDetailsUseCase,
            getEntityLastSyncedAtUseCase,
            moduleItemCardStateMapper,
            aiAssistContextProvider,
            dashboardEventHandler,
            learnEventHandler,
            savedStateHandle,
            networkStateProvider,
            featureFlagProvider,
            getLastSyncedAtUseCase
        )
    }
}
