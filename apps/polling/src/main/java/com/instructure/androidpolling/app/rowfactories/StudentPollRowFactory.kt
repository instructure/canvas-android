/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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

package com.instructure.androidpolling.app.rowfactories

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView

import com.instructure.androidpolling.app.R
import com.instructure.androidpolling.app.model.AnswerValue
import kotlinx.android.synthetic.main.listview_item_student_answer.view.*

object StudentPollRowFactory {

    fun buildRowView(layoutInflater: LayoutInflater, context: Context, answerValue: AnswerValue, position: Int, hasSubmitted: Boolean, isPublished: Boolean, conView: View?): View {
        var convertView = conView
        val holder: ViewHolder?
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listview_item_student_answer, null, false)

            holder = ViewHolder(convertView)

            convertView!!.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        holder.answerText.text = answerValue.value ?: ""

        holder.correctAnswer.isChecked = answerValue.isSelected

        // If we've submitted stuff, we want to gray everything out and remove the background
        if (hasSubmitted || !isPublished) {
            holder.addAnswerContainer.setBackgroundColor(context.resources.getColor(R.color.white))
            // Don't want to let it be clicked if the user has submitted the poll
            holder.correctAnswer.isEnabled = false
            if (!answerValue.isSelected) {
                holder.answerText.setTextColor(context.resources.getColor(R.color.gray))
            }
        } else {
            holder.correctAnswer.isEnabled = true
        }
        return convertView
    }

    internal class ViewHolder(view: View) {
        var addAnswerContainer: LinearLayout = view.addAnswerContainer
        var correctAnswer: RadioButton = view.correctAnswer
        var answerText: TextView = view.answerText
    }
}
