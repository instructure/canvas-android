package com.instructure.horizon.features.aiassistant.common.model

data class AiAssistContext(
    val contextString: String? = null,
    val contextSources: List<AiAssistContextSource> = emptyList(),
    val chatHistory: List<AiAssistMessage> = emptyList(),
) {
    fun isEmpty(): Boolean {
        return contextString.isNullOrEmpty() && chatHistory.isEmpty()
    }
}

sealed class AiAssistContextSource(val rawValue: String, open val id: String) {
    class Assignment(id: String): AiAssistContextSource("Assignment", id)
    class Page(id: String): AiAssistContextSource("Page", id)
    class CourseId(id: String): AiAssistContextSource("course-id", id)
    class ModuleId(id: String): AiAssistContextSource("module-id", id)

    fun toPair(): Pair<String, String> {
        return Pair(rawValue, id)
    }
}

fun List<AiAssistContextSource>.toMap(): Map<String, String> {
    return this.associate { it.toPair() }
}