package com.instructure.horizon.features.aiassistant.common.model

import android.content.Context
import android.os.Parcelable
import com.instructure.horizon.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class AiAssistMessage(
    val prompt: AiAssistMessagePrompt,
    val role: AiAssistMessageRole,
): Parcelable

@Parcelize
sealed class AiAssistMessagePrompt: Parcelable {

    @Parcelize
    data object Summarize: AiAssistMessagePrompt()

    @Parcelize
    data object TellMeMore: AiAssistMessagePrompt()

    @Parcelize
    data object KeyTakeAway: AiAssistMessagePrompt()

    @Parcelize
    data class Custom(val message: String): AiAssistMessagePrompt()
}

@Parcelize
sealed class AiAssistMessageRole: Parcelable {
    @Parcelize
    data object User: AiAssistMessageRole()

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