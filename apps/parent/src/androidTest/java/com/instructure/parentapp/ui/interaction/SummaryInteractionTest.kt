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

import androidx.test.core.app.ApplicationProvider
import com.instructure.canvas.espresso.common.pages.AssignmentDetailsPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarEventDetailsPage
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCourseCalendarEvent
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.espresso.ModuleItemInteractions
import com.instructure.pandautils.utils.getDisplayDate
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.util.Calendar

@HiltAndroidTest
class SummaryInteractionTest: ParentComposeTest() {
    private val assignmentDetailsPage = AssignmentDetailsPage(ModuleItemInteractions())
    private val calendarEventDetailsPage = CalendarEventDetailsPage(composeTestRule)

    @Test
    fun testSummaryItemsAreDisplayed() {
        val data = initData()
        val course = data.courses.values.first()
        setupTabs(data, course)

        goToCourseDetails(data, course.name)
        composeTestRule.waitForIdle()

        val courseEvents = data.courseCalendarEvents.values.flatten()
        courseEvents.forEach {
            summaryPage.assertItemDisplayed(it.title!!, it.getDisplayDate(ApplicationProvider.getApplicationContext()))
        }

        val userEvents = data.userCalendarEvents.values.flatten()
        userEvents.forEach {
            summaryPage.assertItemDisplayed(it.title!!, it.getDisplayDate(ApplicationProvider.getApplicationContext()))
        }

        val assignments = data.assignments.values
        assignments.forEach {
            summaryPage.assertItemDisplayed(it.name!!, ScheduleItem(startAt = it.dueAt).getDisplayDate(ApplicationProvider.getApplicationContext()))
        }
    }

    @Test
    fun testAssignmentItemNavigation() {
        val data = initData()
        val course = data.courses.values.first()
        setupTabs(data, course)

        goToCourseDetails(data, course.name)
        composeTestRule.waitForIdle()

        val assignment = data.assignments.values.first()
        summaryPage.selectItem(assignment.name!!, ScheduleItem(startAt = assignment.dueAt).getDisplayDate(ApplicationProvider.getApplicationContext()))
        composeTestRule.waitForIdle()


        assignmentDetailsPage.assertAssignmentDetails(assignment)
    }

    @Test
    fun testCalendarItemNavigation() {
        val data = initData()
        val course = data.courses.values.first()
        setupTabs(data, course)

        goToCourseDetails(data, course.name)
        composeTestRule.waitForIdle()

        val calendarItems = data.courseCalendarEvents.values.flatten().first()
        summaryPage.selectItem(calendarItems.title!!, calendarItems.getDisplayDate(ApplicationProvider.getApplicationContext()))
        composeTestRule.waitForIdle()

        calendarEventDetailsPage.assertEventTitle(calendarItems.title!!)
    }

    private fun initData(): MockCanvas {
        return MockCanvas.init(
            parentCount = 1,
            studentCount = 1,
            courseCount = 1
        )
    }

    private fun setupTabs(data: MockCanvas, course: Course) {
        course.homePage = Course.HomePage.HOME_SYLLABUS
        course.syllabusBody = "This is the syllabus"
        data.courseTabs[course.id]?.add(Tab(tabId = Tab.SYLLABUS_ID))
        data.courseSettings[course.id] = CourseSettings(
            courseSummary = true
        )

        val dueDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.time.toApiString()
        val item = data.addCourseCalendarEvent(
            course,
            dueDate,
            "Course Calendar Event",
            "Course Calendar Event Description",
        )
        data.courseCalendarEvents[course.id] = mutableListOf(item.copy(htmlUrl = "https://${data.domain}/calendar?event_id=${item.id}"))

        data.addAssignment(course.id, dueAt = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.time.toApiString())
    }

    private fun goToCourseDetails(data: MockCanvas, courseName: String) {
        val parent = data.parents.first()
        val token = data.tokenFor(parent)!!
        tokenLogin(data.domain, token, parent)
        coursesPage.tapCurseItem(courseName)
        courseDetailsPage.selectTab("SUMMARY")
    }
}