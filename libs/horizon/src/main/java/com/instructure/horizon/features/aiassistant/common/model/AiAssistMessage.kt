package com.instructure.horizon.features.aiassistant.common.model

import android.content.Context
import com.instructure.horizon.R
import kotlinx.serialization.Serializable

@Serializable
data class AiAssistMessage(
    val prompt: AiAssistMessagePrompt,
    val role: AiAssistMessageRole,
)

@Serializable
sealed class AiAssistMessagePrompt {

    @Serializable
    data object Summarize: AiAssistMessagePrompt()

    @Serializable
    data object TellMeMore: AiAssistMessagePrompt()

    @Serializable
    data object KeyTakeAway: AiAssistMessagePrompt()

    @Serializable
    data class Custom(val message: String): AiAssistMessagePrompt()
}

@Serializable
sealed class AiAssistMessageRole {
    @Serializable
    data object User: AiAssistMessageRole()

    @Serializable
    data object Assistant: AiAssistMessageRole()
}

fun AiAssistMessagePrompt.toDisplayText(context: Context): String {
    return when(this) {
        is AiAssistMessagePrompt.Custom -> this.message
        is AiAssistMessagePrompt.Summarize -> context.getString(R.string.ai_summarize)
        is AiAssistMessagePrompt.TellMeMore -> context.getString(R.string.ai_tellMeMore)
        is AiAssistMessagePrompt.KeyTakeAway -> context.getString(R.string.ai_giveMeKeyTakeaways)
    }
}