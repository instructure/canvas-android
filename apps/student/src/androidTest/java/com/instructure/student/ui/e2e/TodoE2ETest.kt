package com.instructure.student.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.refresh
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.retry
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedAssignments
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.lang.Thread.sleep

@HiltAndroidTest
class TodoE2ETest: StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.TODOS, TestCategory.E2E)
    fun testTodoE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 2)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val favoriteCourse = data.coursesList[1]

        Log.d(PREPARATION_TAG,"Seed an assignment for ${course.name} course with tomorrow due date.")
        val testAssignment = createAssignment(course, teacher)

        Log.d(PREPARATION_TAG,"Seed another assignment for ${course.name} course with 7 days from now due date.")
        val seededAssignments2 = seedAssignments(
            courseId = course.id,
            teacherToken = teacher.token,
            dueAt = 7.days.fromNow.iso8601
        )

        val borderDateAssignment = seededAssignments2[0] //We show items in the to do section which are within 7 days.

        Log.d(PREPARATION_TAG,"Seed a quiz for ${course.name} course with tomorrow due date.")
        val quiz = createQuiz(course, teacher, 1.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG,"Seed another quiz for ${course.name} course with 8 days from now due date..")
        val tooFarAwayQuiz = createQuiz(course, teacher, 8.days.fromNow.iso8601)

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Navigate to 'To Do' Page via bottom-menu.")
        dashboardPage.clickTodoTab()

        Log.d(STEP_TAG,"Assert that ${testAssignment.name} assignment is displayed and ${borderDateAssignment.name} is displayed because it's 7 days away from now..")
        Log.d(STEP_TAG,"Assert that ${quiz.title} quiz is displayed and ${tooFarAwayQuiz.title} quiz is not displayed because it's end date is more than a week away..")
        retry(times = 5, delay = 3000, catchBlock = { refresh() } ) {
            todoPage.assertAssignmentDisplayed(testAssignment)
            todoPage.assertAssignmentDisplayed(borderDateAssignment)
            todoPage.assertQuizDisplayed(quiz)
            todoPage.assertQuizNotDisplayed(tooFarAwayQuiz)
        }

        Log.d(PREPARATION_TAG,"Submit ${testAssignment.name} assignment for ${student.name} student.")
        SubmissionsApi.seedAssignmentSubmission(SubmissionsApi.SubmissionSeedRequest(
            assignmentId = testAssignment.id,
            courseId = course.id,
            studentToken = student.token,
            submissionSeedsList = listOf(SubmissionsApi.SubmissionSeedInfo(
                amount = 1,
                submissionType = SubmissionType.ONLINE_TEXT_ENTRY
            ))
        ))

        Log.d(STEP_TAG, "Refresh the 'To Do' Page.")
        refresh()

        Log.d(STEP_TAG, "Assert that the previously submitted assignment: '${testAssignment}', is not displayed on the To Do list any more.")
        todoPage.assertAssignmentNotDisplayed(testAssignment)
        todoPage.assertAssignmentDisplayedWithRetries(borderDateAssignment, 5)

        Log.d(STEP_TAG, "Apply 'Favorited Courses' filter. Assert that the 'Favorited Courses' header filter and the empty view is displayed.")
        todoPage.chooseFavoriteCourseFilter()
        todoPage.assertFavoritedCoursesFilterHeader()
        todoPage.assertEmptyView()

        Log.d(STEP_TAG, "Clear 'Favorited Courses' filter. Assert that all the To Do items will be displayed again.")
        todoPage.clearFilter()
        sleep(2000) //Allow the filter clarification to propagate.

        Log.d(STEP_TAG,"Assert that '${borderDateAssignment.name}' assignment and '${quiz.title}' quiz are displayed.")
        todoPage.assertAssignmentDisplayedWithRetries(borderDateAssignment, 5)
        todoPage.assertQuizDisplayed(quiz)

        Log.d(STEP_TAG,"Assert that '${testAssignment}' assignment and '${tooFarAwayQuiz.title}' quiz are not displayed.")
        todoPage.assertAssignmentNotDisplayed(testAssignment)
        todoPage.assertQuizNotDisplayed(tooFarAwayQuiz)

        Log.d(PREPARATION_TAG,"Seed an assignment for ${favoriteCourse.name} course with tomorrow due date.")
        val favoriteCourseAssignment = createAssignment(favoriteCourse, teacher)

        Log.d(STEP_TAG, "Navigate back to the Dashboard Page. Open ${favoriteCourse.name} course. Mark it as favorite.")
        Espresso.pressBack()
        dashboardPage.clickEditDashboard()
        editDashboardPage.favoriteCourse(favoriteCourse.name)

        Log.d(STEP_TAG, "Navigate back to the Dashboard Page and open the To Do Page again.")
        Espresso.pressBack()
        sleep(3000) //Wait for the bottom toast message 'Added to Dashboard' to be disappear.
        dashboardPage.clickTodoTab()

        Log.d(STEP_TAG, "Apply 'Favorited Courses' filter. Assert that the 'Favorited Courses' header filter and the empty view is displayed.")
        todoPage.chooseFavoriteCourseFilter()
        refresh() // We need to refresh the page in order to see the favorite course's assignments.
        // It's working well on product version, maybe the backend is working differently on beta environment.
        todoPage.assertFavoritedCoursesFilterHeader()

        Log.d(STEP_TAG, "Assert that only the favorited course's assignment, '${borderDateAssignment.name}' is displayed.")
        todoPage.assertAssignmentDisplayedWithRetries(favoriteCourseAssignment, 5)
        todoPage.assertAssignmentNotDisplayed(testAssignment)
        todoPage.assertAssignmentNotDisplayed(borderDateAssignment)
        todoPage.assertQuizNotDisplayed(quiz)
        todoPage.assertQuizNotDisplayed(tooFarAwayQuiz)
    }

    private fun createQuiz(
        course: CourseApiModel,
        teacher: CanvasUserApiModel,
        dueAt: String
    ) = QuizzesApi.createQuiz(
        QuizzesApi.CreateQuizRequest(
            courseId = course.id,
            withDescription = true,
            published = true,
            token = teacher.token,
            dueAt = dueAt
        )
    )

    private fun createAssignment(
        course: CourseApiModel,
        teacher: CanvasUserApiModel
    ): AssignmentApiModel {
        return AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = GradingType.POINTS,
                teacherToken = teacher.token,
                pointsPossible = 15.0,
                dueAt = 1.days.fromNow.iso8601
            )
        )
    }
}