package com.instructure.student.features.pages.list

import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.pages.list.datasource.PageListNetworkDataSource
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PageListNetworkDataSourceTest {

    private val pageApi: PageAPI.PagesInterface = mockk(relaxed = true)

    private val dataSource = PageListNetworkDataSource(pageApi)

    @Test
    fun `Return pages successfully`() = runTest {
        val expected = listOf(
            Page(
                id = 1L,
                title = "Page 1"
            ), Page(
                id = 2L,
                title = "Page 2"
            )
        )
        coEvery { pageApi.getFirstPagePages(any(), any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.loadPages(CanvasContext.defaultCanvasContext(), false)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get pages error throws exception`() = runTest {
        coEvery { pageApi.getFirstPagePages(any(), any(), any()) } returns DataResult.Fail()

        dataSource.loadPages(CanvasContext.defaultCanvasContext(), true)
    }
}