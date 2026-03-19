/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.student.features.inbox.compose

import com.instructure.pandautils.features.inbox.compose.InboxComposeBehavior
import com.instructure.pandautils.features.llm.engine.LlmEngine
import com.instructure.pandautils.features.llm.engine.LlmState
import com.instructure.pandautils.features.llm.engine.isModelLoaded
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class StudentInboxComposeBehavior(
    private val llmEngine: LlmEngine
) : InboxComposeBehavior {

    override suspend fun shouldHideSendIndividual(): Boolean = false

    override suspend fun suggestQuickReplies(conversationSnippet: String): List<String> {
        if (!llmEngine.state.value.isModelLoaded) {
            val ready = withTimeoutOrNull(60_000L) {
                llmEngine.state.first { it is LlmState.ModelLoaded || it is LlmState.Error }
            }
            if (ready == null || ready is LlmState.Error) return emptyList()
        }

        val snippet = conversationSnippet.take(200)
        val prompt = "/no_think\nReply options for: \"$snippet\"\n1."

        val raw = llmEngine.complete(prompt, maxTokens = 60)
        val cleaned = raw.replace(Regex("<think>[\\s\\S]*?</think>"), "").trim()
        return parseNumberedList("1.$cleaned")
    }

    private fun parseNumberedList(response: String): List<String> {
        return response.lines()
            .map { it.trim() }
            .filter { it.matches(Regex("""^\d+[.)]\s+.+""")) }
            .map { it.replaceFirst(Regex("""^\d+[.)]\s+"""), "").trim() }
            .filter { it.isNotBlank() }
            .take(3)
    }
}
