package com.instructure.student.holders

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ToDo
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.setTextForVisibility
import com.instructure.student.R
import com.instructure.student.adapter.TodoListRecyclerAdapter
import com.instructure.student.databinding.ViewholderTodoBinding
import com.instructure.student.interfaces.NotificationAdapterToFragmentCallback

class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    @SuppressLint("SetTextI18n")
    fun bind(
        context: Context,
        item: ToDo,
        adapterToFragmentCallback: NotificationAdapterToFragmentCallback<ToDo>?,
        checkboxCallback: TodoListRecyclerAdapter.TodoCheckboxCallback
    ) = with(ViewholderTodoBinding.bind(itemView)){
        root.setOnClickListener {
            if (checkboxCallback.isEditMode) {
                checkboxCallback.onCheckChanged(item, !item.isChecked, adapterPosition)
            } else {
                adapterToFragmentCallback?.onRowClicked(item, adapterPosition, true)
            }
        }

        root.setOnLongClickListener(View.OnLongClickListener {
            if (item.ignore == null) return@OnLongClickListener false
            checkboxCallback.onCheckChanged(item, !item.isChecked, adapterPosition)
            true
        })

        when {
            item.canvasContext?.name != null -> {
                course.text = item.canvasContext!!.name
                course.setTextColor(item.canvasContext.color)
            }
            item.scheduleItem?.contextType == CanvasContext.Type.USER -> {
                course.text = context.getString(R.string.PersonalCalendar)
                course.setTextColor(item.canvasContext.color)
            }
            else -> course.text = ""
        }

        // Get courseColor
        val iconColor = item.canvasContext?.color ?: ContextCompat.getColor(context, R.color.textDarkest)

        if (item.isChecked) {
            root.setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundMedium))
        } else {
            root.setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundLightest))
        }

        var todoDetails: String? = ""
        var titlePrefix = ""
        when (item.type) {
            ToDo.Type.Submitting -> {
                titlePrefix = context.getString(R.string.toDoTurnIn) + " "
                title.text = titlePrefix + item.title
                todoDetails = DateHelper.createPrefixedDateTimeString(context, R.string.dueAt, item.comparisonDate)
            }
            ToDo.Type.UPCOMING_ASSIGNMENT -> {
                // Upcoming assignments can be either grading or submitting and we don't know, so they have no prefix
                title.text = item.title
                todoDetails = DateHelper.createPrefixedDateTimeString(context, R.string.dueAt, item.comparisonDate)
            }
            ToDo.Type.Grading -> {
                title.text = context.resources.getString(R.string.grade) + " " + item.title
                val count = item.needsGradingCount
                todoDetails = context.resources.getQuantityString(R.plurals.to_do_needs_grading, count, count)
            }
            ToDo.Type.UpcomingEvent -> {
                title.text = item.title
                todoDetails = item.scheduleItem!!.getStartToEndString(context)
            }
            null -> {}
        }

        description.setTextForVisibility(todoDetails)

        val drawableResId: Int = when {
            item.type == ToDo.Type.UpcomingEvent -> R.drawable.ic_calendar
            item.assignment?.quizId ?: 0 > 0 || item.quiz != null -> R.drawable.ic_quiz
            item.assignment!!.discussionTopicHeader != null -> R.drawable.ic_discussion
            else -> R.drawable.ic_assignment
        }
        val drawable = ColorKeeper.getColoredDrawable(context, drawableResId, iconColor)
        icon.setImageDrawable(drawable)
    }

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_todo
    }
}
