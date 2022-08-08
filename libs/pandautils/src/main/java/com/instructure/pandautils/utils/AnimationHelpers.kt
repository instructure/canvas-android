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
package com.instructure.pandautils.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.os.Build
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import kotlin.math.hypot
import kotlin.math.sqrt

object AnimationHelpers {
    /**
     * Creates a CircularReveal Animator which reveals from the center
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun createRevealAnimator(view: View): Animator {
        val animator: Animator
        val centerX = view.width / 2
        val centerY = view.height / 2
        val startRadius = 0
        val endRadius = hypot(view.width.toFloat(), view.height.toFloat()) / 2
        animator = ViewAnimationUtils.createCircularReveal(
            view,
            centerX,
            centerY,
            startRadius.toFloat(),
            endRadius
        )
        animator.interpolator = DecelerateInterpolator()
        return animator
    }

    /**
     * Helper to remove GlobalLayoutListeners. This stops the animation from being called multiple times.
     */
    fun removeGlobalLayoutListeners(view: View, victim: ViewTreeObserver.OnGlobalLayoutListener?) {
        view.viewTreeObserver.removeOnGlobalLayoutListener(victim)
    }
}
