package com.instructure.student.holders

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.courseOrUserColor
import com.instructure.pandautils.utils.getIconForPlannerItem
import com.instructure.pandautils.utils.getTagForPlannerItem
import com.instructure.pandautils.utils.setTextForVisibility
import com.instructure.student.R
import com.instructure.student.adapter.TodoListRecyclerAdapter
import com.instructure.student.databinding.ViewholderTodoBinding
import com.instructure.student.interfaces.NotificationAdapterToFragmentCallback

class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    @SuppressLint("SetTextI18n")
    fun bind(
        context: Context,
        item: PlannerItem,
        adapterToFragmentCallback: NotificationAdapterToFragmentCallback<PlannerItem>?,
        checkboxCallback: TodoListRecyclerAdapter.TodoCheckboxCallback
    ) = with(ViewholderTodoBinding.bind(itemView)){
        root.setOnClickListener {
            if (checkboxCallback.isEditMode) {
                checkboxCallback.onCheckChanged(item, !item.isChecked, adapterPosition)
            } else {
                adapterToFragmentCallback?.onRowClicked(item, adapterPosition, true)
            }
        }

        root.setOnLongClickListener {
            checkboxCallback.onCheckChanged(item, !item.isChecked, adapterPosition)
            true
        }

        course.text = getContextNameForPlannerItem(context, item)
        course.setTextColor(item.canvasContext.courseOrUserColor)
        title.text = item.plannable.title
        description.setTextForVisibility(getDateForPlannerItem(context, item))
        tag.setTextForVisibility(item.getTagForPlannerItem(context))

        val drawable = ColorKeeper.getColoredDrawable(context, item.getIconForPlannerItem(), item.canvasContext.courseOrUserColor)
        icon.setImageDrawable(drawable)

        if (item.isChecked) {
            root.setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundMedium))
        } else {
            root.setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundLightest))
        }
    }

    private fun getContextNameForPlannerItem(context: Context, plannerItem: PlannerItem): String {
        return if (plannerItem.plannableType == PlannableType.PLANNER_NOTE) {
            if (plannerItem.contextName.isNullOrEmpty()) {
                context.getString(R.string.userCalendarToDo)
            } else {
                context.getString(R.string.courseToDo, plannerItem.contextName)
            }
        } else {
            plannerItem.contextName.orEmpty()
        }
    }

    private fun getDateForPlannerItem(context: Context, plannerItem: PlannerItem): String? {
        return if (plannerItem.plannableType == PlannableType.PLANNER_NOTE) {
            plannerItem.plannable.todoDate.toDate()?.let {
                val dateText = DateHelper.dayMonthDateFormat.format(it)
                val timeText = DateHelper.getFormattedTime(context, it)
                context.getString(R.string.calendarAtDateTime, dateText, timeText)
            }
        } else if (plannerItem.plannableType == PlannableType.CALENDAR_EVENT) {
            val startDate = plannerItem.plannable.startAt
            val endDate = plannerItem.plannable.endAt
            if (startDate != null && endDate != null) {
                val dateText = DateHelper.dayMonthDateFormat.format(startDate)
                val startText = DateHelper.getFormattedTime(context, startDate)
                val endText = DateHelper.getFormattedTime(context, endDate)
                if (plannerItem.plannable.allDay == true) {
                    dateText
                } else if (startDate == endDate) {
                    context.getString(R.string.calendarAtDateTime, dateText, startText)
                } else {
                    context.getString(R.string.calendarFromTo, dateText, startText, endText)
                }
            } else null
        } else {
            plannerItem.plannable.dueAt?.let {
                val dateText = DateHelper.dayMonthDateFormat.format(it)
                val timeText = DateHelper.getFormattedTime(context, it)
                context.getString(R.string.calendarDueDate, dateText, timeText)
            }
        }
    }

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_todo
    }
}
