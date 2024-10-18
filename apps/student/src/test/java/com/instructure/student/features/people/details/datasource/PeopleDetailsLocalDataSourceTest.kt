package com.instructure.student.features.people.details.datasource

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.room.offline.facade.UserFacade
import com.instructure.student.features.people.details.PeopleDetailsLocalDataSource
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PeopleDetailsLocalDataSourceTest {
    private val userFacade: UserFacade = mockk(relaxed = true)
    private val dataSource = PeopleDetailsLocalDataSource(userFacade)

    @Test
    fun `User is returned by id`() = runTest {
        val expected = User(id = 1L, name = "User 1", enrollments = listOf(Enrollment(1L), Enrollment(2L)))
        coEvery { userFacade.getUserById(any()) } returns expected

        val result = dataSource.loadUser(CanvasContext.defaultCanvasContext(), 1L)

        TestCase.assertEquals(expected, result)
    }

    @Test
    fun `User is not exists`() = runTest {
        val expected = null
        coEvery { userFacade.getUserById(any()) } returns null

        val result = dataSource.loadUser(CanvasContext.defaultCanvasContext(), 2L)

        TestCase.assertEquals(expected, result)
    }

    @Test
    fun `Permission is always false`() = runTest {
        val expected = false
        val result = dataSource.loadMessagePermission(CanvasContext.defaultCanvasContext(), listOf("test"), User(), false)
        TestCase.assertEquals(expected, result)
    }
}