package com.instructure.horizon.features.aiassistant.chat

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContext

data class AiAssistChatUiState(
    val messages: AiAssistContext = AiAssistContext(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFeedbackEnabled: Boolean = false,
    val inputTextValue: TextFieldValue = TextFieldValue(""),
    val onInputTextChanged: (TextFieldValue) -> Unit = {},
    val onInputTextSubmitted: () -> Unit = {},
)
