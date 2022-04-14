/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.view

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.instructure.teacher.R
import com.instructure.teacher.utils.getColorCompat

class PublishedStatusIconView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var mAssignmentIcon: ImageView
    private lateinit var mPublishedStatusIcon: ImageView
    private var mIconPlaceholder = R.drawable.ic_assignment
    private var mPublished = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, context.resources.displayMetrics)
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, context.resources.displayMetrics)
        setMeasuredDimension(width.toInt(), height.toInt())
    }

    init {
        //configure image views
        addView(initAssignmentIcon(context))
        addView(initPublishedStatusIcon(context))

        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.PublishedStatusIconView)

            (0 until a.indexCount).map { a.getIndex(it) }.forEach {
                when (it) {
                    R.styleable.PublishedStatusIconView_icon -> mIconPlaceholder = a.getResourceId(it, R.drawable.ic_assignment)
                    R.styleable.PublishedStatusIconView_published -> mPublished = a.getBoolean(it, false)
                }
            }

            a.recycle()
        }

        setIcon(mIconPlaceholder)
        setPublishedStatus(mPublished)
    }

    private fun initAssignmentIcon(context: Context): ImageView {
        mAssignmentIcon = ImageView(context)
        mAssignmentIcon.id = R.id.assignmentIcon

        val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28f, context.resources.displayMetrics)
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28f, context.resources.displayMetrics)
        val assignmentIconParams: LayoutParams = LayoutParams(width.toInt(), height.toInt())

        mAssignmentIcon.layoutParams = assignmentIconParams

        return mAssignmentIcon
    }

    private fun initPublishedStatusIcon(context: Context): ImageView {
        mPublishedStatusIcon = ImageView(context)
        mPublishedStatusIcon.id = R.id.publishedStatusIcon

        mPublishedStatusIcon.setBackgroundResource(R.drawable.bg_published_icon)

        val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics)
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics)
        val publishedIconParams: LayoutParams = LayoutParams(width.toInt(), height.toInt())
        publishedIconParams.gravity = Gravity.BOTTOM or Gravity.END

        mPublishedStatusIcon.layoutParams = publishedIconParams
        val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, context.resources.displayMetrics)
        mPublishedStatusIcon.setPadding(padding.toInt(), padding.toInt(), padding.toInt(), padding.toInt())

        return mPublishedStatusIcon
    }


    fun setIcon(@DrawableRes iconRes: Int, tintColor: Int? = null) {
        mAssignmentIcon.setImageDrawable(ContextCompat.getDrawable(context, iconRes))
        tintColor?.let { mAssignmentIcon.setColorFilter(it) }
    }

    fun setImage(thumbnailUrl: String) {
        Glide.with(this).load(thumbnailUrl).into(mAssignmentIcon)
    }

    fun setPublishedStatus(isPublished: Boolean) {
        //set published drawable
        when(isPublished) {
            true ->  {
                mPublishedStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_complete_solid))
                mPublishedStatusIcon.setColorFilter(context.getColorCompat(R.color.textSuccess))
                mPublishedStatusIcon.contentDescription = context.getString(R.string.published)
            }
            else -> {
                mPublishedStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_complete))
                mPublishedStatusIcon.setColorFilter(context.getColorCompat(R.color.defaultTextGray))
                mPublishedStatusIcon.contentDescription = context.getString(R.string.not_published)
            }
        }
    }


}
