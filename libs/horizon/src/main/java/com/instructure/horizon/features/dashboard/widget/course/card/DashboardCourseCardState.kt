package com.instructure.horizon.features.dashboard.widget.course.card

import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.model.LearningObjectType
import java.util.Date

data class DashboardCourseCardState(
    val chipState: DashboardCourseCardChipState? = null,
    val parentPrograms: List<DashboardCourseCardParentProgramState>? = null,
    val imageState: DashboardCourseCardImageState? = null,
    val title: String? = null,
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
    val onClickAction: CardClickAction,
    val isLoading: Boolean = false,
    val action: suspend () -> Unit = { },
)

data class DashboardCourseCardChipState(
    val label: String,
    val color: StatusChipColor,
)

data class DashboardCourseCardImageState(
    val imageUrl: String? = null,
    val showPlaceholder: Boolean = false,
)

sealed class CardClickAction {
    data class NavigateToProgram(val programId: String): CardClickAction()
    data class NavigateToCourse(val courseId: Long): CardClickAction()
    data class NavigateToModuleItem(val courseId: Long, val moduleItemId: Long): CardClickAction()
    data class Action(val onClick: () -> Unit): CardClickAction()
}