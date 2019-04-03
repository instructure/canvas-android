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
import android.text.format.DateUtils;
import android.view.View;

import com.instructure.pandarecycler.interfaces.ViewHolderHeaderClicked;
import com.instructure.parentapp.holders.ExpandableViewHolder;

import java.util.Calendar;
import java.util.Date;

public class ExpandableHeaderBinder extends BaseBinder {
    public static <MODEL> void bind(
            final Context context,
            final ExpandableViewHolder holder,
            final MODEL genericHeader,
            final Date date,
            boolean isExpanded,
            final ViewHolderHeaderClicked<MODEL> viewHolderHeaderClicked) {

        Calendar cal = Calendar.getInstance(); // locale-specific
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long time = cal.getTimeInMillis();
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_WEEKDAY;
        String headerText = DateUtils.formatDateTime(context, time, flags);

        holder.title.setText(headerText);
        holder.isExpanded = isExpanded;

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolderHeaderClicked.viewClicked(v, genericHeader);

                holder.isExpanded = !holder.isExpanded;

            }
        });
    }
}
