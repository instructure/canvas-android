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

import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setTextForVisibility
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.adapter.BasicItemBinder
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.RubricListData
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui.RubricListCallback
import kotlinx.android.synthetic.main.adapter_rubric_criterion.view.*

class RubricListCriterionBinder : BasicItemBinder<RubricListData.Criterion, RubricListCallback>() {
    override val layoutResId = R.layout.adapter_rubric_criterion
    override val bindBehavior = Item { data, view, callback ->
        with(view) {
            criterionDescription.text = data.description
            selectedRatingDescription.setTextForVisibility(data.ratingDescription)
            ratingLayout.setVisible(data.ratings.isNotEmpty())
            ratingLayout.setRatingData(data.ratings)
            commentContainer.setVisible(data.comment != null)
            comment.text = data.comment
            viewLongDescriptionButton.setVisible(data.showLongDescriptionButton).onClick {
                callback.longDescriptionClicked(data.criterionId)
            }
            bottomPadding.setVisible(!data.showLongDescriptionButton)
        }
    }
}
