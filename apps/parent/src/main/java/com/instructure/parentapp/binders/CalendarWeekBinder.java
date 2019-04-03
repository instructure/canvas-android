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
 *
 */
package com.instructure.parentapp.binders;

import android.content.Context;
import android.view.View;

import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.ScheduleItem;
import com.instructure.canvasapi2.utils.DateHelper;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.pandautils.utils.AssignmentUtils2;
import com.instructure.pandautils.utils.ColorKeeper;
import com.instructure.parentapp.BuildConfig;
import com.instructure.parentapp.R;
import com.instructure.parentapp.holders.CalendarWeekViewHolder;
import com.instructure.parentapp.interfaces.AdapterToFragmentCallback;
import com.instructure.parentapp.util.ViewUtils;

import java.util.Map;

public class CalendarWeekBinder extends BaseBinder {

    public static void bind(
            final Context context,
            final CalendarWeekViewHolder holder,
            final int courseColor,
            final ScheduleItem item,
            final AdapterToFragmentCallback<ScheduleItem> adapterToFragmentCallback,
            final Map<Long, Course> courseMap) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterToFragmentCallback.onRowClicked(item, holder.getAdapterPosition(), false);
            }
        });

        if(item.hasAssignmentOverrides()) {
            String title = String.format(context.getResources().getString(R.string.week_assignment_override_title),
                    item.getTitle(), item.getAssignmentOverrides().get(0).getTitle());
            holder.title.setText(title);
        } else {
            holder.title.setText(item.getTitle());
        }

        switch (item.getItemType()) {

            case TYPE_CALENDAR:
            case TYPE_ASSIGNMENT:

                holder.title.setText(item.getTitle());
                Assignment assignment = item.getAssignment();
                //need to reset drawables
                holder.icon.setImageDrawable(null);
                holder.status.setBackgroundDrawable(null);
                holder.background.setBackgroundDrawable(null);
                setGone(holder.date);
                setVisible(holder.status);
                int drawable;

                if(assignment != null) {
                    int assignmentState = AssignmentUtils2.INSTANCE.getAssignmentState(assignment, assignment.getSubmission());

                    holder.icon.setContentDescription(context.getString(R.string.assignment));
                    switch(assignmentState){
                        //Looks like: Submitted 95%, Color - Light blue
                        case(AssignmentUtils2.ASSIGNMENT_STATE_GRADED):
                            String gradeText = ViewUtils.getGradeText(AssignmentUtils2.ASSIGNMENT_STATE_GRADED,
                                    assignment.getSubmission().getScore(),
                                    assignment.getPointsPossible(), context);
                            holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_done_white_18dp));
                            holder.status.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.assignment_graded_background));
                            holder.background.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.circle_background));
                            holder.status.setText(gradeText);
                            break;
                        //Looks like: Late 95%, Color - Orange-ish yellow
                        case(AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE):
                            String lateGradeText = ViewUtils.getGradeText(AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE,
                                    assignment.getSubmission().getScore(),
                                    assignment.getPointsPossible(), context);
                            holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_done_white_18dp));
                            holder.status.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.late_assignment_background));
                            holder.background.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.orange_circle_background));
                            holder.status.setText(lateGradeText);
                            break;
                        // Looks like: Missing 95%, Color - Red!
                        case(AssignmentUtils2.ASSIGNMENT_STATE_GRADED_MISSING):
                            String missingGradeText = ViewUtils.getGradeText(assignmentState,
                                    assignment.getSubmission().getScore(),
                                    assignment.getPointsPossible(), context);
                            holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_warning_white_18dp));
                            holder.date.setVisibility(View.GONE);
                            holder.status.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.submission_missing_background));
                            holder.background.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.red_circle_background));
                            holder.status.setText(missingGradeText);
                            break;
                        //Looks like: Submitted, Color - Light blue
                        case(AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED):
                            holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_done_white_18dp));
                            holder.status.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.assignment_submitted_background));
                            holder.background.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.light_blue_circle_background));
                            holder.status.setText(context.getString(R.string.submitted));
                            break;
                        //Looks like: Late, Color - Orange-ish yellow
                        case(AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE):
                            holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_done_white_18dp));
                            holder.status.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.late_assignment_background));
                            holder.background.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.orange_circle_background));
                            holder.status.setText(context.getString(R.string.late));
                            break;
                        //Looks like: Missing, Color - Red!
                        case(AssignmentUtils2.ASSIGNMENT_STATE_MISSING):
                            holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_warning_white_18dp));
                            holder.date.setVisibility(View.GONE);
                            holder.status.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.submission_missing_background));
                            holder.background.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.red_circle_background));
                            holder.status.setText(context.getString(R.string.missing));
                            break;
                        //Looks like: no status, assignment icon/grey
                        case(AssignmentUtils2.ASSIGNMENT_STATE_DUE):
                            //match assignment type icon
                            drawable = ViewUtils.getAssignmentIcon(assignment);
                            holder.icon.setImageDrawable(ColorKeeper.getColoredDrawable(context, drawable, context.getResources().getColor(R.color.gray)));
                            holder.background.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.read_circle_background));
                            setGone(holder.status);
                            break;
                        //Looks like: Excused, color, light blue
                        case(AssignmentUtils2.ASSIGNMENT_STATE_EXCUSED):
                            holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_done_white_18dp));
                            holder.status.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.assignment_graded_background));
                            holder.background.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.circle_background));
                            holder.status.setText(context.getString(R.string.excused));
                            break;
                        //Looks like: In-Class, color, light green
                        case(AssignmentUtils2.ASSIGNMENT_STATE_IN_CLASS):
                            drawable = ViewUtils.getAssignmentIcon(assignment);
                            holder.icon.setImageDrawable(ColorKeeper.getColoredDrawable(context, drawable, context.getResources().getColor(R.color.gray)));
                            holder.status.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.assignment_in_class_background));
                            holder.background.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.read_circle_background));
                            holder.status.setText(context.getString(R.string.in_class));
                    }

                    setCleanText(holder.description, getCourseNameById(courseMap, item.getAssignment().getCourseId()));

                } else {
                    //Calendar Event
                    holder.icon.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.vd_calendar_month, context.getResources().getColor(R.color.white)));
                    holder.icon.setContentDescription(context.getString(R.string.calendar_event));
                    holder.background.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.circle_background));
                    holder.title.setText(item.getTitle());

                    holder.date.setVisibility(View.VISIBLE);
                    holder.date.setText(getStartString(context, item));

                    setGone(holder.status);
                    setCleanText(holder.description, getCourseNameById(courseMap, item.getCourseId()));
                }
                //We want to set this at the end so we get the a11y values
                com.instructure.pandautils.utils.Utils.testSafeContentDescription(holder.title,
                        String.format(context.getString(R.string.calendar_title_content_desc), holder.getAdapterPosition()),
                        holder.title.getText().toString(),
                        BuildConfig.IS_TESTING);
                com.instructure.pandautils.utils.Utils.testSafeContentDescription(holder.description,
                        String.format(context.getString(R.string.description_text_content_desc), holder.getAdapterPosition()),
                        holder.description.getText().toString(),
                        BuildConfig.IS_TESTING);
                com.instructure.pandautils.utils.Utils.testSafeContentDescription(holder.status,
                        String.format(context.getString(R.string.status_text_content_desc), holder.getAdapterPosition()),
                        holder.status.getText().toString(),
                        BuildConfig.IS_TESTING);

                break;
            default:
                Logger.d("UNSUPPORTED TYPE FOUND IN SYLLABUS BINDER");
                break;
        }
    }

    public static String getCourseNameById(final Map<Long, Course> courseMap, long id) {
        if(courseMap.containsKey(id)) {
            return courseMap.get(id).getName();
        }
        return "";
    }

    public static Course getCourseById(final Map<Long, Course> courseMap, long id) {
        if(courseMap.containsKey(id)) {
            return courseMap.get(id);
        }
        return null;
    }

    private static String getStartString(Context context, ScheduleItem item) {
        if (item.isAllDay()) {
            return context.getString(R.string.allDayEvent);
        }
        if (item.getStartAt() != null) {
            return DateHelper.createPrefixedDateString(context, R.string.Starts, item.getStartDate());
        }
        return "";
    }
}
