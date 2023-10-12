package com.instructure.student.features.discussion.routing

import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.utils.FEATURE_FLAG_OFFLINE
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.discussion.routing.datasource.DiscussionRouteHelperStudentLocalDataSource
import com.instructure.student.features.discussion.routing.datasource.DiscussionRouteHelperStudentNetworkDataSource
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DiscussionRouteHelperStudentRepositoryTest {

    private val networkDataSource: DiscussionRouteHelperStudentNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: DiscussionRouteHelperStudentLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val repository = DiscussionRouteHelperStudentRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)

    @Before
    fun setup() = runTest {
        coEvery { featureFlagProvider.checkEnvironmentFeatureFlag(FEATURE_FLAG_OFFLINE) } returns true
    }

    @Test
    fun `Call getEnabledFeaturesForCourse function when device is online`() = runTest {
        val expected = true

        coEvery { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getEnabledFeaturesForCourse(any(), any()) } returns expected

        val result = repository.getEnabledFeaturesForCourse(mockk(), false)

        assertEquals(expected, result)
    }

    @Test
    fun `Call getEnabledFeaturesForCourse function when device is offline`() = runTest {
        val expected = true

        coEvery { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getEnabledFeaturesForCourse(any(), any()) } returns expected

        val result = repository.getEnabledFeaturesForCourse(mockk(), false)

        assertEquals(expected, result)
    }

    @Test
    fun `Call getDiscussionTopicHeader function when device is online`() = runTest {
        val onlineExpected = DiscussionTopicHeader(1L)
        val offlineExpected = DiscussionTopicHeader(2L)

        coEvery { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getDiscussionTopicHeader(any(), any(), any()) } returns onlineExpected
        coEvery { localDataSource.getDiscussionTopicHeader(any(), any(), any()) } returns offlineExpected

        val result = repository.getDiscussionTopicHeader(mockk(), 1, true)

        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Call getDiscussionTopicHeader function when device is offline`() = runTest {
        val onlineExpected = DiscussionTopicHeader(1L)
        val offlineExpected = DiscussionTopicHeader(2L)

        coEvery { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getDiscussionTopicHeader(any(), any(), any()) } returns onlineExpected
        coEvery { localDataSource.getDiscussionTopicHeader(any(), any(), any()) } returns offlineExpected

        val result = repository.getDiscussionTopicHeader(mockk(), 1, true)

        assertEquals(offlineExpected, result)
    }

    @Test
    fun `Call getAllGroups function when device is online`() = runTest {
        val onlineExpected = listOf(Group(1L))
        val offlineExpected = listOf(Group(2L))

        coEvery { networkStateProvider.isOnline() } returns true
        coEvery { networkDataSource.getAllGroups(any(), any(), any()) } returns onlineExpected
        coEvery { localDataSource.getAllGroups(any(), any(), any()) } returns offlineExpected

        val result = repository.getAllGroups(mockk(), 1L, true)

        assertEquals(onlineExpected, result)
    }

    @Test
    fun `Call getAllGroups function when device is offline`() = runTest {
        val onlineExpected = listOf(Group(1L))
        val offlineExpected = listOf(Group(2L))

        coEvery { networkStateProvider.isOnline() } returns false
        coEvery { networkDataSource.getAllGroups(any(), any(), any()) } returns onlineExpected
        coEvery { localDataSource.getAllGroups(any(), any(), any()) } returns offlineExpected

        val result = repository.getAllGroups(mockk(), 1L, true)

        assertEquals(offlineExpected, result)
    }
}