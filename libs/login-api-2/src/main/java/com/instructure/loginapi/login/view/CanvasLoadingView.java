/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.loginapi.login.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import androidx.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.instructure.loginapi.login.R;

public class CanvasLoadingView extends View {

    /** Default width and/or height dimension */
    private static final float DEFAULT_DIMEN_DP = 48f;

    /** Distance from view center where circles will 'spawn', as a percent of max ring radius */
    private static final float FRACTION_SPAWN_POINT_RADIUS = 0.31f;

    /** Maximum circle radius, as a percent of max ring radius*/
    private static final float FRACTION_MAX_CIRCLE_RADIUS = 0.29f;

    /** Radius if the inner ring, as a percent of max ring radius */
    private static final float FRACTION_INNER_RING_RADIUS = 1 / 3f;

    /** Default step count */
    private static final int STEP_COUNT = 30;

    /** Number of steps (frames) in each iteration of the animation */
    private int mStepCount = STEP_COUNT;

    /** Distance from view center where circles will 'spawn' */
    private float mSpawnPointRadius;

    /** Maximum radius of individual circles - when located at the max ring radius. Actual
     * radius may be larger if the circle extends beyond max rung radius. */
    private float mMaxCircleRadius;

    /** Radius of the outermost ring. Should be equal to the smallest view dimension */
    private float mMaxRingRadius;

    /** View center, cached for convenience */
    private PointF mCenter = new PointF();

    /** Circular path used for clipping along the outermost ring */
    private Path mClipPath = new Path();

    /** Current step (frame) in the animation */
    private int mStep;

    /** Current iteration */
    private int mIterationCount;

    /** Interpolator, to smooth out transition between animations */
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    /** Paint used to color circles */
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** Default circle colors */
    int[] mColors = {
            0xFFEB282D,
            0xFFC3001D,
            0xFF9E1940,
            0xFFEB282D,
            0xFFEC4013,
            0xFFF57F00,
            0xFFEE5010,
            0xFFEE4831
    };

    public CanvasLoadingView(Context context) {
        super(context);
        init(null);
    }

    public CanvasLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CanvasLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CanvasLoadingView);
            int idx;
            for (int i = 0; i < a.getIndexCount(); i++) {
                idx = a.getIndex(i);
                if(idx == R.styleable.CanvasLoadingView_clv_override_color) {
                    /* Obtain override color */
                    int overrideColor = a.getColor(idx, Color.GRAY);
                    setOverrideColor(overrideColor);
                } else if(idx == R.styleable.CanvasLoadingView_clv_speed_multiplier) {
                    /* Obtain speed multiplier */
                    mStepCount = (int) (STEP_COUNT / a.getFloat(idx, 1f));
                }
            }
            a.recycle();
        }
    }

    public void setOverrideColor(@ColorInt int color) {
        for (int j = 0; j < mColors.length; j++) mColors[j] = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        /* Set clipping mask */
        canvas.clipPath(mClipPath);

        /* Determine raw percentage based on current step and interpolate */
        float percent = (float) mStep / mStepCount;
        percent = mInterpolator.getInterpolation(percent);

        /* Determine raw offset based on current iteration count */
        float offset = ((mIterationCount / 2) % 2 == 0) ? 45 : -45;

        if (mIterationCount % 2 == 1) {
            /* Draw rotation animation on odd iterations */
            offset = offset * (1 - percent);
            drawCircleRing(canvas, offset, FRACTION_INNER_RING_RADIUS);
            if (mIterationCount >= 3) drawCircleRing(canvas, 0, 1f);

        } else {
            /* Draw zoom animation on even iterations */
            float innerRingPercentage = percent * FRACTION_INNER_RING_RADIUS;
            float outerRingPercentage = FRACTION_INNER_RING_RADIUS + percent * (1-FRACTION_INNER_RING_RADIUS);
            float exitingRingPercentage = 1 + percent;
            drawCircleRing(canvas, offset, innerRingPercentage);
            if (mIterationCount >= 2 || isInEditMode()) drawCircleRing(canvas, 0, outerRingPercentage);
            if (mIterationCount >= 4 || isInEditMode()) drawCircleRing(canvas, 0, exitingRingPercentage);
        }

        /* Increment counts, reset current step if needed */
        mStep++;
        if (mStep > mStepCount) {
            mStep = 0;
            mIterationCount++;
        }

        invalidate();
    }

    /**
     * Draws a ring of 8 circles
     *
     * @param canvas A Canvas object into which the ring will be drawn
     * @param angleOffset Angle rotation offset
     * @param growthPercent (0..1+) How 'grown' the circle is. 0 is no growth with circles of zero
     *                      size. 1 is full growth with circles at the outer edge.
     */
    private void drawCircleRing(Canvas canvas, float angleOffset, float growthPercent) {
        float radius = growthPercent * mMaxCircleRadius;
        float ringRadius = mSpawnPointRadius + (growthPercent * (mMaxRingRadius - mSpawnPointRadius));
        for (int i = 0; i < 8; i++) {
            drawCircle(canvas, mColors[i], radius, Math.toRadians(i * 45f + angleOffset) , ringRadius);
        }
    }

    /**
     * Draws an individual circle
     *
     * @param canvas Canvas object into which the circle will be drawn
     * @param color The circle's color
     * @param radius The radius of the circle
     * @param angleRadians Angle of the circle's center from the view's center, in radians
     * @param distanceFromCenter Distance of the circle's center from the view's center
     */
    private void drawCircle(Canvas canvas, int color, float radius, double angleRadians, float distanceFromCenter) {
        float x = (float) (mCenter.x + distanceFromCenter * Math.cos(angleRadians));
        float y = (float) (mCenter.y + distanceFromCenter * Math.sin(angleRadians));
        mPaint.setColor(color);
        canvas.drawCircle(x, y, radius, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        /* Use DEFAULT_DIMEN_DP as width and/or height if not specified */
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) dpToPx(getContext(), DEFAULT_DIMEN_DP), MeasureSpec.EXACTLY);
        }
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) dpToPx(getContext(), DEFAULT_DIMEN_DP), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        /* Set up measurements */
        mCenter.set(MeasureSpec.getSize(widthMeasureSpec) / 2f,
                MeasureSpec.getSize(heightMeasureSpec) / 2f);
        mMaxRingRadius = Math.min(mCenter.x, mCenter.y);
        mSpawnPointRadius = mMaxRingRadius * FRACTION_SPAWN_POINT_RADIUS;
        mMaxCircleRadius = mMaxRingRadius * FRACTION_MAX_CIRCLE_RADIUS;
        mClipPath.rewind();
        mClipPath.addCircle(mCenter.x, mCenter.y, mMaxRingRadius, Path.Direction.CW);

    }

    private static float dpToPx(Context context, float dp){
        Resources resources = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
}
