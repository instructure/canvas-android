package com.instructure.student.ui.e2e.classic

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.CalendarEventApi
import com.instructure.dataseeding.api.PlannerAPI
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.pandautils.R
import com.instructure.student.ui.pages.classic.WebViewTextCheck
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.util.Calendar
import java.util.Date

@HiltAndroidTest
class TodoE2ETest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.TODOS, TestCategory.E2E)
    fun testNewTodoE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 2, favoriteCourses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[1]
        val favoriteCourse = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seed a quiz for '${course.name}' course with today due date.")
        val todayQuiz =
            QuizzesApi.createQuiz(course.id, teacher.token, dueAt = 0.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG, "Seed an assignment for '${course.name}' course with tomorrow due date.")
        val thisWeekAssignment = AssignmentsApi.createAssignment(
            course.id,
            teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 15.0,
            dueAt = 1.days.fromNow.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Log.d(PREPARATION_TAG, "Seed an assignment for '${favoriteCourse.name}' (favorite) course with tomorrow due date.")
        val favoriteCourseAssignment = AssignmentsApi.createAssignment(
            favoriteCourse.id,
            teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 15.0,
            dueAt = 1.days.fromNow.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Log.d(PREPARATION_TAG, "Seed another quiz for '${course.name}' course with 1 week away due date.")
        val nextWeekQuiz =
            QuizzesApi.createQuiz(course.id, teacher.token, dueAt = 8.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG, "Seed an assignment for '${course.name}' course with 2 weeks away due date.")
        val twoWeeksAwayAssignment = AssignmentsApi.createAssignment(
            course.id,
            teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 20.0,
            dueAt = 15.days.fromNow.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Log.d(PREPARATION_TAG, "Seed another quiz for '${course.name}' course with 3 weeks away due date.")
        val threeWeeksAwayQuiz =
            QuizzesApi.createQuiz(course.id, teacher.token, dueAt = 22.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG, "Seed an assignment for '${course.name}' course with 4 weeks away due date.")
        val fourWeeksAwayAssignment = AssignmentsApi.createAssignment(
            course.id,
            teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 20.0,
            dueAt = 29.days.fromNow.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Navigate to 'To Do' Page via bottom-menu.")
        dashboardPage.waitForRender()
        dashboardPage.clickTodoTab()

        Log.d(ASSERTION_TAG, "Assert that '${todayQuiz.title}' quiz, ${favoriteCourseAssignment.name} and '${thisWeekAssignment.name}' assignments are displayed as their due date is in the current week.")
        toDoListPage.assertItemDisplayed(todayQuiz.title)
        toDoListPage.assertItemDisplayed(thisWeekAssignment.name)
        toDoListPage.assertItemDisplayed(favoriteCourseAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the '${nextWeekQuiz.title}', '${threeWeeksAwayQuiz.title}' quizzes and '${twoWeeksAwayAssignment.name}', '${fourWeeksAwayAssignment.name}' assignments are NOT displayed as their due date is beyond the current week.")
        toDoListPage.assertItemNotDisplayed(nextWeekQuiz.title)
        toDoListPage.assertItemNotDisplayed(twoWeeksAwayAssignment.name)
        toDoListPage.assertItemNotDisplayed(threeWeeksAwayQuiz.title)
        toDoListPage.assertItemNotDisplayed(fourWeeksAwayAssignment.name)

        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(ASSERTION_TAG, "Assert that the To Do Filter Page is displayed with the correct title.")
        toDoFilterPage.assertFilterScreenTitle()

        Log.d(ASSERTION_TAG, "Assert that all filter options (and their 'group labels') are displayed correctly on the ToDo Filter Page.")
        toDoFilterPage.assertToDoFilterScreenDetails()

        Log.d(ASSERTION_TAG, "Assert that all 'Visible items' filter options are disabled by default on the ToDo Filter Page.")
        toDoFilterPage.assertVisibleItemOptionCheckedState(R.string.todoFilterShowPersonalToDos, false)
        toDoFilterPage.assertVisibleItemOptionCheckedState(R.string.todoFilterShowCalendarEvents, false)
        toDoFilterPage.assertVisibleItemOptionCheckedState(R.string.todoFilterShowCompleted, false)
        toDoFilterPage.assertVisibleItemOptionCheckedState(R.string.todoFilterFavoriteCoursesOnly, false)

        Log.d(ASSERTION_TAG, "Assert that 'Show tasks from' filter has the '4 weeks ago' option selected by default.")
        toDoFilterPage.assertShowTasksFromOptionSelectedState(R.string.todoFilterFourWeeks, true)
        toDoFilterPage.assertShowTasksFromOptionSelectedState(R.string.todoFilterThreeWeeks, false)
        toDoFilterPage.assertShowTasksFromOptionSelectedState(R.string.todoFilterTwoWeeks, false)
        toDoFilterPage.assertShowTasksFromOptionSelectedState(R.string.todoFilterThisWeek, false)
        toDoFilterPage.assertShowTasksFromOptionSelectedState(R.string.todoFilterToday, false)

        Log.d(ASSERTION_TAG, "Assert that 'Show tasks until' filter has the 'This Week' option selected by default.")
        toDoFilterPage.assertShowTasksUntilOptionSelectedState(R.string.todoFilterThisWeek, true)
        toDoFilterPage.assertShowTasksUntilOptionSelectedState(R.string.todoFilterToday, false)
        toDoFilterPage.assertShowTasksUntilOptionSelectedState(R.string.todoFilterNextWeek, false)
        toDoFilterPage.assertShowTasksUntilOptionSelectedState(R.string.todoFilterInTwoWeeks, false)
        toDoFilterPage.assertShowTasksUntilOptionSelectedState(R.string.todoFilterInThreeWeeks, false)
        toDoFilterPage.assertShowTasksUntilOptionSelectedState(R.string.todoFilterInFourWeeks, false)

        Log.d(STEP_TAG, "Change the 'Show tasks until' filter to 'Today' option and click on 'Done' button.")
        toDoFilterPage.selectShowTasksUntilOption(R.string.todoFilterToday)
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that '${todayQuiz.title}' quiz is still displayed but the '${thisWeekAssignment.name}', '${favoriteCourseAssignment}' assignments are NOT displayed as their's due date is tomorrow and we only show items until today.")
        toDoListPage.assertItemDisplayed(todayQuiz.title)
        toDoListPage.assertItemNotDisplayed(thisWeekAssignment.name)
        toDoListPage.assertItemNotDisplayed(favoriteCourseAssignment.name)

        Log.d(STEP_TAG, "Mark the '${todayQuiz.title}' quiz as done.")
        toDoListPage.clickMarkToDoItemAsDone(todayQuiz.id)

        Log.d(ASSERTION_TAG, "Assert that the snack bar is displayed with the correct quiz title. Wait until the item disappears from the To Do List.")
        toDoListPage.waitForSnackbar(todayQuiz.title)
        toDoListPage.assertSnackbarDisplayed(todayQuiz.title)
        toDoListPage.waitForItemToDisappear(todayQuiz.title)

        Log.d(STEP_TAG, "Click on 'Undo' button on the snack bar.")
        toDoListPage.clickSnackbarUndo()

        Log.d(ASSERTION_TAG, "Assert that the '${todayQuiz.title}' quiz is back on the To Do List as we reverted the marking as done activity by clicked on the 'Undo' on the snack bar.")
        toDoListPage.waitForItemToAppear(todayQuiz.title)
        toDoListPage.assertItemDisplayed(todayQuiz.title)

        Log.d(STEP_TAG, "Mark the '${todayQuiz.title}' quiz as done.")
        toDoListPage.clickMarkToDoItemAsDone(todayQuiz.id)

        Log.d(ASSERTION_TAG, "Assert that the To Do List Page is empty because of the (default) filters.")
        toDoListPage.assertEmptyState()

        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(ASSERTION_TAG, "Assert that the 'Show tasks until' filter has the 'Today' option selected.")
        toDoFilterPage.assertShowTasksUntilOptionSelectedState(R.string.todoFilterToday, true)

        Log.d(STEP_TAG, "Select the 'Completed' visible items filter and click on 'Close' button, thus the changes won't be applied.")
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterShowCompleted)
        toDoFilterPage.clickClose()

        Log.d(ASSERTION_TAG, "Assert that the To Do List Page is still empty because we did not save the 'Completed' filter by clicking on the 'Done' button, rather we clicked on the Close (X) button so the changes won't be applied.")
        toDoListPage.assertEmptyState()

        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(STEP_TAG, "Select the 'Completed' visible items filter and click on 'Done' button.")
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterShowCompleted)
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that '${todayQuiz.title}' quiz is displayed because we are filtered to see completed items (as well).")
        toDoListPage.assertItemDisplayed(todayQuiz.title)
        toDoListPage.assertItemNotDisplayed(favoriteCourseAssignment.name)
        toDoListPage.assertItemNotDisplayed(thisWeekAssignment.name)

        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(STEP_TAG, "UNselect the 'Completed' visible items filter and set back the 'Show tasks until' filter to 'This Week' (default) option and click on 'Done' button.")
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterShowCompleted)
        toDoFilterPage.selectShowTasksUntilOption(R.string.todoFilterThisWeek)
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that '${todayQuiz.title}' quiz is NOT displayed since it's already completed and '${thisWeekAssignment.name}', '${favoriteCourseAssignment.name}' assignments ARE displayed as their's due date is in the current week.")
        toDoListPage.assertItemDisplayed(thisWeekAssignment.name)
        toDoListPage.assertItemDisplayed(favoriteCourseAssignment.name)
        toDoListPage.assertItemNotDisplayed(todayQuiz.title)

        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(STEP_TAG, "Select the 'Favorite Courses Only' visible items filter and click on 'Done' button.")
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterFavoriteCoursesOnly)
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that only the favorite course's assignment, '${favoriteCourseAssignment.name}' is displayed and the rest of the assignments/quizzes are not.")
        toDoListPage.assertItemDisplayed(favoriteCourseAssignment.name)
        toDoListPage.assertItemNotDisplayed(thisWeekAssignment.name)
        toDoListPage.assertItemNotDisplayed(twoWeeksAwayAssignment.name)
        toDoListPage.assertItemNotDisplayed(todayQuiz.title)
        toDoListPage.assertItemNotDisplayed(nextWeekQuiz.title)

        //Check next week in future
        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(STEP_TAG, "Unselect the 'Favorite Courses Only' visible items filter and change the 'Show tasks until' filter to 'Next Week' option and click on 'Done' button")
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterFavoriteCoursesOnly)
        toDoFilterPage.selectShowTasksUntilOption(R.string.todoFilterNextWeek)
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that '${thisWeekAssignment.name}', '${favoriteCourseAssignment.name}' assignments and '${nextWeekQuiz.title}' quiz are displayed as their due date is in the current or next week.")
        toDoListPage.assertItemDisplayed(thisWeekAssignment.name)
        toDoListPage.assertItemDisplayed(favoriteCourseAssignment.name)
        toDoListPage.assertItemDisplayed(nextWeekQuiz.title)

        Log.d(ASSERTION_TAG, "Assert that the '${threeWeeksAwayQuiz.title}' quiz and '${twoWeeksAwayAssignment.name}', '${fourWeeksAwayAssignment.name}' assignments are NOT displayed as their due date is beyond the current or next week. Also assert that the '${todayQuiz.title}' quiz is NOT displayed as it's already completed.")
        toDoListPage.assertItemNotDisplayed(twoWeeksAwayAssignment.name)
        toDoListPage.assertItemNotDisplayed(threeWeeksAwayQuiz.title)
        toDoListPage.assertItemNotDisplayed(fourWeeksAwayAssignment.name)
        toDoListPage.assertItemNotDisplayed(todayQuiz.title) // Not displayed as it's completed.

        //Check 2 weeks in future
        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(STEP_TAG, "Change the 'Show tasks until' filter to 'In Two Weeks' option and click on 'Done' button.")
        toDoFilterPage.selectShowTasksUntilOption(R.string.todoFilterInTwoWeeks)
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that '${thisWeekAssignment.name}', '${favoriteCourseAssignment.name}', '${twoWeeksAwayAssignment.name}' assignments and '${nextWeekQuiz.title}' quiz are displayed as their due date is in the current or next 2 weeks.")
        toDoListPage.assertItemDisplayed(thisWeekAssignment.name)
        toDoListPage.assertItemDisplayed(favoriteCourseAssignment.name)
        toDoListPage.assertItemDisplayed(nextWeekQuiz.title)
        toDoListPage.assertItemDisplayed(twoWeeksAwayAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the '${threeWeeksAwayQuiz.title}' quiz and '${fourWeeksAwayAssignment.name}' assignment are NOT displayed as their due date is beyond the current or next 2 weeks. Also assert that the '${todayQuiz.title}' quiz is NOT displayed as it's already completed.")
        toDoListPage.assertItemNotDisplayed(threeWeeksAwayQuiz.title)
        toDoListPage.assertItemNotDisplayed(fourWeeksAwayAssignment.name)
        toDoListPage.assertItemNotDisplayed(todayQuiz.title) // Not displayed as it's completed.

        //Check 3 weeks in future
        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(STEP_TAG, "Change the 'Show tasks until' filter to 'In Three Weeks' option and click on 'Done' button.")
        toDoFilterPage.selectShowTasksUntilOption(R.string.todoFilterInThreeWeeks)
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that '${thisWeekAssignment.name}', '${favoriteCourseAssignment.name}', '${twoWeeksAwayAssignment.name}' assignments and '${threeWeeksAwayQuiz}' quizzes is displayed as their due date is in the current or next 3 weeks.")
        toDoListPage.assertItemDisplayed(thisWeekAssignment.name)
        toDoListPage.assertItemDisplayed(favoriteCourseAssignment.name)
        toDoListPage.assertItemDisplayed(nextWeekQuiz.title)
        toDoListPage.assertItemDisplayed(twoWeeksAwayAssignment.name)
        toDoListPage.assertItemDisplayed(threeWeeksAwayQuiz.title)

        Log.d(ASSERTION_TAG, "Assert that the '${fourWeeksAwayAssignment.name}' assignment is NOT displayed as it's due date is beyond the current or next 3 weeks. Also assert that the '${todayQuiz.title}' quiz is NOT displayed as it's already completed.")
        toDoListPage.assertItemNotDisplayed(fourWeeksAwayAssignment.name)
        toDoListPage.assertItemNotDisplayed(todayQuiz.title) // Not displayed as it's completed.

        //Check 4 weeks in future
        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(STEP_TAG, "Change the 'Show tasks until' filter to 'In Four Weeks' option and click on 'Done' button.")
        toDoFilterPage.selectShowTasksUntilOption(R.string.todoFilterInFourWeeks)
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that '${thisWeekAssignment.name}', '${favoriteCourseAssignment.name}', '${twoWeeksAwayAssignment.name}', '${fourWeeksAwayAssignment.name}' assignments and '${threeWeeksAwayQuiz}' quizzes are displayed as their due date is in the current or next 4 weeks. ")
        toDoListPage.assertItemDisplayed(thisWeekAssignment.name)
        toDoListPage.assertItemDisplayed(favoriteCourseAssignment.name)
        toDoListPage.assertItemDisplayed(nextWeekQuiz.title)
        toDoListPage.assertItemDisplayed(twoWeeksAwayAssignment.name)
        toDoListPage.assertItemDisplayed(threeWeeksAwayQuiz.title)
        toDoListPage.assertItemDisplayed(fourWeeksAwayAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the '${threeWeeksAwayQuiz.title}' quiz and '${twoWeeksAwayAssignment.name}', '${fourWeeksAwayAssignment.name}' assignments are NOT displayed as their due date is beyond the current or next 4 weeks. Also assert that the '${todayQuiz.title}' quiz is NOT displayed as it's already completed.")
        toDoListPage.assertItemNotDisplayed(todayQuiz.title) // Not displayed as it's completed.

        Log.d(STEP_TAG, "Click on '${thisWeekAssignment.name}' assignment to open its details page.")
        toDoListPage.clickOnItem(thisWeekAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the '${thisWeekAssignment.name}' assignment details page is displayed with the correct assignment details.")
        assignmentDetailsPage.assertAssignmentDetails(thisWeekAssignment)

        Log.d(STEP_TAG, "Navigate back to the To Do List Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on '${nextWeekQuiz.title}' quiz to open its details page.")
        toDoListPage.clickOnItem(nextWeekQuiz.title)

        Log.d(ASSERTION_TAG, "Assert that the '${nextWeekQuiz.title}' quiz title is displayed on the quiz details page.")
        canvasWebViewPage.runTextChecks(WebViewTextCheck(locatorType = Locator.ID, locatorValue = "quiz_title", textValue = nextWeekQuiz.title))
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.TODOS, TestCategory.E2E)
    fun testNewTodoPastFiltersE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seed a quiz for '${course.name}' course with today due date.")
        val todayQuiz =
            QuizzesApi.createQuiz(course.id, teacher.token, dueAt = 0.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG, "Seed an assignment for '${course.name}' course with this week but PAST due date.")
        val thisWeekPastAssignment = AssignmentsApi.createAssignment( // Need to "exclude" this one on sunday runs because then this assignment is on Saturday and it would confuse the test logic.
            course.id,
            teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 15.0,
            dueAt = 1.days.ago.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Log.d(PREPARATION_TAG, "Seed a quiz for '${course.name}' course with 1 week AGO (so 'Last Week') due date.")
        val lastWeekQuiz = QuizzesApi.createQuiz(course.id, teacher.token, dueAt = 7.days.ago.iso8601)

        Log.d(PREPARATION_TAG, "Seed an assignment for '${course.name}' course with 2 weeks AGO due date.")
        val twoWeeksAgoAssignment = AssignmentsApi.createAssignment(
            course.id,
            teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 20.0,
            dueAt = 14.days.ago.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Log.d(PREPARATION_TAG, "Seed another quiz for '${course.name}' course with 3 weeks AGO due date.")
        val threeWeeksAgoQuiz =
            QuizzesApi.createQuiz(course.id, teacher.token, dueAt = 21.days.ago.iso8601)

        Log.d(PREPARATION_TAG, "Seed an assignment for '${course.name}' course with 4 weeks AGO due date.")
        val fourWeeksAgoAssignment = AssignmentsApi.createAssignment(
            course.id,
            teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 20.0,
            dueAt = 28.days.ago.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        val calendar = Calendar.getInstance()

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Navigate to 'To Do' Page via bottom-menu.")
        dashboardPage.waitForRender()
        dashboardPage.clickTodoTab()

        //Check 4 weeks in past (default option)
        Log.d(ASSERTION_TAG,"Assert that '${fourWeeksAgoAssignment.name}', '${thisWeekPastAssignment.name}', '${twoWeeksAgoAssignment.name}' assignments and '${todayQuiz.title}', '${lastWeekQuiz.title}', '${threeWeeksAgoQuiz.title}' quizzes are displayed as their due date is in the current or past 4 weeks.")
        toDoListPage.assertItemDisplayed(todayQuiz.title)
        if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) toDoListPage.assertItemDisplayed(thisWeekPastAssignment.name)
        toDoListPage.assertItemDisplayed(lastWeekQuiz.title)
        toDoListPage.assertItemDisplayed(twoWeeksAgoAssignment.name)
        toDoListPage.assertItemDisplayed(threeWeeksAgoQuiz.title)
        toDoListPage.assertItemDisplayed(fourWeeksAgoAssignment.name)

        //Check 3 weeks in past
        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(ASSERTION_TAG, "Assert that the To Do Filter Page is displayed with the correct title.")
        toDoFilterPage.assertFilterScreenTitle()

        Log.d(ASSERTION_TAG, "Assert that the 'Show tasks from' filter has the '4 Weeks Ago' option selected as default.")
        toDoFilterPage.assertShowTasksFromOptionSelectedState(R.string.todoFilterFourWeeks, true)

        Log.d(STEP_TAG, "Change the 'Show tasks from' filter to '3 Weeks Ago' option and click on 'Done' button.")
        toDoFilterPage.selectShowTasksFromOption(R.string.todoFilterThreeWeeks)
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG,"Assert that '${thisWeekPastAssignment.name}', '${twoWeeksAgoAssignment.name}' assignments and '${todayQuiz.title}', '${lastWeekQuiz.title}', '${threeWeeksAgoQuiz.title}' quizzes are displayed as their due date is in the current or past 3 weeks.")
        toDoListPage.assertItemDisplayed(todayQuiz.title)
        if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) toDoListPage.assertItemDisplayed(thisWeekPastAssignment.name)
        toDoListPage.assertItemDisplayed(lastWeekQuiz.title)
        toDoListPage.assertItemDisplayed(twoWeeksAgoAssignment.name)
        toDoListPage.assertItemDisplayed(threeWeeksAgoQuiz.title)

        Log.d(ASSERTION_TAG,"Assert that the '${fourWeeksAgoAssignment.name}' assignment is NOT displayed as its due date is beyond the past 3 weeks.")
        toDoListPage.assertItemNotDisplayed(fourWeeksAgoAssignment.name)

        //Check 2 weeks in past
        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(STEP_TAG, "Change the 'Show tasks from' filter to '2 Weeks Ago' option and click on 'Done' button.")
        toDoFilterPage.selectShowTasksFromOption(R.string.todoFilterTwoWeeks)
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG,"Assert that '${thisWeekPastAssignment.name}', '${twoWeeksAgoAssignment.name}' assignments and '${todayQuiz.title}', '${lastWeekQuiz.title}', '${threeWeeksAgoQuiz.title}' quizzes are displayed as their due date is in the current or past 2 weeks.")
        toDoListPage.assertItemDisplayed(todayQuiz.title)
        if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) toDoListPage.assertItemDisplayed(thisWeekPastAssignment.name)
        toDoListPage.assertItemDisplayed(lastWeekQuiz.title)
        toDoListPage.assertItemDisplayed(twoWeeksAgoAssignment.name)

        Log.d(ASSERTION_TAG,"Assert that the '${fourWeeksAgoAssignment.name}' assignment and '${threeWeeksAgoQuiz.title}' quiz are NOT displayed as its due date is beyond the past 2 weeks.")
        toDoListPage.assertItemNotDisplayed(fourWeeksAgoAssignment.name)
        toDoListPage.assertItemNotDisplayed(threeWeeksAgoQuiz.title)

        //Check 1 week in past (Last Week)
        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(STEP_TAG, "Change the 'Show tasks from' filter to 'Last Week' option and click on 'Done' button.")
        toDoFilterPage.selectShowTasksFromOption(R.string.todoFilterLastWeek)
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG,"Assert that '${thisWeekPastAssignment.name}', '${twoWeeksAgoAssignment.name}' assignments and '${todayQuiz.title}', '${lastWeekQuiz.title}', '${threeWeeksAgoQuiz.title}' quizzes are displayed as their due date is in the current or last week.")
        toDoListPage.assertItemDisplayed(todayQuiz.title)
        if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) toDoListPage.assertItemDisplayed(thisWeekPastAssignment.name)
        toDoListPage.assertItemDisplayed(lastWeekQuiz.title)

        Log.d(ASSERTION_TAG,"Assert that the '${fourWeeksAgoAssignment.name}', '${twoWeeksAgoAssignment.name}' assignments and '${threeWeeksAgoQuiz.title}' quiz are NOT displayed as its due date is beyond the last week.")
        toDoListPage.assertItemNotDisplayed(fourWeeksAgoAssignment.name)
        toDoListPage.assertItemNotDisplayed(threeWeeksAgoQuiz.title)
        toDoListPage.assertItemNotDisplayed(twoWeeksAgoAssignment.name)

        //Check this week in past (This Week)
        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(STEP_TAG, "Change the 'Show tasks from' filter to 'This Week' option and click on 'Done' button.")
        toDoFilterPage.selectShowTasksFromOption(R.string.todoFilterThisWeek)
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG,"Assert that '${thisWeekPastAssignment.name}' assignment and '${todayQuiz.title}' quiz are displayed as their due date is in the current week.")
        toDoListPage.assertItemDisplayed(todayQuiz.title)
        if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) toDoListPage.assertItemDisplayed(thisWeekPastAssignment.name)

        Log.d(ASSERTION_TAG,"Assert that the '${fourWeeksAgoAssignment.name}', '${twoWeeksAgoAssignment.name}' assignments and '${threeWeeksAgoQuiz.title}', '${lastWeekQuiz.title}' quizzes are NOT displayed as its due date is beyond the week.")
        toDoListPage.assertItemNotDisplayed(fourWeeksAgoAssignment.name)
        toDoListPage.assertItemNotDisplayed(threeWeeksAgoQuiz.title)
        toDoListPage.assertItemNotDisplayed(twoWeeksAgoAssignment.name)
        toDoListPage.assertItemNotDisplayed(lastWeekQuiz.title)

        //Check Today in past filters (Today)
        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(STEP_TAG, "Change the 'Show tasks from' filter to 'Today' option and click on 'Done' button.")
        toDoFilterPage.selectShowTasksFromOption(R.string.todoFilterToday)
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG,"Assert that '${todayQuiz.title}' quiz and '${thisWeekPastAssignment.name}' assignment are displayed as their's due date is in the current week.")
        toDoListPage.assertItemDisplayed(todayQuiz.title)

        Log.d(ASSERTION_TAG,"Assert that the '${fourWeeksAgoAssignment.name}', '${twoWeeksAgoAssignment.name}', '${thisWeekPastAssignment.name}' assignments and '${threeWeeksAgoQuiz.title}', '${lastWeekQuiz.title}' quizzes are NOT displayed as its due date is beyond the week.")
        toDoListPage.assertItemNotDisplayed(fourWeeksAgoAssignment.name)
        toDoListPage.assertItemNotDisplayed(threeWeeksAgoQuiz.title)
        toDoListPage.assertItemNotDisplayed(twoWeeksAgoAssignment.name)
        toDoListPage.assertItemNotDisplayed(lastWeekQuiz.title)
        toDoListPage.assertItemNotDisplayed(thisWeekPastAssignment.name)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.TODOS, TestCategory.E2E)
    fun testToDoCalendarEventsAndPersonalToDosAndSwipeGesturesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seed a calendar event for '${course.name}' (where '${student.name}' student is enrolled as a student.")
        val testCalendarEvent = CalendarEventApi.createCalendarEvent(
            teacher.token,
            CanvasContext.makeContextId(CanvasContext.Type.COURSE, course.id),
            "First Student Test Event",
            Date().toApiString()
        )

        Log.d(PREPARATION_TAG, "Seed a personal To Do for '${student.name}' student.")
        val testCalendarPersonalToDo = PlannerAPI.createPlannerNote(
            student.token,
            "Student Test Personal ToDo",
            "Personal ToDo Details",
            0.days.fromNow.iso8601
        )

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Navigate to 'To Do' Page via bottom-menu.")
        dashboardPage.waitForRender()
        dashboardPage.clickTodoTab()

        Log.d(ASSERTION_TAG, "Assert that the To Do List Page is empty because of the (default) filters.")
        toDoListPage.assertEmptyState()

        //Show both types: Calendar Events and Personal To Dos
        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(STEP_TAG, "Select the 'Show Personal To Dos' and 'Show Calendar Events' visible items filter and click on 'Done' button.")
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterShowPersonalToDos)
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterShowCalendarEvents)
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that '${testCalendarEvent.title}' calendar event and '${testCalendarPersonalToDo.title}' personal To Do are displayed because we filtered to Personal To Dos and calendar events.")
        toDoListPage.assertItemDisplayed(testCalendarEvent.title)
        toDoListPage.assertItemDisplayed(testCalendarPersonalToDo.title)

        Log.d(STEP_TAG, "Click on the '${testCalendarEvent.title}' calendar event item to open its details page.")
        toDoListPage.clickOnItem(testCalendarEvent.title)

        Log.d(ASSERTION_TAG, "Assert that the '${testCalendarEvent.title}' calendar event details page is displayed with the correct event title.")
        calendarEventDetailsPage.assertEventTitle(testCalendarEvent.title)

        Log.d(STEP_TAG, "Navigate back to the To Do List Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on the '${testCalendarPersonalToDo.title}' personal To Do item to open its details page.")
        toDoListPage.clickOnItem(testCalendarPersonalToDo.title)

        Log.d(ASSERTION_TAG, "Assert that the '${testCalendarPersonalToDo.title}' personal To Do details page is displayed with the correct event title.")
        calendarToDoDetailsPage.assertTitle(testCalendarPersonalToDo.title)

        Log.d(STEP_TAG, "Navigate back to the To Do List Page.")
        Espresso.pressBack()

        //Show only Personal To Dos
        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(STEP_TAG, "Select the 'Show Personal To Dos' visible item filter and click on 'Done' button.")
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterShowCalendarEvents) // Toggle off Calendar Events to show only Personal To Dos
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that the '${testCalendarPersonalToDo.title}' personal To Do is displayed, BUT the '${testCalendarEvent.title}' calendar event is not according to current filters.")
        toDoListPage.assertItemDisplayed(testCalendarPersonalToDo.title)
        toDoListPage.assertItemNotDisplayed(testCalendarEvent.title)

        //Show only Calendar Events
        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(STEP_TAG, "Select the 'Show Calendar Events' visible item filter and click on 'Done' button.")
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterShowPersonalToDos) // Toggle off Personal To Dos
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterShowCalendarEvents) // Toggle on Calendar Events
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that the '${testCalendarEvent.title}' calendar event is displayed, BUT the '${testCalendarPersonalToDo.title}' personal To Do is not according to current filters.")
        toDoListPage.assertItemDisplayed(testCalendarEvent.title)
        toDoListPage.assertItemNotDisplayed(testCalendarPersonalToDo.title)

        Log.d(STEP_TAG, "Click on the date badge for today to navigate to the calendar today.")
        val todayDayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        toDoListPage.clickDateBadge(todayDayOfMonth)

        Log.d(ASSERTION_TAG, "Assert that the Calendar Page is displayed with the correct title. Assert that both the previously created calendar event '${testCalendarEvent.title}' and personal To Do '${testCalendarPersonalToDo.title}' are displayed on the calendar for today.")
        calendarScreenPage.assertCalendarPageTitle()
        calendarScreenPage.assertItemDisplayed(testCalendarPersonalToDo.title)
        calendarScreenPage.assertItemDisplayed(testCalendarEvent.title)

        Log.d(STEP_TAG, "Navigate back to the To Do List Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(STEP_TAG, "Select the 'Show Calendar Events' visible item filter and click on 'Done' button.")
        toDoFilterPage.selectVisibleItemsOption(R.string.todoFilterShowPersonalToDos) // Toggle off Personal To Dos
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that '${testCalendarEvent.title}' calendar event and '${testCalendarPersonalToDo.title}' personal To Do are displayed because we filtered to Personal To Dos and calendar events.")
        toDoListPage.assertItemDisplayed(testCalendarEvent.title)
        toDoListPage.assertItemDisplayed(testCalendarPersonalToDo.title)

        Log.d(STEP_TAG, "Swipe right on the '${testCalendarEvent.title}' calendar event to mark it as done.")
        toDoListPage.swipeItemRight(testCalendarEvent.id!!.toLong())

        Log.d(ASSERTION_TAG, "Assert that the snack bar is displayed with the correct calendar event title.")
        toDoListPage.waitForSnackbar(testCalendarEvent.title)
        toDoListPage.assertSnackbarDisplayed(testCalendarEvent.title)

        Log.d(ASSERTION_TAG, "Assert that '${testCalendarEvent.title}' calendar event IS NOT displayed anymore, but the '${testCalendarPersonalToDo.title}' personal To Do is displayed.")
        toDoListPage.assertItemNotDisplayed(testCalendarEvent.title)
        toDoListPage.assertItemDisplayed(testCalendarPersonalToDo.title)

        Log.d(STEP_TAG, "Swipe left on the '${testCalendarPersonalToDo.title}' personal To Do to mark it as done.")
        toDoListPage.swipeItemLeft(testCalendarPersonalToDo.id!!.toLong())

        Log.d(ASSERTION_TAG, "Assert that the snack bar is displayed with the correct personal To Do title.")
        toDoListPage.waitForSnackbar(testCalendarPersonalToDo.title)
        toDoListPage.assertSnackbarDisplayed(testCalendarPersonalToDo.title)

        Log.d(STEP_TAG, "Click on 'Undo' button on the snack bar to 'revert' the swipe action.")
        toDoListPage.clickSnackbarUndo()

        Log.d(ASSERTION_TAG, "Assert that the '${testCalendarPersonalToDo.title}' personal To Do is back on the To Do List as we reverted the marking as done activity by clicked on the 'Undo' on the snack bar.")
        toDoListPage.waitForItemToAppear(testCalendarPersonalToDo.title)
        toDoListPage.assertItemDisplayed(testCalendarPersonalToDo.title)

        Log.d(STEP_TAG, "Swipe left on the '${testCalendarPersonalToDo.title}' personal To Do to mark it as done.")
        toDoListPage.swipeItemLeft(testCalendarPersonalToDo.id!!.toLong())

        Log.d(ASSERTION_TAG, "Assert that the snack bar is displayed with the correct personal To Do title.")
        toDoListPage.waitForSnackbar(testCalendarPersonalToDo.title)
        toDoListPage.assertSnackbarDisplayed(testCalendarPersonalToDo.title)

        Log.d(ASSERTION_TAG, "Assert that the To Do List Page is empty because of the (default) filters.")
        toDoListPage.assertEmptyState()
    }
}