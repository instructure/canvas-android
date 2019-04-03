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
import android.widget.ImageView;
import android.widget.TextView;

import com.instructure.student.R;

public class ModuleHeaderViewHolder extends RecyclerView.ViewHolder {

    public TextView title;
    public ImageView moduleStatus;
    public ImageView expandCollapse;
    public View divider;
    public boolean isExpanded;

    public ModuleHeaderViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        moduleStatus = itemView.findViewById(R.id.moduleStatus);
        expandCollapse = itemView.findViewById(R.id.expandCollapse);
        divider = itemView.findViewById(R.id.divider);
    }

    public static int holderResId() {
        return R.layout.viewholder_header_module;
    }
}
