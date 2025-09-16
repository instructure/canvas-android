/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.StubLandscape
import com.instructure.canvas.espresso.annotations.StubTablet
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addTodo
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.espresso.page.getStringFromResource
import com.instructure.pandautils.utils.date.DateTimeProvider
import com.instructure.student.R
import com.instructure.student.ui.pages.classic.k5.ElementaryDashboardPage
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.di.FakeDateTimeProvider
import com.instructure.student.ui.utils.extensions.tokenLoginElementary
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.AssertionFailedError
import org.junit.Test
import java.util.Calendar
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(CustomGradeStatusModule::class)
class ScheduleInteractionTest : StudentTest() {

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    @Inject
    lateinit var dateTimeProvider: DateTimeProvider

    override fun displaysPageObjects() = Unit

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testShowCorrectHeaderItems() {
        setDate(2021, Calendar.AUGUST, 11)
        val data = createMockData(courseCount = 1)
        goToScheduleTab(data)

        schedulePage.assertPageObjects()
        schedulePage.assertDayHeaderShownByPosition("August 08", "Sunday", 0)
        schedulePage.assertDayHeaderShownByPosition("August 09", "Monday", 2)
        schedulePage.assertNoScheduleItemDisplayed()

        schedulePage.assertDayHeaderShownByPosition("August 10", schedulePage.getStringFromResource(R.string.yesterday), 4)
        schedulePage.assertDayHeaderShownByPosition("August 11", schedulePage.getStringFromResource(R.string.today), 6)
        schedulePage.assertNoScheduleItemDisplayed()

        schedulePage.assertDayHeaderShownByPosition("August 12", schedulePage.getStringFromResource(R.string.tomorrow), 8)
        schedulePage.assertDayHeaderShownByPosition("August 13", "Friday", 10)
        schedulePage.assertNoScheduleItemDisplayed()

        schedulePage.assertDayHeaderShownByPosition("August 14", "Saturday", 12)
        schedulePage.assertNoScheduleItemDisplayed()
    }

    @Test
    @StubLandscape(description = "This is intentionally stubbed on landscape mode because the item view is too narrow, but that's not a bug, it's intentional.")
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testShowScheduledAssignments() {
        setDate(2021, Calendar.AUGUST, 11)
        val data = createMockData(courseCount = 1)

        val courses = data.courses.values.filter { !it.homeroomCourse }
        courses[0].name = "Course 1"

        val currentDate = dateTimeProvider.getCalendar().time.toApiString()
        val assignment1 = data.addAssignment(courses[0].id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY), dueAt = currentDate, name = "Assignment 1")

        goToScheduleTab(data)
        schedulePage.scrollToPosition(10)
        schedulePage.assertCourseHeaderDisplayed(courses[0].name)
        schedulePage.assertScheduleItemDisplayed(assignment1.name!!)
    }

    @Test
    @StubLandscape(description = "This is intentionally stubbed on landscape mode because the item view is too narrow, but that's not a bug, it's intentional.")
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testShowMissingAssignments() {
        setDate(2021, Calendar.AUGUST, 11)
        val data = createMockData(courseCount = 1)

        val courses = data.courses.values.filter { !it.homeroomCourse }

        val currentDate = dateTimeProvider.getCalendar().time.toApiString()
        val assignment1 = data.addAssignment(courses[0].id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY), dueAt = currentDate)

        goToScheduleTab(data)
        schedulePage.scrollToPosition(12)
        schedulePage.assertMissingItemDisplayedInMissingItemSummary(assignment1.name!!, courses[0].name, "10 pts")
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testShowToDoEvents() {
        setDate(2021, Calendar.AUGUST, 11)
        val data = createMockData(courseCount = 1)

        val todo = data.addTodo("To Do event", data.students[0].id, date = dateTimeProvider.getCalendar().time)
        val todo2 = data.addTodo("Calendar event", data.students[0].id, date = dateTimeProvider.getCalendar().time)

        goToScheduleTab(data)
        schedulePage.scrollToPosition(8)
        schedulePage.assertCourseHeaderDisplayed(schedulePage.getStringFromResource(R.string.schedule_todo_title))
        schedulePage.assertScheduleItemDisplayed(todo.plannable.title)
        schedulePage.assertScheduleItemDisplayed(todo2.plannable.title)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testRefresh() {
        setDate(2021, Calendar.AUGUST, 11)
        val data = createMockData(courseCount = 1)

        val courses = data.courses.values.filter { !it.homeroomCourse }

        goToScheduleTab(data)

        // Check that we don't have any elements initially
        schedulePage.assertNoScheduleItemDisplayed()
        schedulePage.scrollToPosition(8)
        schedulePage.assertNoScheduleItemDisplayed()

        val currentDate = dateTimeProvider.getCalendar().time.toApiString()
        val assignment1 = data.addAssignment(courses[0].id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY), dueAt = currentDate)
        val assignment2 = data.addAssignment(courses[0].id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY), dueAt = currentDate)

        schedulePage.scrollToPosition(0)
        schedulePage.refresh()

        // Check that refresh was successful
        schedulePage.scrollToPosition(7)
        schedulePage.assertCourseHeaderDisplayed(courses[0].name)
        schedulePage.assertScheduleItemDisplayed(assignment1.name!!)
        schedulePage.assertScheduleItemDisplayed(assignment2.name!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testGoBack2Weeks() {
        setDate(2021, Calendar.AUGUST, 11)
        val data = createMockData(courseCount = 1)

        goToScheduleTab(data)

        schedulePage.assertDayHeaderShownByPosition("August 08", "Sunday", 0)
        schedulePage.assertDayHeaderShownByPosition("August 09", "Monday", 2)

        schedulePage.previousWeekButtonClick()
        schedulePage.swipeRight()

        schedulePage.assertDayHeaderShownByPosition("July 25", "Sunday", 0, recyclerViewMatcherText = "July 25")
        schedulePage.assertDayHeaderShownByPosition("July 26", "Monday", 2, recyclerViewMatcherText = "July 25")
        schedulePage.assertDayHeaderShownByPosition("July 27", "Tuesday", 4, recyclerViewMatcherText = "July 26")
        schedulePage.assertDayHeaderShownByPosition("July 28", "Wednesday", 6, recyclerViewMatcherText = "July 27")
        schedulePage.assertDayHeaderShownByPosition("July 29", "Thursday", 8, recyclerViewMatcherText = "July 28")
        schedulePage.assertDayHeaderShownByPosition("July 30", "Friday", 10, recyclerViewMatcherText = "July 29")
        schedulePage.assertDayHeaderShownByPosition("July 31", "Saturday", 12, recyclerViewMatcherText = "July 30")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testGoForward2Weeks() {
        setDate(2021, Calendar.AUGUST, 11)
        val data = createMockData(courseCount = 1)

        goToScheduleTab(data)

        schedulePage.assertDayHeaderShownByPosition("August 08", "Sunday", 0)
        schedulePage.assertDayHeaderShownByPosition("August 09", "Monday", 2)

        schedulePage.nextWeekButtonClick()
        schedulePage.swipeLeft()

        schedulePage.assertDayHeaderShownByPosition("August 22", "Sunday", 0, recyclerViewMatcherText = "August 22")
        schedulePage.assertDayHeaderShownByPosition("August 23", "Monday", 2, recyclerViewMatcherText = "August 22")
        schedulePage.assertDayHeaderShownByPosition("August 24", "Tuesday", 4, recyclerViewMatcherText = "August 23")
        schedulePage.assertDayHeaderShownByPosition("August 25", "Wednesday", 6, recyclerViewMatcherText = "August 24")
        schedulePage.assertDayHeaderShownByPosition("August 26", "Thursday", 8, recyclerViewMatcherText = "August 25")
        schedulePage.assertDayHeaderShownByPosition("August 27", "Friday", 10, recyclerViewMatcherText = "August 26")
        schedulePage.assertDayHeaderShownByPosition("August 28", "Saturday", 12, recyclerViewMatcherText = "August 27")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testOpenAssignment() {
        setDate(2021, Calendar.AUGUST, 11)
        val data = createMockData(courseCount = 1)

        val courses = data.courses.values.filter { !it.homeroomCourse }
        courses[0].name = "Course 1"

        val currentDate = dateTimeProvider.getCalendar().time.toApiString()
        val assignment = data.addAssignment(courses[0].id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY), dueAt = currentDate, name = "Assignment 1")

        goToScheduleTab(data)
        schedulePage.scrollToPosition(9)
        schedulePage.clickScheduleItem(assignment.name!!)

        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertAssignmentDetails(assignment)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testOpenCourse() {
        setDate(2021, Calendar.AUGUST, 11)
        val data = createMockData(courseCount = 1)

        val courses = data.courses.values.filter { !it.homeroomCourse }

        val currentDate = dateTimeProvider.getCalendar().time.toApiString()
        data.addAssignment(courses[0].id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY), dueAt = currentDate)

        goToScheduleTab(data)
        schedulePage.scrollToPosition(8)
        schedulePage.clickCourseHeader(courses[0].name)

        elementaryCoursePage.assertPageObjects()
        elementaryCoursePage.assertTitleCorrect(courses[0].originalName!!)
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testMarkAsDone() {
        setDate(2021, Calendar.AUGUST, 11)
        val data = createMockData(courseCount = 1)

        data.addTodo("To Do event", data.students[0].id, date = dateTimeProvider.getCalendar().time)

        goToScheduleTab(data)
        schedulePage.scrollToPosition(8)

        schedulePage.assertMarkedAsDoneNotShown()

        schedulePage.clickDoneCheckbox()
        schedulePage.assertMarkedAsDoneShown()
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    @StubTablet
    fun testTodayButton() {
        setDate(2021, Calendar.AUGUST, 11)
        val data = createMockData(courseCount = 1)

        val courses = data.courses.values.filter { !it.homeroomCourse }
        courses[0].name = "Course 1"

        val currentDate = dateTimeProvider.getCalendar().time.toApiString()
        val assignment1 = data.addAssignment(courses[0].id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY), dueAt = currentDate, name = "Assignment 1")

        goToScheduleTab(data)

        //This try-catch is needed because we don't know if the swipe is enough to go away from 'Today' and to make the Today button displayed.
        schedulePage.swipeUp()
        try {
            schedulePage.assertTodayButtonDisplayed()
        } catch(e: AssertionFailedError) {
            schedulePage.swipeUp()
            schedulePage.assertTodayButtonDisplayed()
        }
        schedulePage.clickOnTodayButton()
        schedulePage.assertCourseHeaderDisplayed(courses[0].name)
        schedulePage.assertScheduleItemDisplayed(assignment1.name!!)
    }

    private fun createMockData(
        courseCount: Int = 0,
        withGradingPeriods: Boolean = false,
        homeroomCourseCount: Int = 0): MockCanvas {

        val data = MockCanvas.init(
            studentCount = 1,
            courseCount = courseCount,
            withGradingPeriods = withGradingPeriods,
            homeroomCourseCount = homeroomCourseCount)

        data.elementarySubjectPages = true

        return data
    }

    private fun goToScheduleTab(data: MockCanvas) {
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLoginElementary(data.domain, token, student)
        elementaryDashboardPage.waitForRender()
        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.SCHEDULE)
    }

    private fun setDate(year: Int, month: Int, dayOfMonth: Int) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        (dateTimeProvider as FakeDateTimeProvider).fakeTimeInMillis = cal.timeInMillis
    }
}