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
package com.instructure.horizon.features.notification

import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.StreamAPI
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.StreamItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NotificationRepositoryTest {
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val streamApi: StreamAPI.StreamInterface = mockk(relaxed = true)
    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface = mockk(relaxed = true)
    private val getCoursesManager: HorizonGetCoursesManager = mockk(relaxed = true)

    private val userId = 1L
    @Before
    fun setup() {
        every { apiPrefs.user?.id } returns userId
    }

    @Test
    fun `Test successful Stream API notification items filtering`() = runTest {
        val validCourse = CourseWithProgress(
            courseId = 1L,
            courseName = "Course 1",
            courseSyllabus = "",
            progress = 5.0
        )
        val notValidCourse = CourseWithProgress(
            courseId = 2L,
            courseName = "Course 2",
            courseSyllabus = "",
            progress = 0.0
        )
        val conversationStreamItem = StreamItem(
            type = "Conversation",
            course_id = validCourse.courseId,
        )
        val messageStreamItem = StreamItem(
            type = "Message",
            course_id = validCourse.courseId,
        )
        val discussionStreamItem = StreamItem(
            type = "DiscussionTopic",
            course_id = validCourse.courseId,
        )
        val announcementStreamItem = StreamItem(
            type = "Announcement",
            course_id = validCourse.courseId,
        )
        val notValidAnnouncementStreamItem = StreamItem(
            type = "Announcement",
            course_id = notValidCourse.courseId,
        )
        val dueDateStreamItem = StreamItem(
            type = "Message",
            notificationCategory = "Due Date",
            course_id = validCourse.courseId,
        )
        val scoredGradeStreamItem = StreamItem(
            type = "Message",
            grade = "A",
            course_id = validCourse.courseId,
        )
        val scoredScoreStreamItem = StreamItem(
            type = "Message",
            score = 95.0,
            course_id = validCourse.courseId,
        )
        val gradingPeriodStreamItem = StreamItem(
            type = "Message",
            notificationCategory = "Grading Policies",
            course_id = validCourse.courseId,
        )
        val streamItems = listOf(conversationStreamItem, messageStreamItem, discussionStreamItem, announcementStreamItem, notValidAnnouncementStreamItem, dueDateStreamItem, scoredGradeStreamItem, scoredScoreStreamItem, gradingPeriodStreamItem)

        coEvery { streamApi.getUserStream(any()) } returns DataResult.Success(streamItems)
        coEvery { getCoursesManager.getCoursesWithProgress(any(), any()) } returns DataResult.Success(listOf(validCourse))

        val result = getRepository().getNotifications(forceRefresh = true)

        assertEquals(4, result.size)
        assertFalse(result.contains(announcementStreamItem))
        assertTrue(result.contains(dueDateStreamItem))
        assertTrue(result.contains(scoredGradeStreamItem))
        assertTrue(result.contains(scoredScoreStreamItem))
        assertTrue(result.contains(gradingPeriodStreamItem))
    }

    @Test(expected = IllegalStateException::class)
    fun `Test failed Stream API call`() = runTest {
        coEvery { getCoursesManager.getCoursesWithProgress(any(), any()) } returns DataResult.Success(emptyList())
        coEvery { streamApi.getUserStream(any()) } returns DataResult.Fail()
        getRepository().getNotifications(true)
    }

    @Test
    fun `Test successful getCourse by id`() = runTest {
        val id = 1L
        val course = Course(id = id, name = "Course 1")
        coEvery { courseApi.getCourse(id, any()) } returns DataResult.Success(course)
        val result = getRepository().getCourse(id)
        assertEquals(course, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Test failed getCourse by id`() = runTest {
        coEvery { courseApi.getCourse(any(), any()) } returns DataResult.Fail()
        getRepository().getCourse(1L)
    }

    private fun getRepository(): NotificationRepository {
        return NotificationRepository(apiPrefs, streamApi, courseApi, getCoursesManager)
    }
}