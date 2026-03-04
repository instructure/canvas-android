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

data class LlmToolParam(
    val name: String,
    val type: String,
    val description: String,
    val required: Boolean = true
)

class LlmTool(
    val name: String,
    val description: String,
    val parameters: List<LlmToolParam>,
    val execute: suspend (arguments: Map<String, Any?>) -> String
)

data class LlmToolCall(
    val name: String,
    val arguments: Map<String, Any?>
)

class LlmToolBuilder(val name: String) {
    var description: String = ""
    private val params = mutableListOf<LlmToolParam>()
    private var executor: (suspend (Map<String, Any?>) -> String)? = null

    fun parameter(name: String, type: String, description: String, required: Boolean = true) {
        params.add(LlmToolParam(name, type, description, required))
    }

    fun execute(block: suspend (arguments: Map<String, Any?>) -> String) {
        executor = block
    }

    fun build(): LlmTool = LlmTool(
        name = name,
        description = description,
        parameters = params.toList(),
        execute = executor ?: error("Tool '$name' has no executor")
    )
}

class LlmToolsBuilder {
    private val tools = mutableListOf<LlmTool>()

    fun tool(name: String, block: LlmToolBuilder.() -> Unit) {
        tools.add(LlmToolBuilder(name).apply(block).build())
    }

    fun build(): List<LlmTool> = tools.toList()
}

fun buildTools(block: LlmToolsBuilder.() -> Unit): List<LlmTool> =
    LlmToolsBuilder().apply(block).build()