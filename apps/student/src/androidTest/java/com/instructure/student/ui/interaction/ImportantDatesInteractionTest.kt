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
import java.util.*

@HiltAndroidTest
class ImportantDatesInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit

    @Test
    @StubTablet(description = "The UI is different on tablet, so we only check the phone version")
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testShowCalendarEvents() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]

        val event = data.addCourseCalendarEvent(course.id, 2.days.fromNow.iso8601, "Important event", "Important event description", true)
        val twoDaysFromNowCalendar = getCustomDateCalendar(2)

        goToImportantDatesTab(data)
        importantDatesPage.assertItemDisplayed(event.title!!)
        importantDatesPage.assertRecyclerViewItemCount(2) // We count both day texts and calendar events here, since both types are part of the recyclerView.
        importantDatesPage.assertDayTextIsDisplayed(concatDayString(twoDaysFromNowCalendar))
    }

    @Test
    @StubTablet(description = "The UI is different on tablet, so we only check the phone version")
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testShowAssignment() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]

        val assignment = data.addAssignment(courseId = course.id, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        data.addAssignmentCalendarEvent(course.id, 2.days.fromNow.iso8601, assignment.name!!, assignment.description!!, true, assignment)
        val twoDaysFromNowCalendar = getCustomDateCalendar(2)

        goToImportantDatesTab(data)
        importantDatesPage.assertItemDisplayed(assignment.name!!)
        importantDatesPage.assertRecyclerViewItemCount(2) // We count both day texts and calendar events here, since both types are part of the recyclerView.
        importantDatesPage.assertDayTextIsDisplayed(concatDayString(twoDaysFromNowCalendar))
    }

    @Test
    @StubTablet(description = "The UI is different on tablet, so we only check the phone version")
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testEmptyView() {
        val data = createMockData(courseCount = 1)

        goToImportantDatesTab(data)

        importantDatesPage.assertEmptyViewDisplayed()
    }

    @Test
    @StubTablet(description = "The UI is different on tablet, so we only check the phone version")
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testPullToRefresh() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]
        data.addCourseCalendarEvent(course.id, 2.days.fromNow.iso8601, "Important event", "Important event description", true)
        val twoDaysFromNowCalendar = getCustomDateCalendar(2)

        goToImportantDatesTab(data)
        val eventToCheck = data.addCourseCalendarEvent(course.id, 2.days.fromNow.iso8601, "Important event 2", "Important event 2 description", true)

        importantDatesPage.assertItemDisplayed(eventToCheck.title!!)
        importantDatesPage.assertRecyclerViewItemCount(2) // We count both day texts and calendar events here, since both types are part of the recyclerView.
        importantDatesPage.assertDayTextIsDisplayed(concatDayString(twoDaysFromNowCalendar))

        //Refresh the page and verify if the previously displayed event will be displayed after the refresh.
        importantDatesPage.pullToRefresh()
        importantDatesPage.assertItemDisplayed(eventToCheck.title!!)
        importantDatesPage.assertRecyclerViewItemCount(2) // We count both day texts and calendar events here, since both types are part of the recyclerView.
        importantDatesPage.assertDayTextIsDisplayed(concatDayString(twoDaysFromNowCalendar))
    }

    @Test
    @StubTablet(description = "The UI is different on tablet, so we only check the phone version")
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testOpenCalendarEvent() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]
        val event = data.addCourseCalendarEvent(course.id, 2.days.fromNow.iso8601, "Important event", "Important event description", true)
        val twoDaysFromNowCalendar = getCustomDateCalendar(2)

        goToImportantDatesTab(data)

        importantDatesPage.assertItemDisplayed(event.title!!)
        importantDatesPage.assertRecyclerViewItemCount(2) // We count both day texts and calendar events here, since both types are part of the recyclerView.

        //Opening the calendar event
        importantDatesPage.clickImportantDatesItem(event.title!!)
        calendarEventPage.verifyTitle(event.title!!)
        calendarEventPage.verifyDescription(event.description!!)
        importantDatesPage.assertDayTextIsDisplayed(concatDayString(twoDaysFromNowCalendar))
    }

    @Test
    @StubTablet(description = "The UI is different on tablet, so we only check the phone version")
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testOpenAssignment() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]

        val assignment = data.addAssignment(courseId = course.id, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        data.addAssignmentCalendarEvent(course.id, 2.days.fromNow.iso8601, assignment.name!!, assignment.description!!, true, assignment)
        val twoDaysFromNowCalendar = getCustomDateCalendar(2)

        goToImportantDatesTab(data)
        importantDatesPage.assertItemDisplayed(assignment.name!!)
        importantDatesPage.assertRecyclerViewItemCount(2) // We count both day texts and calendar events here, since both types are part of the recyclerView.

        //Opening the calendar assignment event
        importantDatesPage.clickImportantDatesItem(assignment.name!!)
        assignmentDetailsPage.verifyAssignmentDetails(assignment)
        importantDatesPage.assertDayTextIsDisplayed(concatDayString(twoDaysFromNowCalendar))
    }

    @Test
    @StubTablet(description = "The UI is different on tablet, so we only check the phone version")
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testShowMultipleCalendarEventsOnSameDay() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]

        val assignment = data.addAssignment(courseId = course.id, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        data.addAssignmentCalendarEvent(course.id, 2.days.fromNow.iso8601, assignment.name!!, assignment.description!!, true, assignment)
        data.addCourseCalendarEvent(course.id, 2.days.fromNow.iso8601, "Important event", "Important event description", true)

        val items = data.courseCalendarEvents
        val twoDaysFromNowCalendar = getCustomDateCalendar(2)

        goToImportantDatesTab(data)

        items.forEach { courseItems ->
            courseItems.value.forEach {
                importantDatesPage.assertItemDisplayed(it.title!!)
            }
        }
        importantDatesPage.assertRecyclerViewItemCount(3) // We count both day texts and calendar events here, since both types are part of the recyclerView.
        importantDatesPage.assertDayTextIsDisplayed(concatDayString(twoDaysFromNowCalendar))
    }

    @Test
    @StubTablet(description = "The UI is different on tablet, so we only check the phone version")
    @TestMetaData(Priority.P1, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testMultipleCalendarEventsOnDifferentDays() {
        val data = createMockData(courseCount = 1)
        val course = data.courses.values.toList()[0]

        val assignment = data.addAssignment(courseId = course.id, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        data.addAssignmentCalendarEvent(course.id,
            2.days.fromNow.iso8601, assignment.name!!, assignment.description!!, true, assignment)
        data.addCourseCalendarEvent(course.id,
            3.days.fromNow.iso8601, "Important event", "Important event description", true)
        data.addCourseCalendarEvent(course.id,
            0.days.fromNow.iso8601, "Important event Today", "Important event today description", true)

        val twoDaysFromNowCalendar = getCustomDateCalendar(2)
        val threeDaysFromNowCalendar = getCustomDateCalendar(3)
        val todayCalendar = getCustomDateCalendar(0)

        val items = data.courseCalendarEvents

        goToImportantDatesTab(data)

        items.forEach { courseItems ->
            courseItems.value.forEach {
                importantDatesPage.assertItemDisplayed(it.title!!)
            }
        }

        importantDatesPage.assertDayTextIsDisplayed(concatDayString(todayCalendar))
        importantDatesPage.assertDayTextIsDisplayed(concatDayString(twoDaysFromNowCalendar))
        importantDatesPage.assertDayTextIsDisplayed(concatDayString(threeDaysFromNowCalendar))
        importantDatesPage.assertRecyclerViewItemCount(6) // We count both day texts and calendar events here, since both types are part of the recyclerView.
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

    private fun concatDayString(calendar: Calendar): String {
        val dayOfMonthIntValue = calendar.get(Calendar.DAY_OF_MONTH)
        val weekDayString = getWeekDayString(calendar.get(Calendar.DAY_OF_WEEK))
        return if(dayOfMonthIntValue < 10) weekDayString + ", " + calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " 0" + calendar.get(Calendar.DAY_OF_MONTH).toString()
        else weekDayString + ", " + calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " " + calendar.get(Calendar.DAY_OF_MONTH).toString()
    }

    private fun getCustomDateCalendar(dayDiffFromToday: Int): Calendar {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.add(Calendar.DATE, dayDiffFromToday)
        cal.set(Calendar.HOUR_OF_DAY, 10)
        cal.set(Calendar.MINUTE, 1)
        cal.set(Calendar.SECOND, 1)
        return cal
    }

    private fun getWeekDayString(weekdayIntValue: Int): String {
        return when (weekdayIntValue) {
            1 -> "Sunday"
            2 -> "Monday"
            3 -> "Tuesday"
            4 -> "Wednesday"
            5 -> "Thursday"
            6 -> "Friday"
            7 -> "Saturday"
            else -> ""
        }
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