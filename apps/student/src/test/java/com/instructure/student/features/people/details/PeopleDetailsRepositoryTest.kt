package com.instructure.student.features.people.details

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PeopleDetailsRepositoryTest {
    private val networkDataSource: PeopleDetailsNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: PeopleDetailsLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val repository = PeopleDetailsRepository(networkDataSource, localDataSource, networkStateProvider, featureFlagProvider)

    @Before
    fun setup() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Test
    fun `Get user if device is online`() = runTest {
        val offlineExpected = User(id = 1L, name = "Offline")
        val onlineExpected = User(id = 1L, name = "Online")

        every { networkStateProvider.isOnline() } returns true
        coEvery { localDataSource.loadUser(any(), any(), any()) } returns offlineExpected
        coEvery { networkDataSource.loadUser(any(), any(), any()) } returns onlineExpected

        val result = repository.loadUser(CanvasContext.defaultCanvasContext(), 1L, true)

        TestCase.assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get user if device is offline`() = runTest {
        val offlineExpected = User(id = 1L, name = "Offline")
        val onlineExpected = User(id = 1L, name = "Online")

        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.loadUser(any(), any(), any()) } returns offlineExpected
        coEvery { networkDataSource.loadUser(any(), any(), any()) } returns onlineExpected

        val result = repository.loadUser(CanvasContext.defaultCanvasContext(), 1L, false)

        TestCase.assertEquals(offlineExpected, result)
    }

    @Test
    fun `Get permission if device is online`() = runTest {
        val offlineExpected = false
        val onlineExpected = true

        every { networkStateProvider.isOnline() } returns true
        coEvery { localDataSource.loadMessagePermission(any(), any(), any(), any()) } returns offlineExpected
        coEvery { networkDataSource.loadMessagePermission(any(), any(), any(), any()) } returns onlineExpected

        val result = repository.loadMessagePermission(CanvasContext.defaultCanvasContext(), User(1L), true)

        TestCase.assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get permission if device is offline`() = runTest {
        val offlineExpected = true
        val onlineExpected = false

        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.loadMessagePermission(any(), any(), any(), any()) } returns offlineExpected
        coEvery { networkDataSource.loadMessagePermission(any(), any(), any(), any()) } returns onlineExpected

        val result = repository.loadMessagePermission(CanvasContext.defaultCanvasContext(), User(1L), false)

        TestCase.assertEquals(offlineExpected, result)
    }
}