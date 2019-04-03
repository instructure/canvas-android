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
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;

import com.instructure.student.R;
import com.instructure.student.holders.ModuleSubHeaderViewHolder;
import com.instructure.student.holders.ModuleViewHolder;
import com.instructure.student.interfaces.ModuleAdapterToFragmentCallback;
import com.instructure.student.util.ModuleUtility;
import com.instructure.canvasapi2.models.ModuleContentDetails;
import com.instructure.canvasapi2.models.ModuleItem;
import com.instructure.canvasapi2.models.ModuleObject;
import com.instructure.canvasapi2.utils.DateHelper;
import com.instructure.pandautils.utils.ColorKeeper;

public class ModuleBinder extends BaseBinder {

    public static void bind(
            final ModuleViewHolder holder,
            final ModuleObject moduleObject,
            final ModuleItem moduleItem,
            final Context context,
            final ModuleAdapterToFragmentCallback adapterToFragmentCallback,
            final boolean isSequentiallEnabled,
            final int courseColor,
            final boolean isFirstItem,
            final boolean isLastItem) {

        boolean isLocked = ModuleUtility.isGroupLocked(moduleObject);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterToFragmentCallback.onRowClicked(moduleObject, moduleItem, holder.getAdapterPosition(), true);
            }
        });

        // Title
        holder.title.setText(moduleItem.getTitle());

        if(ModuleItem.Type.Locked.toString().equalsIgnoreCase(moduleItem.getType()) || ModuleItem.Type.ChooseAssignmentGroup.toString().equalsIgnoreCase(moduleItem.getType())) {
            holder.title.setTypeface(null, Typeface.ITALIC);
            holder.title.setTextColor(context.getResources().getColor(R.color.secondaryText));
        } else {
            holder.title.setTypeface(null, Typeface.NORMAL);
            holder.title.setTextColor(context.getResources().getColor(R.color.primaryText));
        }
        // Description
        if (moduleItem.getCompletionRequirement() != null && moduleItem.getCompletionRequirement().getType() != null) {
            Companion.setVisible(holder.description);
            holder.description.setTextColor(context.getResources().getColor(R.color.canvasTextMedium));
            String requireText = moduleItem.getCompletionRequirement().getType();
            if (ModuleObject.State.MustSubmit.getApiString().equalsIgnoreCase(requireText)) {
                if(moduleItem.getCompletionRequirement().getCompleted()){
                    holder.description.setText(context.getString(R.string.moduleItemSubmitted));
                    holder.description.setTextColor(courseColor);
                }else{
                    holder.description.setText(context.getString(R.string.moduleItemSubmit));
                }
            } else if (ModuleObject.State.MustView.getApiString().equalsIgnoreCase(requireText)) {
                if(moduleItem.getCompletionRequirement().getCompleted()){
                    holder.description.setText(context.getString(R.string.moduleItemViewed));
                }else{
                    holder.description.setText(context.getString(R.string.moduleItemMustView));
                }
            } else if (ModuleObject.State.MustContribute.getApiString().equalsIgnoreCase(requireText)) {
                if(moduleItem.getCompletionRequirement().getCompleted()){
                    holder.description.setText(context.getString(R.string.moduleItemContributed));
                }else{
                    holder.description.setText(context.getString(R.string.moduleItemContribute));
                }
            }
            // MinScore only present when type == 'min_score'
            else if (ModuleObject.State.MinScore.getApiString().equalsIgnoreCase(requireText)) {
                if(moduleItem.getCompletionRequirement().getCompleted()){
                    holder.description.setText(context.getString(R.string.moduleItemMinScoreMet));
                }else{
                    holder.description.setText(context.getString(R.string.moduleItemMinScore) + " " + moduleItem.getCompletionRequirement().getMinScore());
                }
            } else {
                holder.description.setText("");
                Companion.setGone(holder.description);
            }

        } else {
            holder.description.setText("");
            Companion.setGone(holder.description);
        }

        // Indicator
        Companion.setGone(holder.indicator);
        if (moduleItem.getCompletionRequirement() != null && moduleItem.getCompletionRequirement().getCompleted()) {
            Drawable drawable = ColorKeeper.getColoredDrawable(context, R.drawable.vd_check_white_24dp, courseColor);
            holder.indicator.setImageDrawable(drawable);
            Companion.setVisible(holder.indicator);
        }

        if(isLocked) {
            Drawable drawable = ColorKeeper.getColoredDrawable(context, R.drawable.vd_lock, courseColor);
            holder.indicator.setImageDrawable(drawable);
            Companion.setVisible(holder.indicator);
        }

        // Icon
        int drawableResource = -1;
        if (ModuleItem.Type.Assignment.toString().equalsIgnoreCase(moduleItem.getType())) {
            drawableResource = R.drawable.vd_assignment;
        } else if (ModuleItem.Type.Discussion.toString().equalsIgnoreCase(moduleItem.getType())) {
            drawableResource = R.drawable.vd_discussion;
        } else if (ModuleItem.Type.File.toString().equalsIgnoreCase(moduleItem.getType())) {
            drawableResource = R.drawable.vd_download;
        } else if (ModuleItem.Type.Page.toString().equalsIgnoreCase(moduleItem.getType())) {
            drawableResource = R.drawable.vd_pages;
        } else if (ModuleItem.Type.SubHeader.toString().equalsIgnoreCase(moduleItem.getType())) {
            Companion.setGone(holder.icon);
        } else if (ModuleItem.Type.Quiz.toString().equalsIgnoreCase(moduleItem.getType())) {
            drawableResource = R.drawable.vd_quiz;
        } else if (ModuleItem.Type.ExternalUrl.toString().equalsIgnoreCase(moduleItem.getType())) {
            drawableResource = R.drawable.vd_link;
        } else if (ModuleItem.Type.ExternalTool.toString().equalsIgnoreCase(moduleItem.getType())) {
            drawableResource = R.drawable.vd_lti;
        } else if (ModuleItem.Type.Locked.toString().equalsIgnoreCase(moduleItem.getType())) {
            drawableResource = R.drawable.vd_lock;
        } else if (ModuleItem.Type.ChooseAssignmentGroup.toString().equalsIgnoreCase(moduleItem.getType())) {
            drawableResource = R.drawable.vd_pages;
        }

        if(drawableResource == -1) {
            Companion.setGone(holder.icon);
        } else {
            Drawable drawable = ColorKeeper.getColoredDrawable(context, drawableResource, courseColor);
            holder.icon.setImageDrawable(drawable);
        }

        // Details
        ModuleContentDetails details = moduleItem.getModuleDetails();
        if(details != null) {
            boolean hasDate, hasPoints;
            if (details.getDueDate() != null) {
                holder.date.setText(DateHelper.createPrefixedDateTimeString(context, R.string.toDoDue, details.getDueDate()));
                hasDate = true;
            } else {
                holder.date.setText("");
                hasDate = false;
            }

            String points = details.getPointsPossible();
            if(!TextUtils.isEmpty(points)) {
                holder.points.setText(context.getString(R.string.totalPoints, com.instructure.canvasapi2.utils.NumberHelper.INSTANCE.formatDecimal(Double.parseDouble(points), 2, true)));
                hasPoints = true;
            } else {
                holder.points.setText("");
                hasPoints = false;
            }

            if(!hasDate && !hasPoints) {
                Companion.setGone(holder.date);
                Companion.setGone(holder.points);
            } else {
                if(hasDate) Companion.setVisible(holder.date); else Companion.setInvisible(holder.date);
                if(hasPoints) Companion.setVisible(holder.points); else Companion.setInvisible(holder.points);
            }
        } else {
            holder.points.setText("");
            holder.date.setText("");
            Companion.setGone(holder.date);
            Companion.setGone(holder.points);
        }

        Companion.updateShadows(isFirstItem, isLastItem, holder.shadowTop, holder.shadowBottom);
    }

    public static void bindSubHeader(
            final ModuleSubHeaderViewHolder holder,
            final ModuleObject moduleObject,
            final ModuleItem moduleItem,
            final boolean isFirstItem,
            final boolean isLastItem) {

        if(ModuleItem.Type.SubHeader.toString().equalsIgnoreCase(moduleItem.getType())) {
            holder.subTitle.setText(moduleItem.getTitle());
        }

        Companion.updateShadows(isFirstItem, isLastItem, holder.shadowTop, holder.shadowBottom);
    }
}
