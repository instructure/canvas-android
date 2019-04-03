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

package com.instructure.student.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class IndicatorCircleView extends View {
    private Paint mPaint = new Paint();
    private String mText = "";
    private Context mContext;
    private int mBackgroundColor = Color.GRAY;
    private int mTextColor = Color.WHITE;

    public IndicatorCircleView(Context context) {
        super(context);
        init(context);
    }

    public IndicatorCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IndicatorCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int backgroundColor = mBackgroundColor;
        int textColor = mTextColor;
        float centerX = canvas.getWidth() / 2;
        float centerY = canvas.getHeight() / 2;
        mPaint.setColor(backgroundColor);
        canvas.drawCircle(centerX, centerY, canvas.getHeight()/2, mPaint);
        mPaint.setColor(textColor);

        mPaint.setTextSize(canvas.getHeight()/2);
        mPaint.setTextAlign(Paint.Align.CENTER);
        // center text in the view
        float textY =  (int) ((canvas.getHeight() / 2) - ((mPaint.descent() + mPaint.ascent()) / 2)) ;
        canvas.drawText(mText, centerX, textY, mPaint);
    }


    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
        invalidate();
    }

    public void setBackgroundColor(int backgroundColor) {
        this.mBackgroundColor = backgroundColor;
        invalidate();
    }

    public void setText(String text) {
        this.mText = text;
        invalidate();
    }
}
