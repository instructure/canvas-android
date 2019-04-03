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

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import com.instructure.canvasapi.model.RubricCriterionRating;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.util.Const;

public abstract class  BaseRubricViewHolder extends RecyclerView.ViewHolder {

    public final static int TYPE_ITEM_POINTS         = 0;
    public final static int TYPE_ITEM_COMMENT        = 1;
    public final static int TYPE_ITEM_FREEFORM       = 2;
    public final static int TYPE_ITEM_EMPTY_FREEFORM = 3;
    public final static int TYPE_ITEM_EMPTY_COMMENT  = 4;

    public int rubricType;

    public BaseRubricViewHolder(View itemView) {
        super(itemView);
    }

    public int getRubricType(){
        return rubricType;
    }

    public static  BaseRubricViewHolder getRubricViewHolder(View v, int viewType) {
        switch (viewType){
            case TYPE_ITEM_COMMENT:
                return new RubricCommentViewHolder(v);
            case TYPE_ITEM_EMPTY_COMMENT:
                return new RubricEmptyCommentViewHolder(v);
            case TYPE_ITEM_FREEFORM:
                return new RubricFreeFormViewHolder(v);
            case TYPE_ITEM_EMPTY_FREEFORM:
                return new RubricEmptyFreeFormViewHolder(v);
            case TYPE_ITEM_POINTS:
                return new RubricPointsViewHolder(v);
        }
        return null;
    }

    public static int getChildLayoutResIds(int viewType) {
        switch(viewType){
            case TYPE_ITEM_COMMENT:
                return R.layout.viewholder_rubric_criterion_comment;
            case TYPE_ITEM_EMPTY_COMMENT:
                return R.layout.viewholder_rubric_empty_comment;
            case TYPE_ITEM_FREEFORM:
                return R.layout.viewholder_rubric_freeform_comment;
            case TYPE_ITEM_EMPTY_FREEFORM:
                return R.layout.viewholder_rubric_empty_freeform;
            case TYPE_ITEM_POINTS:
                return R.layout.viewholder_rubric_point;
        }
        return -1;
    }

    // It's important to differentiate between the RubricCriterionRatings from an Assignment Rubric and the RubricCriterionRatings
    // returned from a submission's RubricAssessment. RubricAssessment ratings will contain meta data such as isComment isRating,
    // where as an Assignment Rubric does not. For ungraded assignments, the assessmentRating will be null.
    public static int getChildType(RubricCriterionRating rubricRating, @Nullable RubricCriterionRating assessmentRating, RubricCriterionRating commentCache) {
        // Check if the rating is a freeform comment
        if(rubricRating.isFreeFormComment()) {
            // FreeForm Rubrics
            if (!Const.NO_COMMENT.equals(commentCache.getComments()) || assessmentRating != null) {
                return TYPE_ITEM_FREEFORM;
            }
            return TYPE_ITEM_EMPTY_FREEFORM;
        }
        else if(rubricRating.isComment()){
            // Comment Rows
            if(assessmentRating == null){
                return BaseRubricViewHolder.TYPE_ITEM_EMPTY_COMMENT;
            }
            else if(TextUtils.isEmpty(assessmentRating.getComments()) && commentCache.equals(Const.NO_COMMENT)){
                // If a rubric assessment for this rubric rating doesn't exist, and there is no unsaved
                // comment waiting to be saved. Return empycommentt
                return BaseRubricViewHolder.TYPE_ITEM_EMPTY_COMMENT;
            }
            else if(!TextUtils.isEmpty(assessmentRating.getComments()) && TextUtils.isEmpty(commentCache.getComments())){
                // It's possible the grader is attempting to delete a saved comment, so we check for both empty
                // comment cache as well as Const.NO_COMMENT
                return BaseRubricViewHolder.TYPE_ITEM_EMPTY_COMMENT;
            }else{
                // If an assessment comment exists, and No cached comments exist.
                return BaseRubricViewHolder.TYPE_ITEM_COMMENT;
            }
        }

        return TYPE_ITEM_POINTS;
    }
}