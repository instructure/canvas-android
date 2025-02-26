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
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addUserCalendarEvent
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toApiString
import org.junit.Test
import java.util.Calendar
import java.util.Date


abstract class CreateUpdateEventInteractionTest : CanvasComposeTest() {

    val calendarScreenPage = CalendarScreenPage(composeTestRule)
    val calendarEventDetailsPage = CalendarEventDetailsPage(composeTestRule)
    private val createUpdateEventDetailsPage = CalendarEventCreateEditPage(composeTestRule)

    override fun displaysPageObjects() = Unit

    @Test
    fun assertNewTitle() {
        val data = initData()

        goToCreateEvent(data)

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.assertScreenTitle("New Event")
        createUpdateEventDetailsPage.typeTitle("New Title")
        createUpdateEventDetailsPage.clickSave()

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.assertTitle("New Title")
    }

    @Test
    fun assertNewDate() {
        val data = initData()

        goToCreateEvent(data)

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.typeTitle("New Title")
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        createUpdateEventDetailsPage.selectDate(calendar)
        createUpdateEventDetailsPage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.swipeEventsLeft()
        createUpdateEventDetailsPage.assertTitle("New Title")
    }

    @Test
    fun assertNewTimeFrom() {
        val data = initData()
        val course = data.courses.values.first()
        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)

        goToCreateEvent(data)

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.typeTitle("New Title")
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 15)
        }
        createUpdateEventDetailsPage.selectTime("From", calendar)
        createUpdateEventDetailsPage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.clickOnItem("New Title")
        calendarEventDetailsPage.assertEventDateContains("12:15 PM")
    }

    @Test
    fun assertNewTimeTo() {
        val data = initData()
        val course = data.courses.values.first()
        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)

        goToCreateEvent(data)

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.typeTitle("New Title")
        createUpdateEventDetailsPage.selectTime("From", Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 15)
        })
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 45)
        }
        createUpdateEventDetailsPage.selectTime("To", calendar)
        createUpdateEventDetailsPage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.clickOnItem("New Title")
        calendarEventDetailsPage.assertEventDateContains("12:45 PM")
    }

    @Test
    fun assertNewFrequency() {
        val data = initData()
        val course = data.courses.values.first()
        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)

        goToCreateEvent(data)

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.typeTitle("New Title")
        createUpdateEventDetailsPage.selectFrequency("Daily")
        createUpdateEventDetailsPage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.clickOnItem("New Title")
        calendarEventDetailsPage.assertRecurrence("FREQ=DAILY;COUNT=365;INTERVAL=1")
    }

    @Test
    fun assertNewLocation() {
        val data = initData()
        val course = data.courses.values.first()
        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)

        goToCreateEvent(data)

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.typeTitle("New Title")
        createUpdateEventDetailsPage.typeLocation("New Location")
        createUpdateEventDetailsPage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.clickOnItem("New Title")
        calendarEventDetailsPage.assertLocationDisplayed("New Location")
    }

    @Test
    fun assertNewAddress() {
        val data = initData()
        val course = data.courses.values.first()
        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)

        goToCreateEvent(data)

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.typeTitle("New Title")
        createUpdateEventDetailsPage.typeAddress("New Address")
        createUpdateEventDetailsPage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.clickOnItem("New Title")
        calendarEventDetailsPage.assertAddressDisplayed("New Address")
    }

    @Test
    fun assertUpdatedTitle() {
        val data = initData()
        val user = getLoggedInUser()
        data.addUserCalendarEvent(
            userId = user.id,
            startDate = Date().toApiString(),
            endDate = null,
            title = "Test Event",
            description = "Test Description"
        )

        goToEditEvent(data)

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.assertScreenTitle("Edit Event")
        createUpdateEventDetailsPage.typeTitle("Updated Title")
        createUpdateEventDetailsPage.clickSave()

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.assertTitle("Updated Title")
    }

    @Test
    fun assertUpdatedDate() {
        val data = initData()
        val user = getLoggedInUser()
        val event = data.addUserCalendarEvent(
            userId = user.id,
            startDate = Date().toApiString(),
            endDate = null,
            title = "Test Event",
            description = "Test Description"
        )

        goToEditEvent(data)

        composeTestRule.waitForIdle()
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        createUpdateEventDetailsPage.selectDate(calendar)
        createUpdateEventDetailsPage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.swipeEventsLeft()
        createUpdateEventDetailsPage.assertTitle(event.title!!)
    }

    @Test
    fun assertUpdatedFrom() {
        val data = initData()
        val user = getLoggedInUser()
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 15)
        }
        val end = start.apply { add(Calendar.HOUR, 1) }
        val event = data.addUserCalendarEvent(
            userId = user.id,
            startDate = start.time.toApiString(),
            endDate = end.time.toApiString(),
            title = "Test Event",
            description = "Test Description"
        )

        goToEditEvent(data)

        composeTestRule.waitForIdle()
        val updatedStart = start.apply { add(Calendar.HOUR, -1) }
        createUpdateEventDetailsPage.selectTime("From", updatedStart)
        createUpdateEventDetailsPage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.clickOnItem(event.title!!)
        composeTestRule.waitForIdle()
        val expectedTime = DateHelper.getFormattedTime(activityRule.activity, updatedStart.time)
        calendarEventDetailsPage.assertEventDateContains(expectedTime!!)
    }

    @Test
    fun assertUpdatedTo() {
        val data = initData()
        val user = getLoggedInUser()
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 15)
        }
        val end = start.apply { add(Calendar.HOUR, 1) }
        val event = data.addUserCalendarEvent(
            userId = user.id,
            startDate = start.time.toApiString(),
            endDate = end.time.toApiString(),
            title = "Test Event",
            description = "Test Description"
        )

        goToEditEvent(data)

        composeTestRule.waitForIdle()
        val updatedEnd = end.apply { add(Calendar.HOUR, 1) }
        createUpdateEventDetailsPage.selectTime("To", updatedEnd)
        createUpdateEventDetailsPage.clickSave()

        composeTestRule.waitForIdle()

        calendarScreenPage.clickOnItem(event.title!!)
        val expectedTime = DateHelper.getFormattedTime(activityRule.activity, updatedEnd.time)
        calendarEventDetailsPage.assertEventDateContains(expectedTime!!)
    }

    @Test
    fun assertUpdatedFrequency() {
        val data = initData()
        val user = getLoggedInUser()
        val event = data.addUserCalendarEvent(
            userId = user.id,
            startDate = Date().toApiString(),
            endDate = null,
            title = "Test Event",
            description = "Test Description"
        )

        goToEditEvent(data)

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.selectFrequency("Daily")
        createUpdateEventDetailsPage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.clickOnItem(event.title!!)
        calendarEventDetailsPage.assertRecurrence("FREQ=DAILY;COUNT=365;INTERVAL=1")
    }

    @Test
    fun assertUpdatedLocation() {
        val data = initData()
        val user = getLoggedInUser()
        val event = data.addUserCalendarEvent(
            userId = user.id,
            startDate = Date().toApiString(),
            endDate = null,
            title = "Test Event",
            description = "Test Description"
        )

        goToEditEvent(data)

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.typeLocation("Updated Location")
        createUpdateEventDetailsPage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.clickOnItem(event.title!!)
        calendarEventDetailsPage.assertLocationDisplayed("Updated Location")
    }

    @Test
    fun assertUpdatedAddress() {
        val data = initData()
        val user = getLoggedInUser()
        val event = data.addUserCalendarEvent(
            userId = user.id,
            startDate = Date().toApiString(),
            endDate = null,
            title = "Test Event",
            description = "Test Description"
        )

        goToEditEvent(data)

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.typeAddress("Updated Address")
        createUpdateEventDetailsPage.clickSave()

        composeTestRule.waitForIdle()
        calendarScreenPage.clickOnItem(event.title!!)
        calendarEventDetailsPage.assertAddressDisplayed("Updated Address")
    }

    @Test
    fun assertUnsavedChangesDialog() {
        val data = initData()
        val user = getLoggedInUser()
        data.addUserCalendarEvent(
            userId = user.id,
            startDate = Date().toApiString(),
            endDate = null,
            title = "Test Event",
            description = "Test Description"
        )

        goToEditEvent(data)

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.typeTitle("Updated Title")
        createUpdateEventDetailsPage.clickClose()

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.assertUnsavedChangesDialog()
    }

    @Test
    fun saveDisabledWhenTitleBlank() {
        val data = initData()
        goToCreateEvent(data)

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.typeTitle("  ")
        createUpdateEventDetailsPage.assertSaveDisabled()
    }

    @Test
    fun saveEnabledWhenTitleIsNotBlank() {
        val data = initData()
        goToCreateEvent(data)

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.typeTitle("New Title")
        createUpdateEventDetailsPage.assertSaveEnabled()
    }

    abstract fun goToCreateEvent(data: MockCanvas)

    abstract fun goToEditEvent(data: MockCanvas)

    abstract fun initData(): MockCanvas

    abstract fun getLoggedInUser(): User
}
