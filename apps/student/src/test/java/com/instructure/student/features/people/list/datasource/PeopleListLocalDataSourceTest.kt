package com.instructure.student.features.people.list.datasource

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.room.offline.facade.UserFacade
import com.instructure.student.features.people.list.PeopleListLocalDataSource
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PeopleListLocalDataSourceTest {
    private val userFacade: UserFacade = mockk(relaxed = true)
    private val dataSource = PeopleListLocalDataSource(userFacade)

    @Test
    fun `People list returned as first page`() = runTest {
        val expected = listOf(
            User(id = 1L, name = "User 1", enrollments = listOf(Enrollment(1L), Enrollment(2L))),
            User(id = 2L, name = "User 2", enrollments = listOf(Enrollment(3L))),
            User(id = 3L, name = "User 3", enrollments = listOf())
        )
        coEvery { userFacade.getUsersByCourseId(any()) } returns expected

        val people = dataSource.loadFirstPagePeople(CanvasContext.defaultCanvasContext(), false).dataOrNull

        assertEquals(expected, people)
    }

    @Test
    fun `Next page returns no data`() = runTest {
        val expected = emptyList<User>()
        coEvery { userFacade.getUsersByCourseId(any()) } returns expected

        val people = dataSource.loadNextPagePeople(CanvasContext.defaultCanvasContext(), false, "nextUrl").dataOrNull

        assertEquals(expected, people)
    }

    @Test
    fun `Teacher list returned`() = runTest {
        val expected = listOf(
                User(id = 1L, name = "User 1", enrollments = listOf(Enrollment(1L), Enrollment(2L))),
                User(id = 2L, name = "User 2", enrollments = listOf(Enrollment(3L))),
                User(id = 3L, name = "User 3", enrollments = listOf())
        )
        coEvery { userFacade.getUsersByCourseIdAndRole(any(), any()) } returns expected

        val people = dataSource.loadTeachers(CanvasContext.defaultCanvasContext(), false).dataOrNull

        assertEquals(expected, people)
    }

    @Test
    fun `TA list returned`() = runTest {
        val expected = listOf(
                User(id = 1L, name = "User 1", enrollments = listOf(Enrollment(1L), Enrollment(2L))),
                User(id = 2L, name = "User 2", enrollments = listOf(Enrollment(3L))),
                User(id = 3L, name = "User 3", enrollments = listOf())
        )
        coEvery { userFacade.getUsersByCourseIdAndRole(any(), any()) } returns expected

        val people = dataSource.loadTeachers(CanvasContext.defaultCanvasContext(), false).dataOrNull

        assertEquals(expected, people)
    }
}
