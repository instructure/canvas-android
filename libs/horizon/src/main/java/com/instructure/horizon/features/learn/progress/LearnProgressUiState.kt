package com.instructure.horizon.features.learn.progress

import com.instructure.horizon.horizonui.organisms.cards.ModuleHeaderState
import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCardState
import com.instructure.horizon.horizonui.platform.LoadingState

data class LearnProgressUiState(
    val courseId: Long = -1,
    val screenState: LoadingState = LoadingState(),
    val moduleItemStates: Map<ModuleHeaderState, List<ModuleItemCardState>> = emptyMap()
)
