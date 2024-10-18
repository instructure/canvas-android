package com.instructure.student.features.people.list.datasource

import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.people.list.PeopleListNetworkDataSource
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PeopleListNetworkDataSourceTest {
    private val userAPI: UserAPI.UsersInterface = mockk(relaxed = true)
    private val dataSource = PeopleListNetworkDataSource(userAPI)

    @Test
    fun `User Api first page successfully returns data`() = runTest {
        val expected = listOf(
                User(id = 1L, name = "User 1", enrollments = listOf(Enrollment(1L), Enrollment(2L))),
                User(id = 2L, name = "User 2", enrollments = listOf(Enrollment(3L))),
                User(id = 3L, name = "User 3", enrollments = listOf())
        )

        coEvery { userAPI.getFirstPagePeopleList(any(), any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.loadFirstPagePeople(CanvasContext.defaultCanvasContext(), false).dataOrNull

        assertEquals(expected, result)
    }

    @Test
    fun `User Api first page fail`() = runTest {
        coEvery { userAPI.getFirstPagePeopleList(any(), any(), any()) } returns DataResult.Fail()

        val result = dataSource.loadFirstPagePeople(CanvasContext.defaultCanvasContext(), true)
        assertEquals(DataResult.Fail(), result)

    }

    @Test
    fun `User Api next page successfully returns data`() = runTest {
        val expected = listOf(
                User(id = 1L, name = "User 1", enrollments = listOf(Enrollment(1L), Enrollment(2L))),
                User(id = 2L, name = "User 2", enrollments = listOf(Enrollment(3L))),
                User(id = 3L, name = "User 3", enrollments = listOf())
        )

        coEvery { userAPI.getNextPagePeopleList(any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.loadNextPagePeople(CanvasContext.defaultCanvasContext(), false, "nextUrl").dataOrNull

        assertEquals(expected, result)
    }

    @Test
    fun `User Api next page fail`() = runTest {
        coEvery { userAPI.getNextPagePeopleList(any(), any()) } returns DataResult.Fail()

        val result = dataSource.loadNextPagePeople(CanvasContext.defaultCanvasContext(), false, "nextUrl")
        assertEquals(DataResult.Fail(), result)

    }

    @Test
    fun `User Api successfully returns teachers`() = runTest {
        val expected = listOf(
                User(id = 1L, name = "User 1", enrollments = listOf(Enrollment(1L), Enrollment(2L))),
                User(id = 2L, name = "User 2", enrollments = listOf(Enrollment(3L))),
                User(id = 3L, name = "User 3", enrollments = listOf())
        )

        coEvery { userAPI.getFirstPagePeopleList(any(), any(), any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.loadTeachers(CanvasContext.defaultCanvasContext(), false).dataOrNull

        assertEquals(expected, result)
    }

    @Test
    fun `User Api teachers fail`() = runTest {
        coEvery { userAPI.getFirstPagePeopleList(any(), any(), any(), any()) } returns DataResult.Fail()

        val result = dataSource.loadTeachers(CanvasContext.defaultCanvasContext(), true)
        assertEquals(DataResult.Fail(), result)

    }

    @Test
    fun `User Api successfully returns TAs`() = runTest {
        val expected = listOf(
                User(id = 1L, name = "User 1", enrollments = listOf(Enrollment(1L), Enrollment(2L))),
                User(id = 2L, name = "User 2", enrollments = listOf(Enrollment(3L))),
                User(id = 3L, name = "User 3", enrollments = listOf())
        )

        coEvery { userAPI.getFirstPagePeopleList(any(), any(), any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.loadTAs(CanvasContext.defaultCanvasContext(), false).dataOrNull

        assertEquals(expected, result)
    }

    @Test
    fun `User Api TAs fail`() = runTest {
        coEvery { userAPI.getFirstPagePeopleList(any(), any(), any(), any()) } returns DataResult.Fail()

        val result = dataSource.loadTAs(CanvasContext.defaultCanvasContext(), true)
        assertEquals(DataResult.Fail(), result)

    }

}