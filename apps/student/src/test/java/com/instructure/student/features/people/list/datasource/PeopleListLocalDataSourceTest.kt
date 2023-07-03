package com.instructure.student.features.people.list.datasource

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.room.offline.facade.PeopleFacade
import com.instructure.student.features.people.list.PeopleListLocalDataSource
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class PeopleListLocalDataSourceTest {
    private val peopleFacade: PeopleFacade = mockk(relaxed = true)
    private val dataSource = PeopleListLocalDataSource(peopleFacade)

    @Test
    fun `User API models are returned`() = runTest {
        val expected = listOf(
            User(id = 1L, name = "User 1", enrollments = listOf(Enrollment(1L), Enrollment(2L))),
            User(id = 2L, name = "User 2", enrollments = listOf(Enrollment(3L))),
            User(id = 3L, name = "User 3", enrollments = listOf())
        )
        coEvery { peopleFacade.getPeopleByCourseId(any()) } returns expected

        val people = dataSource.loadPeople(CanvasContext.defaultCanvasContext(), false)

        assertEquals(expected, people)
    }
}
