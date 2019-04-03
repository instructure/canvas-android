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

import com.instructure.student.R;
import com.instructure.student.holders.RubricTopHeaderViewHolder;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.pandautils.utils.ThemePrefs;

public class RubricTopHeaderBinder extends BaseBinder {
    public static void bind(Context context, CanvasContext canvasContext, RubricTopHeaderViewHolder holder, String points, String grade, String latePenalty, String finalGrade, boolean isMuted) {
        if(isMuted){
            holder.mutedText.setText(context.getString(R.string.mutedText));
        } else {
            holder.gradeText.setText(grade);
            holder.pointsText.setText(points);

            if(latePenalty == null || finalGrade == null) {
                holder.latePolicy.setVisibility(View.GONE);
            } else {
                holder.latePolicy.setVisibility(View.VISIBLE);
                holder.latePenalty.setText(latePenalty);
                holder.latePenalty.setTextColor(ThemePrefs.getBrandColor());
                holder.finalGrade.setText(finalGrade);
            }
        }
        Companion.ifHasTextSetVisibleElseGone(holder.gradeText);
        Companion.ifHasTextSetVisibleElseGone(holder.pointsText);
        Companion.ifHasTextSetVisibleElseGone(holder.mutedText);

    }
}
