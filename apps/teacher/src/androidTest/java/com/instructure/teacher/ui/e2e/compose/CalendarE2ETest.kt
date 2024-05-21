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
        calendarScreenPage.assertItemDetails(newEventTitle, teacher.name, currentDate)

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
        calendarScreenPage.assertItemDetails(modifiedEventTitle, teacher.name, currentDate)

        Log.d(STEP_TAG, "Click on the previously created '$modifiedEventTitle' event and assert the event details.")
        calendarScreenPage.clickOnItem(modifiedEventTitle)

        Log.d(STEP_TAG, "Assert that previously given location and address values are displayed on the Event Details Page.")
        calendarEventDetailsPage.assertLocationDisplayed("Test Room 1")
        calendarEventDetailsPage.assertAddressDisplayed("Test Address 1")

        Log.d(STEP_TAG, "Click on the 'Delete' overflow menu and confirm the deletion.")
        calendarEventDetailsPage.clickOverflowMenu()
        calendarEventDetailsPage.clickDeleteMenu()
        calendarEventDetailsPage.confirmDelete()

        Log.d(STEP_TAG, "Assert that the deleted item does not exist anymore on the Calendar Screen Page.")
        calendarScreenPage.assertItemNotExist(modifiedEventTitle)

        Log.d(STEP_TAG, "Assert that after the deletion the empty view will be displayed since we don't have any events on the current day.")
        calendarScreenPage.assertEmptyView()
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CALENDAR, TestCategory.E2E)
    fun testCalendarToDoScreenE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, courses = 1)
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Click on the 'Calendar' bottom menu to navigate to the Calendar page. Assert that the page title is 'Calendar'.")
        dashboardPage.clickCalendarTab()
        calendarScreenPage.assertCalendarPageTitle()

        Log.d(STEP_TAG, "Click on the 'Add' (FAB) button and 'Add To Do' to create a new To Do.")
        calendarScreenPage.clickOnAddButton()
        calendarScreenPage.clickAddTodo()

        Log.d(STEP_TAG, "Assert that the page title is 'New To Do' as we are clicked on the 'Add To Do' button to create a new one.")
        calendarToDoCreateUpdatePage.assertPageTitle("New To Do")

        val testTodoTitle = "Test ToDo Title"
        val testTodoDescription = "Details of ToDo"
        Log.d(STEP_TAG, "Fill the title with '$testTodoTitle' and the details/description with '$testTodoDescription' and click on the 'Save' button.")
        calendarToDoCreateUpdatePage.typeTodoTitle(testTodoTitle)
        calendarToDoCreateUpdatePage.typeDetails(testTodoDescription)
        calendarToDoCreateUpdatePage.clickSave()

        Log.d(STEP_TAG, "Assert that the user has been navigated back to the Calendar Screen Page and that the previously created To Do item is displayed with the corresponding title, context and date.")
        val currentDate = getCurrentDateInCanvasCalendarFormat()
        calendarScreenPage.assertItemDetails(testTodoTitle, "To Do", "$currentDate at 12:00 PM")

        Log.d(STEP_TAG, "Clicks on the '$testTodoTitle' To Do item.")
        calendarScreenPage.clickOnItem(testTodoTitle)

        Log.d(STEP_TAG, "Assert that the title is '$testTodoTitle', the context is 'To Do', the date is the current day with 12:00 PM time and the description is '$testTodoDescription'.")
        calendarToDoDetailsPage.assertTitle(testTodoTitle)
        calendarToDoDetailsPage.assertPageTitle("To Do")
        calendarToDoDetailsPage.assertDate("$currentDate at 12:00 PM")
        calendarToDoDetailsPage.assertDescription(testTodoDescription)

        Log.d(STEP_TAG, "Click on the 'Edit To Do' within the toolbar more menu and confirm the editing.")
        calendarToDoDetailsPage.clickToolbarMenu()
        calendarToDoDetailsPage.clickEditMenu()

        Log.d(STEP_TAG, "Assert that the page title is 'Edit To Do' as we are editing an existing To Do item.")
        calendarToDoCreateUpdatePage.assertPageTitle("Edit To Do")

        Log.d(STEP_TAG, "Assert that the 'original' To Do Title and details has been filled into the input fields as we on the edit screen.")
        calendarToDoCreateUpdatePage.assertTodoTitle(testTodoTitle)
        calendarToDoCreateUpdatePage.assertDetails(testTodoDescription)

        val modifiedTestTodoTitle = "Test ToDo Title Mod"
        val modifiedTestTodoDescription = "Details of ToDo Mod"
        Log.d(STEP_TAG, "Modify the title with '$modifiedTestTodoTitle' and the details/description with '$modifiedTestTodoDescription' and click on the 'Save' button.")
        calendarToDoCreateUpdatePage.typeTodoTitle(modifiedTestTodoTitle)
        calendarToDoCreateUpdatePage.typeDetails(modifiedTestTodoDescription)
        calendarToDoCreateUpdatePage.clickSave()

        Log.d(STEP_TAG, "Assert that the user has been navigated back to the Calendar Screen Page and that the previously modified To Do item is displayed with the corresponding title, context and with the same date as we haven't changed it.")
        calendarScreenPage.assertItemDetails(modifiedTestTodoTitle, "To Do", "$currentDate at 12:00 PM")

        Log.d(STEP_TAG, "Clicks on the '$modifiedTestTodoTitle' To Do item.")
        calendarScreenPage.clickOnItem(modifiedTestTodoTitle)

        Log.d(STEP_TAG, "Assert that the To Do title is '$modifiedTestTodoTitle', the page title is 'To Do', the date remained current day with 12:00 PM time (as we haven't modified it) and the description is '$modifiedTestTodoDescription'.")
        calendarToDoDetailsPage.assertTitle(modifiedTestTodoTitle)
        calendarToDoDetailsPage.assertPageTitle("To Do")
        calendarToDoDetailsPage.assertDate("$currentDate at 12:00 PM")
        calendarToDoDetailsPage.assertDescription(modifiedTestTodoDescription)

        Log.d(STEP_TAG, "Click on the 'Delete To Do' within the toolbar more menu and confirm the deletion.")
        calendarToDoDetailsPage.clickToolbarMenu()
        calendarToDoDetailsPage.clickDeleteMenu()
        calendarToDoDetailsPage.confirmDeletion()

        Log.d(STEP_TAG, "Assert that the deleted item does not exist anymore on the Calendar Screen Page.")
        calendarScreenPage.assertItemNotExist(modifiedTestTodoTitle)

        Log.d(STEP_TAG, "Assert that after the deletion the empty view will be displayed since we don't have any To Do items on the current day.")
        calendarScreenPage.assertEmptyView()
    }
}