package com.instructure.horizon.features.aiassistant.chat

import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessage

data class AiAssistChatUiState(
    val messages: List<AiAssistMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFeedbackEnabled: Boolean = false,
    val onClearChatHistory: () -> Unit = {},
    val onChipClicked: (String) -> Unit = {},
    val onNavigateToCards: () -> Unit = {},
)
