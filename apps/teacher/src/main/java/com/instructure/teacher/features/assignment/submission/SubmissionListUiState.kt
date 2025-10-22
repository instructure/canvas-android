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
package com.instructure.teacher.features.assignment.submission

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.Section
import com.instructure.pandautils.features.speedgrader.SubmissionListFilter
import com.instructure.teacher.R

data class SubmissionListUiState(
    val headerTitle: String,
    val searchQuery: String = "",
    val filtersUiState: SubmissionListFiltersUiState,
    val submissions: List<SubmissionUiState> = emptyList(),
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val error: Boolean = false
)

data class CustomGradeStatus(
    val id: String,
    val name: String
)

data class SubmissionUiState(
    val submissionId: Long,
    val assigneeId: Long,
    val userName: String,
    val isFakeStudent: Boolean,
    val avatarUrl: String?,
    val tags: List<SubmissionTag>,
    val grade: String? = null,
    val hidden: Boolean = false,
    val group: Boolean = false
)

sealed class SubmissionTag {
    abstract val icon: Int?
    abstract val color: Int?

    data class Predefined(
        @StringRes val textRes: Int,
        @DrawableRes override val icon: Int?,
        @ColorRes override val color: Int?
    ) : SubmissionTag()

    data class Custom(
        val text: String,
        @DrawableRes override val icon: Int?,
        @ColorRes override val color: Int?
    ) : SubmissionTag()

    companion object {
        val Submitted = Predefined(R.string.submitted, R.drawable.ic_complete, R.color.textSuccess)
        val Late = Predefined(R.string.late, R.drawable.ic_clock, R.color.textWarning)
        val Missing = Predefined(R.string.missingTag, R.drawable.ic_no, R.color.textDanger)
        val Graded = Predefined(R.string.graded, R.drawable.ic_complete_solid, R.color.textSuccess)
        val NeedsGrading = Predefined(R.string.needsGrading, null, R.color.textWarning)
        val Excused = Predefined(R.string.excused, R.drawable.ic_complete_solid, R.color.textWarning)
        val NotSubmitted = Predefined(R.string.notSubmitted, R.drawable.ic_no, R.color.textDark)
    }
}

sealed class SubmissionListAction {
    data object Refresh : SubmissionListAction()
    data class SubmissionClicked(val submissionId: Long) : SubmissionListAction()
    data class Search(val query: String) : SubmissionListAction()
    data class SetFilters(
        val selectedFilters: Set<SubmissionListFilter>,
        val filterValueAbove: Double?,
        val filterValueBelow: Double?,
        val selectedSections: List<Long>,
        val selectedDifferentiationTagIds: Set<String>,
        val includeStudentsWithoutTags: Boolean,
        val sortOrder: SubmissionSortOrder,
        val selectedCustomStatusIds: Set<String>
    ) : SubmissionListAction()

    data object ShowPostPolicy : SubmissionListAction()
    data object SendMessage : SubmissionListAction()
    data class AvatarClicked(val userId: Long) : SubmissionListAction()
}

sealed class SubmissionListViewModelAction {
    data class RouteToSubmission(
        val courseId: Long,
        val assignmentId: Long,
        val selectedIdx: Int,
        val anonymousGrading: Boolean? = null,
        val filteredSubmissionIds: LongArray = longArrayOf(),
        val selectedFilters: Set<SubmissionListFilter> = setOf(SubmissionListFilter.ALL),
        val filterValueAbove: Double? = null,
        val filterValueBelow: Double? = null
    ) : SubmissionListViewModelAction()

    data class ShowPostPolicy(val course: Course, val assignment: Assignment) :
        SubmissionListViewModelAction()

    data class SendMessage(
        val contextCode: String,
        val contextName: String,
        val recipients: List<Recipient>,
        val subject: String
    ) : SubmissionListViewModelAction()

    data class RouteToUser(val userId: Long, val courseId: Long) : SubmissionListViewModelAction()
}