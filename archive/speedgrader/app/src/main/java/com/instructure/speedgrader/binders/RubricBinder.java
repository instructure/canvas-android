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

package com.instructure.speedgrader.binders;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import com.instructure.speedgrader.R;
import com.instructure.canvasapi.model.RubricCriterionRating;
import com.instructure.speedgrader.interfaces.RubricAdapterToFragmentCallback;
import com.instructure.speedgrader.util.Const;
import com.instructure.speedgrader.viewholders.BaseRubricViewHolder;
import com.instructure.speedgrader.viewholders.RubricCommentViewHolder;
import com.instructure.speedgrader.viewholders.RubricEmptyCommentViewHolder;
import com.instructure.speedgrader.viewholders.RubricEmptyFreeFormViewHolder;
import com.instructure.speedgrader.viewholders.RubricFreeFormViewHolder;
import com.instructure.speedgrader.viewholders.RubricPointsViewHolder;
import java.text.DecimalFormat;
import java.util.Locale;

public class RubricBinder extends BaseBinder {

    public static void bind(Context context, BaseRubricViewHolder holder, final RubricCriterionRating rubricRating, @Nullable RubricCriterionRating assessmentRating, RubricCriterionRating mCommentRowCache, final boolean isGrade, final RubricAdapterToFragmentCallback callback) {
        switch(holder.getRubricType()){
            case BaseRubricViewHolder.TYPE_ITEM_COMMENT:
                bindComments((RubricCommentViewHolder)holder, rubricRating, assessmentRating, mCommentRowCache, callback);
                break;
            case BaseRubricViewHolder.TYPE_ITEM_EMPTY_COMMENT:
                bindEmptyComments((RubricEmptyCommentViewHolder)holder, rubricRating, callback);
                break;
            case BaseRubricViewHolder.TYPE_ITEM_FREEFORM:
                bindFreeFormComments((RubricFreeFormViewHolder)holder, assessmentRating, mCommentRowCache,  callback);
                break;
            case BaseRubricViewHolder.TYPE_ITEM_EMPTY_FREEFORM:
                buildEmptyFreeFormRowView((RubricEmptyFreeFormViewHolder)holder, rubricRating, callback);
                break;
            case BaseRubricViewHolder.TYPE_ITEM_POINTS:
                bindPoints(context, (RubricPointsViewHolder) holder, rubricRating, isGrade, callback);
                break;
        }
    }

    private static void bindComments(final RubricCommentViewHolder viewHolder, final RubricCriterionRating rubricRating, RubricCriterionRating assessmentRating, final RubricCriterionRating commentsCache, final RubricAdapterToFragmentCallback callback) {
        if( !Const.NO_COMMENT.equals(commentsCache.getComments())){
            viewHolder.descriptionView.setText(commentsCache.getComments());
        }else if(!TextUtils.isEmpty(assessmentRating.getComments())){
            viewHolder.descriptionView.setText(assessmentRating.getComments());
        }else {
            viewHolder.descriptionView.setText(assessmentRating.getComments());
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onCommentRowClicked(rubricRating, viewHolder.getAdapterPosition());
            }
        });
    }

    private static void bindEmptyComments(final RubricEmptyCommentViewHolder viewHolder, final RubricCriterionRating criterionRating, final RubricAdapterToFragmentCallback callback) {

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onCommentRowClicked(criterionRating, viewHolder.getAdapterPosition());
            }
        });
        viewHolder.commentEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onCommentRowClicked(criterionRating, viewHolder.getAdapterPosition());
            }
        });
    }

    private static void bindPoints(Context context, final RubricPointsViewHolder viewHolder, final RubricCriterionRating criterionRating, final boolean isGrade, final RubricAdapterToFragmentCallback callback) {
        String points = "";
        double value = criterionRating.getPoints();
        if (Math.floor(value) == value) {
            points += (int)value;
        } else {
            points += value;
        }

        viewHolder.pointView.setText(points);
        viewHolder.pointView.setText(getScoreText(context, criterionRating, viewHolder.getRubricType()));

        if(isGrade){
            viewHolder.pointView.changeColor(getColorForRating(criterionRating, context));
            viewHolder.pointView.setTextColor(context.getResources().getColor(R.color.white));
        }else{
            viewHolder.pointView.changeColor(context.getResources().getColor(R.color.white));
            viewHolder.pointView.setTextColor(context.getResources().getColor(R.color.lightGray));
        }

        viewHolder.descriptionView.setText(criterionRating.getRatingDescription());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onRowClicked(criterionRating, viewHolder.getAdapterPosition());
            }
        });
    }

    private static void bindFreeFormComments(final RubricFreeFormViewHolder viewHolder, RubricCriterionRating assessmentRating, final RubricCriterionRating commentRatingCache, final RubricAdapterToFragmentCallback callback) {
        viewHolder.pointsPossible.setText(String.valueOf(commentRatingCache.getMaxPoints()));
        String points, comment;
        // If bindFreeFormComments is called, that means there exists either a cached comment, or an existing comment from the rubric assessment
        if(assessmentRating == null){
            points = String.valueOf(commentRatingCache.getPoints());
            comment = commentRatingCache.getComments();
        }else{
            //If an assessment rating exists as well as a comment cache, the cached value will take precedence over the older assessment value
            if(commentRatingCache.getPoints() != Integer.MIN_VALUE){
                points = String.valueOf(commentRatingCache.getPoints());
            }else{
                points = String.valueOf(assessmentRating.getPoints());
            }

            if(!Const.NO_COMMENT.equals(commentRatingCache.getComments())){
                comment = commentRatingCache.getComments();
            }else{
                comment = assessmentRating.getComments();
            }
        }
        viewHolder.pointsText.setText(points);
        viewHolder.commentText.setText(comment);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onCommentRowClicked(commentRatingCache, viewHolder.getAdapterPosition());
            }
        });
    }

    public static void buildEmptyFreeFormRowView(final RubricEmptyFreeFormViewHolder viewHolder, final RubricCriterionRating rating, final RubricAdapterToFragmentCallback callback) {

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onCommentRowClicked(rating, viewHolder.getAdapterPosition());
            }
        });
        viewHolder.commentEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onCommentRowClicked(rating, viewHolder.getAdapterPosition());
            }
        });

        viewHolder.pointsEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onFreeFormRowClicked(rating, viewHolder.getAdapterPosition());
            }
        });
    }

    private static String getScoreText(final Context context, final RubricCriterionRating rating, final int rubricType){
        final double value = rating.getPoints();
        final double maxValue = rating.getMaxPoints();

        String points = "";
        if(rating.isFreeFormComment()){
            DecimalFormat format = new DecimalFormat("0.#");
            points = String.format( Locale.getDefault(),
                    context.getString(R.string.freeFormRubricPoints),
                    format.format(value),
                    format.format(maxValue));
        }
        else if (Math.floor(value) == value) {
            points += (int)value;
        }
        else {
            points += value;
        }

        return points;
    }

    /**
     * Description : If the lowest score possible is selected, highlight that selection red. Max score is green. Median score is orange.
     * @param rating
     * @param context
     * @return
     */
    public static int getColorForRating(RubricCriterionRating rating, Context context){
        double maxScore    = rating.getMaxPoints();
        double ratingScore = rating.getPoints();
        double percentScore = ratingScore/maxScore * 100;
        if(percentScore >= 0 && percentScore <= 10){
            return context.getResources().getColor(R.color.rating_0_10);
        }else if(percentScore >= 11 && percentScore <= 20){
            return context.getResources().getColor(R.color.rating_11_20);
        }else if(percentScore >= 21 && percentScore <= 30){
            return context.getResources().getColor(R.color.rating_21_30);
        }else if(percentScore >= 31 && percentScore <= 40){
            return context.getResources().getColor(R.color.rating_31_40);
        }else if(percentScore >= 41 && percentScore <= 50){
            return context.getResources().getColor(R.color.rating_41_50);
        }else if(percentScore >= 51 && percentScore <= 60){
            return context.getResources().getColor(R.color.rating_51_60);
        }else if(percentScore >= 61 && percentScore <= 70){
            return context.getResources().getColor(R.color.rating_61_70);
        }else if(percentScore >= 71 && percentScore <= 80){
            return context.getResources().getColor(R.color.rating_71_80);
        }else if(percentScore >= 81 && percentScore <= 90){
            return context.getResources().getColor(R.color.rating_81_90);
        }else{
            return context.getResources().getColor(R.color.rating_91_100);
        }
    }
}
