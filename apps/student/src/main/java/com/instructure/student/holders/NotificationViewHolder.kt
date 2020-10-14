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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.StreamItem
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.adapter.NotificationListRecyclerAdapter
import com.instructure.student.util.BinderUtils
import com.instructure.student.interfaces.NotificationAdapterToFragmentCallback
import kotlinx.android.synthetic.main.viewholder_notification.view.*

class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @SuppressLint("SetTextI18n")
    fun bind(
        context: Context,
        item: StreamItem,
        checkboxCallback: NotificationListRecyclerAdapter.NotificationCheckboxCallback,
        adapterToFragmentCallback: NotificationAdapterToFragmentCallback<StreamItem>
    ) = with(itemView) {

        setOnClickListener {
            if (checkboxCallback.isEditMode()) {
                checkboxCallback.onCheckChanged(item, !item.isChecked, adapterPosition)
            } else {
                adapterToFragmentCallback.onRowClicked(item, adapterPosition, true)
            }
        }

        setOnLongClickListener {
            checkboxCallback.onCheckChanged(item, !item.isChecked, adapterPosition)
            true
        }

        title.text = item.getTitle(context)

        // Course Name
        val courseName: String? = if (item.contextType === CanvasContext.Type.COURSE && item.canvasContext != null) {
            item.canvasContext!!.secondaryName
        } else if (item.contextType === CanvasContext.Type.GROUP && item.canvasContext != null) {
            item.canvasContext!!.name
        } else {
            ""
        }

        course.text = courseName
        course.setTextColor(ColorKeeper.getOrGenerateColor(item.canvasContext))

        // Description
        if (!TextUtils.isEmpty(item.getMessage(context))) {
            description.text = BinderUtils.getHtmlAsText(item.getMessage(context)!!)
            description.setVisible()
        } else {
            description.text = ""
            description.setGone()
        }

        if (item.isChecked) {
            setBackgroundColor(ContextCompat.getColor(context, R.color.lightGray))
        } else {
            setBackgroundColor(ContextCompat.getColor(context, R.color.canvasBackgroundWhite))
        }

        // Icon
        val drawableResId: Int
        when (item.getStreamItemType()) {
            StreamItem.Type.DISCUSSION_TOPIC -> {
                drawableResId = R.drawable.vd_discussion
                icon.contentDescription = context.getString(R.string.discussionIcon)
            }
            StreamItem.Type.ANNOUNCEMENT -> {
                drawableResId = R.drawable.vd_announcement
                icon.contentDescription = context.getString(R.string.announcementIcon)
            }
            StreamItem.Type.SUBMISSION -> {
                drawableResId = R.drawable.vd_assignment
                icon.contentDescription = context.getString(R.string.assignmentIcon)

                // Need to prepend "Grade" in the message if there is a valid score
                if (item.score != -1.0) {
                    // If the submission has a grade (like a letter or percentage) display it
                    if (item.grade != null
                        && item.grade != ""
                        && item.grade != "null"
                    ) {
                        description.text = context.resources.getString(R.string.grade) + ": " + item.grade
                    } else {
                        description.text = context.resources.getString(R.string.grade) + description.text
                    }
                }
            }
            StreamItem.Type.CONVERSATION -> {
                drawableResId = R.drawable.vd_inbox
                icon.contentDescription = context.getString(R.string.conversationIcon)
            }
            StreamItem.Type.MESSAGE -> when {
                item.contextType === CanvasContext.Type.COURSE -> {
                    drawableResId = R.drawable.vd_assignment
                    icon.contentDescription = context.getString(R.string.assignmentIcon)
                }
                item.notificationCategory.contains("assignment graded", ignoreCase = true) -> {
                    drawableResId = R.drawable.vd_grades
                    icon.contentDescription = context.getString(R.string.gradesIcon)
                }
                else -> {
                    drawableResId = R.drawable.vd_navigation_change_user
                    icon.contentDescription = context.getString(R.string.defaultIcon)
                }
            }
            StreamItem.Type.CONFERENCE -> {
                drawableResId = R.drawable.vd_conferences
                icon.contentDescription = context.getString(R.string.icon)
            }
            StreamItem.Type.COLLABORATION -> {
                drawableResId = R.drawable.vd_collaborations
                icon.contentDescription = context.getString(R.string.icon)
            }
            StreamItem.Type.COLLECTION_ITEM -> drawableResId = R.drawable.vd_peer_review
            else -> drawableResId = R.drawable.vd_peer_review
        }

        val courseColor: Int = if (item.canvasContext != null) {
            ColorKeeper.getOrGenerateColor(item.canvasContext)
        } else
            ThemePrefs.primaryColor

        val drawable = ColorKeeper.getColoredDrawable(context, drawableResId, courseColor)
        icon.setImageDrawable(drawable)

        // Read/Unread
        if (item.isReadState) {
            title.setTypeface(null, Typeface.NORMAL)
        } else {
            title.setTypeface(null, Typeface.BOLD)
        }
    }

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_notification
    }
}
