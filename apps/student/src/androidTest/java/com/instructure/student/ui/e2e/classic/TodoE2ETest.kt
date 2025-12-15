package com.instructure.student.ui.e2e.classic

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.annotations.Stub
import com.instructure.canvas.espresso.refresh
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.pandautils.R
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.seedAssignments
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.lang.Thread.sleep

@HiltAndroidTest
class TodoE2ETest: StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @Stub
    @TestMetaData(Priority.MANDATORY, FeatureCategory.TODOS, TestCategory.E2E)
    fun testTodoE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 2)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val favoriteCourse = data.coursesList[1]

        Log.d(
            PREPARATION_TAG,
            "Seed an assignment for '${course.name}' course with tomorrow due date."
        )
        val testAssignment = AssignmentsApi.createAssignment(
            course.id,
            teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 15.0,
            dueAt = 1.days.fromNow.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Log.d(
            PREPARATION_TAG,
            "Seed another assignment for '${course.name}' course with 7 days from now due date."
        )
        val seededAssignments2 = seedAssignments(
            courseId = course.id,
            teacherToken = teacher.token,
            dueAt = 7.days.fromNow.iso8601
        )

        val borderDateAssignment =
            seededAssignments2[0] //We show items in the to do section which are within 7 days.

        Log.d(PREPARATION_TAG, "Seed a quiz for '${course.name}' course with tomorrow due date.")
        val quiz = QuizzesApi.createQuiz(course.id, teacher.token, dueAt = 1.days.fromNow.iso8601)

        Log.d(
            PREPARATION_TAG,
            "Seed another quiz for '${course.name}' course with 8 days from now due date.."
        )
        val farAwayQuiz =
            QuizzesApi.createQuiz(course.id, teacher.token, dueAt = 8.days.fromNow.iso8601)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)

        Log.d(
            STEP_TAG,
            "Wait for the Dashboard Page to be rendered. Navigate to 'To Do' Page via bottom-menu."
        )
        dashboardPage.waitForRender()
        dashboardPage.clickTodoTab()

        Log.d(
            ASSERTION_TAG,
            "Assert that '${testAssignment.name}' assignment is displayed and '${borderDateAssignment.name}' assignment is displayed because it's 7 days away from now." +
                    "Assert that '${quiz.title}' quiz is displayed and '${farAwayQuiz.title}' quiz is also displayed."
        )
        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = { refresh() }) {
            todoPage.assertAssignmentDisplayed(testAssignment)
            todoPage.assertAssignmentDisplayed(borderDateAssignment)
            todoPage.assertQuizDisplayed(quiz)
            todoPage.assertQuizDisplayed(farAwayQuiz)
        }

        Log.d(
            PREPARATION_TAG,
            "Submit' '${testAssignment.name}' assignment for '${student.name}' student."
        )
        SubmissionsApi.seedAssignmentSubmission(
            course.id, student.token, testAssignment.id,
            submissionSeedsList = listOf(
                SubmissionsApi.SubmissionSeedInfo(
                    amount = 1,
                    submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )
            )
        )

        Log.d(STEP_TAG, "Refresh the 'To Do' Page.")
        refresh()

        Log.d(
            ASSERTION_TAG,
            "Assert that the previously submitted assignment: '${testAssignment}', is not displayed on the To Do list any more."
        )
        todoPage.assertAssignmentNotDisplayed(testAssignment)
        todoPage.assertAssignmentDisplayedWithRetries(borderDateAssignment, 5)

        Log.d(STEP_TAG, "Apply 'Favorite Courses' filter.")
        todoPage.chooseFavoriteCourseFilter()

        Log.d(
            ASSERTION_TAG,
            "Assert that the 'Favorite Courses' header filter and the empty view is displayed."
        )
        todoPage.assertFavoritedCoursesFilterHeader()
        todoPage.assertEmptyView()

        Log.d(STEP_TAG, "Clear 'Favorite Courses' filter.")
        todoPage.clearFilter()

        sleep(2000) //Allow the filter clarification to propagate.

        Log.d(
            ASSERTION_TAG,
            "Assert that '${borderDateAssignment.name}' assignment and '${quiz.title}' quiz are displayed."
        )
        todoPage.assertAssignmentDisplayedWithRetries(borderDateAssignment, 5)
        todoPage.assertQuizDisplayed(quiz)

        Log.d(ASSERTION_TAG, "Assert that '${testAssignment}' assignment is not displayed.")
        todoPage.assertAssignmentNotDisplayed(testAssignment)

        Log.d(
            PREPARATION_TAG,
            "Seed an assignment for '${favoriteCourse.name}' course with tomorrow due date."
        )
        val favoriteCourseAssignment = AssignmentsApi.createAssignment(
            favoriteCourse.id,
            teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 15.0,
            dueAt = 1.days.fromNow.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Log.d(
            STEP_TAG,
            "Navigate back to the Dashboard Page. Open '${favoriteCourse.name}' course. Mark it as favorite."
        )
        Espresso.pressBack()
        dashboardPage.openAllCoursesPage()
        allCoursesPage.favoriteCourse(favoriteCourse.name)

        Log.d(STEP_TAG, "Navigate back to the Dashboard Page and open the To Do Page again.")
        Espresso.pressBack()
        sleep(3000) //Wait for the bottom toast message 'Added to Dashboard' to be disappear.
        dashboardPage.clickTodoTab()

        Log.d(STEP_TAG, "Apply 'Favorite Courses' filter.")
        todoPage.chooseFavoriteCourseFilter()

        Log.d(ASSERTION_TAG, "Assert that the 'Favorite Courses' header filter is displayed.")
        todoPage.assertFavoritedCoursesFilterHeader()
        refresh() // We need to refresh the page in order to see the favorite course's assignments.

        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = { refresh() }) {
            // It's working well on product version, maybe the backend is working differently on beta environment.
            Log.d(
                ASSERTION_TAG,
                "Assert that only the favorite course's assignment, '${favoriteCourseAssignment.name}' is displayed."
            )
            todoPage.assertAssignmentDisplayedWithRetries(favoriteCourseAssignment, 5)
        }

        todoPage.assertAssignmentNotDisplayed(testAssignment)
        todoPage.assertAssignmentNotDisplayed(borderDateAssignment)
        todoPage.assertQuizNotDisplayed(quiz)
        todoPage.assertQuizNotDisplayed(farAwayQuiz)
    }

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
        val todayQuiz = QuizzesApi.createQuiz(course.id, teacher.token, dueAt = 0.days.fromNow.iso8601)

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

        Log.d(STEP_TAG, "Change the 'Show tasks until' filter to 'Next Week' option and click on 'Done' button.")
        toDoFilterPage.selectShowTasksUntilOption(R.string.todoFilterNextWeek)
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that '${thisWeekAssignment.name}', '${favoriteCourseAssignment.name}' assignments and '${todayQuiz.title}' quiz are displayed as their due date is in the current or next week.")
        toDoListPage.assertItemDisplayed(thisWeekAssignment.name)
        toDoListPage.assertItemDisplayed(favoriteCourseAssignment.name)
        toDoListPage.assertItemDisplayed(nextWeekQuiz.title)

        Log.d(ASSERTION_TAG, "Assert that the '${threeWeeksAwayQuiz.title}' quiz and '${twoWeeksAwayAssignment.name}', '${fourWeeksAwayAssignment.name}' assignments are NOT displayed as their due date is beyond the current or next week.")
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

        Log.d(ASSERTION_TAG, "Assert that '${thisWeekAssignment.name}', '${favoriteCourseAssignment.name}', '${twoWeeksAwayAssignment.name}' assignments and '${todayQuiz.title}' quiz are displayed as their due date is in the current or next 2 weeks.")
        toDoListPage.assertItemDisplayed(thisWeekAssignment.name)
        toDoListPage.assertItemDisplayed(favoriteCourseAssignment.name)
        toDoListPage.assertItemDisplayed(nextWeekQuiz.title)
        toDoListPage.assertItemDisplayed(twoWeeksAwayAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the '${threeWeeksAwayQuiz.title}' quiz and '${fourWeeksAwayAssignment.name}' assignment are NOT displayed as their due date is beyond the current or next 2 weeks.")
        toDoListPage.assertItemNotDisplayed(threeWeeksAwayQuiz.title)
        toDoListPage.assertItemNotDisplayed(fourWeeksAwayAssignment.name)
        toDoListPage.assertItemNotDisplayed(todayQuiz.title) // Not displayed as it's completed.

        //Check 3 weeks in future
        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(STEP_TAG, "Change the 'Show tasks until' filter to 'In Three Weeks' option and click on 'Done' button.")
        toDoFilterPage.selectShowTasksUntilOption(R.string.todoFilterInThreeWeeks)
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that '${thisWeekAssignment.name}', '${favoriteCourseAssignment.name}', '${twoWeeksAwayAssignment.name}' assignments and '${todayQuiz.title}' and '${threeWeeksAwayQuiz}' quizzes are displayed as their due date is in the current or next 3 weeks.")
        toDoListPage.assertItemDisplayed(thisWeekAssignment.name)
        toDoListPage.assertItemDisplayed(favoriteCourseAssignment.name)
        toDoListPage.assertItemDisplayed(nextWeekQuiz.title)
        toDoListPage.assertItemDisplayed(twoWeeksAwayAssignment.name)
        toDoListPage.assertItemNotDisplayed(threeWeeksAwayQuiz.title)

        Log.d(ASSERTION_TAG, "Assert that the '${fourWeeksAwayAssignment.name}' assignment is NOT displayed as it's due date is beyond the current or next 3 weeks.")
        toDoListPage.assertItemNotDisplayed(fourWeeksAwayAssignment.name)
        toDoListPage.assertItemNotDisplayed(todayQuiz.title) // Not displayed as it's completed.

        //Check 4 weeks in future
        Log.d(STEP_TAG, "Open the To Do Filter Page.")
        toDoListPage.clickFilterButton()

        Log.d(STEP_TAG, "Change the 'Show tasks until' filter to 'In Four Weeks' option and click on 'Done' button.")
        toDoFilterPage.selectShowTasksUntilOption(R.string.todoFilterInFourWeeks)
        toDoFilterPage.clickDone()

        Log.d(ASSERTION_TAG, "Assert that '${thisWeekAssignment.name}', '${favoriteCourseAssignment.name}', '${twoWeeksAwayAssignment.name}', '${fourWeeksAwayAssignment.name}' assignments and '${todayQuiz.title}' and '${threeWeeksAwayQuiz}' quizzes are displayed as their due date is in the current or next 4 weeks.")
        toDoListPage.assertItemDisplayed(thisWeekAssignment.name)
        toDoListPage.assertItemDisplayed(favoriteCourseAssignment.name)
        toDoListPage.assertItemDisplayed(nextWeekQuiz.title)
        toDoListPage.assertItemDisplayed(twoWeeksAwayAssignment.name)
        toDoListPage.assertItemNotDisplayed(threeWeeksAwayQuiz.title)
        toDoListPage.assertItemNotDisplayed(fourWeeksAwayAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the '${threeWeeksAwayQuiz.title}' quiz and '${twoWeeksAwayAssignment.name}', '${fourWeeksAwayAssignment.name}' assignments are NOT displayed as their due date is beyond the current or next 4 weeks.")
        toDoListPage.assertItemNotDisplayed(todayQuiz.title) // Not displayed as it's completed.
    }
}