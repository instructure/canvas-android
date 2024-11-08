package com.instructure.parentapp.features.alerts.details

import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.lang.IllegalStateException
import java.time.Instant
import java.util.Date

class AnnouncementDetailsRepositoryTest {

    private val announcementApi: AnnouncementAPI.AnnouncementInterface = mockk(relaxed = true)
    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface = mockk(relaxed = true)

    private lateinit var announcementDetailsRepository: AnnouncementDetailsRepository

    private val courseTestResponse = Course(
        id = 10,
        name = "Course Name"
    )

    private val courseAnnouncementTestResponse = DiscussionTopicHeader(
        id = 1,
        title = "Alert Title",
        message = "Alert Message",
        postedDate = Date.from(
            Instant.parse("2024-01-03T00:00:00Z")
        ),
        attachments = mutableListOf(
            RemoteFile(
                id = 1,
                fileName = "attachment_file_name",
                size = 100,
                displayName = "File Name",
                thumbnailUrl = "thumbnail_url"
            )
        )
    )

    private val globalAnnouncementTestResponse = AccountNotification(
        id = 2,
        subject = "Alert Title",
        message = "Alert Message",
        startAt = "2024-01-03T00:00:00Z"
    )

    @Before
    fun setup() {
        createRepository()

        coEvery { courseApi.getCourse(any(), any()) } returns DataResult.Success(courseTestResponse)
        coEvery { announcementApi.getCourseAnnouncement(any(), any(), any()) } returns DataResult.Success(courseAnnouncementTestResponse)
        coEvery { accountNotificationApi.getAccountNotification(any(), any()) } returns DataResult.Success(globalAnnouncementTestResponse)
    }

    @Test
    fun `getCourse should return proper data`() = runTest {
        val result = announcementDetailsRepository.getCourse(1L, false)
        assertEquals(courseTestResponse, result)
    }

    @Test
    fun `getCourseAnnouncement should return proper data`() = runTest {
        val result = announcementDetailsRepository.getCourseAnnouncement(1L, 1L, false)
        assertEquals(courseAnnouncementTestResponse, result)
    }

    @Test
    fun `getGlobalAnnouncement should return proper data`() = runTest {
        val result = announcementDetailsRepository.getGlobalAnnouncement(1L, false)
        assertEquals(globalAnnouncementTestResponse, result)
    }

    @Test
    fun `getCourse returns null if call fails`() = runTest {
        coEvery { courseApi.getCourse(any(), any()) } returns DataResult.Fail()

        val result = announcementDetailsRepository.getCourse(1L, false)
        assertNull(result)
    }

    @Test(expected = IllegalStateException::class)
    fun `getCourseAnnouncement throw exception if call fails`() = runTest {
        coEvery { announcementApi.getCourseAnnouncement(any(), any(), any()) } returns DataResult.Fail()

        announcementDetailsRepository.getCourseAnnouncement(1L, 1L, false)
    }

    @Test(expected = IllegalStateException::class)
    fun `getGlobalAnnouncement throw exception if call fails`() = runTest {
        coEvery { accountNotificationApi.getAccountNotification(any(), any()) } returns DataResult.Fail()

        announcementDetailsRepository.getGlobalAnnouncement(1L, false)
    }

    private fun createRepository() {
        announcementDetailsRepository = AnnouncementDetailsRepository(announcementApi, courseApi, accountNotificationApi)
    }
}
