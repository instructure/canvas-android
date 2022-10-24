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
package com.instructure.teacher.features.postpolicies

import android.content.Context
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.textAndIconColor
import com.instructure.teacher.R
import com.instructure.teacher.features.postpolicies.ui.PostGradeViewState
import com.instructure.teacher.mobius.common.ui.Presenter

object PostGradePresenter : Presenter<PostGradeModel, PostGradeViewState> {
    override fun present(model: PostGradeModel, context: Context): PostGradeViewState {
        val courseColor = CanvasContext.emptyCourseContext(model.assignment.courseId).textAndIconColor
        if (model.isLoading) {
            return PostGradeViewState.Loading(courseColor)
        }

        // Get the status text, or return empty if no status (all posted/hidden)
        val statusText = getStatusText(model, context) ?: return emptyViewState(model, context)

        return PostGradeViewState.LoadedViewState(
            courseColor = courseColor,
            statusText = statusText,
            gradedOnlyText = getGradedOnlyText(model, context),
            specificSectionsVisible = model.specificSectionsVisible,
            postText = getPostButtonText(model, context),
            postProcessing = model.isProcessing,
            sections = model.sections.map { it.copy(courseColor = courseColor) }
        )
    }

    private fun emptyViewState(model: PostGradeModel, context: Context) = when {
        model.isHidingGrades -> PostGradeViewState.EmptyViewState(
            imageResId = R.drawable.ic_panda_all_hidden,
            emptyTitle = context.getString(R.string.postPolicyAllHiddenTitle),
            emptyMessage = context.getString(R.string.postPolicyAllHiddenMessage)
        )
        else -> PostGradeViewState.EmptyViewState(
            imageResId = R.drawable.ic_panda_all_posted,
            emptyTitle = context.getString(R.string.postPolicyAllPostedTitle),
            emptyMessage = context.getString(R.string.postPolicyAllPostedMessage)
        )
    }

    private fun getStatusText(model: PostGradeModel, context: Context): String? {
        val (posted, hidden) = model.submissions.partition { it.postedAt != null }

        return when {
            model.isHidingGrades -> {
                if (posted.isEmpty()) null else context.resources.getQuantityString(R.plurals.gradesPostedCount, posted.count(), posted.count())
            }
            else -> {
                if (hidden.isEmpty()) null else context.resources.getQuantityString(R.plurals.gradesHiddenCount, hidden.count(), hidden.count())
            }
        }
    }

    private fun getGradedOnlyText(model: PostGradeModel, context: Context): String? {
        return when {
            model.isHidingGrades -> null
            model.postGradedOnly -> context.getString(R.string.postToGradedTitle)
            else -> context.getString(R.string.postToEveryoneTitle)
        }
    }

    private fun getPostButtonText(model: PostGradeModel, context: Context): String? {
        return when {
            model.isProcessing -> null
            model.isHidingGrades -> context.getString(R.string.hideGradesTab)
            else -> context.getString(R.string.postGradesTab)
        }
    }
}
