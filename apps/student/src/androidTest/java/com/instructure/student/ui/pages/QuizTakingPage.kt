/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.student.ui.pages

import androidx.test.espresso.web.webdriver.Locator

// Similar to CanvasWebViewPage in that it interacts with R.id.canvasWebView, but
// tailored to quiz taking.
class QuizTakingPage : CanvasWebViewPage() {

    fun assertQuestionDisplayed(questionId: Long, questionText: String) {
        runTextChecks(
                WebViewTextCheck(locatorType = Locator.ID, locatorValue = "question_${questionId}_question_text", textValue = questionText)
        )
    }

    fun selectAnyAnswer(questionId: Long) {
        pressButton(
                locatorType = Locator.ID, locatorValue = "question_${questionId}",
                subElementType = Locator.CLASS_NAME, subElementValue = "answer_label")
    }

    // OK, so it turns out to be pretty impossible to inject text for an essay answer.
    // Any attempt to do so results in:
    //
    //      Error in evaluationEvaluation: status: 13 value: {message=Cannot set the selection end} hasMessage: true message: Cannot set the selection end
    //
    // Caused by some combination of using webKeys() on an element with contenteditable="true"
    // and possibly iframes.
    //
    // See setText() in http://www.dataxsecure.com/js/closure/goog.bck.201310042312/docs/closure_goog_dom_selection.js.source.html
    // The useSelectionProperties_() method always returns false.
    //
    // There is evidently no way to work around it.
    //
    // I'll keep the code around (commented-out) just in case anyone wants to take another stab at this.
    //
    // Note: Assumes that you only have one essay question
//    fun answerEssayQuestion(essayAnswer: String) {
//        onWebView(allOf(withId(R.id.canvasWebView), isDisplayed()))
//                .inWindow(selectFrameByIdOrName("question_input_0_ifr")) // iframe
//                .withElement(findElement(Locator.CLASS_NAME, "mce-content-body"))
//                .perform(webScrollIntoView())
//                .perform(webClick()) // Get focus
//                .perform(clearElement())
//                .perform(webKeys(essayAnswer))
//    }

    fun submitQuiz() {
        pressButton(locatorType = Locator.ID, locatorValue = "submit_quiz_button")
    }
}