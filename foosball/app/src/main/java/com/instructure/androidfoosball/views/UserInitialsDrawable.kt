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

import android.graphics.*
import android.graphics.drawable.Drawable
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.utils.avatarColor
import com.instructure.androidfoosball.utils.clampToBrightness
import com.instructure.androidfoosball.utils.initials
import com.instructure.androidfoosball.utils.unless

class UserInitialsDrawable(val user: User, val size: Int = 0) : Drawable() {

    private val MAX_BG_BRIGHTNESS = 0.6f
    private val MAX_TEXT_WIDTH_PERCENTAGE = 0.9f

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawColor(user.avatarColor.clampToBrightness(MAX_BG_BRIGHTNESS))
        val initials = user.initials
        val srcHyp = Math.sqrt(
                Math.pow(mPaint.measureText(initials).toDouble(), 2.0)
                        + Math.pow(-mPaint.fontMetrics.ascent.toDouble(), 2.0)
        ).toFloat()
        mPaint.textSize = MAX_TEXT_WIDTH_PERCENTAGE * (mPaint.textSize * canvas.width / srcHyp)
        canvas.drawText(
                initials,
                canvas.width / 2f,
                (canvas.height - mPaint.fontMetrics.ascent - mPaint.fontMetrics.descent) / 2f,
                mPaint
        )
    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT
    override fun setAlpha(alpha: Int) { }
    override fun setColorFilter(colorFilter: ColorFilter?) { }
    override fun getIntrinsicWidth() = bounds.width() unless {it == 0} then size
    override fun getIntrinsicHeight() = bounds.height() unless {it == 0} then size
}
