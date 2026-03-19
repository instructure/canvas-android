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
 */
package com.instructure.student.util

import android.content.Context
import android.util.Log
import com.instructure.pandautils.features.llm.engine.LlmEngine
import com.instructure.pandautils.features.llm.engine.LlmState
import com.instructure.pandautils.features.llm.engine.isModelLoaded
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmModelInitializer @Inject constructor(
    private val llmEngine: LlmEngine,
    @ApplicationContext private val context: Context
) {
    fun initialize(scope: CoroutineScope) {
        scope.launch {
            try {
                if (llmEngine.state.value.isModelLoaded) return@launch

                llmEngine.state.first { it is LlmState.Ready || it is LlmState.Error }
                if (llmEngine.state.value is LlmState.Error) return@launch

                val modelFile = withContext(Dispatchers.IO) {
                    val modelsDir = File(context.filesDir, "models").also { it.mkdirs() }
                    val target = File(modelsDir, MODEL_FILENAME)

                    if (target.exists() && target.length() > 0) {
                        Log.i(TAG, "Model already cached: ${target.length() / 1024 / 1024} MB")
                        return@withContext target
                    }

                    Log.i(TAG, "Downloading model from $MODEL_URL ...")
                    val client = OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(5, TimeUnit.MINUTES)
                        .followRedirects(true)
                        .build()

                    val request = Request.Builder().url(MODEL_URL).build()
                    val response = client.newCall(request).execute()

                    if (!response.isSuccessful) {
                        throw RuntimeException("Download failed: HTTP ${response.code}")
                    }

                    val body = response.body ?: throw RuntimeException("Empty response body")
                    val tempFile = File(modelsDir, "$MODEL_FILENAME.tmp")
                    body.byteStream().use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output, bufferSize = 256 * 1024)
                        }
                    }
                    tempFile.renameTo(target)
                    Log.i(TAG, "Download complete: ${target.length() / 1024 / 1024} MB")
                    target
                }

                Log.i(TAG, "Loading model: ${modelFile.name}")
                llmEngine.loadModel(modelFile.absolutePath)
                Log.i(TAG, "Model loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Model initialization failed", e)
            }
        }
    }

    companion object {
        private const val TAG = "LlmModelInitializer"
        private const val MODEL_URL = "https://huggingface.co/litert-community/Qwen3-0.6B/resolve/main/Qwen3-0.6B.litertlm"
        private const val MODEL_FILENAME = "Qwen3-0.6B.litertlm"
    }
}
