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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.views.CircularTextView;
import com.instructure.speedgrader.views.HelveticaTextView;

public class AssignmentViewHolder extends RecyclerView.ViewHolder {

    public HelveticaTextView title, description, date;
    public ImageView icon;
    public CircularTextView badge;

    public AssignmentViewHolder(View itemView) {
        super(itemView);
        badge = (CircularTextView) itemView.findViewById(R.id.badge);
        title = (HelveticaTextView) itemView.findViewById(R.id.title);
        description = (HelveticaTextView) itemView.findViewById(R.id.description);
        date = (HelveticaTextView) itemView.findViewById(R.id.date);
        icon = (ImageView) itemView.findViewById(R.id.icon);
    }

    public static int holderResId() {
        return R.layout.listview_item_row_assignments;
    }
}
