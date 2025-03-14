/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.ui

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addQuestionToQuiz
import com.instructure.canvas.espresso.mockCanvas.addQuizSubmission
import com.instructure.canvas.espresso.mockCanvas.addQuizToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.QuizAnswer
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class QuizSubmissionListPageTest : TeacherComposeTest() {

    @Test
    override fun displaysPageObjects() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.assertPageObjects()
    }

    @Test
    fun displaysNoSubmissionsView() {
        goToAssignmentSubmissionListPage(
                students = 0,
                submissions = 0
        )
        assignmentSubmissionListPage.assertDisplaysNoSubmissionsView()
    }

    @Test
    fun filterLateSubmissions() {
        goToAssignmentSubmissionListPage(
                dueAt = 7.days.ago.iso8601
        )
        assignmentSubmissionListPage.clickFilterButton()
        assignmentSubmissionListPage.clickFilterButton()
        assignmentSubmissionListPage.clickFilterSubmittedLate()
        assignmentSubmissionListPage.clickFilterDialogOk()
        assignmentSubmissionListPage.assertFilterLabelText("Submitted Late")
        assignmentSubmissionListPage.assertHasSubmission()
    }

    @Test
    fun filterPendingReviewSubmissions() {
        goToAssignmentSubmissionListPage(addQuestion = true)
        assignmentSubmissionListPage.clickFilterButton()
        assignmentSubmissionListPage.clickFilterButton()
        assignmentSubmissionListPage.clickFilterUngraded()
        assignmentSubmissionListPage.clickFilterDialogOk()
        assignmentSubmissionListPage.assertFilterLabelText("Haven't Been Graded")
        assignmentSubmissionListPage.assertHasSubmission()
    }

    @Test
    fun displaysQuizStatusComplete() {
        goToAssignmentSubmissionListPage(complete = true)
        assignmentSubmissionListPage.assertSubmissionStatusSubmitted()
    }

    @Test
    fun messageStudentsWho() {
        val data = goToAssignmentSubmissionListPage()
        val student = data.students[0]
        assignmentSubmissionListPage.clickAddMessage()

        inboxComposePage.assertRecipientSelected(student.shortName!!)
    }

    private fun goToAssignmentSubmissionListPage(
            students: Int = 1,
            submissions: Int = 1,
            dueAt: String? = null,
            complete: Boolean = true,
            addQuestion: Boolean = false): MockCanvas {
        val data = MockCanvas.init(teacherCount = 1, studentCount = students, courseCount = 1, favoriteCourseCount = 1)
        val course = data.courses.values.first()
        val teacher = data.teachers[0]

        if(submissions > 0 && students < 1) {
            throw Exception("Need at least one student in order to have a submission")
        }

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val quiz = data.addQuizToCourse(
                course = course,
                dueAt = dueAt,
                quizType = Quiz.TYPE_ASSIGNMENT,
                published = true
        )

        if (addQuestion) {
            data.addQuestionToQuiz(
                    course = course,
                    quizId = quiz.id,
                    questionName = "Here's a question",
                    questionText = "Who's the best college football coach ever?",
                    answers = arrayOf(
                            QuizAnswer(id = data.newItemId(), answerText = "Bear Bryant", answerWeight = 1),
                            QuizAnswer(id = data.newItemId(), answerText = "Nick Saban", answerWeight = 0),
                            QuizAnswer(id = data.newItemId(), answerText = "Urban Meyer", answerWeight = 0),
                            QuizAnswer(id = data.newItemId(), answerText = "Jimmy Johnson", answerWeight = 0)
                    )
            )
        }

        for (s in 0 until submissions) {
            val student = data.students[0]
            data.addQuizSubmission(quiz = quiz, user = student, state = if(complete) "complete" else "untaken")
        }

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        dashboardPage.openCourse(course)
        courseBrowserPage.openQuizzesTab()
        quizListPage.clickQuiz(quiz)
        quizDetailsPage.openSubmissionsPage()

        return data
    }
}
