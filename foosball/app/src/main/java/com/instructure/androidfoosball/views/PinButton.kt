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

package com.instructure.androidfoosball.views

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.TextView

class PinButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TextView(context, attrs, defStyleAttr) {

    private val animator = AnimatorSet().apply {
        playTogether(ObjectAnimator.ofFloat(this@PinButton, "scaleX", TOUCH_ZOOM, 1f),
                ObjectAnimator.ofFloat(this@PinButton, "scaleY", TOUCH_ZOOM, 1f))
        duration = 150
    }

    var onTap: (String) -> Unit = {}

    init {
        gravity = Gravity.CENTER
        setOnClickListener { if (!text.isNullOrBlank()) onTap(text.toString()) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                animator.cancel()
                scaleX = TOUCH_ZOOM
                scaleY = TOUCH_ZOOM
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> animator.start()
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.EXACTLY) {
            val height = View.MeasureSpec.getSize(heightMeasureSpec).toFloat()
            setTextSize(TypedValue.COMPLEX_UNIT_PX, height * NUMBER_SIZE)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    companion object {
        private val NUMBER_SIZE = 0.5f
        private val TOUCH_ZOOM = 1.3f
    }
}