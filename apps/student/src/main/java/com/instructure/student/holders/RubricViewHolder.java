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

package com.instructure.student.holders;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.instructure.student.R;

public class RubricViewHolder extends RecyclerView.ViewHolder {
    public final static int TYPE_ITEM_POINTS = 0;
    public final static int TYPE_ITEM_COMMENT = 1;

    public TextView pointView;
    public TextView descriptionView;
    public View checkmark;
    public int rubricType;

    public RubricViewHolder(View itemView) {
        super(itemView);
    }

    public RubricViewHolder(View itemView, int rubricType) {
        super(itemView);
        pointView = (TextView) itemView.findViewById(R.id.rubric_criterion_rating_points);
        descriptionView = (TextView) itemView.findViewById(R.id.rubric_criterion_rating_text);
        this.rubricType = rubricType;
        if (rubricType == TYPE_ITEM_POINTS) {
            checkmark = itemView.findViewById(R.id.checkMark);
        }
    }

    public static int holderResId(int rubricType) {
        switch (rubricType) {
            case TYPE_ITEM_COMMENT:
                return R.layout.viewholder_rubric_criterion_rating_comment;
            default:
                return R.layout.viewholder_rubric_criterion_rating_grade;
        }
    }
}
