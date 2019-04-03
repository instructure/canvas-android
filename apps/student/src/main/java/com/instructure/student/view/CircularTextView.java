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
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.instructure.student.R;

public class CircularTextView extends TextView {

    private Paint  painter;
    private Paint  bitmapPaint;
    private Bitmap bitmap;
    private Canvas canvas;
    private int width, height;

    private boolean hasBorder;
    private boolean hasShadow;
    public CircularTextView(Context context) {
        super(context);
        init(context, null);
    }

    public CircularTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircularTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        painter = new Paint();

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CircularTextView,
                    0, 0);

            try {
                String colorValue = a.getString(R.styleable.CircularTextView_circleColor);
                if (colorValue != null) {
                    painter.setColor(
                            Color.parseColor(colorValue));
                } else {
                    painter.setColor(Color.BLACK);
                }

                hasBorder = a.getBoolean(R.styleable.CircularTextView_hasBorder, false);
                hasShadow = a.getBoolean(R.styleable.CircularTextView_hasShadow, false);
            } finally {
                a.recycle();
            }
        } else {
            painter.setColor(Color.BLACK);
        }

        painter.setStrokeWidth(4);
        painter.setAntiAlias(true);
        painter.setStyle(Paint.Style.FILL_AND_STROKE);
        bitmapPaint = new Paint(Paint.DITHER_FLAG);
        this.setLayerType(LAYER_TYPE_SOFTWARE, painter);

        if(hasShadow){
            // Draw stroke
            painter.setShadowLayer(4.0f, 0.0f, 2.0f, Color.GRAY);
        }

        this.setGravity(Gravity.CENTER);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        canvas.drawCircle(w/2, h/2, h/3, painter);
        if(hasBorder){
            // Draw stroke
            canvas.drawCircle(w/2, h/2, h/3, getStroke());
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        super.onDraw(canvas);
    }

    public int getBorderColor() {
        return painter.getColor();
    }

    /**
     * Sets the border color, also the same as
     * borderColor attribute in xml.
     * @param color
     */
    public void changeColor(int color){
        painter.setColor(color);
        if(painter != null && canvas != null){
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);


            // Draw Circle
            canvas.drawCircle(width/2, height/2, height/3, painter);
            if(hasBorder){
                // Draw stroke
                canvas.drawCircle(width/2, height/2,height/3, getStroke());
            }
        }
    }

    public void changeCircleColor(int color){
        painter.setColor(color);
        if(painter != null && canvas != null){
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);

            canvas.drawCircle(width/2, height/2, height/3, painter);
        }
    }

    private Paint getStroke(){
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStrokeWidth(4);
        p.setColor(Color.WHITE);
        p.setStyle(Paint.Style.STROKE);
        return p;
    }
}
