/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.holders

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.databinding.AdapterQuizBinding
import java.util.*

class QuizViewHolder(private val binding: AdapterQuizBinding) : RecyclerView.ViewHolder(binding.root) {

    init {
        val ungradedCount = itemView.findViewById<TextView>(R.id.ungradedCount)
        ungradedCount.setTextColor(ThemePrefs.brandColor)
        DrawableCompat.setTint(ungradedCount.background, ThemePrefs.brandColor)
    }

    fun bind(context: Context, quiz: Quiz, iconColor: Int, callback: (Quiz) -> Unit) = with(binding) {
        quizLayout.setOnClickListener { callback(quiz) }
        quizTitle.text = quiz.title
        quizIcon.setIcon(R.drawable.ic_quiz, iconColor)
        quizIcon.setPublishedStatus(quiz.published)
        publishedBar.visibility = if (quiz.published) View.VISIBLE else View.INVISIBLE

        val resources = binding.root.resources

        // String to track if the assignment is closed. If it isn't, we'll prepend the due date string with an empty string and it will look the same
        // Otherwise, we want it to say "Closed" and the due date with a dot as a separator
        var closedString: String = ""
        if (quiz.lockDate?.before(Date()) == true) {
            closedString = context.getString(R.string.cmp_closed) + context.getString(R.string.utils_dotWithSpaces)
        }

        // due dates
        if (quiz.allDates.size > 1) {
            //we have multiple due dates
            dueDate.text = closedString + context.getString(R.string.multiple_due_dates)
        } else {
            if (quiz.dueAt != null) {
                dueDate.text = closedString + context.getString(
                    R.string.due,
                    DateHelper.getMonthDayAtTime(context, quiz.dueAt.toDate(), context.getString(R.string.at))
                )
            } else {
                dueDate.text = closedString + context.getString(R.string.no_due_date)
            }
        }

        // points and question count
        var pointString: String = ""
        var contentDescriptionPointsString: String = ""
        quiz.pointsPossible?.let {
            if ((quiz.quizType == Quiz.TYPE_GRADED_SURVEY || quiz.quizType == Quiz.TYPE_ASSIGNMENT || quiz.quizType == Quiz.TYPE_NEW_QUIZZES)) {
                pointString = resources.getQuantityString(
                    R.plurals.quantityPointsAbbreviated,
                    it.toDouble().toInt(),
                    NumberHelper.formatDecimal(it.toDouble(), 1, true)
                )

                pointString += context.getString(R.string.utils_dotWithSpaces)

                contentDescriptionPointsString = resources.getQuantityString(
                    R.plurals.quantityPointsFull,
                    it.toDouble().toInt(),
                    NumberHelper.formatDecimal(it.toDouble(), 1, true)
                )
            }
        }

        val questionCountString: String = resources.getQuantityString(
            R.plurals.quantityQuestions,
            quiz.questionCount,
            quiz.questionCount
        )

        pointsAndQuestions.text = pointString + questionCountString

        // ungraded count
        if (quiz._assignment == null || quiz._assignment?.needsGradingCount == 0L) {
            ungradedCount.setGone().text = ""
        } else {
            val quantity = quiz._assignment?.needsGradingCount ?: 0
            ungradedCount.setVisible().text = context.resources.getQuantityString(
                R.plurals.needsGradingCount,
                quantity.toInt(),
                NumberHelper.formatInt(quantity)
            )
            ungradedCount.setAllCaps(true)
        }

        // set the content description on the container so we can tell the user that it is published as the last piece of information. When a content description is on a container
        quizLayout.contentDescription = quiz.title + " " + dueDate.text + " " + contentDescriptionPointsString + " " + questionCountString + " " + ungradedCount.text + " " + if (quiz.published) context.getString(
                R.string.published
            ) else context.getString(R.string.not_published)
    }
}
