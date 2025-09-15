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
 */
package com.instructure.canvas.espresso.common.interaction

import com.instructure.canvas.espresso.CanvasComposeTest
import com.instructure.canvas.espresso.annotations.StubLandscape
import com.instructure.canvas.espresso.common.pages.compose.CalendarEventDetailsPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarFilterPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarScreenPage
import com.instructure.canvas.espresso.common.pages.compose.CalendarToDoDetailsPage
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCourseCalendarEvent
import com.instructure.canvas.espresso.mockCanvas.addDiscussionTopicToAssignment
import com.instructure.canvas.espresso.mockCanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockCanvas.addPlannable
import com.instructure.canvas.espresso.mockCanvas.addQuizToCourse
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.features.calendar.CalendarPrefs
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date

abstract class CalendarInteractionTest : CanvasComposeTest() {

    val calendarScreenPage = CalendarScreenPage(composeTestRule)
    private val todoDetailsPage = CalendarToDoDetailsPage(composeTestRule)
    private val eventDetailsPage = CalendarEventDetailsPage(composeTestRule)
    private val calendarFilterPage = CalendarFilterPage(composeTestRule)

    @Before
    fun setUp() {
        CalendarPrefs.clearPrefs()
    }

    @Test
    fun calendarItemsAreShownForToday() {
        val data = initData()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 13)
        }

        val course = data.courses.values.first()
        val user = getLoggedInUser()
        val todo = data.addPlannable(
            name = "Test Todo",
            course = course,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = calendar.time
        )

        val assignment = data.addAssignment(
            course.id,
            name = "Test Assignment",
            dueAt = calendar.time.toApiString()
        )

        val quiz = data.addQuizToCourse(
            title = "Test Quiz",
            course = course,
            quizType = Quiz.TYPE_ASSIGNMENT,
            dueAt = calendar.time.toApiString(),
            description = "Here's a description!"
        )

        val discussionAssignment = data.addAssignment(
            courseId = course.id,
            submissionTypeList = listOf(Assignment.SubmissionType.DISCUSSION_TOPIC),
            name = "Discussion assignment",
            pointsPossible = 12,
            dueAt = calendar.time.toApiString()
        )

        data.addDiscussionTopicToCourse(
            topicTitle = "Discussion topic",
            course = course,
            user = user,
            assignment = discussionAssignment
        )

        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = calendar.apply { set(Calendar.HOUR_OF_DAY, 15) }.time.toApiString(),
            endDate = calendar.apply { set(Calendar.HOUR_OF_DAY, 16) }.time.toApiString(),
            title = "Test Event",
            description = "Test Description"
        )

        goToCalendar(data)

        calendarScreenPage.assertItemDetails(assignment.name!!, course.name, index = 0)
        calendarScreenPage.assertItemDetails(quiz.title!!, course.name, index = 1)
        calendarScreenPage.assertItemDetails(discussionAssignment.name!!, course.name, index = 2)
        calendarScreenPage.assertItemDetails(todo.plannable.title, "${course.name} To Do", index = 3)
        calendarScreenPage.assertItemDetails(event.title!!, course.name, index = 4)
    }

    @Test
    @StubLandscape("Month view is not available in landscape")
    fun calendarMonthChangesAfterSwipe() {
        val data = initData()

        val course = data.courses.values.first()
        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Test Event",
            description = "Test Description"
        )

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 1)
        val eventNextMonth = data.addCourseCalendarEvent(
            course = course,
            startDate = Date(calendar.timeInMillis).toApiString(),
            title = "Next month event",
            description = "Test Description"
        )

        goToCalendar(data)
        calendarScreenPage.clickCalendarHeader()

        calendarScreenPage.assertItemDetails(event.title!!, course.name)

        calendarScreenPage.swipeCalendarLeft()
        calendarScreenPage.assertItemDetails(eventNextMonth.title!!, course.name)
        calendarScreenPage.assertItemNotDisplayed(event.title!!)
    }

    @Test
    fun calendarWeekChangesAfterSwipeInCollapsedState() {
        val data = initData()

        val course = data.courses.values.first()
        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Test Event",
            description = "Test Description"
        )

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        val eventNextWeek = data.addCourseCalendarEvent(
            course = course,
            startDate = Date(calendar.timeInMillis).toApiString(),
            title = "Next week event",
            description = "Test Description"
        )

        goToCalendar(data)

        calendarScreenPage.assertItemDetails(event.title!!, course.name)

        calendarScreenPage.swipeCalendarLeft()
        calendarScreenPage.assertItemDetails(eventNextWeek.title!!, course.name)
        calendarScreenPage.assertItemNotDisplayed(event.title!!)
    }

    @Test
    fun showNextDayAfterSwipeLeftOnEvents() {
        val data = initData()

        val course = data.courses.values.first()
        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Test Event",
            description = "Test Description"
        )

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val eventNextDay = data.addCourseCalendarEvent(
            course = course,
            startDate = Date(calendar.timeInMillis).toApiString(),
            title = "Next day event",
            description = "Test Description"
        )

        goToCalendar(data)

        calendarScreenPage.assertItemDetails(event.title!!, course.name)

        calendarScreenPage.swipeEventsLeft()
        calendarScreenPage.assertItemDetails(eventNextDay.title!!, course.name)
        calendarScreenPage.assertItemNotDisplayed(event.title!!)
    }

    @Test
    fun showPreviousDayAfterSwipeRightOnEvents() {
        val data = initData()

        val course = data.courses.values.first()
        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Test Event",
            description = "Test Description"
        )

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val eventPreviousDay = data.addCourseCalendarEvent(
            course = course,
            startDate = Date(calendar.timeInMillis).toApiString(),
            title = "previous day event",
            description = "Test Description"
        )

        goToCalendar(data)

        calendarScreenPage.assertItemDetails(event.title!!, course.name)

        calendarScreenPage.swipeEventsRight()
        calendarScreenPage.assertItemDetails(eventPreviousDay.title!!, course.name)
        calendarScreenPage.assertItemNotDisplayed(event.title!!)
    }

    @Test
    fun onlyFilteredItemsAreDisplayedWhenFilteringCalendar() {
        val data = initData()

        val course = data.courses.values.first()
        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Test Event",
            description = "Test Description"
        )

        val course2 = data.courses.values.last()
        val event2 = data.addCourseCalendarEvent(
            course = course2,
            startDate = Date().toApiString(),
            title = "Course 2 event",
            description = "Test Description"
        )

        goToCalendar(data)

        calendarScreenPage.assertItemDetails(event.title!!, course.name)
        calendarScreenPage.assertItemDetails(event2.title!!, course2.name)

        calendarScreenPage.clickCalendarFilters()
        calendarFilterPage.clickOnFilterItem(course.name)
        calendarFilterPage.closeFilterPage()

        calendarScreenPage.assertItemDetails(event2.title!!, course2.name)
        calendarScreenPage.assertItemNotExist(event.title!!)
    }

    @Test
    fun selectTodoOpensTodoDetails() {
        val data = initData()

        val course = data.courses.values.first()
        val user = getLoggedInUser()
        val todo = data.addPlannable(
            name = "Test Todo",
            course = course,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = Date()
        )

        goToCalendar(data)

        calendarScreenPage.clickOnItem(todo.plannable.title)
        todoDetailsPage.assertTitle(todo.plannable.title)
    }

    @Test
    fun selectEventOpensEventDetails() {
        val data = initData()

        val course = data.courses.values.first()
        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Test Event",
            description = "Test Description"
        )

        goToCalendar(data)

        calendarScreenPage.clickOnItem(event.title!!)
        eventDetailsPage.assertEventTitle(event.title!!)
    }

    @Test
    fun selectAssignmentOpensAssignmentDetails() {
        val data = initData()

        val course = data.courses.values.first()
        val assignment = data.addAssignment(
            course.id,
            name = "Test Assignment",
            dueAt = Calendar.getInstance().time.toApiString()
        )

        goToCalendar(data)

        calendarScreenPage.clickOnItem(assignment.name!!)
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

        goToCalendar(data)

        calendarScreenPage.clickOnItem(quiz.title!!)
        assertAssignmentDetailsTitle(quiz.title!!)
    }

    @Test
    fun selectDiscussionOpensDiscussionDetails() {
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

        goToCalendar(data)

        calendarScreenPage.clickOnItem(discussionAssignment.name!!)

        assertDiscussionDetailsTitle("Discussion topic")
    }

    @Test
    fun clickingTodayButtonWillNavigateBackToTodayAndShowTodaysEvents() {
        val data = initData()

        val course = data.courses.values.first()
        val event = data.addCourseCalendarEvent(
            course = course,
            startDate = Date().toApiString(),
            title = "Test Event",
            description = "Test Description"
        )

        val user = getLoggedInUser()
        val todo = data.addPlannable(
            name = "Test Todo",
            course = course,
            userId = user.id,
            type = PlannableType.PLANNER_NOTE,
            date = Date()
        )

        goToCalendar(data)

        calendarScreenPage.swipeCalendarLeft()
        calendarScreenPage.swipeCalendarLeft()
        calendarScreenPage.swipeCalendarLeft()

        calendarScreenPage.assertItemNotDisplayed(event.title!!)
        calendarScreenPage.assertItemNotDisplayed(todo.plannable.title)

        clickTodayButton()

        calendarScreenPage.assertItemDetails(event.title!!, course.name)
        calendarScreenPage.assertItemDetails(todo.plannable.title, "${course.name} To Do")
    }

    override fun displaysPageObjects() = Unit

    abstract fun goToCalendar(data: MockCanvas)

    abstract fun initData(): MockCanvas

    abstract fun getLoggedInUser(): User

    abstract fun assertAssignmentDetailsTitle(title: String)

    abstract fun assertDiscussionDetailsTitle(title: String)
    
    abstract fun clickTodayButton()
}