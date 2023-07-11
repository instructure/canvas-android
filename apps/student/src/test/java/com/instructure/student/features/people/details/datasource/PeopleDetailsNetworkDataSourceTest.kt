package com.instructure.student.features.people.details.datasource

import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.people.details.PeopleDetailsNetworkDataSource
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PeopleDetailsNetworkDataSourceTest {
    private val userAPI: UserAPI.UsersInterface = mockk(relaxed = true)
    private val dataSource = PeopleDetailsNetworkDataSource(userAPI)

    @Test
    fun `User Api returns data`() = runTest {
        val expected = User(id = 1L, name = "User 1", enrollments = listOf(Enrollment(1L), Enrollment(2L)))

        coEvery { userAPI.getUserForContextId(any(), any(), any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.loadUser(CanvasContext.defaultCanvasContext(), 1L).dataOrNull

        TestCase.assertEquals(expected, result)
    }

    @Test
    fun `User Api first page fail`() = runTest {
        val expected = DataResult.Fail()

        coEvery { userAPI.getUserForContextId(any(), any(), any(), any()) } returns DataResult.Fail()

        val result = dataSource.loadUser(CanvasContext.defaultCanvasContext(), 1L)
        TestCase.assertEquals(expected, result)

    }
}