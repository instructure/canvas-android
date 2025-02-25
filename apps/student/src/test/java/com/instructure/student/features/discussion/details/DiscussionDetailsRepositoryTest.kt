package com.instructure.student.features.discussion.details

import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.discussion.details.datasource.DiscussionDetailsLocalDataSource
import com.instructure.student.features.discussion.details.datasource.DiscussionDetailsNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DiscussionDetailsRepositoryTest {

    private val networkDataSource: DiscussionDetailsNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: DiscussionDetailsLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val repository = DiscussionDetailsRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)

    @Before
    fun setup() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Test
    fun `Call markAsRead function when device is online`() = runTest {
        val discussionEntryIds = listOf(1L, 2L, 3L)

        coEvery { networkDataSource.markAsRead(any(), any(), any()) } returns DataResult.Success(mockk())

        val result = repository.markAsRead(mockk(), 1L, discussionEntryIds)

        assertEquals(discussionEntryIds, result)
    }

    @Test
    fun `Call deleteDiscussionEntry function when device is online`() = runTest {

        repository.deleteDiscussionEntry(mockk(), 1, 1)
        coVerify(exactly = 1) { networkDataSource.deleteDiscussionEntry(any(), any(), any())}
    }

    @Test
    fun `Call deleteDiscussionEntry function when device is offline`() = runTest {

        repository.deleteDiscussionEntry(mockk(), 1, 1)
        coVerify(exactly = 1) { networkDataSource.deleteDiscussionEntry(any(), any(), any())}
    }

    @Test
    fun `Call rateDiscussionEntry function when device is online`() = runTest {

        repository.rateDiscussionEntry(mockk(), 1, 1, 1)
        coVerify(exactly = 1) { networkDataSource.rateDiscussionEntry(any(), any(), any(), any())}
    }

    @Test
    fun `Call rateDiscussionEntry function when device is offline`() = runTest {

        repository.rateDiscussionEntry(mockk(), 1, 1, 1)
        coVerify(exactly = 1) { networkDataSource.rateDiscussionEntry(any(), any(), any(), any())}
    }

    @Test
    fun `Get getAuthenticatedSession function when device is online`() = runTest {

        val expectedResult: AuthenticatedSession = mockk()
        coEvery { networkDataSource.getAuthenticatedSession("") } returns DataResult.Success(expectedResult)
        val result = repository.getAuthenticatedSession("")

        assertEquals(expectedResult, result)
    }

    @Test
    fun `Get getAuthenticatedSession function when device is offline`() = runTest {

        coEvery { networkDataSource.getAuthenticatedSession(any()) } returns DataResult.Fail(
            Failure.Exception(Exception(), "")
        )

        val result = repository.getAuthenticatedSession("")

        assertEquals(null, result)
    }

    @Test
    fun `Get course settings when device is online`() = runTest {
        val localResult = CourseSettings(true, true)
        val networkResult = CourseSettings(false, false)

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getCourseSettings(any(), any()) } returns DataResult.Success(networkResult)
        coEvery { localDataSource.getCourseSettings(any(), any()) } returns DataResult.Success(localResult)

        val result = repository.getCourseSettings(1, true)

        assertEquals(networkResult, result)
    }

    @Test
    fun `Get course settings when device is offline`() = runTest {
        val localResult = CourseSettings(true, true)
        val networkResult = CourseSettings(false, false)

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getCourseSettings(any(), any()) } returns DataResult.Success(networkResult)
        coEvery { localDataSource.getCourseSettings(any(), any()) } returns DataResult.Success(localResult)

        val result = repository.getCourseSettings(1, true)

        assertEquals(localResult, result)
    }

    @Test
    fun `Get detailed discussion when device is online`() = runTest {
        val localResult = DiscussionTopicHeader(1)
        val networkResult = DiscussionTopicHeader(2)

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getDetailedDiscussion(any(), any(), any()) } returns DataResult.Success(networkResult)
        coEvery { localDataSource.getDetailedDiscussion(any(), any(), any()) } returns DataResult.Success(localResult)

        val result = repository.getDetailedDiscussion(mockk(), 1, true)

        assertEquals(networkResult, result)
    }

    @Test
    fun `Get detailed discussion when device is offline`() = runTest {
        val localResult = DiscussionTopicHeader(1)
        val networkResult = DiscussionTopicHeader(2)

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getDetailedDiscussion(any(), any(), any()) } returns DataResult.Success(networkResult)
        coEvery { localDataSource.getDetailedDiscussion(any(), any(), any()) } returns DataResult.Success(localResult)

        val result = repository.getDetailedDiscussion(mockk(), 1, true)

        assertEquals(localResult, result)
    }

    @Test
    fun `Get all group when device is online`() = runTest {
        val localResult = listOf(Group(1), Group(3))
        val networkResult = listOf(Group(2), Group(4))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getFirstPageGroups(any(), any()) } returns DataResult.Success(networkResult)
        coEvery { networkDataSource.getNextPageGroups(any(), any()) } returns DataResult.Success(emptyList())
        coEvery { localDataSource.getFirstPageGroups(any(), any()) } returns DataResult.Success(localResult)
        coEvery { localDataSource.getNextPageGroups(any(), any()) } returns DataResult.Success(emptyList())

        val result = repository.getAllGroups(1, true)

        assertEquals(networkResult, result)
    }

    @Test
    fun `Get all group when device is offline`() = runTest {
        val localResult = listOf(Group(1), Group(3))
        val networkResult = listOf(Group(2), Group(4))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getFirstPageGroups(any(), any()) } returns DataResult.Success(networkResult)
        coEvery { networkDataSource.getNextPageGroups(any(), any()) } returns DataResult.Success(emptyList())
        coEvery { localDataSource.getFirstPageGroups(any(), any()) } returns DataResult.Success(localResult)
        coEvery { localDataSource.getNextPageGroups(any(), any()) } returns DataResult.Success(emptyList())

        val result = repository.getAllGroups(1,true)

        assertEquals(localResult, result)
    }

    @Test
    fun `Get full discussion topic when device is online`() = runTest {
        val localResult = DiscussionTopic(mutableListOf(1))
        val networkResult = DiscussionTopic(mutableListOf(2))

        every { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getFullDiscussionTopic(any(), any(), any()) } returns DataResult.Success(networkResult)
        coEvery { localDataSource.getFullDiscussionTopic(any(), any(), any()) } returns DataResult.Success(localResult)
        val result = repository.getFullDiscussionTopic(mockk(), 1, true)

        assertEquals(networkResult, result)
    }

    @Test
    fun `Get full discussion topic when device is offline`() = runTest {
        val localResult = DiscussionTopic(mutableListOf(1))
        val networkResult = DiscussionTopic(mutableListOf(2))

        every { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getFullDiscussionTopic(any(), any(), any()) } returns DataResult.Success(networkResult)
        coEvery { localDataSource.getFullDiscussionTopic(any(), any(), any()) } returns DataResult.Success(localResult)
        val result = repository.getFullDiscussionTopic(mockk(), 1, true)

        assertEquals(localResult, result)
    }
}