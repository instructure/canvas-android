package com.instructure.pandautils.room.offline.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Page
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.PageEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PageDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var pageDao: PageDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        pageDao = db.pageDao()
        courseDao = db.courseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertReplaces() = runTest {
        val courseEntity = CourseEntity(Course(id = 1L))
        courseDao.insert(courseEntity)

        val pageEntity = PageEntity(Page(id = 1, title = "Page1"), courseId = 1L)
        pageDao.insert(pageEntity)

        val updated = pageEntity.copy(title = "Update page")
        pageDao.insert(updated)

        val result = pageDao.findById(1L)

        assertEquals(updated, result)
    }

    @Test
    fun testFindAll() = runTest {
        val courseEntity = CourseEntity(Course(id = 1L))
        courseDao.insert(courseEntity)

        val pageEntity = PageEntity(Page(id = 1, title = "Page1"), courseId = 1L)
        val pageEntity2 = PageEntity(Page(id = 2, title = "Page2"), courseId = 1L)
        pageDao.insert(pageEntity)
        pageDao.insert(pageEntity2)

        val result = pageDao.findAll()

        assertEquals(listOf(pageEntity, pageEntity2), result)
    }

    @Test
    fun testFindById() = runTest {
        val courseEntity = CourseEntity(Course(id = 1L))
        courseDao.insert(courseEntity)

        val pageEntity = PageEntity(Page(id = 1, title = "Page1"), courseId = 1L)
        val pageEntity2 = PageEntity(Page(id = 2, title = "Page2"), courseId = 1L)
        pageDao.insert(pageEntity)
        pageDao.insert(pageEntity2)

        val result = pageDao.findById(2L)

        assertEquals(pageEntity2, result)
    }

    @Test
    fun testFindByUrl() = runTest {
        val courseEntity = CourseEntity(Course(id = 1L))
        val courseEntity2 = CourseEntity(Course(id = 2L))
        courseDao.insert(courseEntity)
        courseDao.insert(courseEntity2)

        val pageEntity = PageEntity(Page(id = 1, title = "Page1", url = "page-1-url"), courseId = 1L)
        val pageEntity2 = PageEntity(Page(id = 2, title = "Page2", url = "page-2-url"), courseId = 1L)
        val pageEntity3 = PageEntity(Page(id = 3, title = "Page3", url = "page-2-url"), courseId = 2L)
        pageDao.insert(pageEntity)
        pageDao.insert(pageEntity2)
        pageDao.insert(pageEntity3)

        val result = pageDao.findByUrlAndCourse("page-2-url", 1L)

        assertEquals(pageEntity2, result)
    }

    @Test
    fun testFindFrontPage() = runTest {
        val courseEntity = CourseEntity(Course(id = 1L))
        courseDao.insert(courseEntity)

        val pageEntity = PageEntity(Page(id = 1, title = "Page1", frontPage = true), courseId = 1L)
        val pageEntity2 = PageEntity(Page(id = 2, title = "Page2"), courseId = 1L)
        pageDao.insert(pageEntity)
        pageDao.insert(pageEntity2)

        val result = pageDao.getFrontPage(1L)

        assertEquals(pageEntity, result)
    }

    @Test
    fun testFindByCourseId() = runTest {
        val courseEntity = CourseEntity(Course(id = 1L))
        val courseEntity2 = CourseEntity(Course(id = 2L))
        courseDao.insert(courseEntity)
        courseDao.insert(courseEntity2)

        val pageEntity = PageEntity(Page(id = 1, title = "Page1"), courseId = 1L)
        val pageEntity2 = PageEntity(Page(id = 2, title = "Page2"), courseId = 1L)
        val pageEntity3 = PageEntity(Page(id = 3, title = "Page3"), courseId = 2L)
        val pageEntity4 = PageEntity(Page(id = 4, title = "Page4"), courseId = 2L)
        pageDao.insert(pageEntity)
        pageDao.insert(pageEntity2)
        pageDao.insert(pageEntity3)
        pageDao.insert(pageEntity4)

        val result1 = pageDao.findByCourseId(1L)
        assertEquals(listOf(pageEntity, pageEntity2), result1)

        val result2 = pageDao.findByCourseId(2L)
        assertEquals(listOf(pageEntity3, pageEntity4), result2)
    }

    @Test
    fun testFindPageDetails() = runTest {
        val courseEntity = CourseEntity(Course(id = 1L))
        courseDao.insert(courseEntity)

        val pageEntity = PageEntity(Page(id = 1, title = "Page1", url = "url"), courseId = 1L)
        val pageEntity2 = PageEntity(Page(id = 2, title = "Page2"), courseId = 1L)
        val pageEntity3 = PageEntity(Page(id = 3, title = "Page3"), courseId = 1L)
        pageDao.insert(pageEntity)
        pageDao.insert(pageEntity2)
        pageDao.insert(pageEntity3)

        val resultByUrl = pageDao.getPageDetails(1L, "url")
        assertEquals(pageEntity, resultByUrl)

        val resultByTitle = pageDao.getPageDetails(1L, "Page2")
        assertEquals(pageEntity2, resultByTitle)
    }

    @Test
    fun testDeleteAllByCourseId() = runTest {
        val courseEntity = CourseEntity(Course(id = 1L))
        courseDao.insert(courseEntity)

        val pageEntity = PageEntity(Page(id = 1, title = "Page1"), courseId = 1L)
        pageDao.insert(pageEntity)

        val result = pageDao.findByCourseId(1L)

        assertEquals(listOf(pageEntity), result)

        pageDao.deleteAllByCourseId(1L)

        val deletedResult = pageDao.findByCourseId(1L)

        Assert.assertTrue(deletedResult.isEmpty())
    }
}
