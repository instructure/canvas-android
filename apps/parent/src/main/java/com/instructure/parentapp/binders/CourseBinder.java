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
package com.instructure.parentapp.binders;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.CourseGrade;
import com.instructure.canvasapi2.utils.NumberHelper;
import com.instructure.pandautils.utils.ColorKeeper;
import com.instructure.pandautils.utils.Utils;
import com.instructure.parentapp.BuildConfig;
import com.instructure.parentapp.R;
import com.instructure.parentapp.holders.CourseViewHolder;
import com.instructure.parentapp.interfaces.BasicAdapterToFragmentCallback;
import com.instructure.parentapp.util.ParentPrefs;

public class CourseBinder extends BaseBinder {

    public static void bind(
            final CourseViewHolder holder,
            final Course course,
            final Context context,
            final BasicAdapterToFragmentCallback adapterToFragmentCallback) {
        holder.courseTitle.setText(course.getName());
        Utils.testSafeContentDescription(holder.courseTitle,
                String.format(context.getString(R.string.course_title_content_desc), holder.getAdapterPosition()),
                course.getName(),
                BuildConfig.IS_TESTING);
        holder.courseCode.setText(course.getCourseCode());

        holder.gradeContainer.setVisibility(View.VISIBLE);
        holder.scoreText.setVisibility(View.VISIBLE);
        holder.gradeText.setVisibility(View.VISIBLE);

        holder.gradeText.setTextColor(ParentPrefs.getCurrentColor());
        holder.scoreText.setTextColor(ParentPrefs.getCurrentColor());

        CourseGrade courseGrade = course.getCourseGrade(false);
        holder.lockedGradeImage.setVisibility(View.GONE);

        if (courseGrade != null) {
            if (courseGrade.isLocked()) {
                holder.gradeContainer.setVisibility(View.GONE);
                holder.lockedGradeImage.setVisibility(View.VISIBLE);
                holder.lockedGradeImage.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.vd_lock, ParentPrefs.getCurrentColor()));
            } else {
                setGradeView(holder.gradeText, holder.scoreText, course.getCourseGrade(false), context, holder.getAdapterPosition());
            }
        } else {
            setGradeView(holder.gradeText, holder.scoreText, null, context, holder.getAdapterPosition());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterToFragmentCallback.onRowClicked(holder.getAdapterPosition(), false);
            }
        });
    }

    private static void setGradeView(TextView gradeTextView, TextView scoreTextView, CourseGrade courseGrade, Context context, int adapterPosition) {
        if(courseGrade == null || courseGrade.getNoCurrentGrade()) {
            gradeTextView.setText(R.string.noGradeText);
            scoreTextView.setVisibility(View.GONE);
        } else {
            Double score = courseGrade.getCurrentScore();
            String scoreString = NumberHelper.INSTANCE.doubleToPercentage(score, 2);

            gradeTextView.setText((courseGrade.hasCurrentGradeString()) ? courseGrade.getCurrentGrade() : "");
            scoreTextView.setText((scoreString));
            Utils.testSafeContentDescription(gradeTextView,
                    String.format(context.getString(R.string.grade_text_content_desc), adapterPosition),
                    courseGrade.getCurrentGrade(),
                    BuildConfig.IS_TESTING);
            Utils.testSafeContentDescription(scoreTextView,
                    String.format(context.getString(R.string.score_text_content_desc), adapterPosition),
                    scoreString + "%",
                    BuildConfig.IS_TESTING);
        }
    }
}
