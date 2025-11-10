package com.instructure.horizon.features.dashboard.widget.course.card

import com.instructure.horizon.model.LearningObjectType
import java.util.Date

data class DashboardCourseCardState(
    val parentPrograms: List<DashboardCourseCardParentProgramState>? = null,
    val imageState: DashboardCourseCardImageState? = null,
    val title: String? = null,
    val description: String? = null,
    val progress: Double? = null,
    val moduleItem: DashboardCourseCardModuleItemState? = null,
    val onClickAction: CardClickAction? = null,
) {
    companion object {
        val Loading = DashboardCourseCardState(
            parentPrograms = listOf(DashboardCourseCardParentProgramState("Loading Program", "1", CardClickAction.Action {})),
            imageState = DashboardCourseCardImageState(imageUrl = "url"),
            title = "Loading Course Title",
            progress = 20.0,
            moduleItem = DashboardCourseCardModuleItemState(
                moduleItemTitle = "Loading Module Item",
                moduleItemType = LearningObjectType.PAGE,
                onClickAction = CardClickAction.Action({})
            )
        )
    }
}

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