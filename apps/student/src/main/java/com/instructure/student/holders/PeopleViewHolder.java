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

import de.hdodenhof.circleimageview.CircleImageView;

public class PeopleViewHolder extends RecyclerView.ViewHolder {

    public TextView title, role;
    public CircleImageView icon;
    public View shadowBottom, shadowTop;

    public PeopleViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        role = itemView.findViewById(R.id.role);
        icon = itemView.findViewById(R.id.icon);
        shadowBottom = itemView.findViewById(R.id.shadowBottom);
        shadowTop = itemView.findViewById(R.id.shadowTop);
    }

    public static int holderResId(){
        return R.layout.viewholder_people;
    }
}
