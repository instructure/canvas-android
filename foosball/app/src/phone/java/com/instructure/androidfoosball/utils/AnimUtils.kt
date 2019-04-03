/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.androidfoosball.utils

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation


object AnimUtils {

    fun fadeIn(duration: Long, view: View) {
        if (view.visibility == View.VISIBLE) return

        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = duration
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                view.visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })
        view.startAnimation(anim)
    }

    fun fadeOut(duration: Long, view: View) {
        if (view.visibility != View.VISIBLE) return

        val anim = AlphaAnimation(1.0f, 0.0f)
        anim.duration = duration
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                view.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })
        view.startAnimation(anim)
    }

}
