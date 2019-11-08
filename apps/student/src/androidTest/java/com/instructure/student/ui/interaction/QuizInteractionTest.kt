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
 */
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addQuestionToQuiz
import com.instructure.canvas.espresso.mockCanvas.addQuizToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.QuizAnswer
import com.instructure.canvasapi2.models.QuizQuestion
import com.instructure.canvasapi2.models.Tab
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Test

class QuizInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    // Some fields that are populated during initialization
    var quiz: Quiz? = null
    var course: Course? = null

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_essayQuiz() {
        // Quizzes with just Essay questions should open in a native view and not a WebView

        val data = getToCourse()
        val question = data.addQuestionToQuiz(
                course = course!!,
                quizId = quiz!!.id,
                questionName = "Essay question",
                questionText = "Describe yourself in 100 words",
                questionType = QuizQuestion.QuestionType.ESSAY.stringVal
        )

        courseBrowserPage.selectQuizzes()
        quizListPage.assertQuizDisplayed(quiz!!)
        quizListPage.selectQuiz(quiz!!)
        quizDetailsPage.assertQuizDisplayed(quiz = quiz!!, submitted = false, questions = listOf(question))

    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_fileUploadQuiz() {
        // Quizzes with just File Upload questions should open in a native view and not a WebView

        val data = getToCourse()
        val question = data.addQuestionToQuiz(
                course = course!!,
                quizId = quiz!!.id,
                questionName = "File upload question",
                questionText = "Upload your resume",
                questionType = QuizQuestion.QuestionType.FILE_UPLOAD.stringVal
        )

        courseBrowserPage.selectQuizzes()
        quizListPage.assertQuizDisplayed(quiz!!)
        quizListPage.selectQuiz(quiz!!)
        quizDetailsPage.assertQuizDisplayed(quiz = quiz!!, submitted = false, questions = listOf(question))

    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_fillInTheBlankQuiz() {
        // Quizzes with just Fill In The Blank questions should open in a native view and not a WebView

        val data = getToCourse()
        val question = data.addQuestionToQuiz(
                course = course!!,
                quizId = quiz!!.id,
                questionName = "Fill-in-the-blank question",
                questionText = "2 + 2 = ?",
                questionType = "fill_in_the_blank_question",
                answers = arrayOf(QuizAnswer(answerText = "4"))
        )

        courseBrowserPage.selectQuizzes()
        quizListPage.assertQuizDisplayed(quiz!!)
        quizListPage.selectQuiz(quiz!!)
        quizDetailsPage.assertQuizDisplayed(quiz = quiz!!, submitted = false, questions = listOf(question))

    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_matchingQuiz() {
        // Quizzes with just Matching questions should open in a native view and not a WebView

        val data = getToCourse()
        val question = data.addQuestionToQuiz(
                course = course!!,
                quizId = quiz!!.id,
                questionName = "Matching question",
                questionText = "Match these teams with their leagues",
                questionType = QuizQuestion.QuestionType.MATCHING.stringVal,
                answers = arrayOf(
                        QuizAnswer(answerMatchLeft = "Lakers", answerMatchRight = "NBA"),
                        QuizAnswer(answerMatchLeft = "Seahawks", answerMatchRight = "NFL"),
                        QuizAnswer(answerMatchLeft = "Yankees", answerMatchRight = "MLB"),
                        QuizAnswer(answerMatchLeft = "Islanders", answerMatchRight = "NHL")
                )
        )

        courseBrowserPage.selectQuizzes()
        quizListPage.assertQuizDisplayed(quiz!!)
        quizListPage.selectQuiz(quiz!!)
        quizDetailsPage.assertQuizDisplayed(quiz = quiz!!, submitted = false, questions = listOf(question))

    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_multipleAnswerQuiz() {
        // Quizzes with just Multiple Answer questions should open in a native view and not a WebView

        val data = getToCourse()
        val question = data.addQuestionToQuiz(
                course = course!!,
                quizId = quiz!!.id,
                questionName = "Multiple answer question",
                questionText = "Which of these teams are in the NFL?",
                questionType = QuizQuestion.QuestionType.MULTIPLE_ANSWERS.stringVal,
                answers = arrayOf(
                        QuizAnswer(answerText = "Seahawks", answerWeight = 1),
                        QuizAnswer(answerText = "Padres", answerWeight = 0),
                        QuizAnswer(answerText = "Packers", answerWeight = 1),
                        QuizAnswer(answerText = "Lakers", answerWeight = 0)
                )
        )

        courseBrowserPage.selectQuizzes()
        quizListPage.assertQuizDisplayed(quiz!!)
        quizListPage.selectQuiz(quiz!!)
        quizDetailsPage.assertQuizDisplayed(quiz = quiz!!, submitted = false, questions = listOf(question))
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_multipleChoiceQuiz() {
        // Quizzes with just Multiple Choice questions should open in a native view and not a WebView

        val data = getToCourse()
        val question = data.addQuestionToQuiz(
                course = course!!,
                quizId = quiz!!.id,
                questionName = "Multiple choice question",
                questionText = "Which of these teams are in the NFL?",
                questionType = QuizQuestion.QuestionType.MUTIPLE_CHOICE.stringVal,
                answers = arrayOf(
                        QuizAnswer(answerText = "Seahawks", answerWeight = 1),
                        QuizAnswer(answerText = "Padres", answerWeight = 0),
                        QuizAnswer(answerText = "Red Sox", answerWeight = 0),
                        QuizAnswer(answerText = "Lakers", answerWeight = 0)
                )
        )

        courseBrowserPage.selectQuizzes()
        quizListPage.assertQuizDisplayed(quiz!!)
        quizListPage.selectQuiz(quiz!!)
        quizDetailsPage.assertQuizDisplayed(quiz = quiz!!, submitted = false, questions = listOf(question))

    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_multipleDropdownQuiz() {
        // Quizzes with just Multiple Dropdown questions should open in a native view and not a WebView

        val data = getToCourse()
        val question = data.addQuestionToQuiz(
                course = course!!,
                quizId = quiz!!.id,
                questionName = "Multiple dropdown question",
                questionText = "My favorite colors are [1] and [2]",
                questionType = QuizQuestion.QuestionType.MULTIPLE_DROPDOWNS.stringVal,
                answers = arrayOf(
                        QuizAnswer(answerText = "Red", blankId = 1), // Not sure if this is how blankIds really work
                        QuizAnswer(answerText = "Green", blankId = 1),
                        QuizAnswer(answerText = "Blue", blankId = 2),
                        QuizAnswer(answerText = "Yellow", blankId = 2)
                )
        )

        courseBrowserPage.selectQuizzes()
        quizListPage.assertQuizDisplayed(quiz!!)
        quizListPage.selectQuiz(quiz!!)
        quizDetailsPage.assertQuizDisplayed(quiz = quiz!!, submitted = false, questions = listOf(question))

    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_numericalAnswerQuiz() {
        // Quizzes with just Numerical Answer questions should open in a native view and not a WebView

        val data = getToCourse()
        val question = data.addQuestionToQuiz(
                course = course!!,
                quizId = quiz!!.id,
                questionName = "Numerical answer question",
                questionText = "What is 6x7?",
                questionType = QuizQuestion.QuestionType.NUMERICAL.stringVal,
                answers = arrayOf(
                        QuizAnswer(answerWeight = 1, exact = 42)
                )
        )

        courseBrowserPage.selectQuizzes()
        quizListPage.assertQuizDisplayed(quiz!!)
        quizListPage.selectQuiz(quiz!!)
        quizDetailsPage.assertQuizDisplayed(quiz = quiz!!, submitted = false, questions = listOf(question))

    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_textQuiz() {
        // Quizzes with just Text questions should open in a native view and not a WebView

        val data = getToCourse()
        val question = data.addQuestionToQuiz(
                course = course!!,
                quizId = quiz!!.id,
                questionName = "Text question",
                questionText = "What is 6x7?",
                questionType = QuizQuestion.QuestionType.TEXT_ONLY.stringVal,
                answers = arrayOf(
                        QuizAnswer(answerText = "42", answerWeight = 1)
                )
        )

        courseBrowserPage.selectQuizzes()
        quizListPage.assertQuizDisplayed(quiz!!)
        quizListPage.selectQuiz(quiz!!)
        quizDetailsPage.assertQuizDisplayed(quiz = quiz!!, submitted = false, questions = listOf(question))

    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testOpensNativeView_trueFalseQuiz() {
        // Quizzes with just True-False questions should open in a native view and not a WebView

        val data = getToCourse()
        val question = data.addQuestionToQuiz(
                course = course!!,
                quizId = quiz!!.id,
                questionName = "True/false question",
                questionText = "6x7=42",
                questionType = QuizQuestion.QuestionType.TRUE_FALSE.stringVal,
                answers = arrayOf(
                        QuizAnswer(answerText = "true", answerWeight = 1),
                        QuizAnswer(answerText = "false", answerWeight = 0)
                )
        )

        courseBrowserPage.selectQuizzes()
        quizListPage.assertQuizDisplayed(quiz!!)
        quizListPage.selectQuiz(quiz!!)
        quizDetailsPage.assertQuizDisplayed(quiz = quiz!!, submitted = false, questions = listOf(question))

    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testQuiz_canTakeQuiz() {
        // User should be able to take a valid quiz

        val data = getToCourse()
        val question = data.addQuestionToQuiz(
                course = course!!,
                quizId = quiz!!.id,
                questionName = "Multiple-choice question",
                questionText = "Which is the one true programming language?",
                questionType = QuizQuestion.QuestionType.MUTIPLE_CHOICE.stringVal,
                answers = arrayOf(
                        QuizAnswer(answerText = "C++", answerWeight = 0),
                        QuizAnswer(answerText = "Dart", answerWeight = 0),
                        QuizAnswer(answerText = "Kotlin", answerWeight = 1),
                        QuizAnswer(answerText = "Java", answerWeight = 0)
                )
        )

        courseBrowserPage.selectQuizzes()
        quizListPage.assertQuizDisplayed(quiz!!)
        quizListPage.selectQuiz(quiz!!)
        quizDetailsPage.assertQuizDisplayed(quiz = quiz!!, submitted = false, questions = listOf(question))
        quizDetailsPage.takeQuiz2(questions = listOf(question))


    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testQuiz_canResumeQuiz() {
        // User should be able to resume a valid quiz
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testQuiz_canSubmitQuiz() {
        // User should be able to submit a valid quiz

        val data = getToCourse()
        val question = data.addQuestionToQuiz(
                course = course!!,
                quizId = quiz!!.id,
                questionName = "Multiple-choice question",
                questionText = "Generic question",
                questionType = QuizQuestion.QuestionType.MUTIPLE_CHOICE.stringVal,
                answers = arrayOf(
                        QuizAnswer(answerText = "Answer 1", answerWeight = 0),
                        QuizAnswer(answerText = "Answer 2", answerWeight = 0),
                        QuizAnswer(answerText = "Answer 3", answerWeight = 1),
                        QuizAnswer(answerText = "Answer 4", answerWeight = 0)
                )
        )

        courseBrowserPage.selectQuizzes()
        quizListPage.assertQuizDisplayed(quiz!!)
        quizListPage.selectQuiz(quiz!!)
        quizDetailsPage.assertQuizDisplayed(quiz = quiz!!, submitted = false, questions = listOf(question))
        quizDetailsPage.takeQuiz2(questions = listOf(question))
        quizDetailsPage.submitQuiz()
        quizDetailsPage.assertQuizDisplayed(quiz = quiz!!, submitted = true, questions = listOf(question))
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testQuiz_resumingTimedQuizShowsCorrectTime() {
        // User should be able to resume a timed quiz with the correct time displayed
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.QUIZZES, TestCategory.INTERACTION, true)
    fun testQuiz_quizTimerCountsDownAndEndsSessionIfQuizNotCompleted() {
        // Timer counts down and ends the session if quiz has not been completed
    }

    // Mock a specified number of students and courses, sign in, then navigate to course browser page for
    // first course.
    private fun getToCourse(
            studentCount: Int = 1,
            courseCount: Int = 1): MockCanvas {

        // Basic info
        val data = MockCanvas.init(
                studentCount = studentCount,
                courseCount = courseCount,
                favoriteCourseCount = courseCount)
        course = data.courses.values.first()
        quiz = data.addQuizToCourse(
                course = course!!
        )

        // Sign in
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()

        // Navigate to the (first) course
        dashboardPage.selectCourse(course!!)

        return data
    }

}
