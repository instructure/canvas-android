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
    val onClick: (() -> Unit)? = null,
    val lastAccessed: Date? = null,
)

data class DashboardCourseCardParentProgramState(
    val programName: String,
    val programId: String,
    val onClick: () -> Unit,
)

data class DashboardCourseCardModuleItemState(
    val moduleItemTitle: String,
    val moduleItemType: LearningObjectType,
    val dueDate: Date? = null,
    val estimatedDuration: String? = null,
    val onClick: () -> Unit,
)

data class DashboardCourseCardButtonState(
    val label: String,
    @DrawableRes val iconRes: Int? = null,
    val onClick: () -> Unit,
)

sealed class CardAction {
    data class Navigate(val route: String) : CardAction()
    data class Action(val action: () -> Unit) : CardAction()
    data object None : CardAction()
}