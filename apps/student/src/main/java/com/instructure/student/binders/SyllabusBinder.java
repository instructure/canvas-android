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
import android.text.TextUtils;
import android.view.View;

import com.instructure.student.R;
import com.instructure.student.holders.SyllabusItemViewHolder;
import com.instructure.student.holders.SyllabusViewHolder;
import com.instructure.student.interfaces.AdapterToFragmentCallback;
import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.ScheduleItem;
import com.instructure.canvasapi2.utils.DateHelper;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.pandautils.utils.ColorKeeper;

import java.util.Date;

public class SyllabusBinder extends BaseBinder {

    public static void bind(
            final Context context,
            final SyllabusViewHolder holder,
            final int courseColor,
            final ScheduleItem item,
            final AdapterToFragmentCallback<ScheduleItem> adapterToFragmentCallback) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterToFragmentCallback.onRowClicked(item, holder.getAdapterPosition(), false);
            }
        });

        switch (item.getItemType()) {

            case TYPE_CALENDAR:
            case TYPE_ASSIGNMENT:

                holder.title.setText(item.getTitle());

                Drawable drawable;
                Assignment assignment = item.getAssignment();

                if(assignment != null) {
                    int drawableResId = Companion.getAssignmentIcon(assignment);
                    drawable = ColorKeeper.getColoredDrawable(context, drawableResId, courseColor);
                    holder.icon.setImageDrawable(drawable);

                    Date dueDate = assignment.getDueDate();
                    holder.date.setTextColor(context.getResources().getColor(R.color.secondaryText));
                    if(dueDate != null) {
                        String dateString = DateHelper.createPrefixedDateTimeString(context, R.string.toDoDue, dueDate);
                        holder.date.setText(dateString);
                    } else {
                        holder.date.setText(context.getResources().getString(R.string.toDoNoDueDate));
                    }

                    String description = Companion.getHtmlAsText(assignment.getDescription());
                    Companion.setCleanText(holder.description, description);
                    if(TextUtils.isEmpty(description)) holder.description.setVisibility(View.GONE);
                    else holder.description.setVisibility(View.VISIBLE);

                    //currently submissions aren't returned for the syllabus fragment, so points will be null.
                    Companion.setGone(holder.points);

                } else {

                    drawable = ColorKeeper.getColoredDrawable(context, R.drawable.vd_calendar, courseColor);
                    holder.icon.setImageDrawable(drawable);

                    Companion.setCleanText(holder.date, item.getStartDateString(context));

                    String description = Companion.getHtmlAsText(item.getDescription());
                    Companion.setCleanText(holder.description, description);
                    if(TextUtils.isEmpty(description)) holder.description.setVisibility(View.GONE);
                    else holder.description.setVisibility(View.VISIBLE);

                    holder.points.setText("");
                }

                break;
            default:
                Logger.d("UNSUPPORTED Type FOUND IN SYLLABUS BINDER");
                break;
        }
    }

    public static void bindSyllabusItem(
            final Context context,
            final SyllabusItemViewHolder holder,
            final int courseColor,
            final ScheduleItem item,
            final AdapterToFragmentCallback<ScheduleItem> adapterToFragmentCallback) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterToFragmentCallback.onRowClicked(item, holder.getAdapterPosition(), false);
            }
        });

        holder.title.setText(context.getString(R.string.syllabus));
        Drawable drawable = ColorKeeper.getColoredDrawable(context, R.drawable.vd_syllabus, courseColor);
        holder.icon.setImageDrawable(drawable);
    }
}

