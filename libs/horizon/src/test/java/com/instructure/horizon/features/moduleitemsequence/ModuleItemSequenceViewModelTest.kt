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
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.ModuleCompletionRequirement
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Submission
import com.instructure.horizon.R
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.horizon.domain.usecase.GetAssignmentDetailsUseCase
import com.instructure.horizon.domain.usecase.GetCourseDetailsUseCase
import com.instructure.horizon.domain.usecase.GetEntityLastSyncedAtUseCase
import com.instructure.horizon.domain.usecase.GetModuleItemSequenceUseCase
import com.instructure.horizon.domain.usecase.GetModuleItemUseCase
import com.instructure.horizon.domain.usecase.GetModulesWithItemsUseCase
import com.instructure.horizon.domain.usecase.HasUnreadCommentsUseCase
import com.instructure.horizon.domain.usecase.MarkModuleItemAsDoneUseCase
import com.instructure.horizon.domain.usecase.MarkModuleItemAsNotDoneUseCase
import com.instructure.horizon.domain.usecase.MarkModuleItemAsReadUseCase
import com.instructure.horizon.domain.usecase.SyncCourseProgressUseCase
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.horizon.features.dashboard.DashboardEventHandler
import com.instructure.horizon.features.learn.LearnEventHandler
import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCardStateMapper
import com.instructure.horizon.navigation.MainNavigationRoute
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.offline.HorizonOfflineState
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import kotlinx.coroutines.flow.MutableStateFlow
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import okhttp3.ResponseBody
import junit.framework.TestCase.assertTrue
import java.util.Date
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
    private val getCourseDetailsUseCase: GetCourseDetailsUseCase = mockk(relaxed = true)
    private val syncCourseProgressUseCase: SyncCourseProgressUseCase = mockk(relaxed = true)
    private val offlineState: HorizonOfflineState = mockk(relaxed = true)
    private val createSubmissionDao: CreateSubmissionDao = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val queuedSubmissionsFlow = MutableStateFlow<List<CreateSubmissionEntity>>(emptyList())
    private val isOfflineFlow = MutableStateFlow(false)
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

    private val markDoneModuleItem = ModuleItem(
        id = moduleItemId,
        title = "Mark-Done Item",
        moduleId = 1L,
        type = "Assignment",
        completionRequirement = ModuleCompletionRequirement(
            type = ModuleItem.MUST_MARK_DONE,
            completed = false,
        ),
    )

    private val markDoneTestModule = ModuleObject(
        id = 1L,
        name = "Test Module",
        items = listOf(markDoneModuleItem),
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
        every { offlineState.isOffline } returns isOfflineFlow
        every { createSubmissionDao.findScheduledSubmissionsFlow(any()) } returns queuedSubmissionsFlow
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
    @Test
    fun `Score tag is shown when the current assignment is graded`() = runTest {
        every { context.getString(R.string.attemptCard_score, any<String>(), any<String>()) } answers {
            val varargs = args[1] as Array<*>
            "Score: ${varargs[0]}/${varargs[1]}"
        }
        every { context.resources.getQuantityString(R.plurals.modulePager_pointsPossible, any(), any()) } answers {
            val varargs = args[2] as Array<*>
            "${varargs[0]} points possible"
        }
        val gradedItem = testModuleItem.copy(
            moduleDetails = ModuleContentDetails(pointsPossible = "100")
        )
        coEvery { getModulesWithItemsUseCase(any()) } returns listOf(testModule.copy(items = listOf(gradedItem)))
        coEvery { getAssignmentDetailsUseCase(any()) } returns Assignment(
            id = 1L,
            pointsPossible = 100.0,
            submission = Submission(
                grade = "A",
                score = 85.0,
                workflowState = "graded",
                postedAt = Date()
            )
        )

        val viewModel = getViewModel(savedStateHandle)

        val tags = viewModel.uiState.value.currentItem?.detailTags.orEmpty()
        assertTrue("Expected score tag, was $tags", tags.any { it == "Score: 85/100" })
        assertFalse("Score should replace points possible, was $tags", tags.any { it.contains("points possible") })
    }

    @Test
    fun `Points possible tag is shown when the assignment is not graded yet`() = runTest {
        every { context.getString(R.string.attemptCard_score, any<String>(), any<String>()) } answers {
            val varargs = args[1] as Array<*>
            "Score: ${varargs[0]}/${varargs[1]}"
        }
        every { context.resources.getQuantityString(R.plurals.modulePager_pointsPossible, any(), any()) } answers {
            val varargs = args[2] as Array<*>
            "${varargs[0]} points possible"
        }
        val ungradedItem = testModuleItem.copy(
            moduleDetails = ModuleContentDetails(pointsPossible = "100")
        )
        coEvery { getModulesWithItemsUseCase(any()) } returns listOf(testModule.copy(items = listOf(ungradedItem)))
        coEvery { getAssignmentDetailsUseCase(any()) } returns Assignment(
            id = 1L,
            pointsPossible = 100.0,
            submission = Submission(grade = "A", score = 85.0, workflowState = "graded", postedAt = null)
        )

        val viewModel = getViewModel(savedStateHandle)

        val tags = viewModel.uiState.value.currentItem?.detailTags.orEmpty()
        assertTrue("Expected points possible tag, was $tags", tags.any { it == "100 points possible" })
        assertFalse("Score tag should not appear, was $tags", tags.any { it.startsWith("Score:") })
    }

    @Test
    fun `Points possible tag is shown for non-assignment items`() = runTest {
        every { context.resources.getQuantityString(R.plurals.modulePager_pointsPossible, any(), any()) } answers {
            val varargs = args[2] as Array<*>
            "${varargs[0]} points possible"
        }
        val pageItem = testModuleItem.copy(
            type = "Page",
            moduleDetails = ModuleContentDetails(pointsPossible = "100")
        )
        coEvery { getModulesWithItemsUseCase(any()) } returns listOf(testModule.copy(items = listOf(pageItem)))

        val viewModel = getViewModel(savedStateHandle)

        val tags = viewModel.uiState.value.currentItem?.detailTags.orEmpty()
        assertTrue("Expected points possible tag, was $tags", tags.any { it == "100 points possible" })
        assertFalse("Score tag should not appear, was $tags", tags.any { it.startsWith("Score:") })
    }


    @Test
    fun `syncCourseProgress is called once when mark-done brings course to 100 percent`() = runTest {
        coEvery { getModulesWithItemsUseCase(any()) } returns listOf(markDoneTestModule)
        coEvery { getModuleItemUseCase(any()) } returns markDoneModuleItem
        coEvery { markModuleItemAsDoneUseCase(any()) } returns DataResult.Success(mockk<ResponseBody>(relaxed = true))
        coEvery { getCourseDetailsUseCase(any()) } returns CourseWithProgress(
            courseId = courseId,
            courseName = "Course",
            courseImageUrl = null,
            courseSyllabus = null,
            progress = 100.0,
        )
        coEvery { syncCourseProgressUseCase(any()) } returns DataResult.Success(Unit)

        val viewModel = getViewModel(savedStateHandle)
        viewModel.uiState.value.currentItem?.markAsDoneUiState?.onMarkAsDoneClick?.invoke()

        coVerify(exactly = 1) { syncCourseProgressUseCase(courseId) }
    }

    @Test
    fun `syncCourseProgress is not called when course progress stays below 100 percent`() = runTest {
        coEvery { getModulesWithItemsUseCase(any()) } returns listOf(markDoneTestModule)
        coEvery { getModuleItemUseCase(any()) } returns markDoneModuleItem
        coEvery { markModuleItemAsDoneUseCase(any()) } returns DataResult.Success(mockk<ResponseBody>(relaxed = true))
        coEvery { getCourseDetailsUseCase(any()) } returns CourseWithProgress(
            courseId = courseId,
            courseName = "Course",
            courseImageUrl = null,
            courseSyllabus = null,
            progress = 50.0,
        )

        val viewModel = getViewModel(savedStateHandle)
        viewModel.uiState.value.currentItem?.markAsDoneUiState?.onMarkAsDoneClick?.invoke()

        coVerify(exactly = 0) { syncCourseProgressUseCase(any()) }
    }

    @Test
    fun `syncCourseProgress is called only once across repeated 100 percent completions in the same session`() = runTest {
        coEvery { getModulesWithItemsUseCase(any()) } returns listOf(markDoneTestModule)
        coEvery { getModuleItemUseCase(any()) } returns markDoneModuleItem
        coEvery { markModuleItemAsDoneUseCase(any()) } returns DataResult.Success(mockk<ResponseBody>(relaxed = true))
        coEvery { getCourseDetailsUseCase(any()) } returns CourseWithProgress(
            courseId = courseId,
            courseName = "Course",
            courseImageUrl = null,
            courseSyllabus = null,
            progress = 100.0,
        )
        coEvery { syncCourseProgressUseCase(any()) } returns DataResult.Success(Unit)

        val viewModel = getViewModel(savedStateHandle)
        viewModel.uiState.value.currentItem?.markAsDoneUiState?.onMarkAsDoneClick?.invoke()
        viewModel.uiState.value.currentItem?.markAsDoneUiState?.onMarkAsDoneClick?.invoke()

        coVerify(exactly = 1) { syncCourseProgressUseCase(courseId) }
    }

    @Test
    fun `syncCourseProgress is not called when course progress fetch fails`() = runTest {
        coEvery { getModulesWithItemsUseCase(any()) } returns listOf(markDoneTestModule)
        coEvery { getModuleItemUseCase(any()) } returns markDoneModuleItem
        coEvery { markModuleItemAsDoneUseCase(any()) } returns DataResult.Success(mockk<ResponseBody>(relaxed = true))
        coEvery { getCourseDetailsUseCase(any()) } throws RuntimeException("network error")

        val viewModel = getViewModel(savedStateHandle)
        viewModel.uiState.value.currentItem?.markAsDoneUiState?.onMarkAsDoneClick?.invoke()

        coVerify(exactly = 0) { syncCourseProgressUseCase(any()) }
    }

    @Test
    fun `syncCourseProgress is not called when mark-done itself fails`() = runTest {
        coEvery { getModulesWithItemsUseCase(any()) } returns listOf(markDoneTestModule)
        coEvery { getModuleItemUseCase(any()) } returns markDoneModuleItem
        coEvery { markModuleItemAsDoneUseCase(any()) } returns DataResult.Fail(Failure.Network("nope"))

        val viewModel = getViewModel(savedStateHandle)
        viewModel.uiState.value.currentItem?.markAsDoneUiState?.onMarkAsDoneClick?.invoke()

        coVerify(exactly = 0) { getCourseDetailsUseCase(any()) }
        coVerify(exactly = 0) { syncCourseProgressUseCase(any()) }
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
            getCourseDetailsUseCase,
            syncCourseProgressUseCase,
            savedStateHandle,
            offlineState,
            createSubmissionDao,
            apiPrefs
        )
    }
}
