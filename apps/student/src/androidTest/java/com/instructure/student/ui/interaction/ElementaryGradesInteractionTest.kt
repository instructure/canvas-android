/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.student.ui.interaction

import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addCourseWithEnrollment
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.espresso.page.getStringFromResource
import com.instructure.student.R
import com.instructure.student.ui.pages.classic.k5.ElementaryDashboardPage
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.tokenLoginElementary
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ElementaryGradesInteractionTest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testShowGrades() {
        val data = createMockData(courseCount = 3)
        goToGradesTab(data)

        elementaryGradesPage.assertPageObjects()

        data.courses.forEach {
            elementaryGradesPage.assertCourseShownWithGrades(it.value.name, "B+")
        }
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testRefresh() {
        val data = createMockData(courseCount = 1)
        goToGradesTab(data)

        elementaryGradesPage.assertPageObjects()

        data.courses.forEach {
            elementaryGradesPage.assertCourseShownWithGrades(it.value.name, "B+")
        }

        val newCourse =
            data.addCourseWithEnrollment(data.students[0], Enrollment.EnrollmentType.Student, 50.0)

        elementaryGradesPage.refresh()
        elementaryGradesPage.assertCourseShownWithGrades(newCourse.name, "50%")

    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testOpenCourseGrades() {
        val data = createMockData(courseCount = 3)
        goToGradesTab(data)

        val course = data.courses.values.first()

        elementaryGradesPage.clickGradeRow(course.name)
        gradesPage.assertToolbarTitles(course.name)

        Espresso.pressBack()
        elementaryGradesPage.assertPageObjects()
        data.courses.forEach {
            elementaryGradesPage.assertCourseShownWithGrades(it.value.name, "B+")
        }
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testChangeGradingPeriod() {
        val data = createMockData(courseCount = 3, withGradingPeriods = true)
        goToGradesTab(data)

        elementaryGradesPage.assertSelectedGradingPeriod(elementaryGradesPage.getStringFromResource(R.string.currentGradingPeriod))
        elementaryGradesPage.clickGradingPeriodSelector()

        val gradingPeriod = data.courseGradingPeriods.values.first().first()
        elementaryGradesPage.selectGradingPeriod(gradingPeriod.title!!)
        elementaryGradesPage.assertSelectedGradingPeriod(gradingPeriod.title!!)
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testEmptyView() {
        val data = createMockData(homeroomCourseCount = 1)
        goToGradesTab(data)

        elementaryGradesPage.assertEmptyViewVisible()
        elementaryGradesPage.assertRecyclerViewNotVisible()
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testShowPercentageOnlyIfNoAlphabeticalGrade() {
        val data = createMockData(courseCount = 1)
        goToGradesTab(data)

        elementaryGradesPage.assertPageObjects()

        val alphabeticallyGradedCourse = data.courses.values.first()
        var scoreGradedCourse = data.addCourseWithEnrollment(data.students[0], Enrollment.EnrollmentType.Student, 50.0)
        var bothGradedCourse = data.addCourseWithEnrollment(data.students[0], Enrollment.EnrollmentType.Student, 50.0, "C+")
        var notGradedCourse = data.addCourseWithEnrollment(data.students[0], Enrollment.EnrollmentType.Student)

        elementaryGradesPage.refresh()

        elementaryGradesPage.assertCourseShownWithGrades(alphabeticallyGradedCourse.name, "B+")
        elementaryGradesPage.assertCourseShownWithGrades(scoreGradedCourse.name, "50%")
        elementaryGradesPage.assertCourseShownWithGrades(bothGradedCourse.name, "C+")
        elementaryGradesPage.assertCourseShownWithGrades(notGradedCourse.name, "0%")
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testDontShowProgressWhenQuantitativeDataIsRestricted() {
        val data = createMockData(courseCount = 1)
        goToGradesTab(data)

        elementaryGradesPage.assertPageObjects()

        var course = data.addCourseWithEnrollment(
            data.students[0],
            Enrollment.EnrollmentType.Student,
            50.0,
            "C+",
            restrictQuantitativeData = true
        )

        elementaryGradesPage.refresh()

        elementaryGradesPage.assertCourseShownWithGrades(course.name, "C+")
        elementaryGradesPage.assertProgressNotDisplayed(course.name)
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testDontShowGradeWhenQuantitativeDataIsRestrictedAndThereIsOnlyScore() {
        val data = createMockData(courseCount = 1)
        goToGradesTab(data)

        elementaryGradesPage.assertPageObjects()

        var course = data.addCourseWithEnrollment(
            data.students[0],
            Enrollment.EnrollmentType.Student,
            50.0,
            "",
            restrictQuantitativeData = true
        )

        elementaryGradesPage.refresh()

        elementaryGradesPage.assertCourseShownWithGrades(course.name, "--")
    }

    private fun createMockData(
        courseCount: Int = 0,
        withGradingPeriods: Boolean = false,
        homeroomCourseCount: Int = 0): MockCanvas {

        val data =  MockCanvas.init(
            studentCount = 1,
            courseCount = courseCount,
            withGradingPeriods = withGradingPeriods,
            homeroomCourseCount = homeroomCourseCount)

        data.elementarySubjectPages = true

        return data
    }

    private fun goToGradesTab(data: MockCanvas) {
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLoginElementary(data.domain, token, student)
        elementaryDashboardPage.waitForRender()
        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.GRADES)
    }
}