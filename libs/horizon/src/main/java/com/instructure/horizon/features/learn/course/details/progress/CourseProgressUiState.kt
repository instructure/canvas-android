package com.instructure.horizon.features.learn.course.details.progress

import com.instructure.horizon.horizonui.organisms.cards.ModuleHeaderState
import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCardState
import com.instructure.horizon.horizonui.platform.LoadingState

data class CourseProgressUiState(
    val courseId: Long = -1,
    val screenState: LoadingState = LoadingState(),
    val moduleItemStates: Map<Long, Pair<ModuleHeaderState, List<ModuleItemState>>> = emptyMap()
)

sealed class ModuleItemState {
    data class SubHeader(val subHeader: String): ModuleItemState()
    data class ModuleItemCard(val moduleItemId: Long, val cardState: ModuleItemCardState): ModuleItemState()
}