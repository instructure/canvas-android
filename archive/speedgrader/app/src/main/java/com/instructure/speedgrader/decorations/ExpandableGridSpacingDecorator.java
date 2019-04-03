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

package com.instructure.speedgrader.decorations;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.instructure.pandarecycler.BaseExpandableRecyclerAdapter;

public class ExpandableGridSpacingDecorator extends RecyclerView.ItemDecoration {

    private static final int NONE = 0;
    private static final int LEFT = 1;
    private static final int RIGHT = 2;
    private static final int TOP = 3;
    private static final int BOTTOM = 4;

    private int spacing = 0;
    private int halfSpacing = 0;

    public ExpandableGridSpacingDecorator(int spacing) {
        this.spacing = spacing;
        this.halfSpacing = spacing / 2;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        final BaseExpandableRecyclerAdapter adapter = (BaseExpandableRecyclerAdapter)parent.getAdapter();
        final int position = parent.getChildAdapterPosition(view);
        final boolean isHeader = adapter.isPositionGroupHeader(position);
        final RecyclerView.ViewHolder viewHolder = parent.getChildViewHolder(view);

        if (position == RecyclerView.NO_POSITION) {
            // If this ItemDecoration does not affect the positioning of item views,
            AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
            anim.setDuration(175);
            viewHolder.itemView.startAnimation(anim);
            viewHolder.itemView.setVisibility(View.GONE);
            return;
        } else {
            viewHolder.itemView.setVisibility(View.VISIBLE);
        }

        if(isHeader) {
            return;
        }


        final int spanCount = getTotalSpan(parent);
        final int pseudoGroupPosition = adapter.getGroupVisualPosition(position);//the visual position
        final int rows = ((adapter.getGroupItemCount(adapter.getGroup(pseudoGroupPosition)) - 1) / spanCount) + 1;
        final int leftRightEdge = getIsEdgeType(spanCount, position, pseudoGroupPosition);
        final int topBottomEdge = getIsTopBottomEdgeType(spanCount, rows, position, pseudoGroupPosition);

        if(spanCount == 1) {
            outRect.left = spacing;
            outRect.right = spacing;
        } else {
            if (leftRightEdge == LEFT) {
                outRect.left = spacing;
                outRect.right = halfSpacing;
            } else if (leftRightEdge == RIGHT) {
                outRect.left = halfSpacing;
                outRect.right = spacing;
            } else {
                outRect.left = halfSpacing;
                outRect.right = halfSpacing;
            }
        }

        if(rows == 1) {
            outRect.top = spacing;
            outRect.bottom = spacing;
        } else {
            if (topBottomEdge == TOP) {
                outRect.top = spacing;
                outRect.bottom = halfSpacing;
            } else if (topBottomEdge == BOTTOM) {
                outRect.top = halfSpacing;
                outRect.bottom = spacing;
            } else {
                outRect.top = halfSpacing;
                outRect.bottom = halfSpacing;
            }
        }
    }

    protected int getTotalSpan(RecyclerView parent) {
        RecyclerView.LayoutManager mgr = parent.getLayoutManager();
        if (mgr instanceof GridLayoutManager) {
            return ((GridLayoutManager) mgr).getSpanCount();
        }
        return -1;
    }

    private int getIsEdgeType(int spanCount, int position, int pseudoGroupPosition) {
        final int size = ((position - (pseudoGroupPosition + 1)) % spanCount);
        if(size == 0) {
            return LEFT;
        } else if(size == spanCount - 1) {
            return RIGHT;
        } else {
            return NONE;
        }
    }

    private int getIsTopBottomEdgeType(int spanCount, int rows, int position, int pseudoGroupPosition) {
        final int size = ((position - (pseudoGroupPosition + 1)) / spanCount);
        if(size == 0) {
            return TOP;
        } else if(size == rows - 1) {
            return BOTTOM;
        } else {
            return NONE;
        }
    }
}
