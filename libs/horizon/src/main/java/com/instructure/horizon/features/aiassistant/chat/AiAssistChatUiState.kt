package com.instructure.horizon.features.aiassistant.chat

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.canvasapi2.models.journey.JourneyAssistChatMessage

data class AiAssistChatUiState(
    val messages: List<JourneyAssistChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFeedbackEnabled: Boolean = false,
    val inputTextValue: TextFieldValue = TextFieldValue(""),
    val onInputTextChanged: (TextFieldValue) -> Unit = {},
    val onInputTextSubmitted: () -> Unit = {},
    val onClearChatHistory: () -> Unit = {},
    val onChipClicked: (String) -> Unit = {},
)
