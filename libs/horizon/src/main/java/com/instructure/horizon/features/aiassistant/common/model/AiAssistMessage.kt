/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.aiassistant.common.model

import android.content.res.Resources
import com.instructure.canvasapi2.models.journey.assist.JourneyAssistChatMessage
import com.instructure.canvasapi2.models.journey.assist.JourneyAssistChipOption
import com.instructure.canvasapi2.models.journey.assist.JourneyAssistCitation
import com.instructure.canvasapi2.models.journey.assist.JourneyAssistCitationType
import com.instructure.canvasapi2.models.journey.assist.JourneyAssistFlashCard
import com.instructure.canvasapi2.models.journey.assist.JourneyAssistQuizItem
import com.instructure.canvasapi2.models.journey.assist.JourneyAssistRole
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.R

data class AiAssistMessage(
    val text: String = "",
    val role: JourneyAssistRole,
    val chipOptions: List<JourneyAssistChipOption> = emptyList(),
    val flashCards: List<JourneyAssistFlashCard> = emptyList(),
    val quizItems: List<JourneyAssistQuizItem> = emptyList(),
    val citations: List<JourneyAssistCitation> = emptyList(),
    val errorMessage: String? = null
)

fun List<JourneyAssistChatMessage>.toAiAssistMessages(): List<AiAssistMessage> {
    return this.map {
        AiAssistMessage(
            text = it.text,
            role = it.role,
        )
    }
}

fun List<AiAssistMessage>.toJourneyAssistChatMessages(): List<JourneyAssistChatMessage> {
    return this.map {
        JourneyAssistChatMessage(
            text = it.text,
            role = it.role,
        )
    }
}

fun JourneyAssistChipOption.toChipLabel(resources: Resources): String {
    val chipLabel = this.chipKey ?: this.chip
    return when (chipLabel) {
        ChipKey.SUMMARIZE.key -> resources.getString(R.string.ai_summarizeLabel)
        ChipKey.FLASHCARDS.key -> resources.getString(R.string.ai_generateFlashcardsLabel)
        ChipKey.QUIZ_ME.key -> resources.getString(R.string.ai_QuizMeLabel)
        ChipKey.GIVE_ME_KEY_TAKEAWAYS.key -> resources.getString(R.string.ai_giveMeKeyTakeawaysLabel)
        ChipKey.TELL_ME_MORE.key, ChipKey.TELL_ME_MORE_ABOUT_THIS_TOPIC.key -> resources.getString(R.string.ai_tellMeMoreLabel)
        ChipKey.COURSE_SELECTION.key -> resources.getString(R.string.ai_courseSelectionLabel)
        ChipKey.REPHRASE_THIS_SELECTION.key, ChipKey.REPHRASE_THIS_PAGE.key, ChipKey.REPHRASE_THIS_FILE.key -> resources.getString(R.string.ai_rephraseLabel)
        else -> chipLabel ?: this.prompt
    }
}

fun JourneyAssistCitation.toDeepLink(baseUrl: String = ApiPrefs.fullDomain): String {
    val objectType = when (this.sourceType) {
        JourneyAssistCitationType.WIKI_PAGE -> "pages"
        JourneyAssistCitationType.ATTACHMENT -> "files"
        else -> "unknown"
    }

    return "$baseUrl/courses/${this.courseID}/${objectType}/${this.sourceID}"
}

private enum class ChipKey(val key: String) {
    SUMMARIZE("summarize"),
    FLASHCARDS("flashcards"),
    QUIZ_ME("quizMe"),
    GIVE_ME_KEY_TAKEAWAYS("giveMeKeyTakeaways"),
    TELL_ME_MORE("tellMeMore"),
    TELL_ME_MORE_ABOUT_THIS_TOPIC("tellMeMoreAboutThisTopic"),
    COURSE_SELECTION("courseSelection"),
    REPHRASE_THIS_SELECTION("rephraseThisSelection"),
    REPHRASE_THIS_PAGE("rephraseThisPage"),
    REPHRASE_THIS_FILE("rephraseThisFile"),

}