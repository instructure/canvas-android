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
 *
 */
package com.instructure.student.ui.e2e.classic.offline

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.OfflineE2E
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
import com.instructure.espresso.getDateInCanvasCalendarFormat
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import com.instructure.student.ui.utils.offline.OfflineTestUtils
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class OfflineGradesE2ETest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GRADES, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun testOfflineGradesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data =
            seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seed an assignment for '${course.name}' course.")
        val assignment = AssignmentsApi.createAssignment(
            course.id,
            teacher.token,
            withDescription = true,
            gradingType = GradingType.PERCENT,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
            pointsPossible = 15.0,
            dueAt = 1.days.fromNow.iso8601
        )

        Log.d(PREPARATION_TAG, "Seed another assignment for '${course.name}' course.")
        val assignment2 = AssignmentsApi.createAssignment(
            course.id,
            teacher.token,
            withDescription = true,
            gradingType = GradingType.PERCENT,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
            pointsPossible = 15.0,
            dueAt = 1.days.fromNow.iso8601
        )

        Log.d(PREPARATION_TAG, "Seed a submission for '${assignment.name}' assignment.")
        SubmissionsApi.submitCourseAssignment(
            course.id,
            student.token,
            assignment.id,
            SubmissionType.ONLINE_TEXT_ENTRY
        )

        Log.d(PREPARATION_TAG, "Grade the previously seeded submission for '${assignment.name}' assignment.")
        SubmissionsApi.gradeSubmission(
            teacher.token,
            course.id,
            assignment.id,
            student.id,
            postedGrade = "9"
        )

        Log.d(PREPARATION_TAG, "Excuse the other previously seeded submission for '${assignment2.name}' assignment.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, assignment2.id, student.id, excused = true)

        Log.d(PREPARATION_TAG, "Create a quiz with some questions.")
        val quizQuestions = makeQuizQuestions()

        Log.d(PREPARATION_TAG, "Publish the previously made quiz.")
        val quiz = QuizzesApi.createAndPublishQuiz(course.id, teacher.token, quizQuestions)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the '${course.name}' course's 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.clickCourseOverflowMenu(course.name, "Manage Offline Content")

        Log.d(STEP_TAG, "Expand '${course.name}' course.")
        manageOfflineContentPage.expandCollapseItem(course.name)

        Log.d(STEP_TAG, "Select the 'Grades' of '${course.name}' course for sync. Click on the 'Sync' button.")
        manageOfflineContentPage.changeItemSelectionState("Grades")
        manageOfflineContentPage.clickOnSyncButtonAndConfirm()

        Log.d(ASSERTION_TAG, "Assert that the offline sync icon only displayed on the synced course's course card.")
        dashboardPage.assertCourseOfflineSyncIconVisible(course.name)
        device.waitForIdle()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        OfflineTestUtils.waitForNetworkToGoOffline(device)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Select '${course.name}' course.")
        dashboardPage.waitForRender()
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG, "Navigate to Grades Page.")
        courseBrowserPage.selectGrades()

        Log.d(ASSERTION_TAG, "Assert that the Offline Indicator (bottom banner) is displayed on the Grades Page.")
        OfflineTestUtils.assertOfflineIndicator()

        Log.d(ASSERTION_TAG, "Assert that the total grade is 60 because there is only one graded assignment and it's graded to 60% and we have the 'Base on graded assignments' checkbox enabled.")
        gradesPage.assertTotalGradeText("60%")

        Log.d(ASSERTION_TAG, "Assert that 'Base on graded assignment' checkbox is checked and the 'Show What-If Score' checkbox is NOT checked by default.")
        gradesPage.assertBasedOnGradedAssignmentsToggleState(true)
        gradesPage.assertShowWhatIfScoreToggleState(false)

        val dueDateInCanvasFormat = getDateInCanvasCalendarFormat(1.days.fromNow.iso8601)
        Log.d(ASSERTION_TAG, "Assert that the '${assignment.name}' assignment's due date is tomorrow ('$dueDateInCanvasFormat').")
        gradesPage.assertAssignmentDueDate(assignment.name, dueDateInCanvasFormat)

        Log.d(ASSERTION_TAG, "Assert that the '${assignment2.name}' assignment's due date is tomorrow ('$dueDateInCanvasFormat').")
        gradesPage.assertAssignmentDueDate(assignment2.name, dueDateInCanvasFormat)

        Log.d(ASSERTION_TAG, "Assert that the '${quiz.title}' quiz's due date has not set.")
        gradesPage.assertAssignmentDueDate(quiz.title, "No due date")

        Log.d(ASSERTION_TAG, "Assert that the '${quiz.title}' quiz status is 'Not Submitted'.")
        gradesPage.assertAssignmentStatus(quiz.title, "Not Submitted")

        Log.d(ASSERTION_TAG, "Assert that the '${assignment.name}' assignment is displayed and there is 60% grade for it.")
        gradesPage.assertAssignmentIsDisplayed(assignment.name)
        retryWithIncreasingDelay(times = 10, maxDelay = 4000, catchBlock = { gradesPage.refresh() }) {
            gradesPage.assertAssignmentGradeText(assignment.name, "60%")
        }

        Log.d(ASSERTION_TAG, "Assert that the '${quiz.title}' quiz is displayed and there is no grade for it.")
        gradesPage.assertAssignmentIsDisplayed(quiz.title)
        gradesPage.assertAssignmentGradeText(quiz.title, "-/10")

        Log.d(ASSERTION_TAG, "Assert that the '${assignment2.name}' assignment is displayed it's graded is 'Excused'.")
        gradesPage.assertAssignmentIsDisplayed(assignment2.name)
        gradesPage.assertAssignmentGradeText(assignment2.name, "EX/15")

        Log.d(STEP_TAG, "Check in the 'What-If Score' checkbox.")
        gradesPage.clickShowWhatIfScore()

        Log.d(ASSERTION_TAG, "Assert that the 'Show What-If Score' checkbox is checked.")
        gradesPage.assertShowWhatIfScoreToggleState(true)

        Log.d(STEP_TAG, "Enter '12' as a what-if grade for '${assignment.name}' assignment.")
        gradesPage.clickEditWhatIfScore(assignment.name)
        gradesPage.enterWhatIfScore("12")
        gradesPage.clickDoneInWhatIfDialog()

        Log.d(ASSERTION_TAG, "Assert that 'Total' grade is '80%'.")
        gradesPage.assertTotalGradeText("80%")

        Log.d(STEP_TAG, "Enter '4' (of 10) as a what-if grade for '${quiz.title}' quiz.")
        gradesPage.clickEditWhatIfScore(quiz.title)
        gradesPage.enterWhatIfScore("4")
        gradesPage.clickDoneInWhatIfDialog()

        Log.d(ASSERTION_TAG, "Assert that 'Total' grade is '64%'.")
        gradesPage.assertTotalGradeText("64%")

        Log.d(STEP_TAG, "Check out 'Base on graded assignments' checkbox (while What-If Score is still enabled!).")
        gradesPage.clickBasedOnGradedAssignments()

        Log.d(ASSERTION_TAG, "Assert that we can see the correct 'Total' grade score (64%) and the 'Base on graded assignments' checkbox is unchecked. Excused assignment should not count neither as graded or ungraded, so it does not change the 'Total' grade.")
        gradesPage.assertBasedOnGradedAssignmentsToggleState(false)
        gradesPage.assertTotalGradeText("64%")

        Log.d(STEP_TAG, "Uncheck the 'Show What-If Score' checkbox.")
        gradesPage.clickShowWhatIfScore()

        Log.d(ASSERTION_TAG, "Assert that the 'Show What-If Score' checkbox is unchecked.")
        gradesPage.assertShowWhatIfScoreToggleState(false)

        Log.d(ASSERTION_TAG, "Assert that the 'Total' grade is becoming 36% because there is still only one 'real' grade, but since the 'Base on graded assignments' is not checked," +
                " the score will be lower than 60% (9/30 is 36% as the 'Not Submitted' is still not counted). Also assert that the '${assignment.name}' assignment's grades has been set back to 60% as we disabled the 'Show What-If Score' checkbox.")
        gradesPage.assertTotalGradeText("36%")
        gradesPage.assertAssignmentGradeText(assignment.name, "60%")

        Log.d(STEP_TAG, "Check in 'Base on graded assignments' checkbox.")
        gradesPage.clickBasedOnGradedAssignments()

        Log.d(ASSERTION_TAG, "Assert that we can see the correct score (60%) and the 'Base on graded assignments' checkbox is checked.")
        gradesPage.assertBasedOnGradedAssignmentsToggleState(true)
        gradesPage.assertTotalGradeText("60%")

        Log.d(STEP_TAG, "Open '${assignment.name}' assignment.")
        gradesPage.clickAssignment(assignment.name)

        Log.d(ASSERTION_TAG, "Assert if the Assignment Details Page is displayed with the corresponding grade.")
        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertAssignmentGraded("9")

        Log.d(STEP_TAG, "Navigate back to Course Grades Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Collapse the assignment list.")
        gradesPage.clickAssignmentGroupExpandCollapseButton("Upcoming Assignments")

        Log.d(ASSERTION_TAG, "Assert that the '${assignment.name}' and '${assignment2.name}' assignments will disappear from the list view so only the '${quiz.title}' quiz is displayed.")
        gradesPage.assertAllAssignmentItemCount(1)

        Log.d(STEP_TAG, "Expand the assignment list.")
        gradesPage.clickAssignmentGroupExpandCollapseButton("Upcoming Assignments")

        Log.d(ASSERTION_TAG, "Assert that the assignment will be displayed again in the list view.")
        gradesPage.assertAllAssignmentItemCount(3)
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device, so it will come back online.")
        turnOnConnectionViaADB()
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