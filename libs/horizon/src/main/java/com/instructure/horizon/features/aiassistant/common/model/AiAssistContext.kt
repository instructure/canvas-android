package com.instructure.horizon.features.aiassistant.common.model

import com.instructure.canvasapi2.models.journey.assist.JourneyAssistState

data class AiAssistContext(
    val contextString: String? = null,
    val contextSources: List<AiAssistContextSource> = emptyList(),
    val chatHistory: List<AiAssistMessage> = emptyList(),
) {
    val state: JourneyAssistState
        get() {
            return JourneyAssistState(
                courseID = contextSources.firstOrNull { it is AiAssistContextSource.Course }?.id,
                pageID = contextSources.firstOrNull { it is AiAssistContextSource.Page }?.id,
                fileID = contextSources.firstOrNull { it is AiAssistContextSource.File }?.id,
            )
    }
}

fun JourneyAssistState.toContextSourceList(): List<AiAssistContextSource> {
    return buildList {
        val courseId = this@toContextSourceList.courseID
        if (courseId != null) {
            add(AiAssistContextSource.Course(courseId))
        }
        val pageId = this@toContextSourceList.pageID
        if (pageId != null) {
            add(AiAssistContextSource.Page(pageId))
        }
        val fileId = this@toContextSourceList.fileID
        if (fileId != null) {
            add(AiAssistContextSource.File(fileId))
        }
    }
}

sealed class AiAssistContextSource(val rawValue: String, open val id: String) {
    class Assignment(id: String): AiAssistContextSource("Assignment", id)
    class Page(id: String): AiAssistContextSource("Page", id)
    class File(id: String): AiAssistContextSource("File", id)
    class Course(id: String): AiAssistContextSource("course-id", id)
    class Module(id: String): AiAssistContextSource("module-id", id)
    class ModuleItem(id: String): AiAssistContextSource("module-item-id", id)

    fun toPair(): Pair<String, String> {
        return Pair(rawValue, id)
    }
}

fun List<AiAssistContextSource>.toMap(): Map<String, String> {
    return this.associate { it.toPair() }
}