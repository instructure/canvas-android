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

package com.instructure.student.binders;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import com.instructure.student.R;
import com.instructure.student.adapter.TodoListRecyclerAdapter;
import com.instructure.student.holders.TodoViewHolder;
import com.instructure.student.interfaces.NotificationAdapterToFragmentCallback;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.ToDo;
import com.instructure.canvasapi2.utils.DateHelper;
import com.instructure.pandautils.utils.ColorKeeper;

public class TodoBinder extends BaseBinder {

    public static void bind(
            final Context context,
            final TodoViewHolder holder,
            final ToDo item,
            final NotificationAdapterToFragmentCallback<ToDo> adapterToFragmentCallback,
            final TodoListRecyclerAdapter.TodoCheckboxCallback checkboxCallback) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkboxCallback.isEditMode()){
                    checkboxCallback.onCheckChanged(item, !item.isChecked(), holder.getAdapterPosition());
                } else {
                    adapterToFragmentCallback.onRowClicked(item, holder.getAdapterPosition(), true);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(item.getIgnore() == null){
                    return false;
                }
                checkboxCallback.onCheckChanged(item, !item.isChecked(), holder.getAdapterPosition());
                return true;
            }
        });

        if(item.getCanvasContext() != null && item.getCanvasContext().getName() != null) {
            holder.getCourse().setText(item.getCanvasContext().getName());
            holder.getCourse().setTextColor(ColorKeeper.getOrGenerateColor(item.getCanvasContext()));
        } else if (item.getScheduleItem() != null && item.getScheduleItem().getContextType() == CanvasContext.Type.USER) {
            holder.getCourse().setText(context.getString(R.string.PersonalCalendar));
            holder.getCourse().setTextColor(ColorKeeper.getOrGenerateColor(item.getCanvasContext()));
        } else {
            holder.getCourse().setText("");
        }

        //Get courseColor
        int courseColor = context.getResources().getColor(R.color.defaultPrimary);

        if(item.getCanvasContext() != null && item.getCanvasContext().getType() != CanvasContext.Type.USER) {
            courseColor = ColorKeeper.getOrGenerateColor(item.getCanvasContext());
        }

        if(item.isChecked()) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.lightGray));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.canvasBackgroundWhite));
        }

        String todoDetails = "";
        String titlePrefix = "";
        switch (item.getType()) {
            case Submitting:
                titlePrefix = context.getString(R.string.toDoTurnIn) + " ";
                if(item.getQuiz() != null) {
                    holder.getTitle().setText(titlePrefix + item.getTitle());
                    todoDetails = DateHelper.createPrefixedDateTimeString(context, R.string.dueAt, item.getComparisonDate());
                    break;
                }
                // don't break, just continue
            case UPCOMING_ASSIGNMENT:
                // upcoming assignments can be either grading or submitting and we don't know, so they have no prefix;
                holder.getTitle().setText(titlePrefix + item.getTitle());
                todoDetails = DateHelper.createPrefixedDateTimeString(context, R.string.dueAt, item.getComparisonDate());
                break;
            case Grading:
                holder.getTitle().setText(context.getResources().getString(R.string.grade) + " " + item.getTitle());
                int count = item.getNeedsGradingCount();
                todoDetails = context.getResources().getQuantityString(R.plurals.to_do_needs_grading, count, count);
                break;
            case UpcomingEvent:
                holder.getTitle().setText(item.getTitle());
                todoDetails = item.getScheduleItem().getStartToEndString(context);
                break;
            default:
                break;
        }

        if(!TextUtils.isEmpty(todoDetails)) {
            holder.getDescription().setText(todoDetails);
            Companion.setVisible(holder.getDescription());
        } else {
            holder.getDescription().setText("");
            Companion.setGone(holder.getDescription());
        }

        int drawableResId;

        if(item.getType() == ToDo.Type.UpcomingEvent) {
            drawableResId = R.drawable.vd_calendar;
        } else if ((item.getAssignment() != null && item.getAssignment().getQuizId() > 0) || item.getQuiz() != null) {
            drawableResId = R.drawable.vd_quiz;
        } else if (item.getAssignment().getDiscussionTopicHeader() != null) {
            drawableResId = R.drawable.vd_discussion;
        } else {
            drawableResId = R.drawable.vd_assignment;
        }

        Drawable drawable = ColorKeeper.getColoredDrawable(context, drawableResId, courseColor);
        holder.getIcon().setImageDrawable(drawable);

    }
}
