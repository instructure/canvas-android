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

package com.instructure.parentapp.ui.interaction

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addCourseWithEnrollment
import com.instructure.canvas.espresso.mockCanvas.addEnrollment
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test


@HiltAndroidTest
class CoursesInteractionTest : ParentComposeTest() {

    @Test
    fun testNoCourseDisplayed() {
        val data = initData()
        data.courses.clear()

        goToCourses(data)

        composeTestRule.waitForIdle()
        coursesPage.assertEmptyContentDisplayed()
    }

    @Test
    fun testCourseDisplayed() {
        val data = initData()

        goToCourses(data)

        composeTestRule.waitForIdle()
        coursesPage.assertCourseItemDisplayed(data.courses.values.first())
    }

    @Test
    fun testShowGradeIfThereIsACurrentGrade() {
        val data = initData()
        val course = data.courses.values.find {
            val enrollment = it.enrollments!!.first()
            !enrollment.currentGrade.isNullOrEmpty() && enrollment.currentScore != null
        }
        val enrollment = course!!.enrollments!!.first()

        goToCourses(data)

        composeTestRule.waitForIdle()
        coursesPage.assertGradeTextDisplayed(course.name, "${enrollment.currentGrade} ${enrollment.currentScore}%")
    }

    @Test
    fun testShowNoGradeIfThereIsNoCurrentGrade() {
        val data = initData()
        val firstStudent = data.students.first()
        val courseWithoutGrade = data.addCourseWithEnrollment(firstStudent, Enrollment.EnrollmentType.Student, score = null, grade = null)
        data.addEnrollment(data.parents.first(), courseWithoutGrade, Enrollment.EnrollmentType.Observer, firstStudent)

        goToCourses(data)

        composeTestRule.waitForIdle()
        coursesPage.assertGradeTextDisplayed(courseWithoutGrade.name, "No Grade")
    }

    @Test
    fun testShowGradeOnlyIfQuantitativeDataIsRestricted() {
        val data = initData()
        val firstStudent = data.students.first()
        val course = data.addCourseWithEnrollment(firstStudent, Enrollment.EnrollmentType.Student, restrictQuantitativeData = true)
        data.addEnrollment(data.parents.first(), course, Enrollment.EnrollmentType.Observer, firstStudent)

        goToCourses(data)

        composeTestRule.waitForIdle()
        coursesPage.assertGradeTextIsNotDisplayed(course.name)
    }

    @Test
    fun testCourseTapped() {
        val data = initData()

        goToCourses(data)

        composeTestRule.waitForIdle()
        val course = data.courses.values.first()
        coursesPage.clickCourseItem(course.name)
        courseDetailsPage.assertCourseNameDisplayed(course)
    }

    private fun initData(): MockCanvas {
        return MockCanvas.init(
            parentCount = 1,
            studentCount = 1,
            courseCount = 3
        )
    }

    private fun goToCourses(data: MockCanvas) {
        val parent = data.parents.first()
        val token = data.tokenFor(parent)!!
        tokenLogin(data.domain, token, parent)
    }
}
