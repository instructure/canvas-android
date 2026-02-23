/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 *
 */
package com.instructure.student.ui.e2e.classic

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
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
import com.instructure.espresso.convertIso8601ToCanvasFormat
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class GradesE2ETest: StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GRADES, TestCategory.E2E)
    fun testGradesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course.")
        val assignment = AssignmentsApi.createAssignment(course.id, teacher.token, withDescription = true, gradingType = GradingType.PERCENT, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY), pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601)
        val assignment2 = AssignmentsApi.createAssignment(course.id, teacher.token, withDescription = true, gradingType = GradingType.PERCENT, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY), pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG, "Create a quiz with some questions.")
        val quizQuestions = makeQuizQuestions()

        Log.d(PREPARATION_TAG, "Publish the previously made quiz.")
        val quiz = QuizzesApi.createAndPublishQuiz(course.id, teacher.token, quizQuestions)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Select '${course.name}' course.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG, "Navigate to Grades Page.")
        courseBrowserPage.selectGrades()

        Log.d(ASSERTION_TAG, "Assert that the toolbar title is 'Grades' and the subtitle is '${course.name}' course name.")
        gradesPage.assertToolbarTitles(course.name)

        Log.d(ASSERTION_TAG, "Assert that the 'Total' grade text is 'N/A' as there is no grade for any assignments yet.")
        gradesPage.assertTotalGradeText("N/A")

        Log.d(ASSERTION_TAG, "Assert that 'Based on graded assignment' checkbox is checked and the 'Show What-If Score' checkbox is NOT checked by default.")
        gradesPage.assertBasedOnGradedAssignmentsLabel()
        gradesPage.assertBasedOnGradedAssignmentsToggleState(true)
        gradesPage.assertShowWhatIfScoreIsDisplayed()
        gradesPage.assertShowWhatIfScoreToggleState(false)

        Log.d(ASSERTION_TAG, "Assert that the '${assignment.name}', '${assignment2.name}' assignments and '${quiz.title}' quiz are displayed and there is no grade for them.")
        gradesPage.assertAssignmentIsDisplayed(assignment.name)
        gradesPage.assertAssignmentGradeText(assignment.name,"-/15")
        gradesPage.assertAssignmentIsDisplayed(assignment2.name)
        gradesPage.assertAssignmentGradeText(assignment2.name,"-/15")
        gradesPage.assertAssignmentIsDisplayed(quiz.title)
        gradesPage.assertAssignmentGradeText(quiz.title,"-/10")

        var dueDateInCanvasFormat = convertIso8601ToCanvasFormat(assignment.dueAt.orEmpty())
        Log.d(ASSERTION_TAG, "Assert that the '${assignment.name}' and '${assignment2.name}' assignments' due date is tomorrow ('$dueDateInCanvasFormat').")
        gradesPage.assertAssignmentDueDate(assignment.name, "Due $dueDateInCanvasFormat")
        gradesPage.assertAssignmentDueDate(assignment2.name, "Due $dueDateInCanvasFormat")

        Log.d(ASSERTION_TAG, "Assert that the '${quiz.title}' quiz's due date has not set.")
        gradesPage.assertAssignmentDueDate(quiz.title, "No due date")

        Log.d(ASSERTION_TAG, "Assert that all the 3 assignment's state is 'Not Submitted' yet.")
        gradesPage.assertAssignmentStatus(assignment.name, "Not Submitted")
        gradesPage.assertAssignmentStatus(assignment2.name, "Not Submitted")
        gradesPage.assertAssignmentStatus(quiz.title, "Not Submitted")

        Log.d(STEP_TAG, "Check in the 'What-If Score' checkbox.")
        gradesPage.clickShowWhatIfScore()

        Log.d(ASSERTION_TAG, "Assert that the 'Show What-If Score' checkbox is checked.")
        gradesPage.assertShowWhatIfScoreToggleState(true)

        Log.d(STEP_TAG, "Enter '12' as a what-if grade for '${assignment.name}' assignment.")
        gradesPage.clickEditWhatIfScore(assignment.name)
        gradesPage.enterWhatIfScore("12")
        gradesPage.clickDoneInWhatIfDialog()

        Log.d(ASSERTION_TAG, "Assert that the what-if grade for '${assignment.name}' assignment is 'What-if: 12/15'.")
        gradesPage.assertWhatIfGradeText(assignment.name.orEmpty(), "What-if: 12/15")

        Log.d(ASSERTION_TAG, "Assert that 'Total Grade' contains the score '80%'.")
        gradesPage.assertTotalGradeText("80%")

        Log.d(STEP_TAG, "Check out the 'What-If Score' checkbox.")
        gradesPage.clickShowWhatIfScore()

        Log.d(ASSERTION_TAG, "Assert that the 'Show What-If Score' checkbox is unchecked.")
        gradesPage.assertShowWhatIfScoreToggleState(false)

        Log.d(ASSERTION_TAG, "Assert that the 'Total' grade text is 'N/A' as there is no grade for any assignments yet.")
        gradesPage.assertTotalGradeText("N/A")

        Log.d(PREPARATION_TAG, "Seed a submission for '${assignment.name}' assignment.")
        SubmissionsApi.submitCourseAssignment(course.id, student.token, assignment.id, SubmissionType.ONLINE_TEXT_ENTRY)

        Log.d(PREPARATION_TAG, "Grade the previously seeded submission for '${assignment.name}' assignment.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, assignment.id, student.id, postedGrade = "9")

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that the assignment's score is '60%' and so the 'Total' grade.")
        gradesPage.refresh()
        retryWithIncreasingDelay(times = 15, maxDelay = 3000, catchBlock = { gradesPage.refresh() }) {
            gradesPage.assertAssignmentGradeText(assignment.name, "60%")
            gradesPage.assertTotalGradeText("60%")
        }

        Log.d(STEP_TAG, "Check out 'Base on graded assignments' button.")
        gradesPage.clickBasedOnGradedAssignments()

        Log.d(ASSERTION_TAG, "Assert that we can see the correct score (22.5%) and the 'Base on graded assignments' checkbox is unchecked.")
        gradesPage.assertBasedOnGradedAssignmentsToggleState(false)
        gradesPage.assertTotalGradeText("22.5%")

        Log.d(STEP_TAG, "Check in 'Base on graded assignments' button.")
        gradesPage.clickBasedOnGradedAssignments()

        Log.d(ASSERTION_TAG, "Assert that we can see the correct score (60%) and the 'Base on graded assignments' checkbox is checked.")
        gradesPage.assertBasedOnGradedAssignmentsToggleState(true)
        gradesPage.assertTotalGradeText("60%")

        Log.d(PREPARATION_TAG, "Seed a submission for '${assignment2.name}' assignment.")
        SubmissionsApi.submitCourseAssignment(course.id, student.token, assignment2.id, SubmissionType.ONLINE_TEXT_ENTRY)

        Log.d(PREPARATION_TAG, "Grade the previously seeded submission for '${assignment2.name}' assignment.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, assignment2.id, student.id, postedGrade = "10")

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that we can see the correct score at the '${assignment2.name}' assignment (66.67%) and at the total score as well (63.33%).")
        gradesPage.refresh()
        retryWithIncreasingDelay(times = 15, maxDelay = 5000, catchBlock = { gradesPage.refresh() }) {
            gradesPage.assertAssignmentGradeText(assignment2.name, "66.67%")
            gradesPage.assertTotalGradeText("63.33%")
        }

        Log.d(PREPARATION_TAG, "Excuse the submission for '${assignment.name}' assignment.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, assignment.id, student.id, excused = true)

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that we can see the correct score (66.67%).")
        gradesPage.refresh()
        retryWithIncreasingDelay(times = 15, maxDelay = 5000, catchBlock = { gradesPage.refresh() }) {
            gradesPage.assertTotalGradeText("66.67%")
        }

        Log.d(PREPARATION_TAG, "Grade the previously seeded submission for '${assignment.name}' assignment.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, assignment.id, student.id, postedGrade = "9")

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that we can see the correct score (63.33%).")
        gradesPage.refresh()
        retryWithIncreasingDelay(times = 15, maxDelay = 5000, catchBlock = { gradesPage.refresh() }) {
            gradesPage.assertTotalGradeText("63.33%")
        }

        Log.d(STEP_TAG, "Click on the '${assignment.name}' assignment to open it's details.")
        gradesPage.clickAssignment(assignment.name)

        Log.d(ASSERTION_TAG, "Assert if the Assignment Details Page is displayed with the corresponding grade.")
        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertAssignmentGraded("9")

        Log.d(STEP_TAG, "Navigate back to Course Grades Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Collapse the 'Upcoming Assignments' list.")
        gradesPage.clickAssignmentGroupExpandCollapseButton("Upcoming Assignments")

        Log.d(ASSERTION_TAG, "Assert that only 1 assignment, the '${quiz.title}' quiz is display as the others are collapsed.")
        gradesPage.assertAllAssignmentItemCount(1)

        Log.d(STEP_TAG, "Expand the 'Upcoming Assignments' list.")
        gradesPage.clickAssignmentGroupExpandCollapseButton("Upcoming Assignments")

        Log.d(ASSERTION_TAG, "Assert that all the 3 assignments are displayed.")
        gradesPage.assertAllAssignmentItemCount(3)
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
