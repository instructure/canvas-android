package com.instructure.teacher.features.inbox.compose

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.managers.InboxSignatureSettings
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.EnvironmentSettings
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TeacherInboxComposeRepositoryTest {

    private val courseAPI: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val recipientAPI: RecipientAPI.RecipientInterface = mockk(relaxed = true)
    private val inboxAPI: InboxApi.InboxInterface = mockk(relaxed = true)
    private val inboxSettingsManager: InboxSettingsManager = mockk(relaxed = true)
    private val featuresApi: FeaturesAPI.FeaturesInterface = mockk(relaxed = true)

    private val inboxComposeRepository = TeacherInboxComposeRepository(
        courseAPI,
        featuresApi,
        recipientAPI,
        inboxAPI,
        inboxSettingsManager
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Get courses successfully`() = runTest {
        val expected = listOf(
            Course(id = 1, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))),
            Course(id = 2, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE)))
        )

        coEvery { courseAPI.getFirstPageCoursesInbox(any()) } returns DataResult.Success(expected)

        val result = inboxComposeRepository.getCourses().dataOrThrow

        assertEquals(expected, result)
    }

    @Test
    fun `Test course filtering`() = runTest {
        val expected = listOf(
            Course(id = 1, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))),
            Course(id = 2, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_COMPLETED))),
            Course(id = 3, accessRestrictedByDate = true, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_COMPLETED)))
        )

        coEvery { courseAPI.getFirstPageCoursesInbox(any()) } returns DataResult.Success(expected)

        val result = inboxComposeRepository.getCourses().dataOrThrow

        assertEquals(expected, result)
    }

    @Test
    fun `Test courses paging`() = runTest {
        val list1 = listOf(Course(id = 1, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))),)
        val list2 = listOf(Course(id = 2, enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))),)
        val expected = list1 + list2

        coEvery { courseAPI.getFirstPageCoursesInbox(any()) } returns DataResult.Success(list1, LinkHeaders(nextUrl = "next"))
        coEvery { courseAPI.next(any(), any()) } returns DataResult.Success(list2)

        val result = inboxComposeRepository.getCourses().dataOrThrow

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get courses with error`() = runTest {
        coEvery { courseAPI.getFirstPageCoursesInbox(any()) } returns DataResult.Fail()

        inboxComposeRepository.getCourses().dataOrThrow
    }

    @Test
    fun `Get groups returns empty list`() = runTest {
        val expected = emptyList<Group>()

        val result = inboxComposeRepository.getGroups().dataOrThrow

        assertEquals(expected, result)
    }

    @Test
    fun `Get recipients successfully`() = runTest {
        val course = Course(id = 1)
        val expected = listOf(
            Recipient(stringId = "1", commonCourses = hashMapOf(course.id.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue))),
            Recipient(stringId = "2", commonCourses = hashMapOf(course.id.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue)))
        )

        coEvery { recipientAPI.getFirstPageRecipientListNoSyntheticContexts(any(), any(), any()) } returns DataResult.Success(expected)

        val result = inboxComposeRepository.getRecipients("", course.contextId, true).dataOrThrow

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get recipients with error`() = runTest {
        coEvery { recipientAPI.getFirstPageRecipientListNoSyntheticContexts(any(), any(), any()) } returns DataResult.Fail()

        inboxComposeRepository.getRecipients("", Course(id = 1L).contextId, true).dataOrThrow
    }

    @Test
    fun `Post conversation successfully`() = runTest {
        val expected = listOf(Conversation())

        coEvery { inboxAPI.createConversation(any(), any(), any(), any(), any(), any(), any()) } returns DataResult.Success(expected)

        val result = inboxComposeRepository.createConversation(emptyList(), "", "", Course(), emptyList(), false).dataOrThrow

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Post conversation with error`() = runTest {
        coEvery { inboxAPI.createConversation(any(), any(), any(), any(), any(), any(), any()) } returns DataResult.Fail()

        inboxComposeRepository.createConversation(emptyList(), "", "", Course(), emptyList(), false).dataOrThrow
    }

    @Test
    fun `Get signature returns empty string when feature is disabled`() = runTest {
        val expected = InboxSignatureSettings("signature", true)

        coEvery { featuresApi.getAccountSettingsFeatures(any()) } returns DataResult.Success(EnvironmentSettings(enableInboxSignatureBlock = false))
        coEvery { inboxSettingsManager.getInboxSignatureSettings() } returns DataResult.Success(expected)

        val result = inboxComposeRepository.getInboxSignature()

        assertEquals("", result)
    }

    @Test
    fun `Get signature successfully when use signature is true`() = runTest {
        val expected = InboxSignatureSettings("signature", true)

        coEvery { featuresApi.getAccountSettingsFeatures(any()) } returns DataResult.Success(EnvironmentSettings(enableInboxSignatureBlock = true))
        coEvery { inboxSettingsManager.getInboxSignatureSettings() } returns DataResult.Success(expected)

        val result = inboxComposeRepository.getInboxSignature()

        assertEquals("signature", result)
    }

    @Test
    fun `Get signature returns empty string when use signature is false`() = runTest {
        val expected = InboxSignatureSettings("signature", false)

        coEvery { featuresApi.getAccountSettingsFeatures(any()) } returns DataResult.Success(EnvironmentSettings(enableInboxSignatureBlock = true))
        coEvery { inboxSettingsManager.getInboxSignatureSettings() } returns DataResult.Success(expected)

        val result = inboxComposeRepository.getInboxSignature()

        assertEquals("", result)
    }
}