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
package com.instructure.llm.llamacpp.internal

import android.content.Context
import android.util.Log
import com.instructure.pandautils.features.llm.engine.LlmEngine
import com.instructure.pandautils.features.llm.engine.LlmState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

internal class LlamaCppBridge private constructor(
    private val nativeLibDir: String
) : LlmEngine {

    private external fun nativeInit(nativeLibDir: String)
    private external fun nativeLoadModel(modelPath: String): Int
    private external fun nativePrepare(): Int
    private external fun nativeSystemInfo(): String
    private external fun nativeProcessSystemPrompt(systemPrompt: String): Int
    private external fun nativeProcessUserPrompt(userPrompt: String, predictLength: Int): Int
    private external fun nativeGenerateNextToken(): String?
    private external fun nativeUnload()
    private external fun nativeShutdown()

    private val _state = MutableStateFlow<LlmState>(LlmState.Uninitialized)
    override val state: StateFlow<LlmState> = _state.asStateFlow()

    @Volatile
    private var cancelGeneration = false
    private var readyForSystemPrompt = false

    @OptIn(ExperimentalCoroutinesApi::class)
    private val llamaDispatcher = Dispatchers.IO.limitedParallelism(1)
    private val llamaScope = CoroutineScope(llamaDispatcher + SupervisorJob())

    init {
        llamaScope.launch {
            try {
                check(_state.value is LlmState.Uninitialized) {
                    "Cannot init in ${_state.value::class.simpleName}!"
                }
                _state.value = LlmState.Initializing
                Log.i(TAG, "Loading native library...")
                System.loadLibrary("llm-bridge")
                nativeInit(nativeLibDir)
                _state.value = LlmState.Ready
                Log.i(TAG, "Native library loaded! System info: \n${nativeSystemInfo()}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load native library", e)
                _state.value = LlmState.Error(e)
            }
        }
    }

    override suspend fun loadModel(modelPath: String) = withContext(llamaDispatcher) {
        check(_state.value is LlmState.Ready) {
            "Cannot load model in ${_state.value::class.simpleName}!"
        }

        try {
            val file = File(modelPath)
            require(file.exists()) { "Model file not found: $modelPath" }
            require(file.canRead()) { "Cannot read model file: $modelPath" }

            Log.i(TAG, "Loading model from: $modelPath")
            readyForSystemPrompt = false
            _state.value = LlmState.LoadingModel

            val loadResult = nativeLoadModel(modelPath)
            if (loadResult != 0) throw RuntimeException("Failed to load model (error code: $loadResult)")

            val prepareResult = nativePrepare()
            if (prepareResult != 0) throw RuntimeException("Failed to prepare context (error code: $prepareResult)")

            Log.i(TAG, "Model loaded successfully")
            readyForSystemPrompt = true
            cancelGeneration = false
            _state.value = LlmState.ModelLoaded
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model", e)
            _state.value = LlmState.Error(e)
            throw e
        }
    }

    override suspend fun setSystemPrompt(systemPrompt: String) = withContext(llamaDispatcher) {
        require(systemPrompt.isNotBlank()) { "System prompt cannot be blank" }
        check(readyForSystemPrompt) { "System prompt must be set right after model is loaded" }
        check(_state.value is LlmState.ModelLoaded) {
            "Cannot set system prompt in ${_state.value::class.simpleName}!"
        }

        Log.i(TAG, "Processing system prompt...")
        readyForSystemPrompt = false
        val result = nativeProcessSystemPrompt(systemPrompt)
        if (result != 0) {
            val error = RuntimeException("Failed to process system prompt (error code: $result)")
            _state.value = LlmState.Error(error)
            throw error
        }
        Log.i(TAG, "System prompt processed")
        Unit
    }

    override fun sendUserPrompt(message: String, maxTokens: Int): Flow<String> = flow {
        require(message.isNotEmpty()) { "User prompt cannot be empty" }
        check(_state.value is LlmState.ModelLoaded) {
            "Cannot send prompt in ${_state.value::class.simpleName}!"
        }

        try {
            Log.i(TAG, "Processing user prompt...")
            readyForSystemPrompt = false
            cancelGeneration = false

            val processResult = nativeProcessUserPrompt(message, maxTokens)
            if (processResult != 0) {
                Log.e(TAG, "Failed to process user prompt: $processResult")
                return@flow
            }

            Log.i(TAG, "Generating response...")
            _state.value = LlmState.Generating
            while (!cancelGeneration) {
                val token = nativeGenerateNextToken() ?: break
                if (token.isNotEmpty()) emit(token)
            }
            _state.value = LlmState.ModelLoaded
        } catch (e: CancellationException) {
            Log.i(TAG, "Generation cancelled")
            _state.value = LlmState.ModelLoaded
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error during generation", e)
            _state.value = LlmState.Error(e)
            throw e
        }
    }.flowOn(llamaDispatcher)

    override fun unloadModel() {
        cancelGeneration = true
        runBlocking(llamaDispatcher) {
            when (_state.value) {
                is LlmState.ModelLoaded -> {
                    Log.i(TAG, "Unloading model...")
                    readyForSystemPrompt = false
                    nativeUnload()
                    _state.value = LlmState.Ready
                    Log.i(TAG, "Model unloaded")
                }
                is LlmState.Error -> {
                    Log.i(TAG, "Resetting error state...")
                    _state.value = LlmState.Ready
                }
                else -> throw IllegalStateException("Cannot unload in ${_state.value::class.simpleName}")
            }
        }
    }

    override fun destroy() {
        cancelGeneration = true
        runBlocking(llamaDispatcher) {
            readyForSystemPrompt = false
            when (_state.value) {
                is LlmState.Uninitialized, is LlmState.Initializing -> {}
                is LlmState.Ready -> nativeShutdown()
                else -> {
                    // Safe even if model loaded but context/sampler not yet prepared —
                    // native code has null guards.
                    nativeUnload()
                    nativeShutdown()
                }
            }
        }
        llamaScope.cancel()
    }

    companion object {
        private const val TAG = "LlamaCppBridge"

        @Volatile
        private var instance: LlamaCppBridge? = null

        internal fun getInstance(context: Context): LlamaCppBridge =
            instance ?: synchronized(this) {
                instance ?: run {
                    val nativeLibDir = context.applicationInfo.nativeLibraryDir
                    require(nativeLibDir.isNotBlank()) { "Invalid native library path" }
                    LlamaCppBridge(nativeLibDir).also { instance = it }
                }
            }
    }
}