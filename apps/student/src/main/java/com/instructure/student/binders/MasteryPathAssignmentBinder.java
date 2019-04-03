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

package com.instructure.student.binders;

import android.content.Context;
import android.view.View;

import com.instructure.student.holders.MasteryAssignmentViewHolder;
import com.instructure.student.interfaces.AdapterToFragmentCallback;
import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.MasteryPathAssignment;
import com.instructure.pandautils.utils.ColorKeeper;


public class MasteryPathAssignmentBinder extends BaseBinder {

    public static void bind(
            Context context,
            final MasteryAssignmentViewHolder holder,
            final MasteryPathAssignment masteryPathAssignment,
            final int courseColor,
            final AdapterToFragmentCallback<Assignment> adapterToFragmentCallback) {

        holder.title.setText(masteryPathAssignment.getModel().getName());

        final int drawable = Companion.getAssignmentIcon(masteryPathAssignment.getModel());
        holder.icon.setImageDrawable(ColorKeeper.getColoredDrawable(context, drawable, courseColor));
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterToFragmentCallback.onRowClicked(masteryPathAssignment.getModel(), 0, true);
            }
        });
    }
}
