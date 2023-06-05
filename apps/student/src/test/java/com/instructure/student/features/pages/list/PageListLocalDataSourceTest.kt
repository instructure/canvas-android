package com.instructure.student.features.pages.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.pandautils.room.offline.daos.PageDao
import com.instructure.pandautils.room.offline.daos.TabDao
import com.instructure.pandautils.room.offline.entities.PageEntity
import com.instructure.student.features.offline.coursebrowser.CourseBrowserLocalDataSource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class PageListLocalDataSourceTest {

    private val pageDao: PageDao = mockk(relaxed = true)

    private val dataSource = PageListLocalDataSource(pageDao)

    @Test
    fun `Returns Page api models`() = runTest {
        val expected = listOf(Page(id = 1L, title = "Page 1"), Page(id = 2, title = "Page 2"))
        coEvery { pageDao.findByCourseId(any()) } returns expected.map { PageEntity(it, 1L) }

        val result = dataSource.loadPages(CanvasContext.defaultCanvasContext(), false)

        assertEquals(expected, result)
    }
}