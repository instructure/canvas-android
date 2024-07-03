/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.courses.list

import android.content.Context
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseGrade
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.parentapp.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test


class CourseGradeFormatterTest {

    private val context: Context = mockk(relaxed = true)
    private val courseGradeFormatter = CourseGradeFormatter(context)

    @Test
    fun `Course grade maps correctly when locked`() = runTest {
        val student = User(1L)
        val course = spyk(Course(id = 1L, name = "Course 1", courseCode = "code-1", enrollments = mutableListOf(Enrollment(userId = student.id))))
        every { course.parentGetCourseGradeFromEnrollment(any(), any()) } returns CourseGrade(isLocked = true)

        val result = courseGradeFormatter.getGradeText(course, student.id)

        Assert.assertEquals(null, result)
    }

    @Test
    fun `Course grade maps correctly when restricted without grade string`() = runTest {
        val student = User(1L)
        val course = spyk(
            Course(
                id = 1L, name = "Course 1", courseCode = "code-1",
                enrollments = mutableListOf(Enrollment(userId = student.id)),
                settings = CourseSettings(restrictQuantitativeData = true)
            )
        )
        every { course.parentGetCourseGradeFromEnrollment(any(), any()) } returns CourseGrade()

        val result = courseGradeFormatter.getGradeText(course, student.id)

        Assert.assertEquals(null, result)
    }

    @Test
    fun `Course grade maps correctly when restricted with grade string`() = runTest {
        val student = User(1L)
        val course = spyk(
            Course(
                id = 1L, name = "Course 1", courseCode = "code-1",
                enrollments = mutableListOf(Enrollment(userId = student.id)),
                settings = CourseSettings(restrictQuantitativeData = true)
            )
        )
        every { course.parentGetCourseGradeFromEnrollment(any(), any()) } returns CourseGrade(
            currentScore = 100.0,
            currentGrade = "A"
        )

        val result = courseGradeFormatter.getGradeText(course, student.id)

        Assert.assertEquals("A ", result)
    }

    @Test
    fun `Course grade maps correctly without grade`() = runTest {
        every { context.getString(R.string.noGrade) } returns "No Grade"
        val student = User(1L)
        val course = spyk(Course(id = 1L, name = "Course 1", courseCode = "code-1", enrollments = mutableListOf(Enrollment(userId = student.id))))
        every { context.getString(R.string.noGrade) } returns "No Grade"
        every { course.parentGetCourseGradeFromEnrollment(any(), any()) } returns CourseGrade(
            noCurrentGrade = true
        )

        val result = courseGradeFormatter.getGradeText(course, student.id)

        Assert.assertEquals("No Grade", result)
    }

    @Test
    fun `Course grade maps correctly with score and grade string`() = runTest {
        val student = User(1L)
        val course = spyk(Course(id = 1L, name = "Course 1", courseCode = "code-1", enrollments = mutableListOf(Enrollment(userId = student.id))))
        every { course.parentGetCourseGradeFromEnrollment(any(), any()) } returns CourseGrade(
            currentScore = 100.0,
            currentGrade = "A"
        )

        val result = courseGradeFormatter.getGradeText(course, student.id)

        Assert.assertEquals("A 100%", result)
    }
}
