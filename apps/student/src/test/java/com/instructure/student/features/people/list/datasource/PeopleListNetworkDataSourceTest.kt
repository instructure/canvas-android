package com.instructure.student.features.people.list.datasource

import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.people.list.PeopleListNetworkDataSource
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class PeopleListNetworkDataSourceTest {
    private val userAPI: UserAPI.UsersInterface = mockk(relaxed = true)
    private val dataSource = PeopleListNetworkDataSource(userAPI)

    @Test
    fun `User Api successfully returns data`() = runTest {
        val expected = listOf(
                User(id = 1L, name = "User 1", enrollments = listOf(Enrollment(1L), Enrollment(2L))),
                User(id = 2L, name = "User 2", enrollments = listOf(Enrollment(3L))),
                User(id = 3L, name = "User 3", enrollments = listOf())
        )

        coEvery { userAPI.getFirstPagePeopleList(any(), any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.loadPeople(CanvasContext.defaultCanvasContext(), false)

        TestCase.assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `User Api error throws exception`() = runTest {
        coEvery { userAPI.getFirstPagePeopleList(any(), any(), any()) } returns DataResult.Fail()

        dataSource.loadPeople(CanvasContext.defaultCanvasContext(), true)
    }
}