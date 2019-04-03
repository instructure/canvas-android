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
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.utils.dp

class CountdownCircle @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private val rect = RectF()
    private var strokeSize = 6f
    private var onCountdown: (Int) -> Unit = {}
    private var startTime = 0L
    private var endTime = 0L
    private var timerLength = 0
    private var running = false
    private var currentSecond = 0

    init {
        if (!isInEditMode) strokeSize = strokeSize.dp()
    }

    fun startCountdown(seconds: Int, onCountdown: (Int) -> Unit) {
        this.onCountdown = onCountdown
        currentSecond = seconds
        onCountdown(seconds)
        startTime = System.currentTimeMillis()
        timerLength = seconds * 1000
        endTime = System.currentTimeMillis() + timerLength
        running = true
        invalidate()
    }

    fun stopCountdown() {
        running = false
    }

    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.colorAccent)
            style = Paint.Style.STROKE
            strokeWidth = strokeSize
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!running) return
        val progress = 1 - ((System.currentTimeMillis() - startTime) / timerLength.toFloat()).coerceIn(0f, 1f)
        canvas.drawArc(rect, 270f, -360f * progress, false, paint)

        val second = ((999 + progress * timerLength) / 1000).toInt()
        if (second != currentSecond) {
            currentSecond = second
            onCountdown(currentSecond)
        }

        if (progress > 0) {
            invalidate()
        } else {
            running = false
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val inset = strokeSize / 2 + 0.5f
        rect.set(inset, inset, w - inset, h - inset)
    }

}
