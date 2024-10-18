package com.instructure.student.features.discussion.details.datasource

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DiscussionDetailsNetworkDataSourceTest {
    private val discussionApi: DiscussionAPI.DiscussionInterface = mockk(relaxed = true)
    private val oAuthApi: OAuthAPI.OAuthInterface = mockk(relaxed = true)
    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val groupApi: GroupAPI.GroupInterface = mockk(relaxed = true)

    private val dataSource = DiscussionDetailsNetworkDataSource(
        discussionApi = discussionApi,
        oAuthApi = oAuthApi,
        courseApi = courseApi,
        groupApi = groupApi
    )

    @Test
    fun `Call mark as read`() = runTest {
        dataSource.markAsRead(CanvasContext.defaultCanvasContext(), 1, 1)

        coVerify(exactly = 1) { discussionApi.markDiscussionTopicEntryRead(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Call delete discussion`() = runTest {
        dataSource.deleteDiscussionEntry(CanvasContext.defaultCanvasContext(), 1, 1)

        coVerify(exactly = 1) { discussionApi.deleteDiscussionEntry(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Call rate discussion`() = runTest {
        dataSource.rateDiscussionEntry(CanvasContext.defaultCanvasContext(), 1, 1, 1)

        coVerify(exactly = 1) { discussionApi.rateDiscussionEntry(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Get authenticatedSession on successful call`() = runTest {
        val expectedUrl = AuthenticatedSession("testUrl")

        coEvery { oAuthApi.getAuthenticatedSession(any(), any()) } returns DataResult.Success(expectedUrl)

        val result = dataSource.getAuthenticatedSession("").dataOrNull

        assertEquals(expectedUrl, result)
    }

    @Test
    fun `Get authenticatedSession on failed call`() = runTest {
        coEvery { oAuthApi.getAuthenticatedSession(any(), any()) } returns DataResult.Fail(null, null)

        val result = dataSource.getAuthenticatedSession("").dataOrNull

        assertEquals(null, result)
    }

    @Test
    fun `Get course settings on successful call`() = runTest {
        val expectedCourseSettings = CourseSettings()

        coEvery { courseApi.getCourseSettings(any(), any()) } returns DataResult.Success(expectedCourseSettings)

        val result = dataSource.getCourseSettings(1, true).dataOrNull

        assertEquals(expectedCourseSettings, result)
    }

    @Test
    fun `Get course settings on failed call`() = runTest {
        coEvery { courseApi.getCourseSettings(any(), any()) } returns DataResult.Fail(null, null)

        val result = dataSource.getCourseSettings(1, true).dataOrNull

        assertEquals(null, result)
    }

    @Test
    fun `Get detailed discussion on successful call`() = runTest {
        val expectedDiscussionTopicHeader = DiscussionTopicHeader()

        coEvery { discussionApi.getDetailedDiscussion(any(), any(), any(), any()) } returns DataResult.Success(expectedDiscussionTopicHeader)

        val result = dataSource.getDetailedDiscussion(CanvasContext.defaultCanvasContext(), 1, true).dataOrNull

        assertEquals(expectedDiscussionTopicHeader, result)
    }

    @Test
    fun `Get detailed discussion on failed call`() = runTest {
        coEvery { discussionApi.getDetailedDiscussion(any(), any(), any(), any()) } returns DataResult.Fail(null, null)

        val result = dataSource.getDetailedDiscussion(CanvasContext.defaultCanvasContext(), 1, true).dataOrNull

        assertEquals(null, result)
    }

    @Test
    fun `Get groups first page on successful call`() = runTest {
        val expectedGroupsFirstPage = listOf(Group(1), Group(2))

        coEvery { groupApi.getFirstPageGroups(any()) } returns DataResult.Success(expectedGroupsFirstPage)

        val result = dataSource.getFirstPageGroups(1, true).dataOrNull

        assertEquals(expectedGroupsFirstPage, result)
    }

    @Test
    fun `Get groups first page on failed call`() = runTest {
        coEvery { groupApi.getFirstPageGroups(any()) } returns DataResult.Fail(null, null)

        val result = dataSource.getFirstPageGroups(1, true).dataOrNull

        assertEquals(null, result)
    }

    @Test
    fun `Get groups next page on successful call`() = runTest {
        val expectedGroupsNextPage = listOf(Group(1), Group(2))

        coEvery { groupApi.getNextPageGroups(any(), any()) } returns DataResult.Success(expectedGroupsNextPage)

        val result = dataSource.getNextPageGroups("", true).dataOrNull

        assertEquals(expectedGroupsNextPage, result)
    }

    @Test
    fun `Get groups next page on failed call`() = runTest {
        coEvery { groupApi.getNextPageGroups(any(), any()) } returns DataResult.Fail(null, null)

        val result = dataSource.getNextPageGroups("", true).dataOrNull

        assertEquals(null, result)
    }

    @Test
    fun `Get full discussion topic on successful call`() = runTest {
        val expectedDiscussionTopic = DiscussionTopic()

        coEvery { discussionApi.getFullDiscussionTopic(any(), any(), any(), any(), any()) } returns DataResult.Success(expectedDiscussionTopic)

        val result = dataSource.getFullDiscussionTopic(CanvasContext.defaultCanvasContext(),1, true).dataOrNull

        assertEquals(expectedDiscussionTopic, result)
    }

    @Test
    fun `Get full discussion topic on failed call`() = runTest {
        coEvery { discussionApi.getFullDiscussionTopic(any(), any(), any(), any(), any()) } returns DataResult.Fail(null, null)

        val result = dataSource.getFullDiscussionTopic(CanvasContext.defaultCanvasContext(),1, true).dataOrNull

        assertEquals(null, result)
    }
}