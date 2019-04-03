/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.decorations;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.instructure.teacher.R;


public class SimpleVerticalDividerDecoration extends RecyclerView.ItemDecoration {

    private float mStrokeWidth;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public SimpleVerticalDividerDecoration(Context context) {
        this(context, R.dimen.list_divider, R.color.lightGray);
    }

    public SimpleVerticalDividerDecoration(Context context, @DimenRes int strokeWidthResId, @ColorRes int strokeColorResId) {
        mStrokeWidth = context.getResources().getDimension(strokeWidthResId);
        mPaint.setColor(ContextCompat.getColor(context, strokeColorResId));
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) > 0) {
            outRect.top = (int) mStrokeWidth;
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount();
        for (int i = 1; i < childCount; i++) {
            float top = parent.getChildAt(i).getTop();
            c.drawRect(
                    parent.getPaddingLeft(),
                    top,
                    parent.getWidth() - parent.getPaddingRight(),
                    top + mStrokeWidth,
                    mPaint);
        }
    }

}
