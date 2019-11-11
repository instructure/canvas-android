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
package com.instructure.student.ui.pages

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvasapi2.models.Quiz
import com.instructure.dataseeding.model.QuizApiModel
import com.instructure.dataseeding.model.QuizQuestion
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertContainsText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.scrollTo
import com.instructure.espresso.typeText
import com.instructure.student.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import java.lang.Integer.min

class QuizDetailsPage: BasePage(R.id.quizDetailsPage) {
    private val quizTitle by OnViewWithId(R.id.quiz_title)
    private val quizPointsLabel by OnViewWithId(R.id.quiz_points_possible)
    private val quizPointsValue by OnViewWithId(R.id.quiz_points_details)
    private val quizQuestionsLabel by OnViewWithId(R.id.quiz_question_count)
    private val quizQuestionsValue by OnViewWithId(R.id.quiz_question_count_details)
    private val nextButton by OnViewWithId(R.id.next)

    fun assertQuizDisplayed(quiz: QuizApiModel, submitted: Boolean, questions: List<QuizQuestion>) {
        quizTitle.assertHasText(quiz.title)
        quizQuestionsValue.assertHasText(""+questions.size)
        var totalPoints = 0
        for(question in questions) totalPoints += question.pointsPossible
        quizPointsValue.assertHasText(""+totalPoints)

        if(submitted) {
            nextButton.assertContainsText("VIEW QUESTIONS")
        }
        else {
            nextButton.assertContainsText("START")
        }
    }

    fun assertQuizDisplayed(quiz: Quiz, submitted: Boolean, questions: List<com.instructure.canvasapi2.models.QuizQuestion>) {
        quizTitle.assertHasText(quiz.title!!)
        quizQuestionsValue.assertHasText(""+questions.size)
        var totalPoints = 0
        for(question in questions) totalPoints += question.pointsPossible
        quizPointsValue.assertHasText(""+totalPoints)

        if(submitted) {
            nextButton.assertContainsText("VIEW QUESTIONS")
        }
        else {
            nextButton.assertContainsText("START")
        }
    }

    // May or may not answer all of the questions, depending on setting of completionCount
    fun takeQuiz(questions: List<QuizQuestion>, completionCount: Int? = null) {
        val wrappedQuestions = mutableListOf<QuizQuestionWrapper>()
        for(question in questions) {
            wrappedQuestions.add(QuizQuestionWrapper(question))
        }
        takeQuizCommon(wrappedQuestions, completionCount)
    }

    // Arrggghh... sorry about the "2" in the name.  Because of JVM type-erasure, the signatures of
    // these methods clashed if I didn't tack the "2" on the end.
    // May or may not answer all of the questions, depending on setting of completionCount
    fun takeQuiz2(questions: List<com.instructure.canvasapi2.models.QuizQuestion>, completionCount: Int? = null) {
        val wrappedQuestions = mutableListOf<QuizQuestionWrapper>()
        for(question in questions) {
            wrappedQuestions.add(QuizQuestionWrapper(question))
        }
        takeQuizCommon(wrappedQuestions, completionCount)
    }

    private fun takeQuizCommon(questions: List<QuizQuestionWrapper>, completionCount: Int? = null) {
        nextButton.assertContainsText("START")
        nextButton.scrollTo().click() // Start the quiz

        // If completionCount is null, elementsToProcess will be "all of them".
        // Otherwise, elementsToProcess will be the minimum of "all of them" and completionCount.
        val elementsToProcess = min(questions.size, completionCount ?: questions.size)

        // Answer the desired number of questions
        for(i in 0..elementsToProcess-1) {
            val question = questions[i]
            answerQuestion(question)
        }

    }

    // Answers quiz questions from startQuestion onward.
    fun completeQuiz(questions: List<QuizQuestion>, startQuestion: Int) {
        val wrappedQuestions = mutableListOf<QuizQuestionWrapper>()
        for(question in questions) {
            wrappedQuestions.add(QuizQuestionWrapper(question))
        }
        completeQuizCommon(wrappedQuestions, startQuestion)
    }

    // Answers quiz questions from startQuestion onward.
    fun completeQuiz2(questions: List<com.instructure.canvasapi2.models.QuizQuestion>, startQuestion: Int) {
        val wrappedQuestions = mutableListOf<QuizQuestionWrapper>()
        for(question in questions) {
            wrappedQuestions.add(QuizQuestionWrapper(question))
        }
        completeQuizCommon(wrappedQuestions, startQuestion)
    }

    // Answers quiz questions from startQuestion onward.
    private fun completeQuizCommon(questions: List<QuizQuestionWrapper>, startQuestion: Int) {
        nextButton.assertContainsText("RESUME")
        nextButton.scrollTo().click() // Resume the quiz

        for(i in startQuestion..questions.size-1) {
            val question = questions[i]
            answerQuestion(question)
        }

    }

    private fun answerQuestion(question: QuizQuestionWrapper) {
        when(question.questionType) {
            "multiple_choice_question" -> {
                val matcher = allOf(
                        withId(R.id.answer_checkbox),
                        hasSibling(allOf(
                                withId(R.id.text_answer),
                                withText(question.answers!![0].answerText)
                        ))
                )
                scrollRecyclerView(R.id.recyclerView, matcher)
                onView(matcher).click()
            }

            "essay_question" -> {
                // There is no way for me to tie an essay editText with the corresponding
                // essay question.  (The question text is in a WebView.) So there had better
                // only be one essay question.
                val matcher = withId(R.id.question_answer)
                scrollRecyclerView(R.id.recyclerView, matcher)
                onView(matcher).typeText("A long, thoughtful essay answer")
            }
        }
    }


    // Submit the quiz
    fun submitQuiz() {
        Espresso.closeSoftKeyboard()

        // Submit the quiz
        val matcher = withId(R.id.submit_button)
        scrollRecyclerView(R.id.recyclerView, matcher)
        onView(matcher).assertDisplayed()
        onView(matcher).click()

        // Confirm submission
        onView(allOf(
                containsTextCaseInsensitive("ok"),
                isAssignableFrom(Button::class.java))
        ).click()
    }

    /** Read the countdown timer value.
     * Assumes that we are inside a quiz; would not work (or make sense) from the quiz details page.
     */
    fun readTimerSeconds() : Int {
        val stringHolder = mutableListOf<String>()

        onView(withId(R.id.timer)).perform( object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return withId(R.id.timer)
            }

            override fun getDescription(): String {
                return "Reading value of countdown timer"
            }

            override fun perform(uiController: UiController?, view: View?) {
                val tv = view as TextView
                val reading = tv.text.toString()
                stringHolder.add(reading)
                Log.d("elapsedTime", "element reading = $reading")
            }

        })

        val timerString = stringHolder[0]
        val hms = timerString.split(":")
        // Assume for now that we're under 60 seconds.  Don't count minutes/hours.
        val secs = hms.last().toInt()
        return secs
    }
}

// A wrapper that can be used to represent a dataseeding QuizQuestion or a canvasapi2 QuizQuestion
private class QuizQuestionWrapper {
    var questionName: String?
    var questionType: String?
    var questionText: String?
    var answers: MutableList<QuizAnswerWrapper>?

    constructor(question: com.instructure.dataseeding.model.QuizQuestion) {
        questionName = question.questionName
        questionType = question.questionType
        questionText = question.questionText
        answers = mutableListOf<QuizAnswerWrapper>()
        for(answer in question.answers) {
            answers!!.add(QuizAnswerWrapper(answer))
        }
    }

    constructor(question: com.instructure.canvasapi2.models.QuizQuestion) {
        questionName = question.questionName
        questionType = question.questionTypeString
        questionText = question.questionText
        answers = mutableListOf<QuizAnswerWrapper>()
        if(question.answers != null) {
            for (answer in question.answers!!) {
                answers!!.add(QuizAnswerWrapper(answer))
            }
        }
    }
}

// A wrapper class that can be used to represent a dataseeding QuizAnswer or
// a canvasapi2 QuizAnswer.
private class QuizAnswerWrapper {
    var answerText: String?
    var answerWeight: Int?
    constructor(answer: com.instructure.dataseeding.model.QuizAnswer) {
        answerText = answer.text
        answerWeight = answer.weight
    }

    constructor(answer: com.instructure.canvasapi2.models.QuizAnswer) {
        answerText = answer.answerText
        answerWeight = answer.answerWeight
    }
}

