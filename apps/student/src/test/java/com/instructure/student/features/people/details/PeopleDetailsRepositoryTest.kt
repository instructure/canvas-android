package com.instructure.student.features.people.details

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class PeopleDetailsRepositoryTest {
    private val networkDataSource: PeopleDetailsNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: PeopleDetailsLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)

    private val repository = PeopleDetailsRepository(networkDataSource, localDataSource, networkStateProvider)

    @Test
    fun `Get user if device is online`() = runTest {
        val offlineExpected = User(id = 1L, name = "Offline")
        val onlineExpected = User(id = 1L, name = "Online")

        every { networkStateProvider.isOnline() } returns true
        coEvery { localDataSource.loadUser(any(), any()) } returns DataResult.Success(offlineExpected)
        coEvery { networkDataSource.loadUser(any(), any()) } returns DataResult.Success(onlineExpected)

        val result = repository.loadUser(CanvasContext.defaultCanvasContext(), 1L).dataOrNull

        TestCase.assertEquals(onlineExpected, result)
    }

    @Test
    fun `Get user if device is offline`() = runTest {
        val offlineExpected = User(id = 1L, name = "Offline")
        val onlineExpected = User(id = 1L, name = "Online")

        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.loadUser(any(), any()) } returns DataResult.Success(offlineExpected)
        coEvery { networkDataSource.loadUser(any(), any()) } returns DataResult.Success(onlineExpected)

        val result = repository.loadUser(CanvasContext.defaultCanvasContext(), 1L).dataOrNull

        TestCase.assertEquals(offlineExpected, result)
    }
}