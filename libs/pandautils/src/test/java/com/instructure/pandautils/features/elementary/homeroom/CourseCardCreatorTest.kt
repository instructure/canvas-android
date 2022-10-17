/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.features.elementary.homeroom

import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import com.instructure.canvasapi2.managers.AnnouncementManager
import com.instructure.canvasapi2.managers.PlannerManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.mvvm.Event
import io.mockk.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import java.util.*

@ExperimentalCoroutinesApi
class CourseCardCreatorTest {

    private val plannerManager: PlannerManager = mockk(relaxed = true)
    private val userManager: UserManager = mockk(relaxed = true)
    private val announcementManager: AnnouncementManager = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val events: MutableLiveData<Event<HomeroomAction>> = mockk(relaxed = true)

    private val courseCardCreator = CourseCardCreator(plannerManager, userManager, announcementManager, resources)

    @Before
    fun setUp() {
        mockkStatic("kotlinx.coroutines.AwaitKt")

        val announcementsDeferred: Deferred<DataResult<List<DiscussionTopicHeader>>> = mockk()
        every { announcementManager.getLatestAnnouncementAsync(any(), any()) } returns announcementsDeferred
        coEvery { listOf(announcementsDeferred).awaitAll() } returns listOf(DataResult.Success(emptyList()))

        every { plannerManager.getPlannerItemsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { userManager.getAllMissingSubmissionsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }
    }

    @Test
    fun `Planner items should be requested only for today`() = runBlockingTest {
        // Given
        val today = LocalDate.now()
        val todayString = today.toApiString()
        val tomorrowString = today.plusDays(1).toApiString()

        // When
        courseCardCreator.createCourseCards(emptyList(), true, false, events)

        // Then
        verify { plannerManager.getPlannerItemsAsync(any(), eq(todayString!!), eq(tomorrowString!!)) }
    }

    @Test
    fun `Force network call for planner items and missing assignments when updating assignment info`() = runBlockingTest {
        // Given
        val updateAssignments = true

        // When
        courseCardCreator.createCourseCards(emptyList(), false, updateAssignments, events)

        // Then
        verify { plannerManager.getPlannerItemsAsync(true, any(), any()) }
        verify { userManager.getAllMissingSubmissionsAsync(true) }
    }

    @Test
    fun `Create course card with correct course name, color and image url`() = runBlockingTest {
        // Given
        val courses = listOf(Course(id = 1, name = "Test course", courseColor = "#123456", imageUrl = "www.imageurl.com"))

        // When
        val courseCards = courseCardCreator.createCourseCards(courses, false, false, events)

        // Then
        assertEquals(1, courseCards.size)
        assertEquals("Test course", courseCards[0].data.courseName)
        assertEquals("#123456", courseCards[0].data.color)
        assertEquals("www.imageurl.com", courseCards[0].data.imageUrl)
    }

    @Test
    fun `Create course card with the first announcement of the course`() = runBlockingTest {
        // Given
        val courses = listOf(Course(id = 1))

        val announcements = listOf(DiscussionTopicHeader(title = "First"), DiscussionTopicHeader(title = "Second"))
        val announcementsDeferred: Deferred<DataResult<List<DiscussionTopicHeader>>> = mockk()
        every { announcementManager.getLatestAnnouncementAsync(courses[0], any()) } returns announcementsDeferred
        coEvery { listOf(announcementsDeferred).awaitAll() } returns listOf(DataResult.Success(announcements))

        // When
        val courseCards = courseCardCreator.createCourseCards(courses, false, false, events)

        // Then
        assertEquals(1, courseCards.size)
        assertEquals("First", courseCards[0].data.announcementText)
    }

    @Test
    fun `Create course card with due today text from the not submitted and not missing assignments due today`() = runBlockingTest {
        // Given
        val courses = listOf(Course(id = 1), Course(id = 2))

        val plannerItems = listOf(
            createPlannerItem(1, PlannableType.ASSIGNMENT, false, false),
            createPlannerItem(1, PlannableType.TODO, false, false),
            createPlannerItem(1, PlannableType.ASSIGNMENT, false, true),
            createPlannerItem(1, PlannableType.ASSIGNMENT, true, false),
            createPlannerItem(2, PlannableType.ASSIGNMENT, false, false),
            createPlannerItem(2, PlannableType.ASSIGNMENT, false, false),
            createPlannerItem(3, PlannableType.ASSIGNMENT, false, false),
        )

        val announcementsDeferred: Deferred<DataResult<List<DiscussionTopicHeader>>> = mockk()
        every { announcementManager.getLatestAnnouncementAsync(any(), any()) } returns announcementsDeferred
        coEvery { listOf(announcementsDeferred, announcementsDeferred).awaitAll() } returns listOf(DataResult.Success(emptyList()), DataResult.Success(emptyList()))

        every { plannerManager.getPlannerItemsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(plannerItems)
        }

        every { resources.getString(eq(R.string.dueToday), any()) } answers { call -> "${(call.invocation.args[1] as Array<Any>)[0]} due today" }

        // When
        val courseCards = courseCardCreator.createCourseCards(courses, false, false, events)

        // Then
        assertEquals(2, courseCards.size)
        assertEquals("1 due today", courseCards[0].data.assignmentsDueText)
        assertEquals("2 due today", courseCards[1].data.assignmentsDueText)
    }

    @Test
    fun `Create course card with nothing due today text if user don't have due today assignments for courses`() = runBlockingTest {
        // Given
        val courses = listOf(Course(id = 1))

        val plannerItems = listOf(
            createPlannerItem(1, PlannableType.TODO, false, false),
            createPlannerItem(1, PlannableType.ASSIGNMENT, false, true),
            createPlannerItem(1, PlannableType.ASSIGNMENT, true, false),
            createPlannerItem(2, PlannableType.ASSIGNMENT, false, false),
        )

        every { plannerManager.getPlannerItemsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(plannerItems)
        }

        every { resources.getString(eq(R.string.nothingDueToday)) } returns "Nothing due today"

        // When
        val courseCards = courseCardCreator.createCourseCards(courses, false, false, events)

        // Then
        assertEquals(1, courseCards.size)
        assertEquals("Nothing due today", courseCards[0].data.assignmentsDueText)
    }

    @Test
    fun `Create course card with missing text from the missing submissions that are not dismissed`() = runBlockingTest {
        // Given
        val courses = listOf(Course(id = 1))

        val assignments = listOf(
            Assignment(id = 1, courseId = 1),
            Assignment(id = 2, courseId = 2),
            Assignment(id = 3, courseId = 1, plannerOverride = PlannerOverride(plannableId = 3, plannableType = PlannableType.ASSIGNMENT, dismissed = false)),
            Assignment(id = 4, courseId = 1, plannerOverride = PlannerOverride(plannableId = 4, plannableType = PlannableType.ASSIGNMENT, dismissed = true)))
        every { userManager.getAllMissingSubmissionsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(assignments)
        }

        every { resources.getString(eq(R.string.missing), any()) } answers { call -> "${(call.invocation.args[1] as Array<Any>)[0]} missing" }

        // When
        val courseCards = courseCardCreator.createCourseCards(courses, false, false, events)

        // Then
        assertEquals(1, courseCards.size)
        assertEquals("2 missing", courseCards[0].data.assignmentsMissingText)
    }

    @Test
    fun `Create course card without missing text if no assignment is missing for the course`() = runBlockingTest {
        // Given
        val courses = listOf(Course(id = 1))

        val assignments = listOf(
            Assignment(id = 1, courseId = 2),
            Assignment(id = 4, courseId = 1, plannerOverride = PlannerOverride(plannableId = 4, plannableType = PlannableType.ASSIGNMENT, dismissed = true)))
        every { userManager.getAllMissingSubmissionsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(assignments)
        }

        // When
        val courseCards = courseCardCreator.createCourseCards(courses, false, false, events)

        // Then
        assertEquals(1, courseCards.size)
        assertEquals("", courseCards[0].data.assignmentsMissingText)
    }

    @Test
    fun `Create course card with default color if no color is set`() = runBlockingTest {
        // Given
        val courses = listOf(Course(id = 1, name = "Test course"))

        // When
        val courseCards = courseCardCreator.createCourseCards(courses, false, false, events)

        // Then
        assertEquals(1, courseCards.size)
        assertEquals("#394B58", courseCards[0].data.color)
    }

    private fun createPlannerItem(courseId: Long, plannableType: PlannableType, submitted: Boolean, missing: Boolean): PlannerItem {
        val plannable = mockk<Plannable>()
        return PlannerItem(courseId, null, null, null, null, plannableType, plannable, Date(), null, SubmissionState(submitted, missing), false)
    }
}