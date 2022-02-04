/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.StubTablet
import com.instructure.canvas.espresso.mockCanvas.*
import com.instructure.canvasapi2.models.Assignment
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.pages.ElementaryDashboardPage
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLoginElementary
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ImportantDatesInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit

    @Test
    //The UI is different on tablet, so we only check the phone version
    @StubTablet
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testShowCalendarEvents() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]

        val event = data.addCourseCalendarEvent(course.id, 2.days.fromNow.iso8601, "Important event", "Important event description", true)

        goToImportantDatesTab(data)
        importantDatesPage.assertItemDisplayed(event.title!!)
    }

    @Test
    @StubTablet
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testShowAssignment() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]

        val assignment = data.addAssignment(courseId = course.id, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        data.addAssignmentCalendarEvent(course.id, 2.days.fromNow.iso8601, assignment.name!!, assignment.description!!, true, assignment)

        goToImportantDatesTab(data)
        importantDatesPage.assertItemDisplayed(assignment.name!!)
    }

    @Test
    @StubTablet
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testEmptyView() {
        val data = createMockData(courseCount = 1)

        goToImportantDatesTab(data)

        importantDatesPage.assertEmptyViewDisplayed()
    }

    @Test
    @StubTablet
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testPullToRefresh() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]
        data.addCourseCalendarEvent(course.id, 2.days.fromNow.iso8601, "Important event", "Important event description", true)

        goToImportantDatesTab(data)
        val eventToCheck = data.addCourseCalendarEvent(course.id, 2.days.fromNow.iso8601, "Important event 2", "Important event 2 description", true)

        importantDatesPage.pullToRefresh()
        importantDatesPage.assertItemDisplayed(eventToCheck.title!!)
    }

    @Test
    @StubTablet
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testOpenCalendarEvent() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]
        val event = data.addCourseCalendarEvent(course.id, 2.days.fromNow.iso8601, "Important event", "Important event description", true)

        goToImportantDatesTab(data)
        importantDatesPage.assertItemDisplayed(event.title!!)
        importantDatesPage.clickImportantDatesItem(event.title!!)
        calendarEventPage.verifyTitle(event.title!!)
        calendarEventPage.verifyDescription(event.description!!)
    }

    @Test
    @StubTablet
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testOpenAssignment() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]

        val assignment = data.addAssignment(courseId = course.id, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        data.addAssignmentCalendarEvent(course.id, 2.days.fromNow.iso8601, assignment.name!!, assignment.description!!, true, assignment)

        goToImportantDatesTab(data)
        importantDatesPage.assertItemDisplayed(assignment.name!!)
        importantDatesPage.clickImportantDatesItem(assignment.name!!)
        assignmentDetailsPage.verifyAssignmentDetails(assignment)
    }

    private fun goToImportantDatesTab(data: MockCanvas) {
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLoginElementary(data.domain, token, student)
        elementaryDashboardPage.waitForRender()
        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.IMPORTANT_DATES)
        //We need this to allow the ViewPager to switch tabs
        Thread.sleep(100)
    }

    private fun createMockData(
            courseCount: Int = 0,
            withGradingPeriods: Boolean = false,
            homeroomCourseCount: Int = 0): MockCanvas {

        return MockCanvas.init(
                studentCount = 1,
                courseCount = courseCount,
                withGradingPeriods = withGradingPeriods,
                homeroomCourseCount = homeroomCourseCount)
    }
}