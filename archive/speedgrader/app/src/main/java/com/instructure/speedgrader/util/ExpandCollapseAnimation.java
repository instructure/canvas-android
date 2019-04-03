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

package com.instructure.speedgrader.util;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ExpandCollapseAnimation extends Animation {
    private View mAnimatedView;
    private int mEndHeight;
    private int mStartVisibility;

    public ExpandCollapseAnimation(View view, int duration) {
        setDuration(duration);
        mAnimatedView = view;
        mEndHeight = mAnimatedView.getLayoutParams().height;
        mStartVisibility = mAnimatedView.getVisibility();
        if(mStartVisibility == View.GONE || mStartVisibility == View.INVISIBLE) {
            mAnimatedView.setVisibility(View.VISIBLE);
            mAnimatedView.getLayoutParams().height = 0;
        }
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        if (interpolatedTime < 1.0f) {
            if(mStartVisibility == View.GONE || mStartVisibility == View.INVISIBLE) {
                mAnimatedView.getLayoutParams().height = (int) (mEndHeight * interpolatedTime);
            } else {
                mAnimatedView.getLayoutParams().height = mEndHeight - (int) (mEndHeight * interpolatedTime);
            }
            mAnimatedView.requestLayout();
        } else {
            if(mStartVisibility == View.GONE || mStartVisibility == View.INVISIBLE) {
                mAnimatedView.getLayoutParams().height = mEndHeight;
                mAnimatedView.requestLayout();
            } else {
                mAnimatedView.getLayoutParams().height = 0;
                mAnimatedView.setVisibility(View.GONE);
                mAnimatedView.requestLayout();
                mAnimatedView.getLayoutParams().height = mEndHeight;
            }
        }
    }

    public static void setHeightForWrapContent(Activity activity, View view) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int screenWidth = metrics.widthPixels;

        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.EXACTLY);

        view.measure(widthMeasureSpec, heightMeasureSpec);
        int height = view.getMeasuredHeight();
        view.getLayoutParams().height = height;
    }
}