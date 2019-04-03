package com.instructure.parentapp.unit

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.parentapp.factorys.WeekViewPresenterFactory
import com.instructure.parentapp.models.WeekHeaderItem
import com.instructure.parentapp.presenters.WeekPresenter

import org.junit.Before
import org.junit.Test

import java.util.Calendar
import java.util.GregorianCalendar

import org.junit.Assert.assertTrue

class WeekPresenterTest {

    private var mPresenter: WeekPresenter? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        val student = User()
        val course = Course(
                name = "Curious George and the Hidden Course of Doom",
                courseCode = "course_12345"
        )

        mPresenter = WeekViewPresenterFactory(student, course).create()
    }

    @Test
    @Throws(Exception::class)
    fun compare_headerOrder() {
        assertTrue(mPresenter!!.compare(getHeader(MONDAY), getHeader(TUESDAY)) == -1)
    }

    @Test
    @Throws(Exception::class)
    fun compare_headerOrderReverse() {
        assertTrue(mPresenter!!.compare(getHeader(TUESDAY), getHeader(MONDAY)) == 1)
    }

    @Test
    @Throws(Exception::class)
    fun compare_headerOrderEquals() {
        assertTrue(mPresenter!!.compare(getHeader(MONDAY), getHeader(MONDAY)) == 0)
    }

    @Test
    @Throws(Exception::class)
    fun getStudent_notNull() {
        assertTrue(mPresenter!!.student != null)
    }

    @Test
    @Throws(Exception::class)
    fun getCourses_notNull() {
        assertTrue(mPresenter!!.course != null)
    }

    @Test
    @Throws(Exception::class)
    fun getCoursesMap_notNull() {
        assertTrue(mPresenter!!.coursesMap != null)
    }

    companion object {

        private const val MONDAY = 1484000031210L//Monday when test was written
        private const val TUESDAY = 1484086479434L//Day after Monday, Tuesday

        private fun getHeader(time: Long): WeekHeaderItem {
            val date = GregorianCalendar()
            date.timeInMillis = time
            val header = WeekHeaderItem(date.get(Calendar.DAY_OF_WEEK))
            header.date = date
            return header
        }
    }
}
