package com.instructure.horizon.features.aiassistant.common.model

import kotlinx.serialization.Serializable

@Serializable
data class AiAssistContext(
    val contextString: String? = null,
    val contextSources: Map<String, String> = emptyMap(),
    val chatHistory: List<AiAssistMessage> = emptyList(),
) {
    fun isEmpty(): Boolean {
        return contextString.isNullOrEmpty() && contextSources.isEmpty() && chatHistory.isEmpty()
    }
}