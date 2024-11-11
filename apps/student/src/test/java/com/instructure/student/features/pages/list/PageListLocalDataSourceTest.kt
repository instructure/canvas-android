package com.instructure.student.features.pages.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.pandautils.room.offline.facade.PageFacade
import com.instructure.student.features.pages.list.datasource.PageListLocalDataSource
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PageListLocalDataSourceTest {

    private val pageFacade: PageFacade = mockk(relaxed = true)

    private val dataSource = PageListLocalDataSource(pageFacade)

    @Test
    fun `Returns Page api models`() = runTest {
        val expected = listOf(Page(id = 1L, title = "Page 1"), Page(id = 2, title = "Page 2"))
        coEvery { pageFacade.findByCourseId(any()) } returns expected

        val result = dataSource.loadPages(CanvasContext.defaultCanvasContext(), false)

        assertEquals(expected, result)
    }
}