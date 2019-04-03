/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */


package com.instructure.parentapp.unit

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.parentapp.presenters.CourseListPresenter
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CourseListPresenterTest {

    private lateinit var course1: Course
    private lateinit var course2: Course
    private lateinit var presenter: CourseListPresenter

    @Before
    fun setUp() {
        course1 = Course(id = 12345)
        course2 = Course(id = 12345)
        val student = User()
        presenter = CourseListPresenter(student)
    }

    @Test
    fun compare_TestEquals() {
        assertEquals(0, presenter.compare(course1, course2))
    }

    @Test
    fun compare_TestNegative() {
        val c2 = course2.copy(id = 1234567)

        assertEquals(-1, presenter.compare(course1, c2))
    }

    @Test
    fun compare_TestPositive() {
        val c = course1.copy(id = 1234578)
        val c2 = course2.copy(id = 123457)

        assertEquals(1, presenter.compare(c, c2))
    }

    @Test
    fun areContentsTheSame() {
        // Always false
        assertEquals(false, presenter.areContentsTheSame(course1, course1))
    }

    @Test
    fun areItemsTheSame_TestTrue() {
        assertEquals(true, presenter.areItemsTheSame(course1, course2))
    }

    @Test
    fun areItemsTheSame_TestFalse() {
        val c2 = course2.copy(id = 1234565)
        assertEquals(false, presenter.areItemsTheSame(course1, c2))
    }
}
