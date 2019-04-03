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
import android.graphics.Typeface
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.StreamItem
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.student.R
import com.instructure.student.adapter.NotificationListRecyclerAdapter
import com.instructure.student.holders.NotificationViewHolder
import com.instructure.student.interfaces.NotificationAdapterToFragmentCallback

class NotificationBinder : BaseBinder() {
    companion object {

        fun bind(
                context: Context,
                holder: NotificationViewHolder,
                item: StreamItem,
                checkboxCallback: NotificationListRecyclerAdapter.NotificationCheckboxCallback,
                adapterToFragmentCallback: NotificationAdapterToFragmentCallback<StreamItem>) {

            holder.itemView.setOnClickListener {
                if (checkboxCallback.isEditMode) {
                    checkboxCallback.onCheckChanged(item, !item.isChecked, holder.adapterPosition)
                } else {
                    adapterToFragmentCallback.onRowClicked(item, holder.adapterPosition, true)
                }
            }

            holder.itemView.setOnLongClickListener {
                checkboxCallback.onCheckChanged(item, !item.isChecked, holder.adapterPosition)
                true
            }

            holder.title.text = item.getTitle(context)

            // Course Name
            val courseName: String? = if (item.contextType === CanvasContext.Type.COURSE && item.canvasContext != null) {
                item.canvasContext!!.secondaryName
            } else if (item.contextType === CanvasContext.Type.GROUP && item.canvasContext != null) {
                item.canvasContext!!.name
            } else {
                ""
            }

            holder.course.text = courseName
            holder.course.setTextColor(ColorKeeper.getOrGenerateColor(item.canvasContext))

            // Description
            if (!TextUtils.isEmpty(item.getMessage(context))) {
                holder.description.text = BaseBinder.getHtmlAsText(item.getMessage(context)!!)
                BaseBinder.setVisible(holder.description)
            } else {
                holder.description.text = ""
                BaseBinder.setGone(holder.description)
            }

            if (item.isChecked) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.lightGray))
            } else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.canvasBackgroundWhite))
            }

            // Icon
            val drawableResId: Int
            when (item.getStreamItemType()) {
                StreamItem.Type.DISCUSSION_TOPIC -> {
                    drawableResId = R.drawable.vd_discussion
                    holder.icon.contentDescription = context.getString(R.string.discussionIcon)
                }
                StreamItem.Type.ANNOUNCEMENT -> {
                    drawableResId = R.drawable.vd_announcement
                    holder.icon.contentDescription = context.getString(R.string.announcementIcon)
                }
                StreamItem.Type.SUBMISSION -> {
                    drawableResId = R.drawable.vd_assignment
                    holder.icon.contentDescription = context.getString(R.string.assignmentIcon)

                    //need to prepend "Grade" in the message if there is a valid score
                    if (item.score != -1.0) {
                        //if the submission has a grade (like a letter or percentage) display it
                        if (item.grade != null
                                && item.grade != ""
                                && item.grade != "null") {
                            holder.description.text = context.resources.getString(R.string.grade) + ": " + item.grade
                        } else {
                            holder.description.text = context.resources.getString(R.string.grade) + holder.description.text
                        }
                    }
                }
                StreamItem.Type.CONVERSATION -> {
                    drawableResId = R.drawable.vd_inbox
                    holder.icon.contentDescription = context.getString(R.string.conversationIcon)
                }
                StreamItem.Type.MESSAGE -> when {
                    item.contextType === CanvasContext.Type.COURSE -> {
                        drawableResId = R.drawable.vd_assignment
                        holder.icon.contentDescription = context.getString(R.string.assignmentIcon)
                    }
                    item.notificationCategory.toLowerCase().contains("assignment graded") -> {
                        drawableResId = R.drawable.vd_grades
                        holder.icon.contentDescription = context.getString(R.string.gradesIcon)
                    }
                    else -> {
                        drawableResId = R.drawable.vd_navigation_change_user
                        holder.icon.contentDescription = context.getString(R.string.defaultIcon)
                    }
                }
                StreamItem.Type.CONFERENCE -> {
                    drawableResId = R.drawable.vd_conferences
                    holder.icon.contentDescription = context.getString(R.string.icon)
                }
                StreamItem.Type.COLLABORATION -> {
                    drawableResId = R.drawable.vd_collaborations
                    holder.icon.contentDescription = context.getString(R.string.icon)
                }
                StreamItem.Type.COLLECTION_ITEM -> drawableResId = R.drawable.vd_peer_review
                else -> drawableResId = R.drawable.vd_peer_review
            }

            val courseColor: Int = if (item.canvasContext != null) {
                ColorKeeper.getOrGenerateColor(item.canvasContext)
            } else
                ThemePrefs.primaryColor

            val drawable = ColorKeeper.getColoredDrawable(context, drawableResId, courseColor)
            holder.icon.setImageDrawable(drawable)

            //Read/Unread
            if (item.isReadState) {
                holder.title.setTypeface(null, Typeface.NORMAL)
            } else {
                holder.title.setTypeface(null, Typeface.BOLD)
            }
        }
    }
}
