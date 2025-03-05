/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.teacher.features.submission

import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.teacher.R
import com.instructure.teacher.features.assignment.submission.SubmissionListFilter

data class SubmissionListUiState(
    val assignmentName: String,
    val courseColor: Color,
    val filter: SubmissionListFilter = SubmissionListFilter.ALL,
    val sections: List<CanvasContext> = emptyList(),
    val submissions: Map<Int, List<SubmissionUiState>> = emptyMap(),
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val error: Boolean = false,
    val actionHandler: (SubmissionListAction) -> Unit
)

data class SubmissionUiState(
    val submissionId: Long,
    val userName: String,
    val avatarUrl: String?,
    val tags: List<SubmissionTag>,
    val grade: String? = null
)

enum class SubmissionTag(@StringRes val text: Int, @DrawableRes val icon: Int? = null, @ColorRes val color: Int? = null) {
    SUBMITTED(R.string.submitted, R.drawable.ic_complete, R.color.textSuccess),
    LATE(R.string.late, R.drawable.ic_clock, R.color.textWarning),
    MISSING(R.string.missingTag, R.drawable.ic_no, R.color.textDanger),
    GRADED(R.string.graded, R.drawable.ic_complete_solid, R.color.textSuccess),
    NEEDS_GRADING(R.string.needsGrading, null, R.color.textWarning),
    EXCUSED(R.string.excused, R.drawable.ic_complete_solid, R.color.textWarning),
    NOT_SUBMITTED(R.string.notSubmitted, R.drawable.ic_no, R.color.textDark),
}

sealed class SubmissionListAction {
    data object Refresh : SubmissionListAction()
    data class SubmissionClicked(val submissionId: Long) : SubmissionListAction()
}

sealed class SubmissionListViewModelAction {
    data class RouteToSubmission(val courseId: Long,
                                 val assignmentId: Long,
                                 val selectedIdx: Int,
                                 val anonymousGrading: Boolean? = null,
                                 val filteredSubmissionIds: LongArray = longArrayOf(),
                                 val filter: SubmissionListFilter? = null,
                                 val filterValue: Double = 0.0) : SubmissionListViewModelAction()
}