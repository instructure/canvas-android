/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 */
package com.instructure.parentapp.ui.e2e

import android.util.Log
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.dataseeding.api.CalendarEventApi
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.model.EnrollmentTypes.STUDENT_ENROLLMENT
import com.instructure.dataseeding.model.EnrollmentTypes.TEACHER_ENROLLMENT
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.espresso.getDateInCanvasCalendarFormat
import com.instructure.pandautils.features.calendar.CalendarPrefs
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.seedData
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test
import java.time.Year
import java.util.Calendar
import java.util.Date

@HiltAndroidTest
class CalendarE2ETest : ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @Before
    fun clearPreferences() {
        CalendarPrefs.clearPrefs()
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CALENDAR, TestCategory.E2E)
    fun testCalendarEventScreenE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, parents = 1, courses = 1)
        val student = data.studentsList[0]
        val student2 = data.studentsList[1]
        val parent = data.parentsList[0]

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        val selectedShortName = if (student.sortableName < student2.sortableName) {
            student.shortName
        } else {
            student2.shortName
        }

        Log.d(STEP_TAG, "Click on the 'Calendar' bottom menu to navigate to the Calendar page.")
        dashboardPage.clickCalendarBottomMenu()

        Log.d(ASSERTION_TAG, "Assert that the student selector is displayed and the selected student is that the one is the first ordered by 'sortableName'.")
        dashboardPage.assertSelectedStudent(selectedShortName)

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
        var currentDate = getDateInCanvasCalendarFormat()
        calendarScreenPage.assertItemDetails(newEventTitle, parent.name, currentDate)

        Log.d(STEP_TAG, "Click on the previously created '$newEventTitle' event and assert the event details.")
        calendarScreenPage.clickOnItem(newEventTitle)

        Log.d(STEP_TAG, "Assert that the Calendar Event Details Page is displayed and the title is 'Event'.")
        calendarEventDetailsPage.assertEventDetailsPageTitle()

        Log.d(STEP_TAG, "Assert that the event title is '$newEventTitle' and the date is the current day (and current year).")
        calendarEventDetailsPage.assertEventTitle(newEventTitle)
        currentDate += ", " + Year.now().toString()
        calendarEventDetailsPage.assertEventDateContains(currentDate)

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
        currentDate = getDateInCanvasCalendarFormat()
        calendarScreenPage.assertItemDetails(modifiedEventTitle, parent.name, currentDate)

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
        calendarScreenPage.assertEmptyView()
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CALENDAR, TestCategory.E2E)
    fun testCalendarToDoScreenE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, parents = 1, courses = 1)
        val student = data.studentsList[0]
        val student2 = data.studentsList[1]
        val parent = data.parentsList[0]

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        val selectedShortName = if (student.sortableName < student2.sortableName) {
            student.shortName
        } else {
            student2.shortName
        }

        Log.d(STEP_TAG, "Click on the 'Calendar' bottom menu to navigate to the Calendar page.")
        dashboardPage.clickCalendarBottomMenu()

        Log.d(ASSERTION_TAG, "Assert that the student selector is displayed and the selected student is that the one is the first ordered by 'sortableName'.")
        dashboardPage.assertSelectedStudent(selectedShortName)

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
        val currentDate = getDateInCanvasCalendarFormat()
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

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CALENDAR, TestCategory.E2E)
    fun testCalendarScreenE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, parents = 1, courses = 2)
        val course = data.coursesList[0]
        val parent = data.parentsList[0]

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Click on the 'Calendar' bottom menu to navigate to the Calendar page. Assert that there is no item in Calendar yet.")
        dashboardPage.clickCalendarBottomMenu()
        calendarScreenPage.assertEmptyView()

        Log.d(STEP_TAG, "Assert that the Calendar is collapsed and only 1 week is displayed in this state.")
        calendarScreenPage.assertCalendarCollapsed()

        Log.d(STEP_TAG, "Click on the calendar header (Year and month string) to collapse the calendar. Assert that the Calendar is expanded and multiple weeks are displayed in this state.")
        calendarScreenPage.clickCalendarHeader()
        calendarScreenPage.assertCalendarExpanded()

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
        val currentDate = getDateInCanvasCalendarFormat()
        calendarScreenPage.assertItemDetails(newEventTitle, parent.name, currentDate)

        Log.d(STEP_TAG, "Click on the 'Add' (FAB) button and 'Add To Do' to create a new To Do.")
        calendarScreenPage.clickOnAddButton()
        calendarScreenPage.clickAddTodo()

        Log.d(STEP_TAG, "Assert that the page title is 'New To Do' as we are clicked on the 'Add To Do' button to create a new one.")
        calendarToDoCreateUpdatePage.assertPageTitle("New To Do")

        val testTodoTitle = "Test ToDo Title"
        val testTodoDescription = "Details of ToDo"

        Log.d(STEP_TAG, "Fill the title with '$testTodoTitle' and the details/description with '$testTodoDescription'.")
        calendarToDoCreateUpdatePage.typeTodoTitle(testTodoTitle)
        calendarToDoCreateUpdatePage.typeDetails(testTodoDescription)

        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 2) }
        Log.d(STEP_TAG, "Select a date which is 2 days in the future from today and select '${course.name}' course as the canvas context.")
        calendarToDoCreateUpdatePage.selectDate(calendar)
        Thread.sleep(2000)
        calendarToDoCreateUpdatePage.selectCanvasContext(course.name)

        Log.d(STEP_TAG, "Click on the 'Save' button.")
        calendarToDoCreateUpdatePage.clickSave()

        Log.d(STEP_TAG, "Assert that the created To Do item is not displayed on today's calendar.")
        calendarScreenPage.assertItemNotExist(testTodoTitle) //It's created for 2 days from today so it shouldn't displayed for today.

        Log.d(STEP_TAG, "Swipe the calendar item 'body' to 2 days in the future from now.")
        calendarScreenPage.swipeEventsLeft()
        calendarScreenPage.swipeEventsLeft()

        Log.d(STEP_TAG, "Assert that the '$testTodoTitle' To Do item is displayed because we created it to this particular day. Assert that '$newEventTitle' calendar event is not displayed because it's created for today.")
        calendarScreenPage.assertItemDisplayed(testTodoTitle)
        calendarScreenPage.assertItemNotExist(newEventTitle)

        Log.d(STEP_TAG, "Click on 'Calendars' to open the Calendar Filter Page.")
        calendarScreenPage.clickCalendarFilters()

        Log.d(STEP_TAG, "Click on the checkbox before the '${course.name}' course to filter it out from the calendar and close the Calendar Filter Page.")
        calendarFilterPage.clickOnFilterItem(course.name)
        calendarFilterPage.closeFilterPage()

        Log.d(STEP_TAG, "Assert that the '$testTodoTitle' To Do item is not displayed because we filtered out from the calendar. Assert that the empty view is displayed because there are no items for today.")
        calendarScreenPage.assertItemNotExist(testTodoTitle)
        calendarScreenPage.assertEmptyView()

        Log.d(STEP_TAG, "Click on the 'Today' button to navigate back to the current day.")
        dashboardPage.clickTodayButton()

        Log.d(STEP_TAG, "Assert that the event is displayed with the corresponding details (title, context name, date, status) on the page.")
        calendarScreenPage.assertItemDetails(newEventTitle, parent.name, currentDate)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CALENDAR, TestCategory.E2E)
    fun testChangeStudentCalendarsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, parents = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val student2 = data.studentsList[1]
        val parent = data.parentsList[0]
        val teacher = data.teachersList[0]

        val retrofitClient = CanvasNetworkAdapter.createAdminRetrofitClient("mobileqa.beta.instructure.com")
        val coursesService = retrofitClient.create(CoursesApi.CoursesService::class.java)
        val enrollmentsService = retrofitClient.create(EnrollmentsApi.EnrollmentsService::class.java)
        val course = CoursesApi.createCourse(coursesService = coursesService)
        val course2 = CoursesApi.createCourse(coursesService = coursesService)

        Log.d(PREPARATION_TAG,"Enroll '${student.name}' student, '${teacher.name}' teacher and '${parent.name}' parent to '${course.name}' course.")
        EnrollmentsApi.enrollUser(course.id, student.id, STUDENT_ENROLLMENT, enrollmentsService)
        EnrollmentsApi.enrollUser(course.id, teacher.id, TEACHER_ENROLLMENT, enrollmentsService)
        EnrollmentsApi.enrollUserAsObserver(course.id, parent.id, student.id)

        Log.d(PREPARATION_TAG,"Enroll '${student.name}' student, '${teacher.name}' teacher and '${parent.name}' parent to '${course2.name}' course.")
        EnrollmentsApi.enrollUser(course2.id, student2.id, STUDENT_ENROLLMENT, enrollmentsService)
        EnrollmentsApi.enrollUser(course2.id, teacher.id, TEACHER_ENROLLMENT, enrollmentsService)
        EnrollmentsApi.enrollUserAsObserver(course2.id, parent.id, student2.id)

        Log.d(PREPARATION_TAG, "Seed a calendar event for '${course.name}' (where '${student.name}' student is enrolled but '${student2.name}' student isn't.)")
        val testEventFirstStudent = CalendarEventApi.createCalendarEvent(
            teacher.token,
            CanvasContext.makeContextId(CanvasContext.Type.COURSE, course.id),
            "First Student Test Event",
            Date().toApiString()
        )

        Log.d(PREPARATION_TAG, "Seed a calendar event for '${course2.name}' (where '${student2.name}' student is enrolled but '${student.name}' student isn't.)")
        val testEventSecondStudent = CalendarEventApi.createCalendarEvent(
            teacher.token,
            CanvasContext.makeContextId(CanvasContext.Type.COURSE, course2.id),
            "Second Student Test Event",
            Date().toApiString()
        )

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Click on the 'Calendar' bottom menu to navigate to the Calendar page.")
        dashboardPage.clickCalendarBottomMenu()

        Log.d(ASSERTION_TAG, "Assert that the '${testEventFirstStudent.title}' is displayed only.")
        calendarScreenPage.assertItemDisplayed(testEventFirstStudent.title.orEmpty())
        calendarScreenPage.assertItemNotDisplayed(testEventSecondStudent.title.orEmpty())

        Log.d(STEP_TAG, "Open the student selector.")
        dashboardPage.openStudentSelector()

        val selectedShortName = if (student.sortableName < student2.sortableName) {
            student.shortName
        } else {
            student2.shortName
        }

        val otherStudentName = if (selectedShortName == student.shortName) {
            student2.shortName
        } else {
            student.shortName
        }

        Log.d(STEP_TAG, "Select the other student which was not initially selected (default selection is based on 'sortable' name alphabetic order).")
        dashboardPage.selectStudent(otherStudentName)

        Log.d(ASSERTION_TAG, "Assert that now the selected student became '$otherStudentName' student, the one which was not selected initially.")
        dashboardPage.assertSelectedStudent(otherStudentName)

        Log.d(ASSERTION_TAG, "Assert that the '${testEventSecondStudent.title}' is displayed only.")
        calendarScreenPage.assertItemDisplayed(testEventSecondStudent.title.orEmpty())
        calendarScreenPage.assertItemNotDisplayed(testEventFirstStudent.title.orEmpty())
    }
}