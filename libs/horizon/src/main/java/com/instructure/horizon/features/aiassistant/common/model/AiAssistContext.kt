package com.instructure.horizon.features.aiassistant.common.model

data class AiAssistContext(
    val contextString: String = "",
    val chatHistory: List<AiAssistMessage> = emptyList(),
)

data class AiAssistMessage(
    val message: String,
    val role: AiAssistMessageRole,
)

enum class AiAssistMessageRole {
    USER,
    ASSISTANT,
}