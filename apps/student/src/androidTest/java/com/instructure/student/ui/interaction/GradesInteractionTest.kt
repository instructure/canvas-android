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

import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addCourseWithEnrollment
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigPrefs
import com.instructure.espresso.page.getStringFromResource
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.R
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLoginElementary
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class GradesInteractionTest : StudentTest() {

    override fun displaysPageObjects() = Unit

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD_HOMEROOM, TestCategory.INTERACTION)
    fun testShowGrades() {
        val data = createMockData(courseCount = 3)
        goToGrades(data)

        gradesPage.assertPageObjects()

        data.courses.forEach {
            gradesPage.assertCourseShownWithGrades(it.value.name, "B+")
        }
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD_HOMEROOM, TestCategory.INTERACTION)
    fun testRefresh() {
        val data = createMockData(courseCount = 3)
        goToGrades(data)

        gradesPage.assertPageObjects()

        data.courses.forEach {
            gradesPage.assertCourseShownWithGrades(it.value.name, "B+")
        }

        val newCourse = data.addCourseWithEnrollment(data.students[0], Enrollment.EnrollmentType.Student, 50.0)

        gradesPage.refresh()

        gradesPage.assertCourseShownWithGrades(newCourse.name, "50%")
    }

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.K5_DASHBOARD_HOMEROOM, TestCategory.INTERACTION)
    fun testEmptyView() {
        val data = createMockData(homeroomCourseCount = 1)
        goToGrades(data)

        gradesPage.assertEmptyViewVisible()
        gradesPage.assertRecyclerViewNotVisible()
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD_HOMEROOM, TestCategory.INTERACTION)
    fun testOpenCourseGrades() {
        val data = createMockData(courseCount = 3)
        goToGrades(data)

        val course = data.courses.values.first()

        gradesPage.clickGradeRow(course.name)
        courseGradesPage.assertPageObjects()
        courseGradesPage.assertTotalGrade(containsTextCaseInsensitive("B+"))
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD_HOMEROOM, TestCategory.INTERACTION)
    fun testChangeGradingPeriod() {
        val data = createMockData(courseCount = 3, withGradingPeriods = true)
        goToGrades(data)

        gradesPage.assertSelectedGradingPeriod(gradesPage.getStringFromResource(R.string.currentGradingPeriod))
        gradesPage.clickGradingPeriodSelector()

        val gradingPeriod = data.courseGradingPeriods.values.first().first()
        gradesPage.selectGradingPeriod(gradingPeriod.title!!)
        gradesPage.assertSelectedGradingPeriod(gradingPeriod.title!!)
    }

    private fun createMockData(
        courseCount: Int = 0,
        withGradingPeriods: Boolean = false,
        homeroomCourseCount: Int = 0): MockCanvas {

        // We have to add this delay to be sure that the remote config is already fetched before we want to override remote config values.
        Thread.sleep(3000)
        RemoteConfigPrefs.putString(RemoteConfigParam.K5_DESIGN.rc_name, "true")

        return MockCanvas.init(
            studentCount = 1,
            courseCount = courseCount,
            withGradingPeriods = withGradingPeriods,
            homeroomCourseCount = homeroomCourseCount)
    }

    private fun goToGrades(data: MockCanvas) {
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLoginElementary(data.domain, token, student)
        elementaryDashboardPage.waitForRender()
        elementaryDashboardPage.selectGradesTab()
    }
}