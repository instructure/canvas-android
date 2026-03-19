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
package com.instructure.llm.litert.internal

import android.content.Context
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.instructure.pandautils.features.llm.engine.LlmEngine
import com.instructure.pandautils.features.llm.engine.LlmState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
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

internal class LiteRtBridge private constructor(
    private val appContext: Context
) : LlmEngine {

    private val _state = MutableStateFlow<LlmState>(LlmState.Ready)
    override val state: StateFlow<LlmState> = _state.asStateFlow()

    @Volatile
    private var cancelGeneration = false

    private var engine: Engine? = null
    private var conversation: Conversation? = null
    private var systemPrompt: String? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private val liteRtDispatcher = Dispatchers.IO.limitedParallelism(1)
    private val scope = CoroutineScope(liteRtDispatcher + SupervisorJob())

    override suspend fun loadModel(modelPath: String) = withContext(liteRtDispatcher) {
        check(_state.value is LlmState.Ready) {
            "Cannot load model in ${_state.value::class.simpleName}!"
        }

        try {
            require(File(modelPath).exists()) { "Model file not found: $modelPath" }

            Log.i(TAG, "Loading LiteRT model from: $modelPath")
            _state.value = LlmState.LoadingModel

            val cacheDir = File(appContext.cacheDir, "litert").also { it.mkdirs() }

            val gpuConfig = EngineConfig(
                modelPath = modelPath,
                backend = Backend.GPU,
                cacheDir = cacheDir.absolutePath
            )

            val eng = Engine(gpuConfig)

            try {
                Log.i(TAG, "Attempting GPU backend...")
                eng.initialize()
                Log.i(TAG, "Engine initialized with GPU backend")
            } catch (gpuError: Exception) {
                Log.w(TAG, "GPU init failed, falling back to CPU", gpuError)
                eng.close()

                val cpuConfig = EngineConfig(
                    modelPath = modelPath,
                    backend = Backend.CPU,
                    cacheDir = cacheDir.absolutePath
                )
                val cpuEng = Engine(cpuConfig)
                cpuEng.initialize()
                engine = cpuEng
                Log.i(TAG, "Engine initialized with CPU backend")
                cancelGeneration = false
                _state.value = LlmState.ModelLoaded
                return@withContext
            }

            engine = eng
            cancelGeneration = false
            _state.value = LlmState.ModelLoaded
            Log.i(TAG, "Model loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model", e)
            _state.value = LlmState.Error(e)
            throw e
        }
    }

    override suspend fun setSystemPrompt(systemPrompt: String) = withContext(liteRtDispatcher) {
        require(systemPrompt.isNotBlank()) { "System prompt cannot be blank" }
        check(_state.value is LlmState.ModelLoaded) {
            "Cannot set system prompt in ${_state.value::class.simpleName}!"
        }

        val eng = engine ?: throw IllegalStateException("Engine not initialized")

        Log.i(TAG, "Setting system prompt and creating conversation...")
        this@LiteRtBridge.systemPrompt = systemPrompt

        conversation?.close()
        val convConfig = ConversationConfig(systemMessage = Message.of(systemPrompt))
        conversation = eng.createConversation(convConfig)
        Log.i(TAG, "Conversation created with system prompt")
        Unit
    }

    override fun sendUserPrompt(message: String, maxTokens: Int): Flow<String> = flow {
        require(message.isNotEmpty()) { "User prompt cannot be empty" }
        check(_state.value is LlmState.ModelLoaded) {
            "Cannot send prompt in ${_state.value::class.simpleName}!"
        }

        val eng = engine ?: throw IllegalStateException("Engine not initialized")

        try {
            cancelGeneration = false

            if (conversation == null) {
                conversation = eng.createConversation()
            }

            Log.i(TAG, "Sending user prompt...")
            _state.value = LlmState.Generating

            val responseFlow = conversation!!.sendMessageAsync(Message.of(message))
            responseFlow.collect { msg ->
                if (cancelGeneration) return@collect
                val text = msg.contents
                    .filterIsInstance<Content.Text>()
                    .joinToString("") { it.text }
                if (text.isNotEmpty()) emit(text)
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
    }.flowOn(liteRtDispatcher)

    override fun unloadModel() {
        cancelGeneration = true
        scope.launch {
            when (_state.value) {
                is LlmState.ModelLoaded -> {
                    Log.i(TAG, "Unloading model...")
                    closeResources()
                    _state.value = LlmState.Ready
                    Log.i(TAG, "Model unloaded")
                }
                is LlmState.Error -> {
                    Log.i(TAG, "Resetting error state...")
                    closeResources()
                    _state.value = LlmState.Ready
                }
                else -> Log.w(TAG, "Cannot unload in ${_state.value::class.simpleName}")
            }
        }
    }

    override fun destroy() {
        cancelGeneration = true
        scope.launch {
            closeResources()
        }
    }

    private fun closeResources() {
        try { conversation?.close() } catch (e: Exception) { Log.w(TAG, "Error closing conversation", e) }
        try { engine?.close() } catch (e: Exception) { Log.w(TAG, "Error closing engine", e) }
        conversation = null
        engine = null
        systemPrompt = null
    }

    companion object {
        private const val TAG = "LiteRtBridge"

        @Volatile
        private var instance: LiteRtBridge? = null

        internal fun getInstance(context: Context): LiteRtBridge =
            instance ?: synchronized(this) {
                instance ?: LiteRtBridge(context.applicationContext).also { instance = it }
            }
    }
}
