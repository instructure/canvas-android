/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
package com.instructure.student.holders

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.QuizSubmissionQuestion
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.onTextChanged
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.interfaces.QuizPostNumerical
import com.instructure.student.interfaces.QuizToggleFlagState
import kotlinx.android.synthetic.main.quiz_numerical_question.view.*


class QuizNumericalViewHolder(v: View) : RecyclerView.ViewHolder(v) {

    fun bind(
        quizSubmissionQuestion: QuizSubmissionQuestion,
        courseColor: Int,
        position: Int,
        shouldLetAnswer: Boolean,
        context: Context,
        flagStateCallback: QuizToggleFlagState,
        callback: CanvasWebView.CanvasEmbeddedWebViewCallback,
        webViewClientCallback: CanvasWebView.CanvasWebViewClientCallback,
        numericalCallback: QuizPostNumerical
    ) = with(itemView) {
        question.loadUrl("about:blank")
        question.canvasWebViewClientCallback = webViewClientCallback
        question.loadHtml(quizSubmissionQuestion.questionText, "")
        question.setBackgroundColor(Color.TRANSPARENT)
        question.canvasEmbeddedWebViewCallback = callback
        questionNumber.text = context.getString(R.string.question) + " " + (position + 1)

        answerInput.setVisible(shouldLetAnswer)
        divider.setVisible(shouldLetAnswer)
        flag.isEnabled = shouldLetAnswer

        answerInput.setText(quizSubmissionQuestion.answer?.toString().orEmpty())

        answerInput.onTextChanged {
            numericalCallback.postNumerical(quizSubmissionQuestion.id, sanitizeInput(it))
        }

        updateFlagDrawable(quizSubmissionQuestion.isFlagged, courseColor)

        flag.onClick {
            quizSubmissionQuestion.isFlagged = !quizSubmissionQuestion.isFlagged
            flagStateCallback.toggleFlagged(quizSubmissionQuestion.isFlagged, quizSubmissionQuestion.id)
            updateFlagDrawable(quizSubmissionQuestion.isFlagged, courseColor)
        }
    }

    private fun updateFlagDrawable(isFlagged: Boolean, courseColor: Int) = with(itemView) {
        if (isFlagged) {
            flag.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.vd_bookmark_filled, courseColor))
        } else {
            flag.setImageDrawable(
                ColorKeeper.getColoredDrawable(
                    context,
                    R.drawable.vd_navigation_bookmarks,
                    ContextCompat.getColor(context, R.color.defaultTextGray)
                )
            )
        }
    }

    companion object {

        const val HOLDER_RES_ID = R.layout.quiz_numerical_question

        fun sanitizeInput(input: String): String {
            return input.trimEnd('.').let { it.toIntOrNull() ?: it.toDoubleOrNull() ?: 0 }.toString()
        }

    }

}
