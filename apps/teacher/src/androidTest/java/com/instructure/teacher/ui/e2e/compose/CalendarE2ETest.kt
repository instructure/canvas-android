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
package com.instructure.teacher.ui.e2e.compose

import android.util.Log
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.espresso.getCurrentDateInCanvasCalendarFormat
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.clickCalendarTab
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.time.Year

@HiltAndroidTest
class CalendarE2ETest: TeacherComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CALENDAR, TestCategory.E2E)
    fun testCalendarEventScreenE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, courses = 1)
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Click on the 'Calendar' bottom menu to navigate to the Calendar page. Assert that the page title is 'Calendar'.")
        dashboardPage.clickCalendarTab()
        calendarScreenPage.assertCalendarPageTitle()

        Log.d(STEP_TAG, "Click on the 'Add' (FAB) button and 'Add Event' to create a new event.")
        calendarScreenPage.clickOnAddButton()
        calendarScreenPage.clickAddEvent()

        Log.d(STEP_TAG, "Assert that the Calendar Event Page is displayed and the title is 'New Event' as we are making a new one.")
        calendarEventCreateEditPage.assertTitle("New Event")

        Log.d(STEP_TAG, "Type 'New Test Event' to the title input field and click on 'Save'.")
        val newEventTitle = "New Test Event"
        calendarEventCreateEditPage.typeTitle(newEventTitle)
        calendarEventCreateEditPage.clickSave()

        Log.d(STEP_TAG, "Assert that the event is displayed with the corresponding details (title, context name, date, status) on the page.")
        var currentDate = getCurrentDateInCanvasCalendarFormat()
        calendarScreenPage.assertEventDetails(newEventTitle, teacher.name, currentDate)

        Log.d(STEP_TAG, "Click on the previously created '$newEventTitle' event and assert the event details.")
        calendarScreenPage.clickOnItem(newEventTitle)

        Log.d(STEP_TAG, "Assert that the Calendar Event Details Page is displayed and the title is 'Event'.")
        calendarEventDetailsPage.assertEventDetailsPageTitle()

        Log.d(STEP_TAG, "Assert that the event title is '$newEventTitle' and the date is the current day (and current year).")
        calendarEventDetailsPage.assertEventTitle(newEventTitle)
        currentDate += ", " + Year.now().toString()
        calendarEventDetailsPage.assertEventDate(currentDate)

        Log.d(STEP_TAG, "Assert that neither the 'Location' and 'Address' sections are not displayed since they are not filled.")
        calendarEventDetailsPage.assertLocationNotDisplayed()
        calendarEventDetailsPage.assertAddressNotDisplayed()

        Log.d(STEP_TAG, "Click on the 'Edit' overflow menu.")
        calendarEventDetailsPage.clickOverflowMenu()
        calendarEventDetailsPage.clickEditMenu()

        Log.d(STEP_TAG, "Assert that the Calendar Event Page is displayed and the title is 'New Event' as we are editing an existing one.")
        calendarEventCreateEditPage.assertTitle("Edit Event")

        val modifiedEventTitle = "Modified Test Event"
        Log.d(STEP_TAG, "Type '$modifiedEventTitle' to the title, 'Test Room 1' to the 'Location', 'Test Address 1' to the 'Address' input fields.")
        calendarEventCreateEditPage.typeTitle(modifiedEventTitle)
        calendarEventCreateEditPage.typeLocation("Test Room 1")
        calendarEventCreateEditPage.typeAddress("Test Address 1")
        calendarEventCreateEditPage.clickSave()

        Log.d(STEP_TAG, "Assert that the event is displayed with the corresponding modified details (title, context name, date) on the page.")
        currentDate = getCurrentDateInCanvasCalendarFormat()
        calendarScreenPage.assertEventDetails(modifiedEventTitle, teacher.name, currentDate)

        Log.d(STEP_TAG, "Click on the previously created '$modifiedEventTitle' event and assert the event details.")
        calendarScreenPage.clickOnItem(modifiedEventTitle)

        Log.d(STEP_TAG, "Assert that previously given location and address values are displayed on the Event Details Page.")
        calendarEventDetailsPage.assertLocationDisplayed("Test Room 1")
        calendarEventDetailsPage.assertAddressDisplayed("Test Address 1")

        Log.d(STEP_TAG, "Click on the 'Delete' overflow menu and confirm the deletion.")
        calendarEventDetailsPage.clickOverflowMenu()
        calendarEventDetailsPage.clickDeleteMenu()
        calendarEventDetailsPage.confirmDelete()

        Log.d(STEP_TAG, "Assert that after the deletion the empty view will be displayed since we don't have any events on the current day.")
        calendarScreenPage.assertEmptyEventsView()
    }
}