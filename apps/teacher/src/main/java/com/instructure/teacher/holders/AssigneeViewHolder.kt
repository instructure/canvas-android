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
 */
package com.instructure.teacher.holders

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.models.AssigneeCategory
import com.instructure.teacher.models.EveryoneAssignee
import com.instructure.teacher.presenters.AssigneeListPresenter
import kotlinx.android.synthetic.main.adapter_assignee.view.*
import kotlinx.android.synthetic.main.adapter_assignee_header.view.*

abstract class AssigneeViewHolder(view: View) : RecyclerView.ViewHolder(view)

class AssigneeItemViewHolder(view: View) : AssigneeViewHolder(view) {

    lateinit var assigneeTitleViewForTest: TextView

    companion object {
        private const val SELECTION_TRANSPARENCY_MASK = 0x08FFFFFF
        const val HOLDER_RES_ID = R.layout.adapter_assignee
    }

    fun bind(item: Any, presenter: AssigneeListPresenter, selectionColor: Int) = with(itemView) {

        assigneeTitleViewForTest = assigneeTitleView

        fun setChecked(isChecked: Boolean = true) {
            if (isChecked) {
                setBackgroundColor(selectionColor and SELECTION_TRANSPARENCY_MASK)
                assigneeAvatarImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_circle)?.apply {
                    mutate().setTintList(ColorStateList.valueOf(selectionColor))
                })
                checkMarkImageView.setVisible()
            } else {
                setBackgroundColor(Color.TRANSPARENT)
                checkMarkImageView.setGone()
            }
        }

        setChecked(false)

        when (item) {
            is EveryoneAssignee -> {
                val itemName = context.getString(if (item.displayAsEveryoneElse) R.string.everyone_else else R.string.everyone)
                assigneeTitleView.text = itemName
                assigneeSubtitleView.text = context.resources.getQuantityString(R.plurals.people_count, item.peopleCount, item.peopleCount)
                if (presenter.isEveryone) {
                    setChecked(true)
                } else {
                    setItemAvatar(itemName, assigneeAvatarImageView)
                }
                setOnClickListener { presenter.toggleIsEveryone(adapterPosition) }
            }
            is User -> {
                if (item.id in presenter.selectedStudents) {
                    setChecked(true)
                } else {
                    ProfileUtils.loadAvatarForUser(assigneeAvatarImageView, item.name, item.avatarUrl)
                }
                assigneeTitleView.text = Pronouns.span(item.name, item.pronouns)
                assigneeSubtitleView.text = item.primaryEmail ?: item.email
                setOnClickListener { presenter.toggleStudent(item.id, adapterPosition) }
            }
            is Section -> {
                val count = item.totalStudents
                assigneeTitleView.text = item.name
                assigneeSubtitleView.text = context.resources.getQuantityString(R.plurals.people_count, count, count)
                if (item.id in presenter.selectedSections) {
                    setChecked(true)
                } else {
                    setItemAvatar(item.name, assigneeAvatarImageView)
                }
                setOnClickListener { presenter.toggleSection(item.id, adapterPosition) }
            }
            is Group -> {
                assigneeTitleView.text = item.name
                assigneeSubtitleView.text = context.resources.getQuantityString(R.plurals.people_count, item.membersCount, item.membersCount)
                if (item.id in presenter.selectedGroups) {
                    setChecked(true)
                } else {
                    setItemAvatar(item.name.orEmpty(), assigneeAvatarImageView)
                }
                setOnClickListener { presenter.toggleGroup(item.id, adapterPosition) }
            }
        }
    }

    private fun setItemAvatar(itemName: String, imageView: ImageView) {
        ProfileUtils.loadAvatarForUser(imageView, itemName, null, 0)
    }
}

class AssigneeTypeViewHolder(view: View) : AssigneeViewHolder(view) {

    companion object {
        const val HOLDER_RES_ID = R.layout.adapter_assignee_header
    }

    fun bind(type: AssigneeCategory) = with(itemView) {
        assigneeTypeTextView.text = context.getString(when (type) {
            AssigneeCategory.SECTIONS -> R.string.assignee_type_course_sections
            AssigneeCategory.GROUPS -> R.string.assignee_type_groups
            AssigneeCategory.STUDENTS -> R.string.assignee_type_students
        })
    }

}
