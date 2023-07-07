package com.instructure.student.features.people.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class PeopleListRepositoryTest {
    private val networkDataSource: PeopleListNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: PeopleListLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)

    private val repository = PeopleListRepository(localDataSource, networkDataSource, networkStateProvider)

    @Test
    fun `Get course people if device is online`() = runTest {
        val offlineExpected = listOf(User(id = 1L, name = "Offline"))
        val onlineExpected = listOf(User(id = 2L, name = "Online"))

        every { networkStateProvider.isOnline() } returns true
        coEvery { localDataSource.loadFirstPagePeople(any(), any()) } returns DataResult.Success(offlineExpected)
        coEvery { networkDataSource.loadFirstPagePeople(any(), any()) } returns DataResult.Success(onlineExpected)

        val people = repository.loadFirstPagePeople(CanvasContext.defaultCanvasContext(), false).dataOrNull

        assertEquals(onlineExpected, people)
    }

    @Test
    fun `Get course people if device is offline`() = runTest {
        val offlineExpected = listOf(User(id = 1L, name = "Offline"))
        val onlineExpected = listOf(User(id = 2L, name = "Online"))

        every { networkStateProvider.isOnline() } returns false
        coEvery { localDataSource.loadFirstPagePeople(any(), any()) } returns DataResult.Success(offlineExpected)
        coEvery { networkDataSource.loadFirstPagePeople(any(), any()) } returns DataResult.Success(onlineExpected)

        val pages = repository.loadFirstPagePeople(CanvasContext.defaultCanvasContext(), false).dataOrNull

        assertEquals(offlineExpected, pages)
    }
}