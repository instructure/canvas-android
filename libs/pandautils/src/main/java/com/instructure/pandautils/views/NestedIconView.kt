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
 */    package com.instructure.pandautils.views

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.setVisible

class NestedIconView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var primaryIcon: ImageView
    private lateinit var nestedIcon: ImageView
    private var iconPlaceholder = R.drawable.ic_document
    private var nestedIconPlaceHolder = R.drawable.ic_add

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, context.resources.displayMetrics)
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, context.resources.displayMetrics)
        setMeasuredDimension(width.toInt(), height.toInt())
    }

    init {
        //configure image views
        addView(initPrimaryIcon(context))
        addView(initNestedIcon(context))

        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.NestedIconView)

            (0 until a.indexCount).map { a.getIndex(it) }.forEach {
                when (it) {
                    R.styleable.NestedIconView_icon -> iconPlaceholder = a.getResourceId(it, R.drawable.ic_document)
                    R.styleable.NestedIconView_nestedIcon -> nestedIconPlaceHolder = a.getResourceId(it, R.drawable.ic_add)
                }
            }

            a.recycle()
        }

        setIcon(iconPlaceholder)
        setNestedIcon(nestedIconPlaceHolder)
    }

    private fun initPrimaryIcon(context: Context): ImageView {
        primaryIcon = ImageView(context)
        primaryIcon.id = R.id.primaryIcon

        val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28f, context.resources.displayMetrics)
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28f, context.resources.displayMetrics)
        val assignmentIconParams = LayoutParams(width.toInt(), height.toInt())

        primaryIcon.layoutParams = assignmentIconParams

        return primaryIcon
    }

    private fun initNestedIcon(context: Context): ImageView {
        nestedIcon = ImageView(context)
        nestedIcon.id = R.id.nestedIcon

        nestedIcon.setBackgroundResource(R.drawable.bg_nested_icon)

        val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics)
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics)
        val publishedIconParams = LayoutParams(width.toInt(), height.toInt())
        publishedIconParams.gravity = Gravity.BOTTOM or Gravity.END

        nestedIcon.layoutParams = publishedIconParams
        val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, context.resources.displayMetrics)
        nestedIcon.setPadding(padding.toInt(), padding.toInt(), padding.toInt(), padding.toInt())

        return nestedIcon
    }

    fun setIcon(icon: Drawable, @ColorInt tintColor: Int? = null) {
        primaryIcon.setImageDrawable(icon)
        tintColor?.let { primaryIcon.setColorFilter(it) }
    }

    fun setIcon(@DrawableRes iconRes: Int, @ColorInt tintColor: Int? = null) {
        primaryIcon.setImageDrawable(ContextCompat.getDrawable(context, iconRes))
        tintColor?.let { primaryIcon.setColorFilter(it) }
    }

    fun setNestedIcon(@DrawableRes iconRes: Int, @ColorInt tintColor: Int? = null) {
        nestedIcon.setVisible() // Need to make sure its visible, in case it was hidden during recycling.
        nestedIcon.setImageDrawable(ContextCompat.getDrawable(context, iconRes))
        tintColor?.let { nestedIcon.setColorFilter(it) }
    }

    fun hideNestedIcon() {
        nestedIcon.setVisible(false)
    }

    fun setImage(thumbnailUrl: String) {
        Glide.with(this).load(thumbnailUrl).into(primaryIcon)
    }

    fun setNestedIconContentDescription(contentDescription: String) {
        nestedIcon.contentDescription = contentDescription
    }

}
