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

package com.instructure.student.util;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;

public class AnimationHelpers {

    /**
     * Creates a CircularReveal Animator which reveals from the center
     * @param view
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Animator createRevealAnimator(View view) {
        final Animator animator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final int centerX = view.getWidth() / 2;
            final int centerY = view.getHeight() / 2;
            final int startRadius = 0;
            final float endRadius = calculateMaxRadius(view);
            animator = ViewAnimationUtils.createCircularReveal(view, centerX, centerY, startRadius, endRadius);
        }else{
            animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            animator.setDuration(200);
        }

        animator.setInterpolator(new DecelerateInterpolator());
        return animator;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Animator createRevealAnimator(View view, TimeInterpolator interpolator) {
        final Animator animator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final int centerX = view.getWidth() / 2;
            final int centerY = view.getHeight() / 2;
            final int startRadius = 0;
            animator = ViewAnimationUtils.createCircularReveal(view, centerX, centerY, startRadius,  calculateMaxRadius(view));
        }else{
            animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        }
        animator.setInterpolator(interpolator);
        return animator;
    }

    public static float calculateMaxRadius(View view) {
        float widthSquared = view.getWidth() * view.getWidth();
        float heightSquared = view.getHeight() * view.getHeight();
        float radius = (float)Math.sqrt(widthSquared + heightSquared) / 2;
        return radius;
    }

    /**
     * Helper to remove GlobalLayoutListeners. This stops the animation from being called multiple times.
     * @param view
     * @param victim
     */
    public static void removeGlobalLayoutListeners(View view, ViewTreeObserver.OnGlobalLayoutListener victim){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.getViewTreeObserver().removeOnGlobalLayoutListener(victim);
        }else{
            view.getViewTreeObserver().removeGlobalOnLayoutListener(victim);
        }
    }
}
