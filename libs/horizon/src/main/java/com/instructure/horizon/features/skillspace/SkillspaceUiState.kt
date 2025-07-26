package com.instructure.horizon.features.skillspace

import com.instructure.horizon.horizonui.platform.LoadingState

data class SkillspaceUiState(
    val loadingState: LoadingState = LoadingState(),
    val webviewUrl: String? = null,
)
