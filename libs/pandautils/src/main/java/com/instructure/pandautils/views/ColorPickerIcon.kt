package com.instructure.pandautils.views

/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible

class ColorPickerIcon @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    lateinit var circle: ImageView
    private lateinit var checkMark: ImageView
    private var circlePlaceholder = R.drawable.ic_color_picker_circle
    private var checkMarkPlaceholder = R.drawable.ic_check_white_24dp

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, context.resources.displayMetrics)
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, context.resources.displayMetrics)
        setMeasuredDimension(width.toInt(), height.toInt())
    }

    init {
        //configure image views
        addView(initCircle(context))
        addView(initCheckMark(context))

        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.ColorPickerIcon)

            (0 until a.indexCount).map { a.getIndex(it) }.forEach {
                when (it) {
                    R.styleable.ColorPickerIcon_circle -> circlePlaceholder = a.getResourceId(it, R.drawable.ic_color_picker_circle)
                    R.styleable.ColorPickerIcon_checkMark -> checkMarkPlaceholder = a.getResourceId(it, R.drawable.ic_check_white_24dp)
                }
            }

            a.recycle()
        }

        setCircle(circlePlaceholder)
        setCheckMark(checkMarkPlaceholder)
    }

    private fun initCircle(context: Context): ImageView {
        circle = ImageView(context)
        circle.id = R.id.circle

        val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, context.resources.displayMetrics)
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, context.resources.displayMetrics)
        val assignmentIconParams = LayoutParams(width.toInt(), height.toInt())

        circle.layoutParams = assignmentIconParams

        return circle
    }

    private fun initCheckMark(context: Context): ImageView {
        checkMark = ImageView(context)
        checkMark.id = R.id.checkMark
        
        val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, context.resources.displayMetrics)
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, context.resources.displayMetrics)
        val publishedIconParams = LayoutParams(width.toInt(), height.toInt())
        publishedIconParams.gravity = Gravity.CENTER

        checkMark.layoutParams = publishedIconParams
        checkMark.setGone()

        return checkMark
    }

    private fun setCircle(@DrawableRes iconRes: Int, @ColorInt tintColor: Int? = null) {
        circle.setImageDrawable(ContextCompat.getDrawable(context, iconRes))
        tintColor?.let { circle.setColorFilter(it) }
    }

    private fun setCheckMark(@DrawableRes iconRes: Int) {
        checkMark.setImageDrawable(ContextCompat.getDrawable(context, iconRes))
        checkMark.setColorFilter(context.getColor(R.color.textLightest))
    }

    fun setSelected() {
        checkMark.setVisible()
    }
}
