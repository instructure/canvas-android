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
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.isValid
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.isVisible
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.databinding.ViewholderQuizBinding
import com.instructure.student.interfaces.AdapterToFragmentCallback
import com.instructure.student.util.BinderUtils
import java.util.Date

class QuizViewHolder(private val binding: ViewholderQuizBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        item: Quiz,
        adapterToFragmentCallback: AdapterToFragmentCallback<Quiz>?,
        context: Context,
        iconAndTextColor: Int,
        restrictQuantitativeData: Boolean
    ) = with(binding) {
        root.onClickWithRequireNetwork { adapterToFragmentCallback?.onRowClicked(item, adapterPosition, true) }

        // Title
        title.text = item.title

        // Description
        val desc = BinderUtils.getHtmlAsText(item.description)
        description.text = desc
        description.setVisible(!desc.isNullOrBlank())

        // Icon
        val drawable = ColorKeeper.getColoredDrawable(context, R.drawable.ic_quiz, iconAndTextColor)
        icon.setImageDrawable(drawable)

        // Status and Date
        status.setTextColor(iconAndTextColor)
        status.setVisible(item.lockDate?.let { Date().after(it) } == true || item.requireLockdownBrowserForResults)
        val dateText: String? =
            item.dueDate?.let { DateHelper.createPrefixedDateTimeString(context, R.string.toDoDue, it) }
        date.setVisible(dateText.isValid()).text = dateText
        bulletStatusAndDate.setVisible(status.isVisible && date.isVisible)
        dateContainer.setVisible(status.isVisible || date.isVisible)

        // Points and Questions
        val possiblePoints = item.pointsPossible?.toDoubleOrNull() ?: 0.0
        points.setVisible(possiblePoints > 0 && !restrictQuantitativeData).text = context.resources.getQuantityString(
            R.plurals.pointCount,
            possiblePoints.toInt(),
            NumberHelper.formatDecimal(possiblePoints, 2, true)
        )
        bulletPointsAndQuestions.setVisible(points.isVisible)
        val count = item.questionCount
        questions.text = context.resources.getQuantityString(R.plurals.question_count, count, count)
    }

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_quiz
    }

}
