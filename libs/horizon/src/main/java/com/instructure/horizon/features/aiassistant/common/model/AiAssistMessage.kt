package com.instructure.horizon.features.aiassistant.common.model

import android.content.Context
import android.os.Parcelable
import com.instructure.horizon.R
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class AiAssistMessage(
    val prompt: AiAssistMessagePrompt,
    val role: AiAssistMessageRole,
): Parcelable

@Serializable
@Parcelize
sealed class AiAssistMessagePrompt: Parcelable {

    @Serializable
    @Parcelize
    data object Summarize: AiAssistMessagePrompt()

    @Serializable
    @Parcelize
    data object TellMeMore: AiAssistMessagePrompt()

    @Serializable
    @Parcelize
    data object KeyTakeAway: AiAssistMessagePrompt()

    @Serializable
    @Parcelize
    data class Custom(val message: String): AiAssistMessagePrompt()
}

@Serializable
@Parcelize
sealed class AiAssistMessageRole: Parcelable {
    @Serializable
    @Parcelize
    data object User: AiAssistMessageRole()

    @Serializable
    @Parcelize
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