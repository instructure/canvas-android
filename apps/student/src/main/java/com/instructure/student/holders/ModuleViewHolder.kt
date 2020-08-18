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
import android.graphics.Typeface
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.isValid
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.util.BinderUtils
import com.instructure.student.interfaces.ModuleAdapterToFragmentCallback
import com.instructure.student.util.ModuleUtility
import kotlinx.android.synthetic.main.viewholder_module.view.*

class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        moduleObject: ModuleObject?,
        moduleItem: ModuleItem,
        context: Context,
        adapterToFragmentCallback: ModuleAdapterToFragmentCallback?,
        courseColor: Int,
        isFirstItem: Boolean,
        isLastItem: Boolean
    ) = with(itemView) {
        val isLocked = ModuleUtility.isGroupLocked(moduleObject)
        setOnClickListener {
            adapterToFragmentCallback?.onRowClicked(moduleObject!!, moduleItem, adapterPosition, true)
        }

        // Title
        title.text = moduleItem.title
        if (ModuleItem.Type.Locked.toString().equals(moduleItem.type, ignoreCase = true)
            || ModuleItem.Type.ChooseAssignmentGroup.toString().equals(moduleItem.type, ignoreCase = true)
        ) {
            title.setTypeface(null, Typeface.ITALIC)
            title.setTextColor(ContextCompat.getColor(context, R.color.secondaryText))
        } else {
            title.setTypeface(null, Typeface.NORMAL)
            title.setTextColor(ContextCompat.getColor(context, R.color.primaryText))
        }

        // Description
        if (moduleItem.completionRequirement?.type != null) {
            val requirement = moduleItem.completionRequirement!!
            val complete = requirement.completed
            description.setVisible()
            description.setTextColor(ContextCompat.getColor(context, R.color.canvasTextMedium))
            val text: String? = when (ModuleObject.State.values().firstOrNull { it.apiString == requirement.type }) {
                ModuleObject.State.MustSubmit -> {
                    if (complete) description.setTextColor(courseColor)
                    if (complete) context.getString(R.string.moduleItemSubmitted) else context.getString(R.string.moduleItemSubmit)
                }
                ModuleObject.State.MustView -> {
                    if (complete) context.getString(R.string.moduleItemViewed) else context.getString(R.string.moduleItemMustView)
                }
                ModuleObject.State.MustContribute -> {
                    if (complete) context.getString(R.string.moduleItemContributed) else context.getString(R.string.moduleItemContribute)
                }
                ModuleObject.State.MinScore -> {
                    if (complete) context.getString(R.string.moduleItemMinScoreMet) else context.getString(R.string.moduleItemMinScore) + " " + requirement.minScore
                }
                else -> null
            }
            description.setTextForVisibility(text)
        } else {
            description.text = ""
            description.setGone()
        }

        // Indicator
        indicator.setGone()
        if (moduleItem.completionRequirement?.completed == true) {
            val drawable = ColorKeeper.getColoredDrawable(context, R.drawable.vd_check_white_24dp, courseColor)
            indicator.setImageDrawable(drawable)
            indicator.setVisible()
        }
        if (isLocked) {
            val drawable = ColorKeeper.getColoredDrawable(context, R.drawable.vd_lock, courseColor)
            indicator.setImageDrawable(drawable)
            indicator.setVisible()
        }

        // Icon
        val drawableResource: Int = when {
            ModuleItem.Type.Assignment.toString().equals(moduleItem.type, ignoreCase = true) -> R.drawable.vd_assignment
            ModuleItem.Type.Discussion.toString().equals(moduleItem.type, ignoreCase = true) -> R.drawable.vd_discussion
            ModuleItem.Type.File.toString().equals(moduleItem.type, ignoreCase = true) -> R.drawable.vd_download
            ModuleItem.Type.Page.toString().equals(moduleItem.type, ignoreCase = true) -> R.drawable.vd_pages
            ModuleItem.Type.Quiz.toString().equals(moduleItem.type, ignoreCase = true) -> R.drawable.vd_quiz
            ModuleItem.Type.ExternalUrl.toString().equals(moduleItem.type, ignoreCase = true) -> R.drawable.vd_link
            ModuleItem.Type.ExternalTool.toString().equals(moduleItem.type, ignoreCase = true) -> R.drawable.vd_lti
            ModuleItem.Type.Locked.toString().equals(moduleItem.type, ignoreCase = true) -> R.drawable.vd_lock
            ModuleItem.Type.ChooseAssignmentGroup.toString()
                .equals(moduleItem.type, ignoreCase = true) -> R.drawable.vd_pages
            else -> -1
            // Details
        }
        if (drawableResource == -1) {
            icon.setGone()
        } else {
            val drawable = ColorKeeper.getColoredDrawable(context, drawableResource, courseColor)
            icon.setImageDrawable(drawable)
        }

        // Details
        val details = moduleItem.moduleDetails
        if (details != null) {
            val hasDate: Boolean
            val hasPoints: Boolean
            if (details.dueDate != null) {
                date.text = DateHelper.createPrefixedDateTimeString(context, R.string.toDoDue, details.dueDate)
                hasDate = true
            } else {
                date.text = ""
                hasDate = false
            }
            val pointsPossible = details.pointsPossible
            if (pointsPossible.isValid()) {
                points.text = context.getString(
                    R.string.totalPoints,
                    NumberHelper.formatDecimal(pointsPossible.toDouble(), 2, true)
                )
                hasPoints = true
            } else {
                points.text = ""
                hasPoints = false
            }
            if (!hasDate && !hasPoints) {
                date.setGone()
                points.setGone()
            } else {
                if (hasDate) date.setVisible() else date.setInvisible()
                if (hasPoints) points.setVisible() else points.setInvisible()
            }
        } else {
            points.text = ""
            date.text = ""
            date.setGone()
            points.setGone()
        }
        BinderUtils.updateShadows(isFirstItem, isLastItem, shadowTop, shadowBottom)
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.viewholder_module
    }
}
