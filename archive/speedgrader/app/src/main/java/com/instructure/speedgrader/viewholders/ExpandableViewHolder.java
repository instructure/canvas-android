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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.instructure.speedgrader.R;

public class ExpandableViewHolder extends RecyclerView.ViewHolder {

    public TextView title;
    public ImageView expandCollapse;
    public RelativeLayout rootView;
    public boolean isExpanded;

    public ExpandableViewHolder(View itemView) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.title);
        expandCollapse = (ImageView) itemView.findViewById(R.id.expand_collapse);
        rootView = (RelativeLayout) itemView.findViewById(R.id.rootView);
    }


    public static int holderResId() {
        return R.layout.viewholder_header_expandable;
    }
}
