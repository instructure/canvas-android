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

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricCriterionRating
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.utils.TeacherPrefs
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class CriterionRatingLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    /** A map of cached rating button view positions, generated during measure and used during layout */
    private val mPositionMap = WeakHashMap<View, Pair<Int, Int>>()

    /** The minimum horizontal and vertical spacing between rating button */
    private var mMinSpacing = context.DP(8f).toInt()

    /** The ID of the criterion associated with the ratings */
    private var mCriterionId = ""

    /** The ID of the student being graded */
    private var mStudentId = -1L

    /** Whether or not rating buttons should be horizontally justified */
    private var mJustifyItems = false

    init {
        // Get XML attribute values
        attrs?.let {
            val a = context.obtainStyledAttributes(attrs, R.styleable.CriterionRatingLayout)
            mJustifyItems = a.getBoolean(R.styleable.CriterionRatingLayout_justifyItems, false)
            mMinSpacing = a.getDimensionPixelSize(R.styleable.CriterionRatingLayout_itemSpacing, mMinSpacing)
            a.recycle()
        }
        // Generate a fake criterion in UI preview
        if (isInEditMode) setCriterion(generatePreviewCriterion(), -1L, 0, false)
    }

    /**
     * Selects the rating button matching the provided value. If the value is null, all buttons
     * will be deselected. If there is no matching value, the custom value button will be selected
     */
    fun selectValue(ratingId: String?, value: Double?) {
        val buttons = children<CriterionRatingButton>()
        buttons.forEach { it.isSelected = false }
        if (ratingId != null) {
            val selection = buttons.firstOrNull { it.ratingId == ratingId } ?: buttons.last()
            value?.let {
                selection.pointValue = value
                selection.isSelected = true
            }
        } else if (value != null) {
            val selection = buttons.firstOrNull { it.pointValue == value } ?: buttons.last()
            selection.pointValue = value
            selection.isSelected = true
        }
    }

    /** Sets the criterion to be used for rating. This MUST be called for proper functionality */
    fun setCriterion(criterion: RubricCriterion, studentId: Long, criterionIdx: Int, isFreeForm: Boolean) {
        mCriterionId = criterion.id ?: ""
        mStudentId = studentId
        removeAllViews()
        if (!isFreeForm) for (rating in criterion.ratings.sortedBy { it.points }) {
            val ratingButton = CriterionRatingButton(context)
            ratingButton.setCriterionRating(rating, criterion, mStudentId)
            addView(ratingButton)
        }
        addView(CriterionRatingButton(context).markAsCustom(criterion, mStudentId))

        // Show the tutorial if the user has not previously viewed it. Focus on the second item of the first criterion.
        if (criterionIdx == 0 && !TeacherPrefs.hasViewedRubricTutorial) {
            children<CriterionRatingButton>().take(2).lastOrNull()?.let {
                val description = context.getString(R.string.tutorialRubricMessage)
                it.post { EventBus.getDefault().post(ShowRatingDescriptionEvent(it, studentId, description, true)) }
            }
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
            val rowWidth = currentRow.sumOf { it.measuredWidth + mMinSpacing } + child.measuredWidth
            if (rowWidth > maxWidth) {
                currentRow = arrayListOf()
                rows += currentRow
            }
            currentRow.add(child)
        }

        var y = 0
        rows.forEachIndexed { index, row ->
            val spacing = if (index == rows.lastIndex || row.size < 2) {
                mMinSpacing
            } else {
                (maxWidth - row.sumOf { it.measuredWidth }) / (row.size - 1)
            }
            var x = 0
            for (view in row) {
                mPositionMap[view] = (x + paddingStart) to (y + paddingTop)
                x += view.measuredWidth
                x += if (mJustifyItems) spacing else mMinSpacing
            }
            y += row.first().measuredHeight + if (index == rows.lastIndex) 0 else mMinSpacing
        }

        setMeasuredDimension(widthMeasureSpec.specSize, y + paddingTop + paddingBottom)

    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (child in children) {
            val (x, y) = mPositionMap[child] ?: 0 to 0
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        EventBus.getDefault().unregister(this)
        super.onDetachedFromWindow()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRatingSelected(event: RatingSelectedEvent) {
        if (event.criterionId == mCriterionId && event.studentId == mStudentId) {
            selectValue(event.ratingId, event.points)
        }
    }

    /** Generates a fake [RubricCriterion] for UI preview purposes */
    private fun generatePreviewCriterion(): RubricCriterion {
        val ratingCount = 14
        val ratingMultiplier = 10

        return RubricCriterion(
                id = "criterion_1",
                description = "Description",
                longDescription = "Long description",
                points = (ratingCount - 1) * ratingMultiplier.toDouble(),
                ratings = MutableList(ratingCount) { i ->
                    RubricCriterionRating(
                            id = "rating_1_$i",
                            description = "Rating $i",
                            points = i.toDouble() * ratingMultiplier
                    )
                }
        )
    }

}
