/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.llm.tool

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.instructure.pandautils.features.llm.engine.LlmEngine
import org.json.JSONException
import org.json.JSONObject

sealed class ChatEvent {
    data class Token(val text: String) : ChatEvent()
    data class ToolCallDetected(val name: String, val arguments: Map<String, Any?>) : ChatEvent()
    data class ToolResult(val name: String, val result: String) : ChatEvent()
}

class LlmAssistant(
    private val engine: LlmEngine,
    private val tools: List<LlmTool>,
    private val systemContext: String = "",
    private val maxToolRounds: Int = 5
) {
    private var systemPromptSet = false

    fun chatStream(userMessage: String): Flow<ChatEvent> = flow {
        if (!systemPromptSet) {
            engine.setSystemPrompt(buildSystemPrompt())
            systemPromptSet = true
        }

        var currentMessage = userMessage
        var round = 0
        var hasToolResult = false

        while (true) {
            val buffer = StringBuilder()

            engine.sendUserPrompt(currentMessage).collect { token ->
                buffer.append(token)
                emit(ChatEvent.Token(token))
            }

            val fullResponse = buffer.toString()
            Log.d(TAG, "Full response (round $round): $fullResponse")

            // After feeding a tool result, don't parse for more tool calls —
            // just accept whatever the model says as the final answer.
            if (hasToolResult) {
                Log.d(TAG, "Post-tool-result round, accepting response as final answer")
                break
            }

            val toolCall = parseToolCall(fullResponse) ?: break

            if (++round > maxToolRounds) {
                Log.w(TAG, "Reached max tool rounds ($maxToolRounds)")
                break
            }

            Log.i(TAG, "Tool call #$round: ${toolCall.name}(${toolCall.arguments})")
            emit(ChatEvent.ToolCallDetected(toolCall.name, toolCall.arguments))

            val tool = tools.find { it.name == toolCall.name }
            if (tool == null) {
                currentMessage = "Error: Unknown tool '${toolCall.name}'. Answer the user directly."
                continue
            }

            val result = try {
                tool.execute(toolCall.arguments)
            } catch (e: Exception) {
                Log.e(TAG, "Tool execution failed", e)
                "ERROR: ${e.javaClass.simpleName}: ${e.message}"
            }

            Log.i(TAG, "Tool result (${result.length} chars): ${result.take(500)}")
            emit(ChatEvent.ToolResult(toolCall.name, result))

            hasToolResult = true
            currentMessage = "Here is the data you requested. Summarize it for the user.\n\n$result"
        }
    }

    private fun buildSystemPrompt(): String = buildString {
        appendLine("You are a helpful Canvas LMS assistant.")
        if (systemContext.isNotBlank()) {
            appendLine(systemContext)
        }
        appendLine()
        appendLine("You have access to the following tools:")
        appendLine()
        for (tool in tools) {
            appendLine("Tool: ${tool.name}")
            appendLine("Description: ${tool.description}")
            if (tool.parameters.isNotEmpty()) {
                appendLine("Parameters:")
                for (param in tool.parameters) {
                    val req = if (param.required) "required" else "optional"
                    appendLine("  - ${param.name} (${param.type}, $req): ${param.description}")
                }
            }
            appendLine()
        }
        appendLine("To call a tool, respond with ONLY a JSON object in this exact format:")
        appendLine("""{"tool": "tool_name", "arguments": {"param_name": value}}""")
        appendLine()
        appendLine("Do not add any other text when calling a tool.")
        appendLine("After receiving the tool result, analyze the data and give a helpful answer to the user.")
    }

    companion object {
        private const val TAG = "LlmAssistant"

        internal fun parseToolCall(response: String): LlmToolCall? {
            val trimmed = stripThinkingBlocks(response).trim()
                .removePrefix("```json").removePrefix("```")
                .removeSuffix("```")
                .trim()

            val jsonStr = extractJsonObject(trimmed) ?: return null

            return try {
                val json = JSONObject(jsonStr)
                val toolName = json.optString("tool", "").ifEmpty {
                    json.optString("name", "")
                }
                if (toolName.isEmpty()) return null

                val argsJson = json.optJSONObject("arguments")
                    ?: json.optJSONObject("parameters")
                    ?: JSONObject()

                val arguments = mutableMapOf<String, Any?>()
                for (key in argsJson.keys()) {
                    arguments[key] = when {
                        argsJson.isNull(key) -> null
                        else -> argsJson.get(key)
                    }
                }

                LlmToolCall(name = toolName, arguments = arguments)
            } catch (e: JSONException) {
                Log.d(TAG, "Not a tool call: ${e.message}")
                null
            }
        }

        private val THINK_PATTERN = Regex("<think>[\\s\\S]*?</think>", RegexOption.IGNORE_CASE)

        internal fun stripThinkingBlocks(text: String): String {
            var result = THINK_PATTERN.replace(text, "")
            // Handle unclosed <think> tag (model still reasoning, no final answer yet)
            val openIdx = result.lastIndexOf("<think>", ignoreCase = true)
            if (openIdx != -1) {
                result = result.substring(0, openIdx)
            }
            return result
        }

        private fun extractJsonObject(text: String): String? {
            val start = text.indexOf('{')
            if (start == -1) return null
            var depth = 0
            for (i in start until text.length) {
                when (text[i]) {
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) return text.substring(start, i + 1)
                    }
                }
            }
            return null
        }
    }
}