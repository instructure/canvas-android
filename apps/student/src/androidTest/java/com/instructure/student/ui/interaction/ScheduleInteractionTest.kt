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

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
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
class ScheduleInteractionTest : StudentTest() {

    override fun displaysPageObjects() = Unit

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testShowCorrectHeaderItems() {
        val data = createMockData(courseCount = 1)
        goToSchedule(data)

        schedulePage.assertPageObjects()
        schedulePage.assertDayHeaderShown("August 15", "Sunday", 0)
        schedulePage.assertDayHeaderShown("August 16", schedulePage.getStringFromResource(R.string.yesterday), 2)
        schedulePage.assertNoScheduleItemDisplayed()

        schedulePage.assertDayHeaderShown("August 17", schedulePage.getStringFromResource(R.string.today), 4)
        schedulePage.assertDayHeaderShown("August 18", schedulePage.getStringFromResource(R.string.tomorrow), 6)
        schedulePage.assertNoScheduleItemDisplayed()

        schedulePage.assertDayHeaderShown("August 19", "Thursday", 8)
        schedulePage.assertDayHeaderShown("August 20", "Friday", 10)
        schedulePage.assertNoScheduleItemDisplayed()

        schedulePage.assertDayHeaderShown("August 21", "Saturday", 12)
        schedulePage.assertNoScheduleItemDisplayed()
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testShowScheduledAssignments() {
        val data = createMockData(courseCount = 1)

        val courses = data.courses.values.filter { !it.homeroomCourse }

        val assignment1 = data.addAssignment(courses[0].id, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        val assignment2 = data.addAssignment(courses[0].id, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY)

        goToSchedule(data)
        schedulePage.scrollToPosition(8)
        schedulePage.assertCourseHeaderDisplayed(courses[0].name)
        schedulePage.assertScheduleItemDisplayed(assignment1.name!!)
        schedulePage.assertScheduleItemDisplayed(assignment2.name!!)
        // TODO Maybe also check due date
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testShowMissingAssignments() {
        val data = createMockData(courseCount = 1)

        val courses = data.courses.values.filter { !it.homeroomCourse }

        val assignment1 = data.addAssignment(courses[0].id, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        val assignment2 = data.addAssignment(courses[0].id, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY)

        goToSchedule(data)
        schedulePage.scrollToPosition(10)
        schedulePage.assertMissingItemDisplayed(assignment1.name!!, courses[0].name, "10 pts")
        schedulePage.assertMissingItemDisplayed(assignment2.name!!, courses[0].name, "10 pts")
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testShowToDoEvents() {
        val data = createMockData(courseCount = 1)
        goToSchedule(data)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testRefresh() {
        val data = createMockData(courseCount = 1)
        goToSchedule(data)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testGoToPreviousWeek() {
        val data = createMockData(courseCount = 1)
        goToSchedule(data)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testGoToNextWeek() {
        val data = createMockData(courseCount = 1)
        goToSchedule(data)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testOpenAssignment() {
        val data = createMockData(courseCount = 1)
        goToSchedule(data)
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testOpenCourse() {
        val data = createMockData(courseCount = 1)
        goToSchedule(data)
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

    private fun goToSchedule(data: MockCanvas) {
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLoginElementary(data.domain, token, student)
        elementaryDashboardPage.waitForRender()
        elementaryDashboardPage.selectScheduleTab()
    }
}