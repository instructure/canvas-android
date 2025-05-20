package com.instructure.horizon.features.aiassistant.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AiAssistContext(
    val contextString: String? = null,
    val contextSources: Map<String, String> = emptyMap(),
    val chatHistory: List<AiAssistMessage> = emptyList(),
): Parcelable {
    fun isEmpty(): Boolean {
        return contextString.isNullOrEmpty() && contextSources.isEmpty() && chatHistory.isEmpty()
    }
}