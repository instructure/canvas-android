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
package com.instructure.parentapp.holders;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.instructure.parentapp.R;

public class CourseViewHolder extends RecyclerView.ViewHolder {

    public TextView courseTitle;
    public TextView courseCode;
    public TextView gradeText;
    public TextView scoreText;
    public ImageView lockedGradeImage;
    public RelativeLayout gradeContainer;

    public CourseViewHolder(View itemView) {
        super(itemView);

        courseTitle = itemView.findViewById(R.id.courseTitle);
        courseCode = itemView.findViewById(R.id.courseCode);
        gradeText = itemView.findViewById(R.id.gradeText);
        scoreText = itemView.findViewById(R.id.scoreText);
        gradeContainer = itemView.findViewById(R.id.gradeContainer);
        lockedGradeImage = itemView.findViewById(R.id.lockedGradeImage);
    }

    public static int holderResId() {
        return R.layout.viewholder_course;
    }
}
