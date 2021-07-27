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
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.PlannerManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.homeroom.HomeroomViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleCourseItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleDayHeaderItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleEmptyItemViewModel
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleMissingItemsHeaderItemViewModel
import com.instructure.pandautils.mvvm.Event
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalCoroutinesApi
class ScheduleViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = TestCoroutineDispatcher()

    private val plannerManager: PlannerManager = mockk(relaxed = true)
    private val courseManager: CourseManager = mockk(relaxed = true)
    private val userManager: UserManager = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private lateinit var viewModel: ScheduleViewModel

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

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

        viewModel = createViewModel()

        viewModel.data.observe(lifecycleOwner, {})

        val courseItemViewModel =
            viewModel.data.value?.itemViewModels?.find { it is ScheduleCourseItemViewModel } as ScheduleCourseItemViewModel

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
    fun `Today button only visible for the other days`() {
        viewModel = createViewModel()

        viewModel.data.observe(lifecycleOwner, {})

        val headerItems = viewModel.data.value?.itemViewModels?.filterIsInstance<ScheduleDayHeaderItemViewModel>()
        assertEquals(6, headerItems?.count { it.todayVisible })

        val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)
        assertEquals(false, headerItems?.get(todayIndex)?.todayVisible)
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
                name = "Assignment 1"
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

        viewModel.data.observe(lifecycleOwner, {})

        val missingItemHeader =
            viewModel.data.value?.itemViewModels?.find { it is ScheduleMissingItemsHeaderItemViewModel } as ScheduleMissingItemsHeaderItemViewModel
        assertEquals(2, missingItemHeader.items.size)

        val firstMissingItem = missingItemHeader.items[0]
        assertEquals("Assignment 1", firstMissingItem.data.title)
        assertEquals("Course 1", firstMissingItem.data.courseName)

        val secondMissingItem = missingItemHeader.items[1]
        assertEquals("Assignment 2", secondMissingItem.data.title)
        assertEquals("Course 2", secondMissingItem.data.courseName)
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

        viewModel.data.observe(lifecycleOwner, {})

        assertEquals(1, viewModel.data.value?.itemViewModels?.count { it is ScheduleMissingItemsHeaderItemViewModel })
        val missingItemHeader =
            viewModel.data.value?.itemViewModels?.find { it is ScheduleMissingItemsHeaderItemViewModel } as ScheduleMissingItemsHeaderItemViewModel
        assertEquals(true, missingItemHeader.collapsed)
        missingItemHeader.toggleItems()
        assertEquals(false, missingItemHeader.collapsed)
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

        viewModel.data.observe(lifecycleOwner, {})

        assertEquals(1, viewModel.data.value?.itemViewModels?.count { it is ScheduleMissingItemsHeaderItemViewModel })
    }

    @Test
    fun `Missing items are not visible if there are none`() {
        viewModel = createViewModel()

        viewModel.data.observe(lifecycleOwner, {})

        assertEquals(14, viewModel.data.value?.itemViewModels?.size)
        assertEquals(null, viewModel.data.value?.itemViewModels?.find { it is ScheduleMissingItemsHeaderItemViewModel })
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

        viewModel.data.observe(lifecycleOwner, {})

        assertEquals(3, viewModel.data.value?.itemViewModels?.count { it is ScheduleCourseItemViewModel })

        val courseItemViewModels = viewModel.data.value?.itemViewModels?.filterIsInstance<ScheduleCourseItemViewModel>()
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
    fun `Mark item as done error`() {
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

        viewModel.data.observe(lifecycleOwner, {})

        val courseItemViewModel =
            viewModel.data.value?.itemViewModels?.find { it is ScheduleCourseItemViewModel } as ScheduleCourseItemViewModel

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

        viewModel.data.observe(lifecycleOwner, {})

        val courseItemViewModel =
            viewModel.data.value?.itemViewModels?.find { it is ScheduleCourseItemViewModel } as ScheduleCourseItemViewModel

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

        viewModel.data.observe(lifecycleOwner, {})

        val courseItemViewModel =
            viewModel.data.value?.itemViewModels?.find { it is ScheduleCourseItemViewModel } as ScheduleCourseItemViewModel

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

        viewModel.data.observe(lifecycleOwner, {})

        val courseItem =
            viewModel.data.value?.itemViewModels?.find { it is ScheduleCourseItemViewModel } as ScheduleCourseItemViewModel

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
                Date()
            )
        )

        every { plannerManager.getPlannerItemsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(plannerItems)
        }

        viewModel = createViewModel()

        viewModel.data.observe(lifecycleOwner, {})

        val courseItemViewModel =
            viewModel.data.value?.itemViewModels?.find { it is ScheduleCourseItemViewModel } as ScheduleCourseItemViewModel

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

        viewModel.data.observe(lifecycleOwner, {})

        assertEquals(14, viewModel.data.value?.itemViewModels?.size)

        val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1) * 2
        val todayHeader = viewModel.data.value?.itemViewModels?.get(todayIndex)
        assert(todayHeader is ScheduleDayHeaderItemViewModel)
        val todayHeaderItemViewModel = todayHeader as ScheduleDayHeaderItemViewModel
        assertEquals("Today", todayHeaderItemViewModel.dayText)

        if (todayIndex != 0) {
            val yesterdayHeader = viewModel.data.value?.itemViewModels?.get(todayIndex - 2)
            val yesterdayHeaderItemViewModel = yesterdayHeader as ScheduleDayHeaderItemViewModel
            assertEquals("Yesterday", yesterdayHeaderItemViewModel.dayText)
        }

        if (todayIndex != 12) {
            val tomorrowHeader = viewModel.data.value?.itemViewModels?.get(todayIndex + 2)
            val tomorrowHeaderItemViewModel = tomorrowHeader as ScheduleDayHeaderItemViewModel
            assertEquals("Tomorrow", tomorrowHeaderItemViewModel.dayText)
        }
    }

    @Test
    fun `Empty view`() {
        viewModel = createViewModel()

        viewModel.data.observe(lifecycleOwner, {})

        assertEquals(14, viewModel.data.value?.itemViewModels?.size)

        assertEquals(7, viewModel.data.value?.itemViewModels?.count { it is ScheduleDayHeaderItemViewModel })
        assertEquals(7, viewModel.data.value?.itemViewModels?.count { it is ScheduleEmptyItemViewModel })
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
            createPlannerItem(course.id, 3, PlannableType.ASSIGNMENT, SubmissionState(excused = true, graded = true), Date()),
            createPlannerItem(course.id, 4, PlannableType.ASSIGNMENT, SubmissionState(graded = true, late = true), Date()),
            createPlannerItem(course.id, 5, PlannableType.ANNOUNCEMENT, SubmissionState(), Date(), newActivity = true),
            createPlannerItem(course.id, 6, PlannableType.DISCUSSION_TOPIC, SubmissionState(late = true, excused = true, withFeedback = true), Date(), newActivity = true)
        )

        every { plannerManager.getPlannerItemsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(plannerItems)
        }

        viewModel = createViewModel()

        viewModel.data.observe(lifecycleOwner, {})

        val dayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1) * 2
        val dayHeader = viewModel.data.value?.itemViewModels?.get(dayIndex)
        assert(dayHeader is ScheduleDayHeaderItemViewModel)
        val dayItemViewModel = dayHeader as ScheduleDayHeaderItemViewModel
        assertEquals(SimpleDateFormat("MMMM dd", Locale.getDefault()).format(Date()), dayItemViewModel.dateText)

        val courseItem = viewModel.data.value?.itemViewModels?.get(dayIndex + 1)
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
        assert(unreadPlannerItem.data.chips.any { it.data.text == "Replies" })

        val unreadGradedLateExcusedPlannerItem = courseItem.data.plannerItems[5]
        assertEquals(4, unreadGradedLateExcusedPlannerItem.data.chips.size)
        assert(
            unreadGradedLateExcusedPlannerItem.data.chips.any { it.data.text == "Replies" }
                    && unreadGradedLateExcusedPlannerItem.data.chips.any { it.data.text == "Late" }
                    && unreadGradedLateExcusedPlannerItem.data.chips.any { it.data.text == "Excused" }
                    && unreadGradedLateExcusedPlannerItem.data.chips.any { it.data.text == "Feedback" }
        )

    }

    private fun createPlannerItem(courseId: Long, assignmentId: Long, plannableType: PlannableType, submissionState: SubmissionState, date: Date, plannerOverride: PlannerOverride? = null, newActivity: Boolean = false): PlannerItem {
        val plannable = Plannable(id = assignmentId, title = "Plannable $assignmentId", courseId, null, null, null, date, assignmentId)
        return PlannerItem(courseId, null, null, null, null, plannableType, plannable, date, null, submissionState, plannerOverride = plannerOverride, newActivity = newActivity)
    }

    private fun createPlannerOverride(id: Long, plannableType: PlannableType, plannableId: Long, markedAsComplete: Boolean): PlannerOverride {
        return PlannerOverride(id = id, plannableType = plannableType, plannableId = plannableId, markedComplete = markedAsComplete)
    }

    private fun createAssignment(id: Long, courseId: Long, submission: Submission? = null, discussionTopicHeader: DiscussionTopicHeader? = null, name: String? = null): Assignment {
        return Assignment(id = id, submission = submission, discussionTopicHeader = discussionTopicHeader, courseId = courseId, name = name)
    }

    private fun createSubmission(id: Long, grade: String?, late: Boolean, excused: Boolean): Submission {
        return Submission(id = id, grade = grade, late = late, excused = excused)
    }

    private fun createToDoItem(id: Long, title: String): PlannerItem {
        val plannable = Plannable(id = id, title = title, null, null, null, null, Date(), null)
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
        every { resources.getString(R.string.schedule_tag_replies) } returns "Replies"
        every { resources.getString(R.string.schedule_tag_feedback) } returns "Feedback"
        every { resources.getString(R.string.schedule_tag_late) } returns "Late"
        every { resources.getString(R.string.schedule_tag_redo) } returns "Redo"
        every { resources.getString(R.string.schedule_tag_excused) } returns "Excused"
        every { resources.getString(R.string.tomorrow) } returns "Tomorrow"
        every { resources.getString(R.string.yesterday) } returns "Yesterday"
        every { resources.getString(R.string.today) } returns "Today"
        every { resources.getString(R.string.schedule_todo_title) } returns "To Do"
    }

    private fun createViewModel(): ScheduleViewModel {
        return ScheduleViewModel(apiPrefs, resources, plannerManager, courseManager, userManager)
    }
}