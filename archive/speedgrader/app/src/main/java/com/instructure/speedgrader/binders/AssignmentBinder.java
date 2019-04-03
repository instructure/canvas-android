/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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

package com.instructure.speedgrader.binders;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.AssignmentDueDate;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.NeedsGradingCount;
import com.instructure.canvasapi.model.Section;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.interfaces.AssignmentAdapterToFragmentCallback;
import com.instructure.speedgrader.viewholders.AssignmentViewHolder;

public class AssignmentBinder extends BaseBinder {

    public static void bind(Context context, AssignmentViewHolder holder, final Assignment assignment, final AssignmentAdapterToFragmentCallback callback) {
        holder.title.setText(assignment.getName());

        // get course color
        int color = CanvasContextColor.getCachedColor(context, CanvasContext.makeContextId(CanvasContext.Type.COURSE, assignment.getCourseId()));

        // Create the assignment icon and set it to the course color
        int drawable = 0;
        if(assignment.getSubmissionTypes().contains(Assignment.SUBMISSION_TYPE.ONLINE_QUIZ)) {
            drawable = R.drawable.ic_cv_quizzes;
        }
        else if(assignment.getSubmissionTypes().contains(Assignment.SUBMISSION_TYPE.DISCUSSION_TOPIC)) {
            drawable = R.drawable.ic_cv_discussions;
        }
        else {
            drawable = R.drawable.ic_cv_assignments;
        }

        Drawable d = CanvasContextColor.getColoredDrawable(context, drawable, color);
        holder.icon.setImageDrawable(d);

        // Get the needs grading count, default to the assignment's total needs grading count
        long needsGrading = assignment.getNeedsGradingCount();

        Section section = callback.getCurrentSection();
        // If we're filtering by a section other than All Sections
        if(assignment.getNeedsGradingCountBySection() != null && section.getId() != Integer.MIN_VALUE){
            needsGrading = 0; // The needs_grading_count_per_section api does not return a value for
            // sections with 0 submissions needing grading, set the default to 0
            for(NeedsGradingCount needsGradingCount : assignment.getNeedsGradingCountBySection()){
                if(needsGradingCount.getSectionId() == section.getId()){
                    needsGrading = needsGradingCount.getNeedsGradingCount();
                }
            }
        }

        if(needsGrading > 0){
            holder.badge.setVisibility(View.VISIBLE);
            if(needsGrading > 99){
                holder.badge.setText(context.getResources().getString(R.string.ninetyNinePlus));
            }else{
                holder.badge.setText(String.valueOf(needsGrading));
            }
        }else{
            holder.badge.setVisibility(View.INVISIBLE);
        }

        // Get the date
        String dateString = getDueDateString(context, assignment);

        holder.description.setText(dateString);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onRowClicked(assignment);
            }
        });
    }

    public static String getDueDateString(Context context, Assignment assignment){
        String dateString = "";
        int index = 0;
        if(assignment.getDueDates() != null){
            for(AssignmentDueDate dueDate : assignment.getDueDates()){
                if(dueDate.getDueDate() != null){
                    if(index == 0){
                        dateString += " " + DateHelpers.createPrefixedDateTimeString(context, context.getResources().getString(R.string.due), dueDate.getDueDate());
                    }else{
                        dateString += ", " + DateHelpers.getDateTimeString(context, dueDate.getDueDate());
                    }
                }
                index++;
            }
        }else if(assignment.getDueDates() == null && assignment.getDueDate() != null){
            dateString = DateHelpers.createPrefixedDateTimeString(context, context.getResources().getString(R.string.due), assignment.getDueDate());
        }

        if(dateString == "") {
            dateString = context.getResources().getString(R.string.noDueDate);
        }

        return dateString;
    }

}
