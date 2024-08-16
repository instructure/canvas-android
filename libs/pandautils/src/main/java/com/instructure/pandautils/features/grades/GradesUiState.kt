package com.instructure.pandautils.features.grades

import android.graphics.Color
import androidx.annotation.DrawableRes


data class GradesUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isRefreshing: Boolean = false,
    val studentColor: Int = Color.BLACK,
    val items: List<AssignmentGroupUiState> = emptyList()
)

data class AssignmentGroupUiState(
    val id: Long,
    val name: String,
    val assignments: List<AssignmentUiState>,
    val expanded: Boolean
)

data class AssignmentUiState(
    @DrawableRes val iconRes: Int,
    val name: String,
    val dueDate: String?,
    val points: String?,
    val pointsPossible: String?
)

sealed class GradesAction {
    data object Refresh : GradesAction()
    data class HeaderClick(val id: Long) : GradesAction()
}