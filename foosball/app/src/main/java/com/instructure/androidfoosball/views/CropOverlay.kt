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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Region
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.instructure.androidfoosball.R


class CropOverlay @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleRes: Int = 0
) : View(context, attrs, defStyleRes) {

    /** Resource ID of the source view */
    private var mCutoutSourceId: Int = 0

    /** Reference to the source view. Bound in onSizeChanged() */
    private var mCutoutSourceView: View? = null

    /** Color of the primary overlay */
    private var mMaskColor = 0xAAFFFFFF.toInt()

    /** Frame path */
    private var mFramePath = Path()

    /** Frame backgroundPaint */
    private var mFramePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init { setup(attrs) }

    private fun setup(attrs: AttributeSet? = null) {
        mFramePaint.style = Paint.Style.STROKE
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.CropOverlay)

            // Get source view ID
            mCutoutSourceId = a.getResourceId(R.styleable.CropOverlay_cutoutViewId, 0)

            // Get primary overlay color
            mMaskColor = a.getColor(R.styleable.CropOverlay_maskColor, mMaskColor)

            // Get frame stroke width
            mFramePaint.strokeWidth = a.getDimension(R.styleable.CropOverlay_frameWidth, 10f)

            // Get frame color
            mFramePaint.color = a.getColor(R.styleable.CropOverlay_frameColor, 0xFF888888.toInt())
            a.recycle()
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (mCutoutSourceId > 0) {
            mCutoutSourceView = (parent as ViewGroup).findViewById(mCutoutSourceId)
            mFramePath.rewind()

            val x = mCutoutSourceView!!.left.toFloat()
            val y = mCutoutSourceView!!.top.toFloat()

            val w = mCutoutSourceView!!.width.toFloat()
            val h = mCutoutSourceView!!.height.toFloat()

            val centerX = x + w / 2f
            val centerY = y + h / 2f
            val radius = Math.min(w, h) / 2f

            mFramePath.addCircle(centerX, centerY, radius, Path.Direction.CW)
        }
    }

    override fun onDraw(canvas: Canvas) {
        // Skip drawing if there is no source view
        if (mCutoutSourceView == null) return

        // Draw primary overlay
        canvas.save()
        canvas.clipPath(mFramePath, Region.Op.DIFFERENCE)
        canvas.drawColor(mMaskColor)
        canvas.restore()

        // Draw frame stroke
        canvas.drawPath(mFramePath, mFramePaint)
    }
}
