package com.instructure.student.features.people.details.datasource

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.people.details.PeopleDetailsNetworkDataSource
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PeopleDetailsNetworkDataSourceTest {
    private val userAPI: UserAPI.UsersInterface = mockk(relaxed = true)
    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val groupApi: GroupAPI.GroupInterface = mockk(relaxed = true)
    private val dataSource = PeopleDetailsNetworkDataSource(userAPI, courseApi, groupApi)

    @Test
    fun `User Api returns data`() = runTest {
        val expected = User(id = 1L, name = "User 1", enrollments = listOf(Enrollment(1L), Enrollment(2L)))

        coEvery { userAPI.getUserForContextId(any(), any(), any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.loadUser(CanvasContext.defaultCanvasContext(), 1L)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `User Api first page fail`() = runTest {
        coEvery { userAPI.getUserForContextId(any(), any(), any(), any()) } returns DataResult.Fail()

        dataSource.loadUser(CanvasContext.defaultCanvasContext(), 1L)

    }

    @Test
    fun `Permission api returns data if it is a course`() = runTest {
        val expected: Boolean = true
        val groupCanvasContext = Course()
        coEvery { courseApi.getCoursePermissions(any(), any(), any()) } returns DataResult.Success(CanvasContextPermission(send_messages = expected))
        coEvery { groupApi.getGroupPermissions(any(), any(), any()) } returns DataResult.Success(CanvasContextPermission(send_messages = !expected))

        val result = dataSource.loadMessagePermission(groupCanvasContext, listOf("test"), User(), false)

        assertEquals(expected, result)
    }

    @Test
    fun `Permission api returns data if it is a group`() = runTest {
        val expected: Boolean = true
        val groupCanvasContext = Group()
        coEvery { groupApi.getGroupPermissions(any(), any(), any()) } returns DataResult.Success(CanvasContextPermission(send_messages = expected))
        coEvery { courseApi.getCoursePermissions(any(), any(), any()) } returns DataResult.Success(CanvasContextPermission(send_messages = !expected))

        val result = dataSource.loadMessagePermission(groupCanvasContext, listOf("test"), User(), false)

        assertEquals(expected, result)
    }
}