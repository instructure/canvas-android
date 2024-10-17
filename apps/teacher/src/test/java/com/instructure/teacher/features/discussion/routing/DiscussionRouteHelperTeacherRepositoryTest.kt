package com.instructure.teacher.features.discussion.routing

import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperNetworkDataSource
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DiscussionRouteHelperTeacherRepositoryTest {
    private val networkDataSource: DiscussionRouteHelperNetworkDataSource = mockk(relaxed = true)

    private val repository = DiscussionRouteHelperTeacherRepository(networkDataSource)

    @Test
    fun `Always show discussion redesign`() = runTest {
        val expected = true

        val result = repository.shouldShowDiscussionRedesign()

        assertEquals(expected, result)
    }

    @Test
    fun `getDiscussionTopicHeader() calls networkDataSource`() = runTest {
        val expected = DiscussionTopicHeader(1L)

        coEvery { networkDataSource.getDiscussionTopicHeader(any(), any(), any()) } returns expected

        val result = repository.getDiscussionTopicHeader(mockk(), 1L, true)

        assertEquals(expected, result)
    }

    @Test
    fun `getAllGroups() calls networkDataSource`() = runTest {
        val expected = listOf(Group(1L))

        coEvery { networkDataSource.getAllGroups(any(), any(), any()) } returns expected

        val result = repository.getAllGroups(mockk(), 1L, true)

        assertEquals(expected, result)
    }
}