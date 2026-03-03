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
package com.instructure.horizon.features.learn.program.details

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithModuleItemDurations
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardEventHandler
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.features.learn.program.details.components.CourseCardStatus
import com.instructure.journey.type.ProgramProgressCourseEnrollmentStatus
import com.instructure.journey.type.ProgramVariantType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class ProgramDetailsViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val repository: ProgramDetailsRepository = mockk(relaxed = true)
    private val dashboardEventHandler: DashboardEventHandler = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testProgramId = "program123"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val sharedPrefs: SharedPreferences = mockk(relaxed = true)
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs
        every { sharedPrefs.getInt(any(), any()) } returns 0

        ContextKeeper.appContext = context
        every { savedStateHandle.get<String>(LearnRoute.LearnProgramDetailsScreen.programIdAttr) } returns testProgramId
        every { context.getString(any()) } returns ""
        every { context.getString(any(), any()) } returns ""
        every { context.getString(any(), any(), any()) } returns ""
        every { context.resources } returns resources
        every { resources.getQuantityString(any(), any(), any(), any()) } returns "1 of 2 courses complete"
        every { resources.getQuantityString(any(), any(), any()) } answers {
            val quantity = secondArg<Int>()
            "$quantity hours"
        }
        every { resources.getString(any()) } returns ""
        every { resources.getString(any(), any()) } returns ""
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial state loads program details successfully`() {
        val program = createTestProgram(
            id = testProgramId,
            name = "Software Engineering",
            requirements = listOf(createTestProgramRequirement(courseId = 1L, progress = 0.0))
        )
        val courses = listOf(createTestCourse(courseId = 1L, courseName = "Intro to Programming"))
        coEvery { repository.getProgramDetails(testProgramId, false) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertFalse(state.loadingState.isLoading)
        assertFalse(state.loadingState.isError)
        assertEquals("Software Engineering", state.programName)
        coVerify { repository.getProgramDetails(testProgramId, false) }
    }

    @Test
    fun `Loading state shows error when repository fails`() {
        coEvery { repository.getProgramDetails(any(), any()) } throws Exception("Network error")

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertTrue(state.loadingState.isError)
        assertFalse(state.loadingState.isLoading)
    }

    @Test
    fun `Program ID is extracted from SavedStateHandle`() {
        val program = createTestProgram(id = testProgramId)
        val courses = emptyList<CourseWithModuleItemDurations>()
        coEvery { repository.getProgramDetails(testProgramId, false) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        getViewModel()

        coVerify { repository.getProgramDetails(testProgramId, false) }
    }

    @Test
    fun `Progress bar shown for linear program with required courses`() {
        val program = createTestProgram(
            variant = ProgramVariantType.LINEAR,
            requirements = listOf(
                createTestProgramRequirement(courseId = 1L, required = true, progress = 0.0)
            )
        )
        val courses = listOf(createTestCourse(courseId = 1L))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertTrue(state.showProgressBar)
    }

    @Test
    fun `Progress bar shown for non-linear program with courseCompletionCount`() {
        val program = createTestProgram(
            variant = ProgramVariantType.NON_LINEAR,
            courseCompletionCount = 2,
            requirements = listOf(
                createTestProgramRequirement(courseId = 1L, required = true, progress = 0.0)
            )
        )
        val courses = listOf(createTestCourse(courseId = 1L))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertTrue(state.showProgressBar)
    }

    @Test
    fun `Progress bar hidden when no requirements`() {
        val program = createTestProgram(requirements = emptyList())
        val courses = emptyList<CourseWithModuleItemDurations>()
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertFalse(state.showProgressBar)
    }

    @Test
    fun `Calculate progress correctly for linear program`() {
        val program = createTestProgram(
            variant = ProgramVariantType.LINEAR,
            requirements = listOf(
                createTestProgramRequirement(courseId = 1L, required = true, progress = 50.0),
                createTestProgramRequirement(courseId = 2L, required = true, progress = 100.0),
                createTestProgramRequirement(courseId = 3L, required = false, progress = 0.0)
            )
        )
        val courses = listOf(
            createTestCourse(courseId = 1L),
            createTestCourse(courseId = 2L),
            createTestCourse(courseId = 3L)
        )
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertEquals(75.0, state.progressBarUiState.progress)
    }

    @Test
    fun `Calculate progress correctly for non-linear program`() {
        val program = createTestProgram(
            variant = ProgramVariantType.NON_LINEAR,
            courseCompletionCount = 2,
            requirements = listOf(
                createTestProgramRequirement(courseId = 1L, required = true, progress = 100.0),
                createTestProgramRequirement(courseId = 2L, required = true, progress = 50.0),
                createTestProgramRequirement(courseId = 3L, required = true, progress = 25.0)
            )
        )
        val courses = listOf(
            createTestCourse(courseId = 1L),
            createTestCourse(courseId = 2L),
            createTestCourse(courseId = 3L)
        )
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertEquals(75.0, state.progressBarUiState.progress)
    }

    @Test
    fun `Program tags include date range when dates present`() {
        val program = createTestProgram(
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            requirements = listOf(createTestProgramRequirement(courseId = 1L))
        )
        val courses = listOf(createTestCourse(courseId = 1L))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertTrue(state.tags.isNotEmpty())
    }

    @Test
    fun `Program tags include duration when courses have module items`() {
        val program = createTestProgram(
            requirements = listOf(createTestProgramRequirement(courseId = 1L, required = true))
        )
        val courses = listOf(createTestCourse(courseId = 1L, moduleItemsDuration = listOf("PT2H", "PT1H")))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertTrue(state.tags.isNotEmpty())
    }

    @Test
    fun `Program tags only include available data`() {
        val program = createTestProgram(
            startDate = null,
            endDate = null,
            requirements = listOf(createTestProgramRequirement(courseId = 1L, required = true))
        )
        val courses = listOf(createTestCourse(courseId = 1L, moduleItemsDuration = emptyList()))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertTrue(state.tags.isEmpty())
    }

    @Test
    fun `Course card status is Inactive when blocked`() {
        val program = createTestProgram(
            requirements = listOf(
                createTestProgramRequirement(
                    courseId = 1L,
                    enrollmentStatus = ProgramProgressCourseEnrollmentStatus.BLOCKED
                )
            )
        )
        val courses = listOf(createTestCourse(courseId = 1L))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertEquals(CourseCardStatus.Inactive, state.programProgressState.courses[0].courseCard.status)
    }

    @Test
    fun `Course card status is Active when not enrolled`() {
        val program = createTestProgram(
            requirements = listOf(
                createTestProgramRequirement(
                    courseId = 1L,
                    enrollmentStatus = ProgramProgressCourseEnrollmentStatus.NOT_ENROLLED
                )
            )
        )
        val courses = listOf(createTestCourse(courseId = 1L))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertEquals(CourseCardStatus.Active, state.programProgressState.courses[0].courseCard.status)
    }

    @Test
    fun `Course card status is Enrolled when enrolled with 0 progress`() {
        val program = createTestProgram(
            requirements = listOf(
                createTestProgramRequirement(
                    courseId = 1L,
                    enrollmentStatus = ProgramProgressCourseEnrollmentStatus.ENROLLED,
                    progress = 0.0
                )
            )
        )
        val courses = listOf(createTestCourse(courseId = 1L))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertEquals(CourseCardStatus.Enrolled, state.programProgressState.courses[0].courseCard.status)
    }

    @Test
    fun `Course card status is InProgress when enrolled with partial progress`() {
        val program = createTestProgram(
            requirements = listOf(
                createTestProgramRequirement(
                    courseId = 1L,
                    enrollmentStatus = ProgramProgressCourseEnrollmentStatus.ENROLLED,
                    progress = 50.0
                )
            )
        )
        val courses = listOf(createTestCourse(courseId = 1L))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertEquals(CourseCardStatus.InProgress, state.programProgressState.courses[0].courseCard.status)
    }

    @Test
    fun `Course card status is Completed when progress is 100`() {
        val program = createTestProgram(
            requirements = listOf(
                createTestProgramRequirement(
                    courseId = 1L,
                    enrollmentStatus = ProgramProgressCourseEnrollmentStatus.ENROLLED,
                    progress = 100.0
                )
            )
        )
        val courses = listOf(createTestCourse(courseId = 1L))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertEquals(CourseCardStatus.Completed, state.programProgressState.courses[0].courseCard.status)
    }

    @Test
    fun `Course chips include locked chip when blocked`() {
        every { context.getString(R.string.programCourseTag_locked) } returns "Locked"
        val program = createTestProgram(
            requirements = listOf(
                createTestProgramRequirement(
                    courseId = 1L,
                    enrollmentStatus = ProgramProgressCourseEnrollmentStatus.BLOCKED
                )
            )
        )
        val courses = listOf(createTestCourse(courseId = 1L))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertTrue(state.programProgressState.courses[0].courseCard.chips.any { it.label == "Locked" })
    }

    @Test
    fun `Course chips include enrolled chip when enrolled`() {
        every { context.getString(R.string.programCourseTag_enrolled) } returns "Enrolled"
        val program = createTestProgram(
            requirements = listOf(
                createTestProgramRequirement(
                    courseId = 1L,
                    enrollmentStatus = ProgramProgressCourseEnrollmentStatus.ENROLLED,
                    progress = 0.0
                )
            )
        )
        val courses = listOf(createTestCourse(courseId = 1L))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertTrue(state.programProgressState.courses[0].courseCard.chips.any { it.label == "Enrolled" })
    }

    @Test
    fun `Course chips include required for linear programs`() {
        every { context.getString(R.string.programCourseTag_required) } returns "Required"
        val program = createTestProgram(
            variant = ProgramVariantType.LINEAR,
            requirements = listOf(
                createTestProgramRequirement(
                    courseId = 1L,
                    required = true,
                    enrollmentStatus = ProgramProgressCourseEnrollmentStatus.NOT_ENROLLED
                )
            )
        )
        val courses = listOf(createTestCourse(courseId = 1L))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertTrue(state.programProgressState.courses[0].courseCard.chips.any { it.label == "Required" })
    }

    @Test
    fun `Course chips include duration and date range`() {
        val program = createTestProgram(
            requirements = listOf(
                createTestProgramRequirement(
                    courseId = 1L,
                    enrollmentStatus = ProgramProgressCourseEnrollmentStatus.NOT_ENROLLED
                )
            )
        )
        val courses = listOf(
            createTestCourse(
                courseId = 1L,
                moduleItemsDuration = listOf("PT2H"),
                startDate = "2024-01-01",
                endDate = "2024-12-31"
            )
        )
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertTrue(state.programProgressState.courses[0].courseCard.chips.size >= 2)
    }

    @Test
    fun `Sequential properties set correctly for linear program first course`() {
        val program = createTestProgram(
            variant = ProgramVariantType.LINEAR,
            requirements = listOf(
                createTestProgramRequirement(courseId = 1L, progress = 0.0),
                createTestProgramRequirement(courseId = 2L, progress = 0.0)
            )
        )
        val courses = listOf(createTestCourse(courseId = 1L), createTestCourse(courseId = 2L))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        val firstCourse = state.programProgressState.courses[0]
        assertTrue(firstCourse.sequentialProperties != null)
        assertEquals(1, firstCourse.sequentialProperties?.index)
        assertTrue(firstCourse.sequentialProperties?.first == true)
        assertFalse(firstCourse.sequentialProperties?.last == true)
    }

    @Test
    fun `Sequential properties set correctly for linear program middle course`() {
        val program = createTestProgram(
            variant = ProgramVariantType.LINEAR,
            requirements = listOf(
                createTestProgramRequirement(courseId = 1L, progress = 100.0),
                createTestProgramRequirement(courseId = 2L, progress = 0.0),
                createTestProgramRequirement(courseId = 3L, progress = 0.0)
            )
        )
        val courses = listOf(
            createTestCourse(courseId = 1L),
            createTestCourse(courseId = 2L),
            createTestCourse(courseId = 3L)
        )
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        val middleCourse = state.programProgressState.courses[1]
        assertTrue(middleCourse.sequentialProperties != null)
        assertEquals(2, middleCourse.sequentialProperties?.index)
        assertFalse(middleCourse.sequentialProperties?.first == true)
        assertFalse(middleCourse.sequentialProperties?.last == true)
        assertTrue(middleCourse.sequentialProperties?.previousCompleted == true)
    }

    @Test
    fun `Sequential properties set correctly for linear program last course`() {
        val program = createTestProgram(
            variant = ProgramVariantType.LINEAR,
            requirements = listOf(
                createTestProgramRequirement(courseId = 1L, progress = 0.0),
                createTestProgramRequirement(courseId = 2L, progress = 0.0)
            )
        )
        val courses = listOf(createTestCourse(courseId = 1L), createTestCourse(courseId = 2L))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses

        val viewModel = getViewModel()

        val state = viewModel.state.value
        val lastCourse = state.programProgressState.courses[1]
        assertTrue(lastCourse.sequentialProperties != null)
        assertEquals(2, lastCourse.sequentialProperties?.index)
        assertFalse(lastCourse.sequentialProperties?.first == true)
        assertTrue(lastCourse.sequentialProperties?.last == true)
    }

    @Test
    fun `Refresh calls repository with forceNetwork true`() {
        val program = createTestProgram()
        val courses = emptyList<CourseWithModuleItemDurations>()
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses
        val viewModel = getViewModel()

        viewModel.state.value.loadingState.onRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.getProgramDetails(testProgramId, true) }
    }

    @Test
    fun `Refresh on error shows snackbar message`() {
        val program = createTestProgram()
        val courses = emptyList<CourseWithModuleItemDurations>()
        coEvery { repository.getProgramDetails(any(), false) } returns program
        coEvery { repository.getCoursesById(any(), false) } returns courses
        every { context.getString(R.string.programDetails_failedToRefresh) } returns "Failed to refresh"
        val viewModel = getViewModel()

        coEvery { repository.getProgramDetails(any(), true) } throws Exception("Network error")
        viewModel.state.value.loadingState.onRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.loadingState.isRefreshing)
        assertTrue(state.loadingState.snackbarMessage != null)
    }

    @Test
    fun `enrollCourse updates loading state`() {
        val program = createTestProgram(
            requirements = listOf(
                createTestProgramRequirement(courseId = 1L, progressId = "progress123")
            )
        )
        val courses = listOf(createTestCourse(courseId = 1L))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses
        coEvery { repository.enrollCourse(any()) } returns DataResult.Success(Unit)

        val viewModel = getViewModel()
        val initialState = viewModel.state.value

        initialState.programProgressState.courses[0].courseCard.onEnrollClicked?.invoke()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.enrollCourse("progress123") }
    }

    @Test
    fun `enrollCourse success triggers dashboard event and refresh`() {
        val program = createTestProgram(
            requirements = listOf(
                createTestProgramRequirement(courseId = 1L, progressId = "progress123")
            )
        )
        val courses = listOf(createTestCourse(courseId = 1L))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses
        coEvery { repository.enrollCourse(any()) } returns DataResult.Success(Unit)

        val viewModel = getViewModel()
        val initialState = viewModel.state.value

        initialState.programProgressState.courses[0].courseCard.onEnrollClicked?.invoke()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { dashboardEventHandler.postEvent(any()) }
        coVerify(atLeast = 2) { repository.getProgramDetails(testProgramId, any()) }
    }

    @Test
    fun `enrollCourse failure shows snackbar message`() {
        val program = createTestProgram(
            requirements = listOf(
                createTestProgramRequirement(courseId = 1L, progressId = "progress123")
            )
        )
        val courses = listOf(createTestCourse(courseId = 1L))
        coEvery { repository.getProgramDetails(any(), any()) } returns program
        coEvery { repository.getCoursesById(any(), any()) } returns courses
        coEvery { repository.enrollCourse(any()) } returns DataResult.Fail()
        every { context.getString(R.string.programDetails_enrollFailed) } returns "Enroll failed"

        val viewModel = getViewModel()
        val initialState = viewModel.state.value

        initialState.programProgressState.courses[0].courseCard.onEnrollClicked?.invoke()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.loadingState.snackbarMessage != null)
    }

    private fun getViewModel(): ProgramDetailsViewModel {
        val viewModel = ProgramDetailsViewModel(context, resources, repository, dashboardEventHandler, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()
        return viewModel
    }

    private fun createTestProgram(
        id: String = testProgramId,
        name: String = "Test Program",
        variant: ProgramVariantType = ProgramVariantType.LINEAR,
        courseCompletionCount: Int? = null,
        startDate: String? = null,
        endDate: String? = null,
        requirements: List<ProgramRequirement> = emptyList()
    ): Program = Program(
        id = id,
        name = name,
        description = "Test description",
        startDate = startDate?.let { parseDate(it) },
        endDate = endDate?.let { parseDate(it) },
        variant = variant,
        courseCompletionCount = courseCompletionCount,
        sortedRequirements = requirements
    )

    private fun createTestProgramRequirement(
        courseId: Long = 1L,
        progress: Double = 0.0,
        required: Boolean = true,
        progressId: String = "progress123",
        enrollmentStatus: ProgramProgressCourseEnrollmentStatus = ProgramProgressCourseEnrollmentStatus.NOT_ENROLLED
    ): ProgramRequirement = ProgramRequirement(
        id = "requirement$courseId",
        progressId = progressId,
        courseId = courseId,
        required = required,
        progress = progress,
        enrollmentStatus = enrollmentStatus
    )

    private fun createTestCourse(
        courseId: Long = 1L,
        courseName: String = "Test Course",
        moduleItemsDuration: List<String> = listOf("PT1H"),
        startDate: String? = null,
        endDate: String? = null
    ): CourseWithModuleItemDurations = CourseWithModuleItemDurations(
        courseId = courseId,
        courseName = courseName,
        moduleItemsDuration = moduleItemsDuration,
        startDate = startDate?.let { parseDate(it) },
        endDate = endDate?.let { parseDate(it) }
    )

    private fun parseDate(dateString: String): Date {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateString) ?: Date()
    }
}
