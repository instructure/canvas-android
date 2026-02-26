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
package com.instructure.pandautils.features.llm.engine

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LlmEngine {

    val state: StateFlow<LlmState>

    suspend fun loadModel(modelPath: String)

    suspend fun setSystemPrompt(systemPrompt: String)

    fun sendUserPrompt(message: String, maxTokens: Int = DEFAULT_MAX_TOKENS): Flow<String>

    suspend fun complete(userMessage: String, maxTokens: Int = DEFAULT_MAX_TOKENS): String {
        val sb = StringBuilder()
        sendUserPrompt(userMessage, maxTokens).collect { sb.append(it) }
        return sb.toString()
    }

    fun unloadModel()

    fun destroy()

    companion object {
        const val DEFAULT_MAX_TOKENS = 1024
    }
}

sealed class LlmState {
    data object Uninitialized : LlmState()
    data object Initializing : LlmState()
    data object Ready : LlmState()
    data object LoadingModel : LlmState()
    data object ModelLoaded : LlmState()
    data object Generating : LlmState()
    data class Error(val exception: Exception) : LlmState()
}

val LlmState.isModelLoaded: Boolean
    get() = this is LlmState.ModelLoaded || this is LlmState.Generating