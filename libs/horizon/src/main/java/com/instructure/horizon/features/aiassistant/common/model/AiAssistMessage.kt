package com.instructure.horizon.features.aiassistant.common.model

data class AiAssistMessage(
    val message: String,
    val role: AiAssistMessageRole,
)

sealed class AiAssistMessageRole {
    data object User: AiAssistMessageRole()
    data object Assistant: AiAssistMessageRole()
}