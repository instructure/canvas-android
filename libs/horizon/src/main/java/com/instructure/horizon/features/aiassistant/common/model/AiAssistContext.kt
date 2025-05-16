package com.instructure.horizon.features.aiassistant.common.model

data class AiAssistContext(
    val contextString: String? = null,
    val contextSources: Map<String, String> = emptyMap(),
)