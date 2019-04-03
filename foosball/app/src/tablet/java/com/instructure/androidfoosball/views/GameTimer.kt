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
package com.instructure.androidfoosball.views

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.widget.TextView

class GameTimer @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleRes: Int = 0
) : TextView(context, attrs, defStyleRes) {

    private var mStartTime = 0L
    private var mIsAttached = false

    private val mTicker: Runnable = object : Runnable {
        override fun run() {
            onTimeChanged()
            val now = SystemClock.uptimeMillis()
            val nowSystem = System.currentTimeMillis()
            val nextSystem = nowSystem + (1000 - (nowSystem - mStartTime) % 1000)
            val next = now + nextSystem - nowSystem
            handler.postAtTime(this, next)
        }
    }

    fun setStartTime(startTime: Long) {
        mStartTime = startTime
        start()
    }

    private fun onTimeChanged() {
        val time = (System.currentTimeMillis() - mStartTime) / 1000
        text = if (isInEditMode) "1:23" else "%d:%02d".format(time / 60, time % 60)
    }

    private fun start() {
        if (mIsAttached) {
            stop()
            mTicker.run()
        }
    }

    private fun stop() {
        handler.removeCallbacks(mTicker)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mIsAttached = true
        mTicker.run()
    }

    override fun onDetachedFromWindow() {
        stop()
        mIsAttached = false
        super.onDetachedFromWindow()
    }

}
