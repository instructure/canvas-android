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

package com.instructure.student.binders

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.widget.TextView
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.student.R
import com.instructure.student.holders.ScheduleItemViewHolder
import com.instructure.student.interfaces.AdapterToFragmentCallback

class ScheduleItemBinder : BaseBinder() {
    companion object {

        fun bind(
                holder: ScheduleItemViewHolder,
                item: ScheduleItem,
                context: Context,
                courseColor: Int,
                adapterToFragmentCallback: AdapterToFragmentCallback<ScheduleItem>) {

            holder.itemView.setOnClickListener { adapterToFragmentCallback.onRowClicked(item, holder.adapterPosition, false) }

            when (item.itemType) {

                ScheduleItem.Type.TYPE_SYLLABUS -> {
                    holder.title.text = context.getString(R.string.syllabus)

                    val drawable = ColorKeeper.getColoredDrawable(context, R.drawable.vd_syllabus, courseColor)
                    holder.icon.setImageDrawable(drawable)
                }
                ScheduleItem.Type.TYPE_CALENDAR -> {
                    val drawable = ColorKeeper.getColoredDrawable(context, R.drawable.vd_calendar, courseColor)
                    holder.icon.setImageDrawable(drawable)
                    holder.title.text = item.title
                    holder.date.text = item.getStartString(context)

                    val description = BaseBinder.getHtmlAsText(item.description.orEmpty())
                    setupDescription(description, holder.description)
                }
                ScheduleItem.Type.TYPE_ASSIGNMENT -> {
                    holder.title.text = item.title

                    val drawable: Drawable
                    val assignment = item.assignment

                    if (assignment != null) {

                        val drawableResId = BaseBinder.getAssignmentIcon(assignment)
                        drawable = ColorKeeper.getColoredDrawable(context, drawableResId, courseColor)
                        holder.icon.setImageDrawable(drawable)

                        val dueDate = assignment.dueDate
                        if (dueDate != null) {
                            val dateString = DateHelper.createPrefixedDateTimeString(context, R.string.toDoDue, dueDate)
                            holder.date.text = dateString
                        } else {
                            holder.date.text = context.resources.getString(R.string.toDoNoDueDate)
                        }

                        val description = BaseBinder.getHtmlAsText(assignment.description)
                        setupDescription(description, holder.description)

                        // Submissions aren't included with the assignments in the api call, so we don't get grades
                        // so we'll never see the grade
                        BaseBinder.setInvisible(holder.points)

                    } else {
                        drawable = ColorKeeper.getColoredDrawable(context, R.drawable.vd_calendar, courseColor)
                        holder.icon.setImageDrawable(drawable)

                        holder.date.text = item.getStartString(context)

                        val description = BaseBinder.getHtmlAsText(item.description)
                        setupDescription(description, holder.description)
                    }
                }
            }
        }

        private fun setupDescription(description: String?, textView: TextView) {
            if (!TextUtils.isEmpty(description)) {
                textView.text = description
                BaseBinder.setVisible(textView)
            } else {
                textView.text = ""
                BaseBinder.setGone(textView)
            }
        }
    }
}
