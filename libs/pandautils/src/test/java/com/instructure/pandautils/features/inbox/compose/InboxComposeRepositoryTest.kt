package com.instructure.pandautils.features.inbox.compose

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class InboxComposeRepositoryTest {

    private val courseAPI: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val groupAPI: GroupAPI.GroupInterface = mockk(relaxed = true)
    private val recipientAPI: RecipientAPI.RecipientInterface = mockk(relaxed = true)
    private val inboxAPI: InboxApi.InboxInterface = mockk(relaxed = true)

    private val inboxComposeRepository: InboxComposeRepository = InboxComposeRepositoryImpl(
        courseAPI,
        groupAPI,
        recipientAPI,
        inboxAPI,
    )

    @Test
    fun `Get courses successfully`() = runTest {
        val expected = listOf(Course(id = 1), Course(id = 2))

        coEvery { courseAPI.getFirstPageCourses(any()) } returns DataResult.Success(expected)

        val result = inboxComposeRepository.getCourses()

        assertEquals(result, expected)
    }

    @Test
    fun `Get courses with error`() = runTest {
        val expected = emptyList<Course>()
        coEvery { courseAPI.getFirstPageCourses(any()) } returns DataResult.Fail()

        val result = inboxComposeRepository.getCourses()

        assertEquals(result, expected)
    }

    @Test
    fun `Get groups successfully`() = runTest {
        val expected = listOf(Group(id = 1), Group(id = 2))

        coEvery { groupAPI.getFirstPageGroups(any()) } returns DataResult.Success(expected)

        val result = inboxComposeRepository.getGroups()

        assertEquals(result, expected)
    }

    @Test
    fun `Get groups with error`() = runTest {
        val expected = emptyList<Group>()

        coEvery { groupAPI.getFirstPageGroups(any()) } returns DataResult.Fail()

        val result = inboxComposeRepository.getGroups()

        assertEquals(result, expected)
    }

    @Test
    fun `Get recipients successfully`() = runTest {
        val expected = listOf(Recipient(stringId = "1"), Recipient(stringId = "2"))

        coEvery { recipientAPI.getFirstPageRecipientList(any(), any(), any()) } returns DataResult.Success(expected)

        val result = inboxComposeRepository.getRecipients("", Course(), true)

        assertEquals(result, expected)
    }

    @Test
    fun `Get recipients with error`() = runTest {
        val expected = emptyList<Recipient>()

        coEvery { recipientAPI.getFirstPageRecipientList(any(), any(), any()) } returns DataResult.Fail()

        val result = inboxComposeRepository.getRecipients("", Course(), true)

        assertEquals(result, expected)
    }

    @Test
    fun `Post conversation successfully`() = runTest {
        val expected = listOf(Conversation())

        coEvery { inboxAPI.createConversation(any(), any(), any(), any(), any(), any(), any()) } returns DataResult.Success(expected)

        val result = inboxComposeRepository.createConversation(emptyList(), "", "", Course(), emptyList(), false)

        assertEquals(result, expected)
    }

    @Test
    fun `Post conversation with error`() = runTest {
        val expected = emptyList<Conversation>()

        coEvery { inboxAPI.createConversation(any(), any(), any(), any(), any(), any(), any()) } returns DataResult.Fail()

        val result = inboxComposeRepository.createConversation(emptyList(), "", "", Course(), emptyList(), false)

        assertEquals(result, expected)
    }
}