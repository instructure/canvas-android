/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

package com.instructure.pandautils.features.elementary.schedule

import android.content.res.Resources
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.CalendarEventManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.PlannerManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionState
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleCourseItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleDayGroupItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleEmptyItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleMissingItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleMissingItemsGroupItemViewModel
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.MissingItemsPrefs
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.pandautils.utils.date.RealDateTimeProvider
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.LifecycleTestOwner
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitAll
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

@ExperimentalCoroutinesApi
class ScheduleViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val lifecycleTestOwner = LifecycleTestOwner()

    private val plannerManager: PlannerManager = mockk(relaxed = true)
    private val courseManager: CourseManager = mockk(relaxed = true)
    private val userManager: UserManager = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val calendarEventManager: CalendarEventManager = mockk(relaxed = true)
    private val assignmentManager: AssignmentManager = mockk(relaxed = true)
    private val missingItemsPrefs: MissingItemsPrefs = mockk(relaxed = true)
    private val dateTimeProvider = RealDateTimeProvider()
    private val colorKeeper: ColorKeeper = mockk(relaxed = true, relaxUnitFun = true)

    private lateinit var viewModel: ScheduleViewModel

    @Before
    fun setUp() {

        mockkStatic("kotlinx.coroutines.AwaitKt")

        setupStrings()

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { userManager.getAllMissingSubmissionsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { plannerManager.getPlannerItemsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { missingItemsPrefs.itemsCollapsed } returns false

        mockkObject(ColorKeeper)
        every { ColorKeeper.getOrGenerateColor(any()) } returns ThemedColor(0)
        every { ColorKeeper.darkTheme } returns false
    }

    @Test
    fun `Open actions map correctly`() {
        val course = Course(id = 1)

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course))
        }

        val plannerItems = listOf(
            createPlannerItem(courseId = course.id, assignmentId = 1, PlannableType.ASSIGNMENT, SubmissionState(submitted = true), Date()),
            createPlannerItem(courseId = course.id, assignmentId = 2, PlannableType.QUIZ, SubmissionState(submitted = true), Date()),
            createPlannerItem(courseId = course.id, assignmentId = 3, PlannableType.ANNOUNCEMENT, SubmissionState(submitted = true), Date()),
            createPlannerItem(courseId = course.id, assignmentId = 4, PlannableType.DISCUSSION_TOPIC, SubmissionState(submitted = true), Date()),
        )

        every { plannerManager.getPlannerItemsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(plannerItems)
        }

        val assignments = listOf(
            createAssignment(3, 1),
            createAssignment(4, 1)
        ).map { DataResult.Success(it) }

        mockAssignments(assignments)

        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels

        val todayHeader = items?.find { it.dayText == "Today" }
        assert(todayHeader is ScheduleDayGroupItemViewModel)
        assertEquals("Today", todayHeader?.dayText)

        val courseItemViewModel = todayHeader?.items?.get(0) as ScheduleCourseItemViewModel

        courseItemViewModel.onHeaderClick.invoke()
        assertEquals(ScheduleAction.OpenCourse(course), viewModel.events.value?.getContentIfNotHandled())

        val assignment = courseItemViewModel.data.plannerItems.find { it.data.type == PlannerItemType.ASSIGNMENT }
        assignment?.open?.invoke()
        assertEquals(
            ScheduleAction.OpenAssignment(plannerItems[0].canvasContext, plannerItems[0].plannable.id),
            viewModel.events.value?.getContentIfNotHandled()
        )

        val quiz = courseItemViewModel.data.plannerItems.find { it.data.type == PlannerItemType.QUIZ }
        quiz?.open?.invoke()
        assertEquals(
            ScheduleAction.OpenAssignment(plannerItems[1].canvasContext, plannerItems[1].plannable.id),
            viewModel.events.value?.getContentIfNotHandled()
        )

        val announcement = courseItemViewModel.data.plannerItems.find { it.data.type == PlannerItemType.ANNOUNCEMENT }
        announcement?.open?.invoke()
        assertEquals(
            ScheduleAction.OpenDiscussion(
                plannerItems[2].canvasContext,
                plannerItems[2].plannable.id,
                plannerItems[2].plannable.title
            ), viewModel.events.value?.getContentIfNotHandled()
        )

        val discussion = courseItemViewModel.data.plannerItems.find { it.data.type == PlannerItemType.DISCUSSION }
        discussion?.open?.invoke()
        assertEquals(
            ScheduleAction.OpenDiscussion(
                plannerItems[3].canvasContext,
                plannerItems[3].plannable.id,
                plannerItems[3].plannable.title
            ), viewModel.events.value?.getContentIfNotHandled()
        )
    }

    @Test
    fun `Missing items set up correctly`() {
        val courses = listOf(
            Course(id = 1, name = "Course 1"),
            Course(id = 2, name = "Course 2")
        )

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        val missingItems = listOf(
            createAssignment(
                1,
                courseId = 1,
                createSubmission(id = 1, grade = null, late = false, excused = false),
                name = "Assignment 1",
                pointsPossible = 20.0
            ),
            createAssignment(
                2,
                courseId = 2,
                createSubmission(id = 2, grade = null, late = false, excused = false),
                name = "Assignment 2"
            )
        )

        every { userManager.getAllMissingSubmissionsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(missingItems)
        }

        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels

        val todayHeader = items?.find { it.dayText == "Today" }
        assert(todayHeader is ScheduleDayGroupItemViewModel)
        assertEquals("Today", todayHeader?.dayText)

        val missingItemHeader =
            todayHeader?.items?.find { it is ScheduleMissingItemsGroupItemViewModel } as ScheduleMissingItemsGroupItemViewModel
        assertEquals(2, missingItemHeader.items.size)

        val firstMissingItem = missingItemHeader.items[0] as ScheduleMissingItemViewModel
        assertEquals("Assignment 1", firstMissingItem.data.title)
        assertEquals("Course 1", firstMissingItem.data.courseName)
        assertEquals("20 pts", firstMissingItem.data.points)

        val secondMissingItem = missingItemHeader.items[1] as ScheduleMissingItemViewModel
        assertEquals("Assignment 2", secondMissingItem.data.title)
        assertEquals("Course 2", secondMissingItem.data.courseName)
    }

    @Test
    fun `Missing item points are not displayed if quantitative data is restricted`() {
        val courses = listOf(Course(id = 1, name = "Course 1", settings = CourseSettings(restrictQuantitativeData = true)),)

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        val missingItems = listOf(
            createAssignment(
                1,
                courseId = 1,
                createSubmission(id = 1, grade = null, late = false, excused = false),
                name = "Assignment 1",
                pointsPossible = 20.0
            )
        )

        every { userManager.getAllMissingSubmissionsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(missingItems)
        }

        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels

        val todayHeader = items?.find { it.dayText == "Today" }
        assert(todayHeader is ScheduleDayGroupItemViewModel)
        assertEquals("Today", todayHeader?.dayText)

        val missingItemHeader =
            todayHeader?.items?.find { it is ScheduleMissingItemsGroupItemViewModel } as ScheduleMissingItemsGroupItemViewModel
        assertEquals(1, missingItemHeader.items.size)

        val firstMissingItem = missingItemHeader.items.first() as ScheduleMissingItemViewModel
        assertEquals("Assignment 1", firstMissingItem.data.title)
        assertEquals("Course 1", firstMissingItem.data.courseName)
        assertEquals(null, firstMissingItem.data.points)
    }

    @Test
    fun `Missing items are open by default`() {
        val course = Course(id = 1)

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course))
        }

        val missingItems = listOf(
            createAssignment(1, courseId = 1, createSubmission(id = 1, grade = null, late = false, excused = false)),
            createAssignment(2, courseId = 1, createSubmission(id = 2, grade = null, late = false, excused = false))
        )

        every { userManager.getAllMissingSubmissionsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(missingItems)
        }

        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels

        val todayHeader = items?.find { it.dayText == "Today" }
        assert(todayHeader is ScheduleDayGroupItemViewModel)
        assertEquals("Today", todayHeader?.dayText)

        val missingItemHeader =
            todayHeader?.items?.find { it is ScheduleMissingItemsGroupItemViewModel } as ScheduleMissingItemsGroupItemViewModel
        assertEquals(false, missingItemHeader.collapsed)
    }

    @Test
    fun `Missing item header changes state correctly`() {
        val course = Course(id = 1)

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course))
        }

        val missingItems = listOf(
            createAssignment(1, courseId = 1, createSubmission(id = 1, grade = null, late = false, excused = false)),
            createAssignment(2, courseId = 1, createSubmission(id = 2, grade = null, late = false, excused = false))
        )

        every { userManager.getAllMissingSubmissionsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(missingItems)
        }

        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels

        val todayHeader = items?.find { it.dayText == "Today" }
        assert(todayHeader is ScheduleDayGroupItemViewModel)
        assertEquals("Today", todayHeader?.dayText)

        val missingItemHeader =
            todayHeader?.items?.find { it is ScheduleMissingItemsGroupItemViewModel } as ScheduleMissingItemsGroupItemViewModel
        assertEquals(false, missingItemHeader.collapsed)
        missingItemHeader.toggleItems()
        assertEquals(true, missingItemHeader.collapsed)
    }

    @Test
    fun `Only one missing item header is found`() {
        val course = Course(id = 1)

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course))
        }

        val missingItems = listOf(
            createAssignment(1, courseId = 1, createSubmission(id = 1, grade = null, late = false, excused = false)),
            createAssignment(2, courseId = 1, createSubmission(id = 2, grade = null, late = false, excused = false))
        )

        every { userManager.getAllMissingSubmissionsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(missingItems)
        }

        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels

        val todayHeader = items?.find { it.dayText == "Today" }
        assert(todayHeader is ScheduleDayGroupItemViewModel)
        assertEquals("Today", todayHeader?.dayText)

        items?.forEach { dayGroup ->
            if (dayGroup != todayHeader) {
                assertEquals(0, dayGroup.items.count { it is ScheduleMissingItemsGroupItemViewModel })
            } else {
                assertEquals(1, dayGroup.items.count { it is ScheduleMissingItemsGroupItemViewModel })
            }
        }
    }

    @Test
    fun `Missing items are not visible if there are none`() {
        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels

        val todayHeader = items?.find { it.dayText == "Today" }
        assert(todayHeader is ScheduleDayGroupItemViewModel)
        assertEquals("Today", todayHeader?.dayText)

        assertEquals(null, todayHeader?.items?.find { it is ScheduleMissingItemsGroupItemViewModel })
    }

    @Test
    fun `Courses map correctly`() {
        val courses = listOf(
            Course(id = 1, name = "Course 1"),
            Course(id = 2, name = "Course 2")
        )

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        val plannerItems = listOf(
            createToDoItem(1, "To Do item"),
            createPlannerItem(1, 1, PlannableType.ASSIGNMENT, SubmissionState(submitted = true), Date()),
            createPlannerItem(2, 2, PlannableType.ASSIGNMENT, SubmissionState(submitted = true), Date())
        )

        every { plannerManager.getPlannerItemsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(plannerItems)
        }

        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels

        val todayHeader = items?.find { it.dayText == "Today" }
        assert(todayHeader is ScheduleDayGroupItemViewModel)
        assertEquals("Today", todayHeader?.dayText)

        assertEquals(3, todayHeader?.items?.count { it is ScheduleCourseItemViewModel })

        val courseItemViewModels = todayHeader?.items?.filterIsInstance<ScheduleCourseItemViewModel>()
        val firstCourseItemViewModel = courseItemViewModels?.find { it.data.courseName == "Course 1" }
        assertEquals(true, firstCourseItemViewModel?.data?.openable)
        assertEquals(1, firstCourseItemViewModel?.data?.plannerItems?.size)
        assertEquals("Plannable 1", firstCourseItemViewModel?.data?.plannerItems?.get(0)?.data?.title)
        assertEquals(true, firstCourseItemViewModel?.data?.plannerItems?.get(0)?.data?.openable)

        val secondCourseItemViewModel = courseItemViewModels?.find { it.data.courseName == "Course 2" }
        assertEquals(true, secondCourseItemViewModel?.data?.openable)
        assertEquals(1, secondCourseItemViewModel?.data?.plannerItems?.size)
        assertEquals("Plannable 2", secondCourseItemViewModel?.data?.plannerItems?.get(0)?.data?.title)
        assertEquals(true, secondCourseItemViewModel?.data?.plannerItems?.get(0)?.data?.openable)

        val todoCourseItemViewModel = courseItemViewModels?.find { it.data.courseName == "To Do" }
        assertEquals(false, todoCourseItemViewModel?.data?.openable)
        assertEquals(1, todoCourseItemViewModel?.data?.plannerItems?.size)
        assertEquals("To Do item", todoCourseItemViewModel?.data?.plannerItems?.get(0)?.data?.title)
        assertEquals(false, todoCourseItemViewModel?.data?.plannerItems?.get(0)?.data?.openable)
    }

    @Test
    fun `Submitted items are marked as done`() {
        val course = Course(id = 1)

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course))
        }

        val plannerItems = listOf(
            createPlannerItem(
                courseId = course.id, assignmentId = 1, PlannableType.ASSIGNMENT, SubmissionState(submitted = true),
                Date()
            )
        )

        every { plannerManager.getPlannerItemsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(plannerItems)
        }

        every { plannerManager.createPlannerOverrideAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels

        val todayHeader = items?.find { it.dayText == "Today" }
        assert(todayHeader is ScheduleDayGroupItemViewModel)
        assertEquals("Today", todayHeader?.dayText)

        val courseItemViewModel = todayHeader?.items?.get(0) as ScheduleCourseItemViewModel

        assertEquals(true, courseItemViewModel.data.openable)

        assertEquals(1, courseItemViewModel.data.plannerItems.size)
        val plannerItemViewModel = courseItemViewModel.data.plannerItems[0]

        assertEquals(true, plannerItemViewModel.completed)
    }

    @Test
    fun `Mark item as done error`() {
        val course = Course(id = 1)

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course))
        }

        val plannerItems = listOf(
            createPlannerItem(
                courseId = course.id, assignmentId = 1, PlannableType.ASSIGNMENT, SubmissionState(submitted = false),
                Date()
            )
        )

        every { plannerManager.getPlannerItemsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(plannerItems)
        }

        every { plannerManager.createPlannerOverrideAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels

        val todayHeader = items?.find { it.dayText == "Today" }
        assert(todayHeader is ScheduleDayGroupItemViewModel)
        assertEquals("Today", todayHeader?.dayText)

        val courseItemViewModel = todayHeader?.items?.get(0) as ScheduleCourseItemViewModel

        assertEquals(true, courseItemViewModel.data.openable)

        assertEquals(1, courseItemViewModel.data.plannerItems.size)
        val plannerItemViewModel = courseItemViewModel.data.plannerItems[0]

        assertEquals(false, plannerItemViewModel.completed)
        plannerItemViewModel.markAsDone.invoke(plannerItemViewModel, true)
        assertEquals(false, plannerItemViewModel.completed)
    }

    @Test
    fun `Update item as not done`() {
        val course = Course(id = 1)

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course))
        }

        val plannerItems = listOf(
            createPlannerItem(
                courseId = course.id,
                assignmentId = 1,
                PlannableType.ASSIGNMENT,
                SubmissionState(submitted = true),
                Date(),
                createPlannerOverride(1, PlannableType.ASSIGNMENT, 1, true)
            )
        )

        every { plannerManager.getPlannerItemsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(plannerItems)
        }

        every { plannerManager.updatePlannerOverrideAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(createPlannerOverride(1, PlannableType.ASSIGNMENT, 1, false))
        }

        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels

        val todayHeader = items?.find { it.dayText == "Today" }
        assert(todayHeader is ScheduleDayGroupItemViewModel)
        assertEquals("Today", todayHeader?.dayText)

        val courseItemViewModel = todayHeader?.items?.get(0) as ScheduleCourseItemViewModel

        assertEquals(true, courseItemViewModel.data.openable)

        assertEquals(1, courseItemViewModel.data.plannerItems.size)
        val plannerItemViewModel = courseItemViewModel.data.plannerItems[0]

        assertEquals(true, plannerItemViewModel.completed)
        plannerItemViewModel.markAsDone.invoke(plannerItemViewModel, false)
        assertEquals(false, plannerItemViewModel.completed)
    }

    @Test
    fun `Mark item as done`() {
        val course = Course(id = 1)

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course))
        }

        val plannerItems = listOf(
            createPlannerItem(
                courseId = course.id,
                assignmentId = 1,
                PlannableType.ASSIGNMENT,
                SubmissionState(),
                Date()
            )
        )

        every { plannerManager.getPlannerItemsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(plannerItems)
        }

        every { plannerManager.createPlannerOverrideAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(createPlannerOverride(1, PlannableType.ASSIGNMENT, 1, true))
        }

        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels

        val todayHeader = items?.find { it.dayText == "Today" }
        assert(todayHeader is ScheduleDayGroupItemViewModel)
        assertEquals("Today", todayHeader?.dayText)

        val courseItemViewModel = todayHeader?.items?.get(0) as ScheduleCourseItemViewModel

        assertEquals(true, courseItemViewModel.data.openable)

        assertEquals(1, courseItemViewModel.data.plannerItems.size)
        val plannerItemViewModel = courseItemViewModel.data.plannerItems[0]

        assertEquals(false, plannerItemViewModel.completed)
        plannerItemViewModel.markAsDone.invoke(plannerItemViewModel, true)
        assertEquals(true, plannerItemViewModel.completed)
    }

    @Test
    fun `ToDo items map correctly`() {
        val plannerItems = listOf(
            createToDoItem(1, "To Do item")
        )

        every { plannerManager.getPlannerItemsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(plannerItems)
        }

        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels

        val todayHeader = items?.find { it.dayText == "Today" }
        assert(todayHeader is ScheduleDayGroupItemViewModel)
        assertEquals("Today", todayHeader?.dayText)

        val courseItem = todayHeader?.items?.get(0) as ScheduleCourseItemViewModel

        assertEquals("To Do", courseItem.data.courseName)
        assertEquals(1, courseItem.data.plannerItems.size)

        val plannerItemViewModel = courseItem.data.plannerItems[0]

        assertEquals("To Do item", plannerItemViewModel.data.title)
        assertEquals(false, plannerItemViewModel.data.openable)
        assertEquals(PlannerItemType.TO_DO, plannerItemViewModel.data.type)
    }

    @Test
    fun `Assignment maps correctly`() {
        val course = Course(id = 1)

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course))
        }

        val plannerItems = listOf(
            createPlannerItem(
                courseId = course.id,
                assignmentId = 1,
                PlannableType.ASSIGNMENT,
                SubmissionState(),
                Date(),
                pointsPossible = 20.0
            )
        )

        every { plannerManager.getPlannerItemsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(plannerItems)
        }

        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels

        val todayHeader = items?.find { it.dayText == "Today" }
        assert(todayHeader is ScheduleDayGroupItemViewModel)
        assertEquals("Today", todayHeader?.dayText)

        val courseItemViewModel = todayHeader?.items?.get(0) as ScheduleCourseItemViewModel

        assertEquals(true, courseItemViewModel.data.openable)

        assertEquals(1, courseItemViewModel.data.plannerItems.size)
        val plannerItemViewModel = courseItemViewModel.data.plannerItems[0]

        assertEquals("Plannable 1", plannerItemViewModel.data.title)
        assertEquals(true, plannerItemViewModel.data.openable)
        assertEquals(PlannerItemType.ASSIGNMENT, plannerItemViewModel.data.type)
        assertEquals("20 pts", plannerItemViewModel.data.points)
    }

    @Test
    fun `Assignment points are not displayed with restricted quantitative data`() {
        val course = Course(id = 1, settings = CourseSettings(restrictQuantitativeData = true))

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course))
        }

        val plannerItems = listOf(
            createPlannerItem(
                courseId = course.id,
                assignmentId = 1,
                PlannableType.ASSIGNMENT,
                SubmissionState(),
                Date(),
                pointsPossible = 20.0
            )
        )

        every { plannerManager.getPlannerItemsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(plannerItems)
        }

        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels

        val todayHeader = items?.find { it.dayText == "Today" }
        assert(todayHeader is ScheduleDayGroupItemViewModel)
        assertEquals("Today", todayHeader?.dayText)

        val courseItemViewModel = todayHeader?.items?.get(0) as ScheduleCourseItemViewModel

        assertEquals(true, courseItemViewModel.data.openable)

        assertEquals(1, courseItemViewModel.data.plannerItems.size)
        val plannerItemViewModel = courseItemViewModel.data.plannerItems[0]

        assertEquals("Plannable 1", plannerItemViewModel.data.title)
        assertEquals(true, plannerItemViewModel.data.openable)
        assertEquals(PlannerItemType.ASSIGNMENT, plannerItemViewModel.data.type)
        assertEquals(null, plannerItemViewModel.data.points)
    }

    @Test
    fun `Day titles set up correctly`() {
        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels

        assertEquals(7, items?.size)

        val todayHeader = items?.find { it.dayText == "Today" }
        assert(todayHeader is ScheduleDayGroupItemViewModel)
        assertEquals("Today", todayHeader?.dayText)
        val todayIndex = items!!.indexOf(todayHeader)

        if (todayIndex != 0) {
            val yesterdayHeader = viewModel.data.value?.itemViewModels?.get(todayIndex - 1)
            val yesterdayHeaderItemViewModel = yesterdayHeader as ScheduleDayGroupItemViewModel
            assertEquals("Yesterday", yesterdayHeaderItemViewModel.dayText)
        }

        if (todayIndex != 6) {
            val tomorrowHeader = viewModel.data.value?.itemViewModels?.get(todayIndex + 1)
            val tomorrowHeaderItemViewModel = tomorrowHeader as ScheduleDayGroupItemViewModel
            assertEquals("Tomorrow", tomorrowHeaderItemViewModel.dayText)
        }
    }

    @Test
    fun `Empty view`() {
        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels

        assertEquals(7, items?.size)

        items?.forEach {
            assertEquals(1, it.items.size)
            assert(it.items[0] is ScheduleEmptyItemViewModel)
        }

    }

    @Test
    fun `Chips are set correctly`() {
        val course = Course(id = 1)

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course))
        }

        val plannerItems = listOf(
            createPlannerItem(course.id, 1, PlannableType.ASSIGNMENT, SubmissionState(late = true), Date()),
            createPlannerItem(course.id, 2, PlannableType.ASSIGNMENT, SubmissionState(graded = true), Date()),
            createPlannerItem(
                course.id,
                3,
                PlannableType.ASSIGNMENT,
                SubmissionState(excused = true, graded = true),
                Date()
            ),
            createPlannerItem(
                course.id,
                4,
                PlannableType.ASSIGNMENT,
                SubmissionState(graded = true, late = true),
                Date()
            ),
            createPlannerItem(course.id, 5, PlannableType.ANNOUNCEMENT, SubmissionState(), Date(), newActivity = true),
            createPlannerItem(
                course.id,
                6,
                PlannableType.DISCUSSION_TOPIC,
                SubmissionState(late = true, excused = true, withFeedback = true),
                Date(),
                newActivity = true
            )
        )

        every { plannerManager.getPlannerItemsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(plannerItems)
        }

        val assignments = listOf(
            createAssignment(5, 1, discussionTopicHeader = DiscussionTopicHeader(4, unreadCount = 2)),
            createAssignment(6, 1, discussionTopicHeader = DiscussionTopicHeader(5, unreadCount = 1))
        ).map { DataResult.Success(it) }

        mockAssignments(assignments)

        viewModel = createViewModel()
        viewModel.getDataForDate(Date().toApiString())
        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner, {})

        val items = viewModel.data.value?.itemViewModels
        val dayGroup = items?.find { it.dayText == "Today" }
        assertEquals("Today", dayGroup?.dayText)

        val courseItem = dayGroup?.items?.get(0)
        assert(courseItem is ScheduleCourseItemViewModel)
        val courseItemViewModel = courseItem as ScheduleCourseItemViewModel
        assertEquals(6, courseItemViewModel.data.plannerItems.size)

        val latePlannerItem = courseItem.data.plannerItems[0]
        assertEquals(1, latePlannerItem.data.chips.size)
        assert(latePlannerItem.data.chips.any { it.data.text == "Late" })

        val gradedPlannerItem = courseItem.data.plannerItems[1]
        assertEquals(1, gradedPlannerItem.data.chips.size)
        assert(gradedPlannerItem.data.chips.any { it.data.text == "Graded" })

        val excusedPlannerItem = courseItem.data.plannerItems[2]
        assertEquals(1, excusedPlannerItem.data.chips.size)
        assert(excusedPlannerItem.data.chips.any { it.data.text == "Excused" })

        val gradedLatePlannerItem = courseItem.data.plannerItems[3]
        assertEquals(2, gradedLatePlannerItem.data.chips.size)
        assert(
            gradedLatePlannerItem.data.chips.any { it.data.text == "Graded" }
                    && gradedLatePlannerItem.data.chips.any { it.data.text == "Late" }
        )

        val unreadPlannerItem = courseItem.data.plannerItems[4]
        assertEquals(1, unreadPlannerItem.data.chips.size)
        assert(unreadPlannerItem.data.chips.any { it.data.text == "2 Replies" })

        val unreadGradedLateExcusedPlannerItem = courseItem.data.plannerItems[5]
        assertEquals(4, unreadGradedLateExcusedPlannerItem.data.chips.size)
        assert(
            unreadGradedLateExcusedPlannerItem.data.chips.any { it.data.text == "1 Reply" }
                    && unreadGradedLateExcusedPlannerItem.data.chips.any { it.data.text == "Late" }
                    && unreadGradedLateExcusedPlannerItem.data.chips.any { it.data.text == "Excused" }
                    && unreadGradedLateExcusedPlannerItem.data.chips.any { it.data.text == "Feedback" }
        )

    }

    private fun createPlannerItem(
        courseId: Long,
        assignmentId: Long,
        plannableType: PlannableType,
        submissionState: SubmissionState,
        date: Date,
        plannerOverride: PlannerOverride? = null,
        newActivity: Boolean = false,
        todoDate: String? = null,
        pointsPossible: Double? = null
    ): PlannerItem {
        val plannable = Plannable(
            id = assignmentId,
            title = "Plannable $assignmentId",
            courseId,
            null,
            null,
            pointsPossible,
            date,
            assignmentId,
            todoDate,
            null,
            null,
            null,
            null
        )
        return PlannerItem(
            courseId,
            null,
            null,
            null,
            null,
            plannableType,
            plannable,
            date,
            null,
            submissionState,
            plannerOverride = plannerOverride,
            newActivity = newActivity
        )
    }

    private fun createPlannerOverride(
        id: Long,
        plannableType: PlannableType,
        plannableId: Long,
        markedAsComplete: Boolean
    ): PlannerOverride {
        return PlannerOverride(
            id = id,
            plannableType = plannableType,
            plannableId = plannableId,
            markedComplete = markedAsComplete
        )
    }

    private fun createAssignment(
        id: Long,
        courseId: Long,
        submission: Submission? = null,
        discussionTopicHeader: DiscussionTopicHeader? = null,
        name: String? = null,
        pointsPossible: Double? = null
    ): Assignment {
        return Assignment(
            id = id,
            submission = submission,
            discussionTopicHeader = discussionTopicHeader,
            courseId = courseId,
            name = name,
            pointsPossible = pointsPossible ?: 0.0
        )
    }

    private fun createSubmission(id: Long, grade: String?, late: Boolean, excused: Boolean): Submission {
        return Submission(id = id, grade = grade, late = late, excused = excused)
    }

    private fun createToDoItem(id: Long, title: String): PlannerItem {
        val plannable = Plannable(id = id, title = title, null, null, null, null, Date(), null, null, null, null, null, null)
        return PlannerItem(
            plannable = plannable,
            plannableType = PlannableType.PLANNER_NOTE,
            plannableDate = Date(),
            courseId = null,
            contextName = null,
            contextType = null,
            groupId = null,
            htmlUrl = null,
            plannerOverride = null,
            submissionState = null,
            userId = null,
            newActivity = false
        )
    }

    private fun setupStrings() {
        every { resources.getString(R.string.schedule_tag_graded) } returns "Graded"
        every { resources.getString(R.plurals.schedule_tag_replies) } returns "Replies"
        every { resources.getString(R.string.schedule_tag_feedback) } returns "Feedback"
        every { resources.getString(R.string.schedule_tag_late) } returns "Late"
        every { resources.getString(R.string.schedule_tag_redo) } returns "Redo"
        every { resources.getString(R.string.schedule_tag_excused) } returns "Excused"
        every { resources.getString(R.string.tomorrow) } returns "Tomorrow"
        every { resources.getString(R.string.yesterday) } returns "Yesterday"
        every { resources.getString(R.string.today) } returns "Today"
        every { resources.getString(R.string.schedule_todo_title) } returns "To Do"
        every { resources.getQuantityString(R.plurals.schedule_tag_replies, 2, 2) } returns "2 Replies"
        every { resources.getQuantityString(R.plurals.schedule_tag_replies, 1, 1) } returns "1 Reply"
        every { resources.getQuantityString(R.plurals.schedule_points, 20, "20") } returns "20 pts"
    }

    private fun createViewModel(): ScheduleViewModel {
        return ScheduleViewModel(
            apiPrefs,
            resources,
            plannerManager,
            courseManager,
            userManager,
            calendarEventManager,
            assignmentManager,
            missingItemsPrefs,
            dateTimeProvider,
            colorKeeper
        )
    }

    private fun mockAssignments(assignments: List<DataResult<Assignment>> = emptyList()) {
        val assignmentDeferred: Deferred<DataResult<Assignment>> = mockk()
        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns assignmentDeferred
        val listOfAssignmentDeferred = assignments.map { assignmentDeferred }
        coEvery { listOfAssignmentDeferred.awaitAll() } returns assignments
    }

    private fun mockCalendarEvents(calendarEvents: List<DataResult<ScheduleItem>> = emptyList()) {
        val calendarEventDeferred: Deferred<DataResult<ScheduleItem>> = mockk()
        every { calendarEventManager.getCalendarEventAsync(any(), any()) } returns calendarEventDeferred
        val listOfCalendarEventDeferred = calendarEvents.map { calendarEventDeferred }
        coEvery { listOfCalendarEventDeferred.awaitAll() } returns calendarEvents
    }
}
