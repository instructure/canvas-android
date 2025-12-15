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
import com.instructure.canvas.espresso.mockcanvas.addCourse
import com.instructure.canvas.espresso.mockcanvas.addCourseCalendarEvent
import com.instructure.canvas.espresso.mockcanvas.addDiscussionTopicToAssignment
import com.instructure.canvas.espresso.mockcanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockcanvas.addEnrollment
import com.instructure.canvas.espresso.mockcanvas.addPlannable
import com.instructure.canvas.espresso.mockcanvas.addQuizToCourse
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import org.junit.Test
import java.util.Calendar
import java.util.Date

abstract class ToDoListInteractionTest : CanvasComposeTest() {

    private val toDoListPage = ToDoListPage(composeTestRule)
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
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterShowCalendarEvents)
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
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterShowPersonalToDos)
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
        toDoListPage.clickMarkToDoItemAsDone(assignment.id)

        // Wait for snackbar to appear
        toDoListPage.waitForSnackbar(assignment.name!!)
        toDoListPage.assertSnackbarDisplayed(assignment.name!!)

        // Click undo button in snackbar
        toDoListPage.clickSnackbarUndo()

        // Wait for item to reappear after undo
        toDoListPage.waitForItemToAppear(assignment.name!!)
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

        // Wait for snackbar to appear
        toDoListPage.waitForSnackbar(assignment.name!!)
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

        // Wait for snackbar to appear
        toDoListPage.waitForSnackbar(assignment.name!!)
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

        // Ensure the assignment is visible by setting future range to cover 3 days ahead
        toDoListPage.clickFilterButton()
        toDoFilterPage.selectShowTasksUntilOption(R.string.todoFilterNextWeek)
        toDoFilterPage.clickDone()

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
        val assignment = data.addAssignment(
            course.id,
            name = "Assignment to Complete",
            dueAt = Calendar.getInstance().time.toApiString()
        )

        goToToDoList(data)

        composeTestRule.waitForIdle()
        toDoListPage.assertItemDisplayed(assignment.name!!)

        // Mark the assignment as done
        toDoListPage.clickMarkToDoItemAsDone(assignment.id)

        // Wait for item to disappear
        toDoListPage.waitForItemToDisappear(assignment.name!!)
        toDoListPage.assertItemNotDisplayed(assignment.name!!)

        // Open filter and enable "Show Completed"
        toDoListPage.clickFilterButton()
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterShowCompleted)
        toDoFilterPage.clickDone()

        composeTestRule.waitForIdle()

        // Verify the completed assignment is now displayed
        toDoListPage.assertItemDisplayed(assignment.name!!)
    }

    @Test
    fun emptyStateDisplayedWhenNoToDos() {
        val data = initData()

        goToToDoList(data)

        composeTestRule.waitForIdle()
        toDoListPage.assertEmptyState()
    }

    @Test
    fun personalToDosFilterShowsPersonalToDos() {
        val data = initData()

        val course = data.courses.values.first()
        val user = getLoggedInUser()
        val assignment = data.addAssignment(
            course.id,
            name = "Regular Assignment",
            dueAt = Calendar.getInstance().time.toApiString()
        )

        val personalTodo = data.addPlannable(
            name = "Personal Todo Item",
            course = course,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = Date()
        )

        goToToDoList(data)

        composeTestRule.waitForIdle()

        // By default, personal todos are hidden
        toDoListPage.assertItemDisplayed(assignment.name!!)
        toDoListPage.assertItemNotDisplayed(personalTodo.plannable.title)

        // Enable personal todos filter
        toDoListPage.clickFilterButton()
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterShowPersonalToDos)
        toDoFilterPage.clickDone()

        composeTestRule.waitForIdle()

        // Verify personal todo is now displayed
        toDoListPage.assertItemDisplayed(assignment.name!!)
        toDoListPage.assertItemDisplayed(personalTodo.plannable.title)
    }

    @Test
    fun calendarEventsFilterShowsCalendarEvents() {
        val data = initData()

        val course = data.courses.values.first()
        val assignment = data.addAssignment(
            course.id,
            name = "Regular Assignment",
            dueAt = Calendar.getInstance().time.toApiString()
        )

        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Calendar Event",
            description = "Event Description"
        )

        goToToDoList(data)

        composeTestRule.waitForIdle()

        // By default, calendar events are hidden
        toDoListPage.assertItemDisplayed(assignment.name!!)
        toDoListPage.assertItemNotDisplayed(event.title!!)

        // Enable calendar events filter
        toDoListPage.clickFilterButton()
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterShowCalendarEvents)
        toDoFilterPage.clickDone()

        composeTestRule.waitForIdle()

        // Verify calendar event is now displayed
        toDoListPage.assertItemDisplayed(assignment.name!!)
        toDoListPage.assertItemDisplayed(event.title!!)
    }

    @Test
    fun filterCloseWithoutSavingDoesNotApplyChanges() {
        val data = initData()

        val course = data.courses.values.first()
        val assignment = data.addAssignment(
            course.id,
            name = "Assignment to Complete",
            dueAt = Calendar.getInstance().time.toApiString()
        )

        goToToDoList(data)

        composeTestRule.waitForIdle()
        toDoListPage.assertItemDisplayed(assignment.name!!)

        // Mark assignment as done so it's hidden
        toDoListPage.clickMarkToDoItemAsDone(assignment.id)
        toDoListPage.waitForItemToDisappear(assignment.name!!)
        toDoListPage.assertItemNotDisplayed(assignment.name!!)

        // Open filter and toggle "Show Completed" but close without saving
        toDoListPage.clickFilterButton()
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterShowCompleted)
        toDoFilterPage.clickClose()

        composeTestRule.waitForIdle()

        // Verify completed item is still hidden (filter was not applied)
        toDoListPage.assertItemNotDisplayed(assignment.name!!)
        toDoListPage.assertFilterIconOutline()
    }

    @Test
    fun filterCloseWithSavingAppliesChanges() {
        val data = initData()

        val course = data.courses.values.first()
        val assignment = data.addAssignment(
            course.id,
            name = "Assignment to Complete",
            dueAt = Calendar.getInstance().time.toApiString()
        )

        goToToDoList(data)

        composeTestRule.waitForIdle()
        toDoListPage.assertItemDisplayed(assignment.name!!)

        // Mark assignment as done so it's hidden
        toDoListPage.clickMarkToDoItemAsDone(assignment.id)
        toDoListPage.waitForItemToDisappear(assignment.name!!)
        toDoListPage.assertItemNotDisplayed(assignment.name!!)

        // Open filter, toggle "Show Completed" and save
        toDoListPage.clickFilterButton()
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterShowCompleted)
        toDoFilterPage.clickDone()

        composeTestRule.waitForIdle()

        // Verify completed item is now displayed (filter was applied)
        toDoListPage.assertItemDisplayed(assignment.name!!)
        toDoListPage.assertFilterIconFilled()
    }

    @Test
    fun pastDateRangeFilterShowsOlderItems() {
        val data = initData()

        val course = data.courses.values.first()
        val calendar = Calendar.getInstance()

        // Create assignment 3 weeks ago
        calendar.add(Calendar.WEEK_OF_YEAR, -3)
        val threeWeeksAgo = calendar.time
        val oldAssignment = data.addAssignment(
            course.id,
            name = "Old Assignment",
            dueAt = threeWeeksAgo.toApiString()
        )

        // Create assignment today
        calendar.time = Date()
        val todayAssignment = data.addAssignment(
            course.id,
            name = "Today Assignment",
            dueAt = calendar.time.toApiString()
        )

        goToToDoList(data)

        composeTestRule.waitForIdle()

        // By default, past date range is "4 Weeks Ago" so assignment from 3 weeks ago should be visible
        toDoListPage.assertItemDisplayed(todayAssignment.name!!)
        toDoListPage.assertItemDisplayed(oldAssignment.name!!)

        // Change past date range to "Last Week" to hide the older assignment
        toDoListPage.clickFilterButton()
        toDoFilterPage.selectShowTasksFromOption(R.string.todoFilterLastWeek)
        toDoFilterPage.clickDone()

        composeTestRule.waitForIdle()

        // Assignment from 3 weeks ago should now be hidden (outside 1-week range)
        toDoListPage.assertItemDisplayed(todayAssignment.name!!)
        toDoListPage.assertItemNotDisplayed(oldAssignment.name!!)

        // Change past date range to "4 Weeks Ago" to show the older assignment again
        toDoListPage.clickFilterButton()
        toDoFilterPage.selectShowTasksFromOption(R.string.todoFilterFourWeeks)
        toDoFilterPage.clickDone()

        composeTestRule.waitForIdle()

        // Assignment from 3 weeks ago should now be visible again (within 4-week range)
        toDoListPage.assertItemDisplayed(todayAssignment.name!!)
        toDoListPage.assertItemDisplayed(oldAssignment.name!!)
    }

    @Test
    fun futureDateRangeFilterShowsFutureItems() {
        val data = initData()

        val course = data.courses.values.first()
        val calendar = Calendar.getInstance()

        // Create assignment today
        val todayAssignment = data.addAssignment(
            course.id,
            name = "Today Assignment",
            dueAt = calendar.time.toApiString()
        )

        // Create assignment 2 weeks from now
        calendar.add(Calendar.WEEK_OF_YEAR, 2)
        val futureAssignment = data.addAssignment(
            course.id,
            name = "Future Assignment",
            dueAt = calendar.time.toApiString()
        )

        goToToDoList(data)

        composeTestRule.waitForIdle()

        // By default, future date range is "Next Week" so assignment from 2 weeks ahead should be hidden
        toDoListPage.assertItemDisplayed(todayAssignment.name!!)
        toDoListPage.assertItemNotDisplayed(futureAssignment.name!!)

        // Change future date range to "In 2 Weeks"
        toDoListPage.clickFilterButton()
        toDoFilterPage.selectShowTasksUntilOption(R.string.todoFilterInTwoWeeks)
        toDoFilterPage.clickDone()

        composeTestRule.waitForIdle()

        // Now both assignments should be visible
        toDoListPage.assertItemDisplayed(todayAssignment.name!!)
        toDoListPage.assertItemDisplayed(futureAssignment.name!!)
    }

    @Test
    fun favoriteCoursesFilterShowsOnlyFavoriteCoursesItems() {
        val data = initData()
        val user = getLoggedInUser()

        val favoriteCourse = data.courses.values.first()
        favoriteCourse.isFavorite = true

        val nonFavoriteCourse = data.addCourse(isFavorite = false)
        data.addEnrollment(user, nonFavoriteCourse, Enrollment.EnrollmentType.Student)

        val favoriteAssignment = data.addAssignment(
            favoriteCourse.id,
            name = "Favorite Course Assignment",
            dueAt = Calendar.getInstance().time.toApiString()
        )

        val nonFavoriteAssignment = data.addAssignment(
            nonFavoriteCourse.id,
            name = "Non-Favorite Course Assignment",
            dueAt = Calendar.getInstance().time.toApiString()
        )

        goToToDoList(data)

        composeTestRule.waitForIdle()

        // By default, both assignments should be visible
        toDoListPage.assertItemDisplayed(favoriteAssignment.name!!)
        toDoListPage.assertItemDisplayed(nonFavoriteAssignment.name!!)

        // Enable favorite courses filter
        toDoListPage.clickFilterButton()
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterFavoriteCoursesOnly)
        toDoFilterPage.clickDone()

        composeTestRule.waitForIdle()

        // Only the favorite course assignment should be visible
        toDoListPage.assertItemDisplayed(favoriteAssignment.name!!)
        toDoListPage.assertItemNotDisplayed(nonFavoriteAssignment.name!!)
    }

    override fun displaysPageObjects() = Unit

    abstract fun goToToDoList(data: MockCanvas)

    abstract fun initData(): MockCanvas

    abstract fun getLoggedInUser(): User

    abstract fun assertAssignmentDetailsTitle(title: String)
}
