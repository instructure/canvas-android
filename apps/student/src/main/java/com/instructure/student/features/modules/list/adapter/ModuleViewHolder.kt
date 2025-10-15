package com.instructure.student.features.modules.list.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.managers.graphql.ModuleItemCheckpoint
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.isValid
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setTextForVisibility
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.databinding.ViewholderModuleBinding
import com.instructure.student.features.modules.util.ModuleUtility
import com.instructure.student.util.BinderUtils

private const val MODULE_INDENT_IN_DP = 10

class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        moduleObject: ModuleObject?,
        moduleItem: ModuleItem,
        context: Context,
        adapterToFragmentCallback: ModuleAdapterToFragmentCallback?,
        courseColor: Int,
        isFirstItem: Boolean,
        isLastItem: Boolean,
        restrictQuantitativeData: Boolean,
        checkpoints: List<ModuleItemCheckpoint>?
    ) = with(ViewholderModuleBinding.bind(itemView)) {

        val isLocked = ModuleUtility.isGroupLocked(moduleObject)
        root.setOnClickListener {
            adapterToFragmentCallback?.onRowClicked(moduleObject!!, moduleItem, adapterPosition, true)
        }

        val indentInPx = context.DP(MODULE_INDENT_IN_DP).toInt()
        moduleItemIndent.layoutParams.width = indentInPx * moduleItem.indent

        // Title
        title.text = moduleItem.title
        if (ModuleItem.Type.Locked.toString().equals(moduleItem.type, ignoreCase = true)
            || ModuleItem.Type.ChooseAssignmentGroup.toString().equals(moduleItem.type, ignoreCase = true)
        ) {
            title.setTypeface(null, Typeface.ITALIC)
            title.setTextColor(ContextCompat.getColor(context, R.color.textDark))
        } else {
            title.setTypeface(null, Typeface.NORMAL)
            title.setTextColor(ContextCompat.getColor(context, R.color.textDarkest))
        }

        // Description
        if (moduleItem.completionRequirement?.type != null) {
            val requirement = moduleItem.completionRequirement!!
            val complete = requirement.completed
            description.setVisible()
            description.setTextColor(ContextCompat.getColor(context, R.color.textDark))
            val text: String? =
                when (ModuleObject.State.values().firstOrNull { it.apiString == requirement.type }) {
                    ModuleObject.State.MustSubmit -> {
                        if (complete) description.setTextColor(courseColor)
                        if (complete) context.getString(R.string.moduleItemSubmitted) else context.getString(
                            R.string.moduleItemSubmit
                        )
                    }
                    ModuleObject.State.MustView -> {
                        if (complete) context.getString(R.string.moduleItemViewed) else context.getString(
                            R.string.moduleItemMustView
                        )
                    }
                    ModuleObject.State.MustContribute -> {
                        if (complete) context.getString(R.string.moduleItemContributed) else context.getString(
                            R.string.moduleItemContribute
                        )
                    }
                    ModuleObject.State.MinScore -> {
                        if (complete) context.getString(R.string.moduleItemMinScoreMet) else context.getString(
                            R.string.moduleItemMinScore
                        ) + " " + requirement.minScore
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
            val drawable =
                ColorKeeper.getColoredDrawable(context, R.drawable.ic_check_white_24dp, courseColor)
            indicator.setImageDrawable(drawable)
            indicator.setVisible()
        }
        if (isLocked) {
            val drawable = ColorKeeper.getColoredDrawable(context, R.drawable.ic_lock, courseColor)
            indicator.setImageDrawable(drawable)
            indicator.setVisible()
        }

        // Icon
        val drawableResource: Int = when {
            moduleItem.quizLti -> R.drawable.ic_quiz
            ModuleItem.Type.Assignment.toString()
                .equals(moduleItem.type, ignoreCase = true) -> R.drawable.ic_assignment
            ModuleItem.Type.Discussion.toString()
                .equals(moduleItem.type, ignoreCase = true) -> R.drawable.ic_discussion
            ModuleItem.Type.File.toString().equals(moduleItem.type, ignoreCase = true) -> R.drawable.ic_download
            ModuleItem.Type.Page.toString().equals(moduleItem.type, ignoreCase = true) -> R.drawable.ic_pages
            ModuleItem.Type.Quiz.toString().equals(moduleItem.type, ignoreCase = true) -> R.drawable.ic_quiz
            ModuleItem.Type.ExternalUrl.toString().equals(moduleItem.type, ignoreCase = true) -> R.drawable.ic_link
            ModuleItem.Type.ExternalTool.toString().equals(moduleItem.type, ignoreCase = true) -> R.drawable.ic_lti
            ModuleItem.Type.Locked.toString().equals(moduleItem.type, ignoreCase = true) -> R.drawable.ic_lock
            ModuleItem.Type.ChooseAssignmentGroup.toString()
                .equals(moduleItem.type, ignoreCase = true) -> R.drawable.ic_pages
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
            // Handle checkpoints or regular due date
            if (!checkpoints.isNullOrEmpty()) {
                // Hide the single date field
                date.setGone()

                // Clear previous checkpoint views
                checkpointDatesContainer.removeAllViews()
                checkpointDatesContainer.setVisible()

                // Create a TextView for each checkpoint
                checkpoints.forEach { checkpoint ->
                    val checkpointDateView = android.widget.TextView(context).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        setTextAppearance(R.style.AdapterItemDescriptionText)
                        text = if (checkpoint.dueAt != null) {
                            DateHelper.createPrefixedDateTimeString(
                                context,
                                R.string.toDoDue,
                                checkpoint.dueAt
                            )
                        } else {
                            context.getString(R.string.toDoNoDueDate)
                        }
                    }
                    checkpointDatesContainer.addView(checkpointDateView)
                }
            } else {
                // Show single date field for regular module items
                checkpointDatesContainer.setGone()
                if (details.dueDate != null) {
                    date.text = DateHelper.createPrefixedDateTimeString(
                        context,
                        R.string.toDoDue,
                        details.dueDate
                    )
                    date.setVisible()
                } else {
                    date.text = ""
                    date.setGone()
                }
            }

            val pointsPossible = details.pointsPossible
            if (pointsPossible.isValid() && !restrictQuantitativeData) {
                points.text = context.getString(
                    R.string.totalPoints,
                    NumberHelper.formatDecimal(pointsPossible.toDouble(), 2, true)
                )
                points.setVisible()
            } else {
                points.text = ""
                points.setGone()
            }
        } else {
            points.text = ""
            date.text = ""
            date.setGone()
            checkpointDatesContainer.setGone()
            points.setGone()
        }
        BinderUtils.updateShadows(isFirstItem, isLastItem, shadowTop, shadowBottom)
    }

    companion object {
        const val HOLDER_RES_ID = R.layout.viewholder_module
    }
}