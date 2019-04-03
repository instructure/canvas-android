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

package com.instructure.speedgrader.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.instructure.speedgrader.R;
import com.instructure.speedgrader.util.ViewUtils;

public class CircularTextView extends TextView {

    private Paint  painter;
    private Paint  bitmapPaint;
    private Bitmap bitmap;
    private Canvas canvas;
    private int width, height;

    private boolean hasBorder;
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
            } finally {
                a.recycle();
            }
        } else {
            painter.setColor(Color.BLACK);
        }

        final float strokeWidth = ViewUtils.convertDipsToPixels(2, context);

        painter.setStrokeWidth(strokeWidth);
        painter.setAntiAlias(true);
        painter.setStyle(Paint.Style.FILL_AND_STROKE);
        bitmapPaint = new Paint(Paint.DITHER_FLAG);
        this.setLayerType(LAYER_TYPE_SOFTWARE, painter);

        final float shadowRadius = ViewUtils.convertDipsToPixels(2, context);
        final float shadowY = ViewUtils.convertDipsToPixels(1, context);
        painter.setShadowLayer(shadowRadius, 0.0f, shadowY, Color.GRAY);
        this.setGravity(Gravity.CENTER);
        setTextColor(getResources().getColor(R.color.white));
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

    private Paint getStroke(){
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final float strokeWidth = ViewUtils.convertDipsToPixels(2, getContext());
        p.setStrokeWidth(strokeWidth);
        p.setColor(Color.WHITE);
        p.setStyle(Paint.Style.STROKE);
        return p;
    }
    public void setTypeface(Typeface tf, int style){
        if(style == Typeface.BOLD){
            setTypeface(Typeface.createFromAsset(getContext().getAssets(), "HelveticaNeueLTCom-BdCn.ttf"));
        }else if(style == Typeface.NORMAL){
            setTypeface(Typeface.createFromAsset(getContext().getAssets(),"HelveticaNeueLTCom-MdCn.ttf"));
        }else if(style == Typeface.ITALIC){
            setTypeface(Typeface.createFromAsset(getContext().getAssets(),"HelveticaNeueLTCom-LtIt.ttf"));
        }
    }
}

