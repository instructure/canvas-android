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

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.instructure.canvas.espresso.CanvasComposeTest
import com.instructure.canvas.espresso.common.pages.compose.CalendarEventCreateEditPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarEventDetailsPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarScreenPage
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addUserCalendarEvent
import com.instructure.canvasapi2.utils.toApiString
import org.junit.Test
import java.util.Date


abstract class CreateUpdateEventInteractionTest : CanvasComposeTest() {

    val calendarScreenPage = CalendarScreenPage(composeTestRule)
    val calendarEventDetailsPage = CalendarEventDetailsPage(composeTestRule)
    private val createUpdateEventDetailsPage = CalendarEventCreateEditPage(composeTestRule)


    /*
    @Test
    fun assertNewTitle() {
        val data = initData()

        goToCreateEvent(data)

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.typeTitle("New Title")
        createUpdateEventDetailsPage.clickSave()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("New Title").assertIsDisplayed()
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
        calendarScreenPage.selectDay(calendar.get(Calendar.DAY_OF_MONTH).toString())
        composeTestRule.onNodeWithText("New Title").assertIsDisplayed()
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

//    @Test
//    fun assertNewDescription() {
//        val data = initData()
//        val course = data.courses.values.first()
//        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)
//
//        goToCreateEvent(data)
//
//        composeTestRule.waitForIdle()
//        createUpdateEventDetailsPage.typeTitle("New Title")
//
//        createUpdateEventDetailsPage.clickSave()
//
//        composeTestRule.waitForIdle()
//        calendarScreenPage.clickOnItem("New Title")
//        calendarEventDetailsPage.assertDescription("New Description")
//    }

     */

    @Test
    fun assertUpdatedTitle() {
        val data = initData()
        val user = data.users.values.first()
        data.addUserCalendarEvent(
            userId = user.id,
            date = Date().toApiString(),
            title = "Test Event",
            description = "Test Description"
        )

        goToEditEvent(data)

        composeTestRule.waitForIdle()
        createUpdateEventDetailsPage.typeTitle("Updated Title")
        createUpdateEventDetailsPage.clickSave()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Updated Title").assertIsDisplayed()
    }

    abstract fun goToCreateEvent(data: MockCanvas)

    abstract fun goToEditEvent(data: MockCanvas)

    abstract fun initData(): MockCanvas
}
