/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */
package com.instructure.pandautils.views

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

class ExpandCollapseAnimation(
    private val mView: View,
    private var mStartWidth: Int,
    private var mTargetWidth: Int,
    onAnimationFinished: () -> Unit
) : Animation() {

    init {
        setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                onAnimationFinished()
            }
        })
    }

    fun updateValues(startWidth: Int, targetWidth: Int) {
        mStartWidth = startWidth
        mTargetWidth = targetWidth
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        mView.layoutParams.width =
            mStartWidth + ((mTargetWidth - mStartWidth) * interpolatedTime).toInt()
        mView.requestLayout()
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}