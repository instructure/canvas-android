/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.canvas.espresso.common.interaction

import com.instructure.canvas.espresso.CanvasComposeTest
import com.instructure.canvas.espresso.common.pages.compose.CalendarEventCreateEditPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarEventDetailsPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarScreenPage
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addCourseCalendarEvent
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toApiString
import org.junit.Test
import java.util.Date


abstract class EventDetailsInteractionTest : CanvasComposeTest() {

    val calendarScreenPage = CalendarScreenPage(composeTestRule)
    private val evenDetailsPage = CalendarEventDetailsPage(composeTestRule)
    private val createUpdateEventDetailsPage = CalendarEventCreateEditPage(composeTestRule)

    override fun displaysPageObjects() = Unit

    @Test
    fun assertCanvasContext() {
        val data = initData()
        val course = data.courses.values.first()
        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Test Event",
            description = "Test Description"
        )
        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)

        goToEventDetails(data)

        composeTestRule.waitForIdle()
        evenDetailsPage.assertEventCalendar(event.contextName!!)
    }

    @Test
    fun assertTitle() {
        val data = initData()
        val course = data.courses.values.first()
        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Test Event",
            description = "Test Description"
        )
        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)

        goToEventDetails(data)

        composeTestRule.waitForIdle()
        evenDetailsPage.assertEventTitle(event.title!!)
    }

    @Test
    fun assertDate() {
        val data = initData()
        val course = data.courses.values.first()
        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Test Event",
            description = "Test Description"
        )
        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)

        goToEventDetails(data)

        composeTestRule.waitForIdle()
        evenDetailsPage.assertEventDateContains(DateHelper.getFormattedDate(activityRule.activity, event.endDate)!!)
    }

    @Test
    fun assertRecurrence() {
        val data = initData()
        val course = data.courses.values.first()
        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Test Event",
            description = "Test Description",
            rrule = "FREQ=DAILY;COUNT=365;INTERVAL=1"
        )
        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)

        goToEventDetails(data)

        composeTestRule.waitForIdle()
        evenDetailsPage.assertRecurrence(event.rrule!!)
    }

    @Test
    fun assertLocation() {
        val data = initData()
        val course = data.courses.values.first()
        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Test Event",
            description = "Test Description",
            location = "Test Location"
        )
        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)

        goToEventDetails(data)

        composeTestRule.waitForIdle()
        evenDetailsPage.assertLocationDisplayed(event.locationName!!)
    }

    @Test
    fun assertAddress() {
        val data = initData()
        val course = data.courses.values.first()
        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Test Event",
            description = "Test Description",
            address = "Test Address"
        )
        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)

        goToEventDetails(data)

        composeTestRule.waitForIdle()
        evenDetailsPage.assertAddressDisplayed(event.locationAddress!!)
    }

    @Test
    fun assertDescription() {
        val data = initData()
        val course = data.courses.values.first()
        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Test Event",
            description = "Test Description"
        )
        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)

        goToEventDetails(data)

        composeTestRule.waitForIdle()
        evenDetailsPage.verifyDescription(event.description!!)
    }

    @Test
    fun openEditEvent() {
        val data = initData()
        val course = data.courses.values.first()
        data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Test Event",
            description = "Test Description"
        )
        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)

        goToEventDetails(data)

        composeTestRule.waitForIdle()
        evenDetailsPage.clickOverflowMenu()
        evenDetailsPage.clickEditMenu()

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.assertScreenTitle("Edit Event")
    }

    @Test
    fun deleteEvent() {
        val data = initData()
        val course = data.courses.values.first()
        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Test Event",
            description = "Test Description"
        )
        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)

        goToEventDetails(data)

        composeTestRule.waitForIdle()
        evenDetailsPage.clickOverflowMenu()
        evenDetailsPage.clickDeleteMenu()

        composeTestRule.waitForIdle()
        evenDetailsPage.assertDeleteDialog()
        evenDetailsPage.confirmDelete()

        composeTestRule.waitForIdle()
        calendarScreenPage.assertItemNotExist(event.title!!)
    }

    abstract fun goToEventDetails(data: MockCanvas)

    abstract fun initData(): MockCanvas
}
