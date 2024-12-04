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
package com.instructure.teacher.view.edit_rubric

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricCriterionRating
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.getDrawableCompat
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.onLongClick
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.R
import com.instructure.teacher.dialog.CustomRubricRatingDialog
import com.instructure.teacher.utils.getColorCompat
import org.greenrobot.eventbus.EventBus

class CriterionRatingButton @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    /** Horizontal padding */
    private val mPaddingLeftRight = context.DP(8f).toInt()

    /** Minimum button width and height */
    private val mMinSize = context.DP(48f).toInt()

    /** Button text size */
    private val mFontSize = 20f

    /** ID of the criterion item this rating applies to */
    private var mCriterionId = ""

    /** ID of the student being graded */
    private var mStudentId = -1L

    /** Max rating allowed for the criterion */
    private var mMaxRating = 0.0

    /** ID of the rating assigned to this button */
    var ratingId : String? = null

    /** Description of this rating item */
    private var mRatingDescription = ""

    /** Whether this button reflects a custom user-input value instead of an existing rating item */
    private var mIsCustom = false

    /** The 'add' icon to draw when [mIsCustom] is true */
    private val mAddDrawable by lazy {
        context.getDrawableCompat(R.drawable.ic_add).apply {
            setColorFilter(context.getColorCompat(R.color.textDark), PorterDuff.Mode.SRC_ATOP)
            val offset = (height - context.DP(20).toInt()) / 2
            setBounds(offset, offset, width - offset, height - offset)
        }
    }

    /** The point value for this rating item. Setting the value will automatically update the UI*/
    var pointValue: Double = 0.0
        get() = field
        set(newValue) {
            field = newValue
            text = NumberHelper.formatDecimal(field, 2, true)
        }

    init {
        // Set text appearance to Medium
        @Suppress("DEPRECATION")
        setTextAppearance(context, R.style.TextFont_Medium)

        // Assign sizes/dimensions
        textSize = mFontSize
        minWidth = mMinSize
        minHeight = mMinSize
        gravity = Gravity.CENTER
        setPadding(mPaddingLeftRight, 0, mPaddingLeftRight, 0)

        // Set the background to handle selected vs unselected states
        background = makeBackground()

        onClick {
            // Hide description popup
            EventBus.getDefault().post(ShowRatingDescriptionEvent(null, mStudentId, ""))
            when {
                mIsCustom -> (context as? AppCompatActivity)?.supportFragmentManager?.let {
                    CustomRubricRatingDialog.show(it, mCriterionId, mStudentId, pointValue, mMaxRating)
                }
                isSelected -> EventBus.getDefault().post(RatingSelectedEvent(null, mCriterionId, ratingId, mStudentId))
                else -> EventBus.getDefault().post(RatingSelectedEvent(pointValue, mCriterionId, ratingId, mStudentId))
            }
        }

        onLongClick {
            EventBus.getDefault().post(ShowRatingDescriptionEvent(this, mStudentId, mRatingDescription))
            true
        }

        // Set text color to handle selected vs unselected states
        if (!isInEditMode) setTextColor(ViewStyler.generateColorStateList(
                intArrayOf(android.R.attr.state_selected) to context.getColor(R.color.textLightest),
                intArrayOf(android.R.attr.state_pressed) to context.getColor(R.color.textInfo),
                intArrayOf() to context.getColorCompat(R.color.textDark)
        ))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mIsCustom && text == "") mAddDrawable.draw(canvas)
    }

    @SuppressLint("GetContentDescriptionOverride") // TODO
    override fun getContentDescription(): CharSequence = when {
        BuildConfig.IS_TESTING -> "criterion_${mCriterionId}_rating_button_${if (mIsCustom) "custom" else ratingId}"
        mIsCustom && isSelected -> context.getString(R.string.rating_button_content_description_custom, text)
        mIsCustom -> context.getString(R.string.enter_custom_value)
        else -> context.getString(R.string.rating_button_content_description, text, mRatingDescription)
    }

    /**
     * Initializes this view with the specified criterion and rating item. This MUST be called
     * for proper functionality.
     */
    fun setCriterionRating(rating: RubricCriterionRating, criterion: RubricCriterion, studentId: Long) {
        mCriterionId = criterion.id ?: ""
        mStudentId = studentId
        ratingId = rating.id
        mMaxRating = criterion.points
        mRatingDescription = rating.description ?: ""
        pointValue = rating.points
    }

    /**
     * Marks this view as a receiver for custom user-input point values. A custom view should
     * launch an input dialog on click and display the result. When deselected, the custom view
     * should show a plus symbol instead of a point value.
     */
    fun markAsCustom(criterion: RubricCriterion, studentId: Long): CriterionRatingButton {
        mCriterionId = criterion.id ?: ""
        mStudentId = studentId
        mMaxRating = criterion.points
        mIsCustom = true
        mRatingDescription = context.getString(R.string.enter_custom_value)
        return this
    }

    /** Generates a background drawable with selected and unselected states */
    private fun makeBackground() = StateListDrawable().apply {
        val selectedState = context.getDrawableCompat(R.drawable.bg_criterion_button_selected)
        val pressedState = context.getDrawableCompat(R.drawable.bg_criterion_button_unselected)
        val defaultState = context.getDrawableCompat(R.drawable.bg_criterion_button_unselected)
        if (isInEditMode) {
            selectedState.setColorFilter(context.getColorCompat(R.color.backgroundDarkest), PorterDuff.Mode.SRC_ATOP)
            pressedState.setColorFilter(context.getColorCompat(R.color.backgroundDarkest), PorterDuff.Mode.SRC_ATOP)
        } else {
            val backgroundColor = context.getColor(R.color.backgroundInfo)
            selectedState.setColorFilter(backgroundColor, PorterDuff.Mode.SRC_ATOP)
            pressedState.setColorFilter(backgroundColor, PorterDuff.Mode.SRC_ATOP)
        }
        addState(intArrayOf(android.R.attr.state_selected), selectedState)
        addState(intArrayOf(android.R.attr.state_pressed), pressedState)
        addState(intArrayOf(), defaultState)
    }

}
