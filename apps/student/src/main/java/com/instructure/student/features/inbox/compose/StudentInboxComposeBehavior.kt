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

import android.util.Log
import com.google.mlkit.genai.common.FeatureStatus
import kotlinx.coroutines.withTimeoutOrNull
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import com.instructure.pandautils.features.inbox.compose.InboxComposeBehavior

class StudentInboxComposeBehavior : InboxComposeBehavior {

    private val generativeModel by lazy { Generation.getClient() }

    override suspend fun shouldHideSendIndividual(): Boolean = false

    override suspend fun suggestQuickReplies(conversationSnippet: String): List<String> {
        Log.d(TAG, "suggestQuickReplies called, snippet length=${conversationSnippet.length}")

        val status = generativeModel.checkStatus()
        Log.d(TAG, "checkStatus=$status (AVAILABLE=${FeatureStatus.AVAILABLE}, DOWNLOADABLE=${FeatureStatus.DOWNLOADABLE}, DOWNLOADING=${FeatureStatus.DOWNLOADING}, UNAVAILABLE=${FeatureStatus.UNAVAILABLE})")

        if (status != FeatureStatus.AVAILABLE) {
            Log.w(TAG, "Model not available (status=$status), skipping suggestions")
            return emptyList()
        }
        Log.d(TAG, "Model available, proceeding")

        val snippet = conversationSnippet.take(200)
        val prompt = "Write exactly 3 short reply messages to this message. One sentence each. Format as:\n1. first reply\n2. second reply\n3. third reply\n\nMessage: \"$snippet\""
        Log.d(TAG, "Sending prompt (${prompt.length} chars), calling generateContent...")

        val startTime = System.currentTimeMillis()
        val response = withTimeoutOrNull(30_000L) {
            generativeModel.generateContent(
                generateContentRequest(TextPart(prompt)) {
                    temperature = 0.3f
                    topK = 20
                    maxOutputTokens = 60
                }
            )
        }
        val elapsed = System.currentTimeMillis() - startTime
        Log.d(TAG, "generateContent returned in ${elapsed}ms")

        if (response == null) {
            Log.w(TAG, "generateContent timed out after 30s")
            return emptyList()
        }

        val text = response.candidates.firstOrNull()?.text
        Log.d(TAG, "Raw response: [$text]")

        if (text.isNullOrBlank()) {
            Log.w(TAG, "Empty response")
            return emptyList()
        }

        val replies = parseReplies(text)
        Log.d(TAG, "Parsed ${replies.size} replies: $replies")
        return replies
    }

    private fun parseReplies(response: String): List<String> {
        // Try numbered list first: "1. ...", "1) ...", "- ..."
        val numberedLines = response.lines()
            .map { it.trim() }
            .filter { it.matches(Regex("""^(\d+[.)]\s+|-\s+|\*\s+).+""")) }
            .map { it.replaceFirst(Regex("""^(\d+[.)]\s+|-\s+|\*\s+)"""), "").trim() }
            .filter { it.isNotBlank() }
            .take(3)

        if (numberedLines.isNotEmpty()) return numberedLines

        // Fallback: split by newlines, take non-blank lines
        return response.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length > 5 }
            .take(3)
    }

    companion object {
        private const val TAG = "QuickReplySuggestions"
    }
}