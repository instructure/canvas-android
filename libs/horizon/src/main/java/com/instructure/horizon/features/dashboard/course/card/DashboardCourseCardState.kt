package com.instructure.horizon.features.dashboard.course.card

import androidx.annotation.DrawableRes
import com.instructure.horizon.model.LearningObjectType
import java.util.Date

data class DashboardCourseCardState(
    val parentPrograms: List<DashboardCourseCardParentProgramState>? = null,
    val imageUrl: String? = null,
    val title: String,
    val description: String? = null,
    val progress: Double? = null,
    val moduleItem: DashboardCourseCardModuleItemState? = null,
    val buttonState: DashboardCourseCardButtonState? = null,
    val onClickAction: CardClickAction? = null,
    val lastAccessed: Date? = null,
)

data class DashboardCourseCardParentProgramState(
    val programName: String,
    val programId: String,
    val onClickAction: CardClickAction,
)

data class DashboardCourseCardModuleItemState(
    val moduleItemTitle: String,
    val moduleItemType: LearningObjectType,
    val dueDate: Date? = null,
    val estimatedDuration: String? = null,
    val onClickAction: CardClickAction,
)

data class DashboardCourseCardButtonState(
    val label: String,
    @DrawableRes val iconRes: Int? = null,
    val onClickAction: CardClickAction,
    val isLoading: Boolean = false,
    val action: suspend () -> Unit = { },
)

sealed class CardClickAction {
    data class NavigateToProgram(val programId: String): CardClickAction()
    data class NavigateToCourse(val courseId: Long): CardClickAction()
    data class NavigateToModuleItem(val courseId: Long, val moduleItemId: Long): CardClickAction()
    data class Action(val onClick: () -> Unit): CardClickAction()
}