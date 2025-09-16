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

import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.StubTablet
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addAssignmentCalendarEvent
import com.instructure.canvas.espresso.mockCanvas.addCourseCalendarEvent
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.student.ui.pages.classic.k5.ElementaryDashboardPage
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.tokenLoginElementary
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltAndroidTest
@UninstallModules(CustomGradeStatusModule::class)
class ImportantDatesInteractionTest : StudentComposeTest() {

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    override fun displaysPageObjects() = Unit

    @Test
    @StubTablet(description = "The UI is different on tablet, so we only check the phone version")
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testShowCalendarEvents() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]

        val event = data.addCourseCalendarEvent(course, 2.days.fromNow.iso8601, "Important event", "Important event description", true)

        goToImportantDatesTab(data)
        importantDatesPage.assertItemDisplayed(event.title!!)
        importantDatesPage.assertRecyclerViewItemCount(1)
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(event.startDate))
    }

    @Test
    @StubTablet(description = "The UI is different on tablet, so we only check the phone version")
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testShowAssignment() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]

        val assignment = data.addAssignment(courseId = course.id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY))
        val assignmentScheduleItem = data.addAssignmentCalendarEvent(course.id, 2.days.fromNow.iso8601, assignment.name!!, assignment.description!!, true, assignment)

        goToImportantDatesTab(data)
        importantDatesPage.assertItemDisplayed(assignment.name!!)
        importantDatesPage.assertRecyclerViewItemCount(1)
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(assignmentScheduleItem.startDate))
    }

    @Test
    @StubTablet(description = "The UI is different on tablet, so we only check the phone version")
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testEmptyView() {
        val data = createMockData(courseCount = 1)

        goToImportantDatesTab(data)

        importantDatesPage.assertEmptyViewDisplayed()
    }

    @Test
    @StubTablet(description = "The UI is different on tablet, so we only check the phone version")
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testPullToRefresh() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]
        val existedEventBeforeRefresh = data.addCourseCalendarEvent(course, 2.days.fromNow.iso8601, "Important event", "Important event description", true)

        goToImportantDatesTab(data)
        val eventToCheck = data.addCourseCalendarEvent(course, 2.days.fromNow.iso8601, "Important event 2", "Important event 2 description", true)

        importantDatesPage.assertRecyclerViewItemCount(1)
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(existedEventBeforeRefresh.startDate))

        //Refresh the page and verify if the previously not displayed event will be displayed after the refresh.
        importantDatesPage.pullToRefresh()
        importantDatesPage.assertItemDisplayed(eventToCheck.title!!)
        importantDatesPage.assertRecyclerViewItemCount(2)
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(eventToCheck.startDate))
    }

    @Test
    @StubTablet(description = "The UI is different on tablet, so we only check the phone version")
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testOpenCalendarEvent() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]
        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)
        val event = data.addCourseCalendarEvent(course, 2.days.fromNow.iso8601, "Important event", "Important event description", true)

        goToImportantDatesTab(data)

        importantDatesPage.assertItemDisplayed(event.title!!)
        importantDatesPage.assertRecyclerViewItemCount(1)

        //Opening the calendar event
        importantDatesPage.clickImportantDatesItem(event.title!!)
        calendarEventDetailsPage.assertEventTitle(event.title!!)
        calendarEventDetailsPage.verifyDescription(event.description!!)
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(event.startDate))
    }

    @Test
    @StubTablet(description = "The UI is different on tablet, so we only check the phone version")
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testOpenAssignment() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]

        val assignment = data.addAssignment(courseId = course.id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY))
        val assignmentScheduleItem = data.addAssignmentCalendarEvent(course.id, 2.days.fromNow.iso8601, assignment.name!!, assignment.description!!, true, assignment)

        goToImportantDatesTab(data)
        importantDatesPage.assertItemDisplayed(assignment.name!!)
        importantDatesPage.assertRecyclerViewItemCount(1) // We count both day texts and calendar events here, since both types are part of the recyclerView.

        //Opening the calendar assignment event
        importantDatesPage.clickImportantDatesItem(assignment.name!!)
        assignmentDetailsPage.assertAssignmentDetails(assignment)
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(assignmentScheduleItem.startDate))
    }

    @Test
    @StubTablet(description = "The UI is different on tablet, so we only check the phone version")
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testShowMultipleCalendarEventsOnSameDay() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]

        val assignment = data.addAssignment(courseId = course.id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY))
        data.addAssignmentCalendarEvent(course.id, 2.days.fromNow.iso8601, assignment.name!!, assignment.description!!, true, assignment)
        val calendarEvent = data.addCourseCalendarEvent(course, 2.days.fromNow.iso8601, "Important event", "Important event description", true)

        val items = data.courseCalendarEvents

        goToImportantDatesTab(data)

        items.forEach { courseItems ->
            courseItems.value.forEach {
                importantDatesPage.assertItemDisplayed(it.title!!)
            }
        }
        importantDatesPage.assertRecyclerViewItemCount(2)
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(calendarEvent.startDate))
    }

    @Test
    @StubTablet(description = "The UI is different on tablet, so we only check the phone version")
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testMultipleCalendarEventsOnDifferentDays() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]

        val assignment = data.addAssignment(courseId = course.id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY))
        val twoDaysFromNowEvent = data.addAssignmentCalendarEvent(course.id,
            2.days.fromNow.iso8601, "Important event two days later", "Important event two days later description", true, assignment)
        val threeDaysFromNowEvent = data.addCourseCalendarEvent(course,
            3.days.fromNow.iso8601, "Important event three days later", "Important event three days later description", true)
        val todayEvent = data.addCourseCalendarEvent(course,
            0.days.fromNow.iso8601, "Important event Today", "Important event today description", true)

        val items = data.courseCalendarEvents

        goToImportantDatesTab(data)

        items.forEach { courseItems ->
            courseItems.value.forEach {
                importantDatesPage.assertItemDisplayed(it.title!!)
            }
        }

        importantDatesPage.assertDayTextIsDisplayed(generateDayString(todayEvent.startDate))
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(twoDaysFromNowEvent.startDate))
        importantDatesPage.assertItemDisplayed(generateDayString(threeDaysFromNowEvent.startDate))
        importantDatesPage.assertRecyclerViewItemCount(3)
        importantDatesPage.swipeUp() // Need to do this because on landscape mode the last item cannot be seen on the view by default.
        importantDatesPage.assertDayTextIsDisplayed(generateDayString(threeDaysFromNowEvent.startDate))
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

    private fun generateDayString(date: Date?): String {
        return SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(date)
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