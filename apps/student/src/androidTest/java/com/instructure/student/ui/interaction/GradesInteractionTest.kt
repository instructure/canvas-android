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
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addCourseWithEnrollment
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.espresso.page.getStringFromResource
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.R
import com.instructure.student.ui.pages.ElementaryDashboardPage
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLoginElementary
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class GradesInteractionTest : StudentTest() {

    override fun displaysPageObjects() = Unit

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testShowGrades() {
        val data = createMockData(courseCount = 3)
        goToGradesTab(data)

        gradesPage.assertPageObjects()

        data.courses.forEach {
            gradesPage.assertCourseShownWithGrades(it.value.name, "B+")
        }
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testRefresh() {
        val data = createMockData(courseCount = 1)
        goToGradesTab(data)

        gradesPage.assertPageObjects()

        data.courses.forEach {
            gradesPage.assertCourseShownWithGrades(it.value.name, "B+")
        }

        val newCourse =
            data.addCourseWithEnrollment(data.students[0], Enrollment.EnrollmentType.Student, 50.0)

        gradesPage.refresh()
        gradesPage.assertCourseShownWithGrades(newCourse.name, "50%")

    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testOpenCourseGrades() {
        val data = createMockData(courseCount = 3)
        goToGradesTab(data)

        val course = data.courses.values.first()

        gradesPage.clickGradeRow(course.name)
        elementaryCoursePage.assertPageObjects()

        Espresso.pressBack()
        gradesPage.assertPageObjects()
        data.courses.forEach {
            gradesPage.assertCourseShownWithGrades(it.value.name, "B+")
        }
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testChangeGradingPeriod() {
        val data = createMockData(courseCount = 3, withGradingPeriods = true)
        goToGradesTab(data)

        gradesPage.assertSelectedGradingPeriod(gradesPage.getStringFromResource(R.string.currentGradingPeriod))
        gradesPage.clickGradingPeriodSelector()

        val gradingPeriod = data.courseGradingPeriods.values.first().first()
        gradesPage.selectGradingPeriod(gradingPeriod.title!!)
        gradesPage.assertSelectedGradingPeriod(gradingPeriod.title!!)
    }

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testEmptyView() {
        val data = createMockData(homeroomCourseCount = 1)
        goToGradesTab(data)

        gradesPage.assertEmptyViewVisible()
        gradesPage.assertRecyclerViewNotVisible()
    }

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testShowPercentageOnlyIfNoAlphabeticalGrade() {
        val data = createMockData(courseCount = 1)
        goToGradesTab(data)

        gradesPage.assertPageObjects()

        val alphabeticallyGradedCourse = data.courses.values.first()
        var scoreGradedCourse = data.addCourseWithEnrollment(data.students[0], Enrollment.EnrollmentType.Student, 50.0)
        var bothGradedCourse = data.addCourseWithEnrollment(data.students[0], Enrollment.EnrollmentType.Student, 50.0, "C+")
        var notGradedCourse = data.addCourseWithEnrollment(data.students[0], Enrollment.EnrollmentType.Student)

        gradesPage.refresh()

        gradesPage.assertCourseShownWithGrades(alphabeticallyGradedCourse.name, "B+")
        gradesPage.assertCourseShownWithGrades(scoreGradedCourse.name, "50%")
        gradesPage.assertCourseShownWithGrades(bothGradedCourse.name, "C+")
        gradesPage.assertCourseShownWithGrades(notGradedCourse.name, "0%")
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