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
package com.instructure.student.ui.e2e

import androidx.test.espresso.Espresso.pressBack
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.model.QuizAnswer
import com.instructure.dataseeding.model.QuizApiModel
import com.instructure.dataseeding.model.QuizQuestion
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Test

class QuizzesE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.PAGES, TestCategory.E2E, false)
    fun testQuizzesE2E() {

        // Seed basic data
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        // Seed an unpublished quiz
        val quizUnpublished = QuizzesApi.createQuiz(QuizzesApi.CreateQuizRequest(
                courseId = course.id,
                withDescription = true,
                published = false,
                token = teacher.token
        ))

        // Seed a published quiz with some questions
        val quizQuestions = listOf(
                QuizQuestion(
                        questionText = "What's your favorite color?",
                        questionType = "multiple_choice_question",
                        pointsPossible = 5,
                        answers = listOf(
                                QuizAnswer(id=1, weight=1, text="Red"),
                                QuizAnswer(id=1, weight=1, text="Blue"),
                                QuizAnswer(id=1, weight=1, text="Yellow")
                        )
                ),
                QuizQuestion(
                        questionText = "Who let the dogs out?",
                        questionType = "multiple_choice_question",
                        pointsPossible = 5,
                        answers = listOf(
                                QuizAnswer(id=1, weight=1, text="Who Who Who-Who"),
                                QuizAnswer(id=1, weight=1, text="Who Who-Who-Who"),
                                QuizAnswer(id=1, weight=1, text="Who-Who Who-Who")
                        )
                ),
                QuizQuestion(
                        questionText = "Why should I give you an A?",
                        questionType = "essay_question",
                        pointsPossible = 12,
                        answers = listOf()
                )
        )
        val quizPublished = createAndPublishQuiz(course.id, teacher.token, quizQuestions)


        // Sign in our user and navigate to our course
        tokenLogin(student)
        dashboardPage.waitForRender()
        dashboardPage.selectCourse(course)

        // Verify that quiz info shows up in Quizzes tab
        courseBrowserPage.selectQuizzes()
        quizListPage.assertQuizDisplayed(quizPublished)
        quizListPage.assertQuizNotDisplayed(quizUnpublished)
        quizListPage.selectQuiz(quizPublished)
        quizDetailsPage.assertQuizDisplayed(quizPublished, false, quizQuestions)
        pressBack() // Back to quiz list page
        pressBack() // Back to course browser page

        // Start a quiz via the assignments tab
        courseBrowserPage.selectAssignments()
        assignmentListPage.clickQuiz(quizPublished)
        assignmentDetailsPage.viewQuiz()
        quizDetailsPage.takeQuiz(questions = quizQuestions, completionCount = 2) // Only answer two of the questions
        // TODO: Write a function that presses the back button until you hit a specified page
        pressBack() // Back to quiz details page
        pressBack() // Back to assignment details page
        pressBack() // Back to assignments tab/list
        pressBack() // Back to course browser page

        // Resume/complete the quiz via the quizzes tab, submit the answers,
        // and check that everything is recorded OK.
        courseBrowserPage.selectQuizzes()
        quizListPage.selectQuiz(quizPublished)
        quizDetailsPage.completeQuiz(questions = quizQuestions,startQuestion = 2)
        quizDetailsPage.submitQuiz()
        quizDetailsPage.assertQuizDisplayed(quizPublished, true, quizQuestions)
        pressBack() // Back to quiz list page
        pressBack() // Back to course browser page

        // Check to see that quiz info shows up in Assignments tab, as a submitted quiz
        courseBrowserPage.selectAssignments()
        assignmentListPage.refresh()
        assignmentListPage.assertQuizDisplayed(quizPublished, possiblePointTotal(quizQuestions).toString())
        assignmentListPage.assertQuizNotDisplayed(quizUnpublished)
        assignmentListPage.clickQuiz(quizPublished)
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.assertSubmittedStatus()
        pressBack() // Back to assignment list page
        pressBack() // Back to course browser page
    }

    private fun possiblePointTotal(questions: List<QuizQuestion>): Int {
        var result = 0
        for(question in questions) result += question.pointsPossible
        return result
    }

    private fun createAndPublishQuiz(courseId: Long, teacherToken: String, questions: List<QuizQuestion>) : QuizApiModel {
        val result = QuizzesApi.createQuiz(QuizzesApi.CreateQuizRequest(
                courseId = courseId,
                withDescription = true,
                published = false, // Will publish in just a bit, after we add questions
                token = teacherToken
        ))

        for(question in questions) {
            QuizzesApi.createQuizQuestion(
                    courseId = courseId,
                    quizId = result.id,
                    teacherToken = teacherToken,
                    quizQuestion = question
            )
        }

        QuizzesApi.publishQuiz(
                courseId = courseId,
                quizId = result.id,
                teacherToken = teacherToken,
                published = true
        )

        return result
    }
}