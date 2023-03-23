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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui.binders

import android.animation.LayoutTransition
import android.os.Handler
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.animation.AccelerateDecelerateInterpolator
import com.instructure.canvasapi2.utils.isValid
import com.instructure.pandautils.adapters.BasicItemBinder
import com.instructure.pandautils.utils.asStateList
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setTextForVisibility
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.databinding.AdapterRubricCriterionBinding
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.RubricListData.Criterion
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui.RubricListCallback

class RubricListCriterionBinder : BasicItemBinder<Criterion, RubricListCallback>() {
    override val layoutResId = R.layout.adapter_rubric_criterion

    override fun initView(view: View) {
        val binding = AdapterRubricCriterionBinding.bind(view)
        val transition = LayoutTransition().apply {
            enableTransitionType(LayoutTransition.CHANGING)
            val interpolator = AccelerateDecelerateInterpolator()
            setInterpolator(LayoutTransition.APPEARING, interpolator)
            setInterpolator(LayoutTransition.DISAPPEARING, interpolator)
            setInterpolator(LayoutTransition.CHANGE_APPEARING, interpolator)
            setInterpolator(LayoutTransition.CHANGE_DISAPPEARING, interpolator)
            setInterpolator(LayoutTransition.CHANGING, interpolator)
            setDuration(250)
        }
        binding.ratingInfoContainer.layoutTransition = transition
        binding.rubricCriterion.layoutTransition = transition
    }

    override val bindBehavior = Item { data, callback, diff ->
        val binding = AdapterRubricCriterionBinding.bind(this)
        // If diff is not null, only perform partial bind with changes
        with (binding) {
            diff?.apply {
                ratingTitle.setTextForVisibility(newItem.ratingTitle)
                ratingDescription.setTextForVisibility(newItem.ratingDescription)
                ratingInfoContainer.setVisible(newItem.ratingTitle.isValid() || newItem.ratingDescription.isValid())
                ratingLayout.updateRatingData(newItem.ratings)
                ratingInfoContainer.contentDescription = context.getString(
                    R.string.a11y_criterion_description_content_description,
                    newItem.ratingTitle,
                    newItem.ratingDescription
                )

                //We need this delay to allow TalkBack to focus on the view.
                Handler().apply {
                    postDelayed({
                        ratingInfoContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                    }, 1000)
                }
                return@Item
            }

            // Otherwise, perform full bind
            criterionTitle.text = data.title
            ratingLayout.setVisible(data.ratings.isNotEmpty())
            ratingLayout.setRatingData(data.ratings, data.tint) { callback.ratingClicked(data.criterionId, it) }
            ratingTitle.setTextForVisibility(data.ratingTitle)
            ratingDescription.setTextForVisibility(data.ratingDescription)
            ratingInfoContainer
                .setVisible(data.ratingTitle.isValid() || data.ratingDescription.isValid())
                .backgroundTintList = data.tint.asStateList()
            commentContainer.setVisible(data.comment != null)
            comment.text = data.comment
            descriptionButton.setVisible(data.showDescriptionButton).onClick {
                callback.longDescriptionClicked(data.criterionId)
            }
        }
    }

    override fun getItemId(item: Criterion) = item.criterionId.hashCode().toLong()
}
