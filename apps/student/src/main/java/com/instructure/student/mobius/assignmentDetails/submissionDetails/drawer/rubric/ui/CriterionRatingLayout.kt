/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.RatingData
import java.util.*
import kotlin.random.Random

class CriterionRatingLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    /** A map of cached rating button view positions, generated during measure and used during layout */
    private val positionMap = WeakHashMap<View, Pair<Int, Int>>()

    /** The minimum horizontal and vertical spacing between rating button */
    private val minSpacing = context.DP(4f).toInt()

    init {
        clipChildren = false
        // Generate fake ratings for layout preview
        if (isInEditMode) setRatingData(generatePreviewRatings(), Color.GRAY) {}
    }

    /** Sets (replaces) the data to be used for rating. This MUST be called for proper functionality */
    fun setRatingData(ratings: List<RatingData>, tint: Int, onRatingClicked: (ratingId: String) -> Unit) {
        removeAllViews()
        ratings.forEach { rating ->
            val button = CriterionRatingButton(context, rating, tint)
            button.accessibilityDelegate = object : AccessibilityDelegate() {
                override fun onInitializeAccessibilityNodeInfo(host: View?, info: AccessibilityNodeInfo?) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    val description = resources.getString(R.string.a11y_criterion_button_click_description)
                    info?.addAction(AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, description))
                }
            }
            button.contentDescription = resources.getString(R.string.a11y_criterion_content_description, rating.text)
            button.onClick {
                onRatingClicked(rating.id)
            }
            addView(button)
        }
    }

    /** Updates the current rating data and selection states */
    fun updateRatingData(ratings: List<RatingData>) {
        if (ratings.size != childCount) throw IllegalArgumentException("New rating count does not match existing count")
        ratings.forEachIndexed { index, rating ->
            val button = getChildAt(index) as CriterionRatingButton
            button.isSelected = rating.isSelected
            button.isActivated = rating.isAssessed
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount == 0 || widthMeasureSpec.specMode == MeasureSpec.UNSPECIFIED) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val maxWidth = widthMeasureSpec.specSize - paddingStart - paddingEnd
        val childWidthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST)
        val childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)

        val rows = arrayListOf(arrayListOf<View>())
        var currentRow = rows[0]

        for (child in children) {
            child.measure(childWidthSpec, childHeightSpec)
            val rowWidth = currentRow.sumOf { it.measuredWidth + minSpacing } + child.measuredWidth
            if (rowWidth > maxWidth) {
                currentRow = arrayListOf()
                rows += currentRow
            }
            currentRow.add(child)
        }

        var y = 0
        rows.forEachIndexed { index, row ->
            var x = 0
            for (view in row) {
                positionMap[view] = (x + paddingStart) to (y + paddingTop)
                x += view.measuredWidth
                x += minSpacing
            }
            y += row.first().measuredHeight + if (index == rows.lastIndex) 0 else minSpacing
        }

        setMeasuredDimension(widthMeasureSpec.specSize, y + paddingTop + paddingBottom)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (child in children) {
            val (x, y) = positionMap[child] ?: 0 to 0
            if (isRTL()) {
                child.layout(measuredWidth - x - child.measuredWidth, y, measuredWidth - x, y + child.measuredHeight)
            } else {
                child.layout(x, y, x + child.measuredWidth, y + child.measuredHeight)
            }
        }
    }

    /** Ensure only [CriterionRatingButton] views can be added to this layout */
    override fun addView(child: View?, index: Int, params: LayoutParams) {
        if (child is CriterionRatingButton) {
            super.addView(child, index, params)
        } else {
            throw IllegalArgumentException("Cannot add child view of type " +
                    "${child?.javaClass?.simpleName}. RubricRatingLayout only supports children " +
                    "of type RubricRatingView")
        }
    }

    /** Generates fake ratings for layout preview */
    private fun generatePreviewRatings(): List<RatingData> {
        val ratingCount = 14
        val selectedRating = Random.nextInt(ratingCount)
        val ratingMultiplier = 10

        return List(ratingCount) {
            RatingData(
                id = "_fake_$it",
                text = (it * ratingMultiplier).toString(),
                isSelected = it == selectedRating,
                isAssessed = false
            )
        }
    }

}
