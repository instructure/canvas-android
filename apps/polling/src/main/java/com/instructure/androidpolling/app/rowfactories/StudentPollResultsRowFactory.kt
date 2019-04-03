/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.instructure.androidpolling.app.R
import kotlinx.android.synthetic.main.listview_item_student_poll_result.view.*

object StudentPollResultsRowFactory {

    internal class ViewHolder(view: View) {
        var answer: TextView = view.answer
        var correctAnswer: ImageView = view.correctAnswer
        var numAnswered: TextView = view.numAnswered
        var percentAnswered: ProgressBar = view.percentAnswered
    }

    fun buildRowView(layoutInflater: LayoutInflater, context: Context, answer: String, percentAnswered: Int, isCorrect: Boolean, isSelected: Boolean, hasCorrectAnswer: Boolean, conView: View?, position: Int): View {
        var convertView = conView
        val holder: ViewHolder?

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listview_item_student_poll_result, null, false)

            holder = ViewHolder(convertView)
            convertView!!.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        holder.numAnswered.text = percentAnswered.toString() + "%"
        holder.answer.text = answer
        holder.percentAnswered.progress = percentAnswered

        if (!isCorrect) {
            when (position % 3) {
                0 -> holder.percentAnswered.progressDrawable.setColorFilter(context.resources.getColor(R.color.polling_aqua), PorterDuff.Mode.SRC_IN)
                1 -> holder.percentAnswered.progressDrawable.setColorFilter(context.resources.getColor(R.color.polling_green), PorterDuff.Mode.SRC_IN)
                2 -> holder.percentAnswered.progressDrawable.setColorFilter(context.resources.getColor(R.color.canvaspollingtheme_color), PorterDuff.Mode.SRC_IN)
                else -> holder.percentAnswered.progressDrawable.setColorFilter(context.resources.getColor(R.color.canvaspollingtheme_color), PorterDuff.Mode.SRC_IN)
            }
        } else {
            holder.percentAnswered.progressDrawable.setColorFilter(context.resources.getColor(R.color.polling_purple), PorterDuff.Mode.SRC_IN)
        }

        // We don't want the user to be able to click it
        holder.correctAnswer.isEnabled = false

        if (hasCorrectAnswer) {
            if (isCorrect && isSelected) {
                holder.correctAnswer.setImageDrawable(context.resources.getDrawable(R.drawable.correct))
            } else if (!isCorrect && isSelected) {
                holder.correctAnswer.setImageDrawable(context.resources.getDrawable(R.drawable.x))
            } else if (isCorrect && !isSelected) {
                holder.correctAnswer.setImageDrawable(context.resources.getDrawable(R.drawable.circle))
            } else {
                holder.correctAnswer.visibility = View.INVISIBLE
            }
        } else {
            if (isSelected) {
                val selected = context.resources.getDrawable(R.drawable.check)
                holder.correctAnswer.setImageDrawable(selected)
            } else {
                holder.correctAnswer.visibility = View.INVISIBLE
            }
        }

        return convertView
    }
}
