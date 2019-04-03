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

package com.instructure.speedgrader.viewholders;

import android.view.View;
import android.widget.TextView;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.views.CircularTextView;

public class RubricPointsViewHolder extends BaseRubricViewHolder {

    public CircularTextView pointView;
    public TextView descriptionView;

    public RubricPointsViewHolder(View itemView) {
        super(itemView);
        pointView = (CircularTextView) itemView.findViewById(R.id.rubric_criterion_rating_points);
        descriptionView = (TextView) itemView.findViewById(R.id.rubric_criterion_rating_text);
    }
}