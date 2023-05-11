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
package com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.emeritus.student.R
import com.instructure.pandautils.adapters.BasicItemCallback
import com.instructure.pandautils.adapters.BasicRecyclerAdapter
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.RubricListData
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricEvent
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricViewState
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui.binders.RubricListCriterionBinder
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui.binders.RubricListEmptyBinder
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui.binders.RubricListGradeBinder
import com.emeritus.student.mobius.common.ui.MobiusView
import com.emeritus.student.router.RouteMatcher
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_submission_rubric.*

class SubmissionRubricView(
    inflater: LayoutInflater,
    parent: ViewGroup
) : MobiusView<SubmissionRubricViewState, SubmissionRubricEvent>(
    R.layout.fragment_submission_rubric,
    inflater,
    parent
) {

    private val adapter = RubricRecyclerAdapter(object : RubricListCallback {
        override fun longDescriptionClicked(criterionId: String) {
            consumer?.accept(SubmissionRubricEvent.LongDescriptionClicked(criterionId))
        }

        override fun ratingClicked(criterionId: String, ratingId: String) {
            consumer?.accept(SubmissionRubricEvent.RatingClicked(criterionId, ratingId))
        }
    })

    init {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    override fun onConnect(output: Consumer<SubmissionRubricEvent>) = Unit

    override fun render(state: SubmissionRubricViewState) {
        adapter.data = state.listData
    }

    override fun onDispose() = Unit

    override fun applyTheme() = Unit

    fun displayCriterionDescription(title: String, description: String) {
        RouteMatcher.route(context,
            SubmissionRubricDescriptionFragment.makeRoute(title, description)
        )
    }
}

interface RubricListCallback : BasicItemCallback {
    fun longDescriptionClicked(criterionId: String)
    fun ratingClicked(criterionId: String, ratingId: String)
}

class RubricRecyclerAdapter(callback: RubricListCallback) :
    BasicRecyclerAdapter<RubricListData, RubricListCallback>(callback) {

    override fun registerBinders() {
        register(RubricListEmptyBinder())
        register(RubricListGradeBinder())
        register(RubricListCriterionBinder())
    }

}
