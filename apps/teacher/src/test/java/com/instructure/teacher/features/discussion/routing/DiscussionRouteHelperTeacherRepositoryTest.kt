package com.instructure.teacher.features.discussion.routing

import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.teacher.features.discussion.routing.datasource.DiscussionRouteHelperTeacherNetworkDataSource
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class DiscussionRouteHelperTeacherRepositoryTest {
    private val networkDataSource: DiscussionRouteHelperTeacherNetworkDataSource = mockk(relaxed = true)

    private val repository = DiscussionRouteHelperTeacherRepository(networkDataSource)

    @Test
    fun `getEnabledFeaturesForCourse() calls networkDataSource`() = runTest {
        val expected = true

        coEvery { networkDataSource.getEnabledFeaturesForCourse(any(), any()) } returns expected

        val result = repository.getEnabledFeaturesForCourse(mockk(), true)

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
        val expected = Pair(Group(1L), 1L)

        coEvery { networkDataSource.getAllGroups(any(), any(), any()) } returns expected

        val result = repository.getAllGroups(mockk(), 1L, true)

        assertEquals(expected, result)
    }
}