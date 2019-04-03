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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.instructure.parentapp.R;

public class CalendarWeekViewHolder extends RecyclerView.ViewHolder {

    public TextView title, description, status, date;
    public ImageView icon;
    public FrameLayout background;

    public CalendarWeekViewHolder(View itemView) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.title);
        date = (TextView) itemView.findViewById(R.id.date);
        description = (TextView) itemView.findViewById(R.id.description);
        status = (TextView) itemView.findViewById(R.id.status);
        icon = (ImageView) itemView.findViewById(R.id.icon);
        background = (FrameLayout) itemView.findViewById(R.id.iconWrapper);
    }

    public static int holderResId() {
        return R.layout.viewholder_card_week_item;
    }
}
