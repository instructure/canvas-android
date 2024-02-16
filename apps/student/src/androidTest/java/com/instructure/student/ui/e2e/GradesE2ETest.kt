package com.instructure.student.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.QuizAnswer
import com.instructure.dataseeding.model.QuizQuestion
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.student.R
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class GradesE2ETest: StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GRADES, TestCategory.E2E)
    fun testGradesE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val assignment = AssignmentsApi.createAssignment(course.id, teacher.token, withDescription = true, gradingType = GradingType.PERCENT, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY), pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601)
        val assignment2 = AssignmentsApi.createAssignment(course.id, teacher.token, withDescription = true, gradingType = GradingType.PERCENT, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY), pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG,"Create a quiz with some questions.")
        val quizQuestions = makeQuizQuestions()

        Log.d(STEP_TAG,"Publish the previously made quiz.")
        val quiz = QuizzesApi.createAndPublishQuiz(course.id, teacher.token, quizQuestions)

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select '${course.name}' course.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG,"Navigate to Grades Page.")
        courseBrowserPage.selectGrades()

        Log.d(STEP_TAG,"Assert that there is no grade for any submission yet.")
        courseGradesPage.assertTotalGrade(withText(R.string.noGradeText))

        val assignmentMatcher = withText(assignment.name)
        val quizMatcher = withText(quiz.title)
        Log.d(STEP_TAG,"Refresh the page. Assert that the '${assignment.name}' assignment and '${quiz.title}' quiz are displayed and there is no grade for them.")
        courseGradesPage.refresh()
        courseGradesPage.assertItemDisplayed(assignmentMatcher)
        courseGradesPage.assertGradeNotDisplayed(assignmentMatcher)
        courseGradesPage.assertItemDisplayed(quizMatcher)
        courseGradesPage.assertGradeNotDisplayed(quizMatcher)

        Log.d(STEP_TAG,"Check in the 'What-If Score' checkbox.")
        courseGradesPage.toggleWhatIf()

        Log.d(STEP_TAG,"Enter '12' as a what-if grade for '${assignment.name}' assignment.")
        courseGradesPage.enterWhatIfGrade(assignmentMatcher, "12")

        Log.d(STEP_TAG,"Assert that 'Total Grade' contains the score '80%'.")
        courseGradesPage.assertTotalGrade(containsTextCaseInsensitive("80"))

        Log.d(STEP_TAG,"Check out the 'What-If Score' checkbox.")
        courseGradesPage.toggleWhatIf()

        Log.d(STEP_TAG,"Assert that after disabling the 'What-If Score' checkbox there will be no 'real' grade.")
        courseGradesPage.assertTotalGrade(withText(R.string.noGradeText))

        Log.d(PREPARATION_TAG,"Seed a submission for '${assignment.name}' assignment.")
        SubmissionsApi.submitCourseAssignment(course.id, student.token, assignment.id, SubmissionType.ONLINE_TEXT_ENTRY)

        Log.d(PREPARATION_TAG,"Grade the previously seeded submission for '${assignment.name}' assignment.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, assignment.id, student.id, postedGrade = "9")

        Log.d(STEP_TAG,"Refresh the page. Assert that the assignment's score is '60%'.")
        courseGradesPage.refresh()
        courseGradesPage.assertGradeDisplayed(
                assignmentMatcher,
                containsTextCaseInsensitive("60"))

        Log.d(STEP_TAG,"Toggle 'Base on graded assignments' button. Assert that we can see the correct score (22.5%).")
        courseGradesPage.toggleBaseOnGradedAssignments()
        courseGradesPage.refreshUntilAssertTotalGrade(containsTextCaseInsensitive("22.5%"))

        Log.d(STEP_TAG,"Disable 'Base on graded assignments' button. Assert that we can see the correct score (60%).")
        courseGradesPage.toggleBaseOnGradedAssignments()
        courseGradesPage.refreshUntilAssertTotalGrade(containsTextCaseInsensitive("60"))

        Log.d(PREPARATION_TAG,"Seed a submission for '${assignment2.name}' assignment.")
        SubmissionsApi.submitCourseAssignment(course.id, student.token, assignment2.id, SubmissionType.ONLINE_TEXT_ENTRY)

        Log.d(PREPARATION_TAG,"Grade the previously seeded submission for '${assignment2.name}' assignment.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, assignment2.id, student.id, postedGrade = "10")

        Log.d(STEP_TAG,"Assert that we can see the correct score at the '${assignment2.name}' assignment (66.67%) and at the total score as well (63.33%).")
        courseGradesPage.refresh()
        courseGradesPage.assertGradeDisplayed(
            withText(assignment2.name),
            containsTextCaseInsensitive("66.67"))

        courseGradesPage.refreshUntilAssertTotalGrade(containsTextCaseInsensitive("63.33"))

        Log.d(PREPARATION_TAG,"Grade the previously seeded submission for '${assignment.name}' assignment.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, assignment.id, student.id, excused = true)
        courseGradesPage.refresh()

        Log.d(STEP_TAG,"Assert that we can see the correct score (66.67%).")
        courseGradesPage.refreshUntilAssertTotalGrade(containsTextCaseInsensitive("66.67"))

        SubmissionsApi.gradeSubmission(teacher.token, course.id, assignment.id, student.id, postedGrade = "9")
        courseGradesPage.refresh()

        Log.d(STEP_TAG,"Assert that we can see the correct score (63.33%).")
        courseGradesPage.refreshUntilAssertTotalGrade(containsTextCaseInsensitive("63.33"))

        Log.d(STEP_TAG, "Open '${assignment.name}' assignment and assert if the Assignment Details Page is displayed with the corresponding grade." +
                "Navigate back to Course Grades Page.")
        courseGradesPage.openAssignment(assignment.name)
        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertAssignmentGraded("9")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on the expand/collapse button to collapse the list and assert that the assignment will disappear from the list view.")
        courseGradesPage.clickOnExpandCollapseButton()
        courseGradesPage.assertAssignmentCount(0)

        Log.d(STEP_TAG, "Click on the expand/collapse button again to expand the list and assert that the assignment will disappear from the list view.")
        courseGradesPage.clickOnExpandCollapseButton()
        courseGradesPage.assertAssignmentCount(3)

      /*  TODO: Submit a quiz if/when we can do so via WebView
        // Let's submit our quiz
        courseGradesPage.selectItem(quizMatcher)
        assignmentDetailsPage.viewQuiz()
        quizDetailsPage.takeQuiz(quizQuestions)
        quizDetailsPage.submitQuiz()
        Espresso.pressBack() // to assignment details
        Espresso.pressBack() // to grades

        // And make sure our new quiz grade shows up in our grades
        courseGradesPage.refresh()
        courseGradesPage.assertGradeDisplayed(quizMatcher, containsTextCaseInsensitive("10/10"))
        courseGradesPage.refreshUntilAssertTotalGrade(containsTextCaseInsensitive("76"))
       */
    }

    private fun makeQuizQuestions() = listOf(
        QuizQuestion(
            pointsPossible = 5,
            questionType = "multiple_choice_question",
            questionText = "Odd or even?",
            answers = listOf(
                QuizAnswer(id = 1, weight = 1, text = "Odd"),
                QuizAnswer(id = 1, weight = 1, text = "Even")
            )

        ),
        QuizQuestion(
            pointsPossible = 5,
            questionType = "multiple_choice_question",
            questionText = "How many roads must a man walk down?",
            answers = listOf(
                QuizAnswer(id = 1, weight = 1, text = "42"),
                QuizAnswer(id = 1, weight = 1, text = "A Gazillion"),
                QuizAnswer(id = 1, weight = 1, text = "13")
            )

        )
    )
}
