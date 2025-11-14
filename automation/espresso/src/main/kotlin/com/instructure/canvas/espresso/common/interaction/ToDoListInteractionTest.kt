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
package com.instructure.canvas.espresso.common.interaction

import com.instructure.canvas.espresso.CanvasComposeTest
import com.instructure.canvas.espresso.common.pages.compose.CalendarEventDetailsPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarScreenPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarToDoDetailsPage
import com.instructure.canvas.espresso.common.pages.compose.ToDoFilterPage
import com.instructure.canvas.espresso.common.pages.compose.ToDoListPage
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addAssignment
import com.instructure.canvas.espresso.mockcanvas.addCourseCalendarEvent
import com.instructure.canvas.espresso.mockcanvas.addDiscussionTopicToAssignment
import com.instructure.canvas.espresso.mockcanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockcanvas.addPlannable
import com.instructure.canvas.espresso.mockcanvas.addQuizToCourse
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.toApiString
import org.junit.Test
import java.util.Calendar
import java.util.Date

abstract class ToDoListInteractionTest : CanvasComposeTest() {

    val toDoListPage = ToDoListPage(composeTestRule)
    private val toDoFilterPage = ToDoFilterPage(composeTestRule)
    private val todoDetailsPage = CalendarToDoDetailsPage(composeTestRule)
    private val eventDetailsPage = CalendarEventDetailsPage(composeTestRule)
    private val calendarScreenPage = CalendarScreenPage(composeTestRule)

    @Test
    fun selectAssignmentOpensAssignmentDetails() {
        val data = initData()

        val course = data.courses.values.first()
        val assignment = data.addAssignment(
            course.id,
            name = "Test Assignment",
            dueAt = Calendar.getInstance().time.toApiString()
        )

        goToToDoList(data)

        composeTestRule.waitForIdle()
        toDoListPage.clickOnItem(assignment.name!!)

        assertAssignmentDetailsTitle(assignment.name!!)
    }

    @Test
    fun selectQuizOpensAssignmentDetails() {
        val data = initData()

        val course = data.courses.values.first()
        val quiz = data.addQuizToCourse(
            title = "Test Quiz",
            course = course,
            quizType = Quiz.TYPE_ASSIGNMENT,
            dueAt = Calendar.getInstance().time.toApiString(),
            description = "Here's a description!"
        )

        goToToDoList(data)

        composeTestRule.waitForIdle()
        toDoListPage.clickOnItem(quiz.title!!)

        assertAssignmentDetailsTitle(quiz.title!!)
    }

    @Test
    fun selectDiscussionOpensAssignmentDetails() {
        val data = initData()

        val course = data.courses.values.first()
        val user = getLoggedInUser()
        val discussionAssignment = data.addAssignment(
            courseId = course.id,
            submissionTypeList = listOf(Assignment.SubmissionType.DISCUSSION_TOPIC),
            name = "Discussion assignment",
            pointsPossible = 12,
            dueAt = Calendar.getInstance().time.toApiString()
        )

        val discussion = data.addDiscussionTopicToCourse(
            topicTitle = "Discussion topic",
            course = course,
            user = user,
            assignment = discussionAssignment
        )

        data.addDiscussionTopicToAssignment(discussionAssignment, discussion)

        goToToDoList(data)

        composeTestRule.waitForIdle()
        toDoListPage.clickOnItem(discussionAssignment.name!!)

        assertAssignmentDetailsTitle(discussionAssignment.name!!)
    }

    @Test
    fun selectCalendarEventOpensEventDetails() {
        val data = initData()

        val course = data.courses.values.first()
        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Test Event",
            description = "Test Description"
        )

        goToToDoList(data)

        composeTestRule.waitForIdle()
        toDoListPage.clickFilterButton()
        toDoFilterPage.toggleShowCalendarEvents()
        toDoFilterPage.clickDone()
        composeTestRule.waitForIdle()
        toDoListPage.clickOnItem(event.title!!)

        eventDetailsPage.assertEventTitle(event.title!!)
    }

    @Test
    fun selectPersonalToDoOpensToDoDetails() {
        val data = initData()

        val course = data.courses.values.first()
        val user = getLoggedInUser()
        val todo = data.addPlannable(
            name = "Test Personal Todo",
            course = course,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = Date()
        )

        goToToDoList(data)

        composeTestRule.waitForIdle()
        toDoListPage.clickFilterButton()
        toDoFilterPage.toggleShowPersonalToDos()
        toDoFilterPage.clickDone()
        composeTestRule.waitForIdle()
        toDoListPage.clickOnItem(todo.plannable.title)

        todoDetailsPage.assertTitle(todo.plannable.title)
    }

    @Test
    fun checkboxMarksItemAsDone() {
        val data = initData()

        val course = data.courses.values.first()
        val assignment = data.addAssignment(
            course.id,
            name = "Test Checkbox Assignment",
            dueAt = Calendar.getInstance().time.toApiString()
        )

        goToToDoList(data)

        composeTestRule.waitForIdle()
        toDoListPage.assertItemDisplayed(assignment.name!!)
        toDoListPage.clickCheckbox(assignment.id)

        Thread.sleep(1000)
        composeTestRule.waitForIdle()

        // Verify snackbar appears with assignment name
        toDoListPage.assertSnackbarDisplayed(assignment.name!!)

        // Click undo button in snackbar
        toDoListPage.clickSnackbarUndo()

        Thread.sleep(1000)
        composeTestRule.waitForIdle()

        // Verify item is still displayed (unmarked as done)
        toDoListPage.assertItemDisplayed(assignment.name!!)
    }

    @Test
    fun swipeRightMarksItemAsDone() {
        val data = initData()

        val course = data.courses.values.first()
        val assignment = data.addAssignment(
            course.id,
            name = "Test Swipe Assignment",
            dueAt = Calendar.getInstance().time.toApiString()
        )

        goToToDoList(data)

        composeTestRule.waitForIdle()
        toDoListPage.assertItemDisplayed(assignment.name!!)
        toDoListPage.swipeItemRight(assignment.id)

        // Wait for API call + snackbar
        Thread.sleep(2000)

        // Verify snackbar appears with assignment name
        toDoListPage.assertSnackbarDisplayed(assignment.name!!)
    }

    @Test
    fun swipeLeftMarksItemAsDone() {
        val data = initData()

        val course = data.courses.values.first()
        val assignment = data.addAssignment(
            course.id,
            name = "Test Swipe Left Assignment",
            dueAt = Calendar.getInstance().time.toApiString()
        )

        goToToDoList(data)

        composeTestRule.waitForIdle()
        toDoListPage.assertItemDisplayed(assignment.name!!)
        toDoListPage.swipeItemLeft(assignment.id)

        // Wait for API call + snackbar
        Thread.sleep(2000)

        // Verify snackbar appears with assignment name
        toDoListPage.assertSnackbarDisplayed(assignment.name!!)
    }

    @Test
    fun clickDateBadgeNavigatesToCalendar() {
        val data = initData()

        val course = data.courses.values.first()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 3) // 3 days from now
        val dueDate = calendar.time
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val assignment = data.addAssignment(
            course.id,
            name = "Test Date Badge Assignment",
            dueAt = dueDate.toApiString()
        )

        goToToDoList(data)

        composeTestRule.waitForIdle()
        toDoListPage.assertItemDisplayed(assignment.name!!)
        toDoListPage.clickDateBadge(dayOfMonth)

        composeTestRule.waitForIdle()
        calendarScreenPage.assertCalendarPageTitle()
    }

    @Test
    fun openFilterScreen() {
        val data = initData()

        goToToDoList(data)

        composeTestRule.waitForIdle()
        toDoListPage.clickFilterButton()
        toDoFilterPage.assertFilterScreenTitle()
    }

    @Test
    fun onlyFilteredItemsAreDisplayedWhenFilteringByCompletedItems() {
        val data = initData()

        val course = data.courses.values.first()

        // Create a regular assignment
        val assignment = data.addAssignment(
            course.id,
            name = "Incomplete Assignment",
            dueAt = Calendar.getInstance().time.toApiString()
        )

        goToToDoList(data)

        composeTestRule.waitForIdle()

        // Initially, should show the assignment
        toDoListPage.assertItemDisplayed(assignment.name!!)

        // Open filter and enable "Show Completed"
        toDoListPage.clickFilterButton()
        toDoFilterPage.toggleShowCompleted()
        toDoFilterPage.clickDone()

        composeTestRule.waitForIdle()

        // Still should show the assignment (it's not completed)
        toDoListPage.assertItemDisplayed(assignment.name!!)
    }

    override fun displaysPageObjects() = Unit

    abstract fun goToToDoList(data: MockCanvas)

    abstract fun initData(): MockCanvas

    abstract fun getLoggedInUser(): User

    abstract fun assertAssignmentDetailsTitle(title: String)
}
