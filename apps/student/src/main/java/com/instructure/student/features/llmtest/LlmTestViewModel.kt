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
package com.instructure.student.features.llmtest

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.pandautils.domain.usecase.announcements.LoadCourseAnnouncementsUseCase
import com.instructure.pandautils.domain.usecase.assignment.LoadAssignmentGroupsUseCase
import com.instructure.pandautils.features.llm.engine.LlmEngine
import com.instructure.pandautils.features.llm.engine.LlmState
import com.instructure.pandautils.features.llm.tool.ChatEvent
import com.instructure.pandautils.features.llm.tool.LlmAssistant
import com.instructure.pandautils.features.llm.tool.buildTools
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class LlmTestViewModel @Inject constructor(
    private val llmEngine: LlmEngine,
    private val loadAssignmentGroupsUseCase: LoadAssignmentGroupsUseCase,
    private val loadCourseAnnouncementsUseCase: LoadCourseAnnouncementsUseCase,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LlmTestUiState())
    val uiState: StateFlow<LlmTestUiState> = _uiState.asStateFlow()

    private var assistant: LlmAssistant? = null

    private val tools = buildTools {
        tool("get_assignment_groups") {
            description = "Get all assignment groups and their assignments for a course, including names, due dates, and points"
            parameter("course_id", "integer", "The ID of the course")
            execute { args ->
                val rawId = args["course_id"]
                Log.d(TAG, "get_assignment_groups called with course_id=$rawId (${rawId?.javaClass?.simpleName})")
                val courseId = when (rawId) {
                    is Number -> rawId.toLong()
                    is String -> rawId.toLongOrNull() ?: error("Invalid course_id: $rawId")
                    else -> error("Missing or invalid course_id: $rawId")
                }
                Log.i(TAG, "Fetching assignment groups for course $courseId...")
                val groups = loadAssignmentGroupsUseCase(
                    LoadAssignmentGroupsUseCase.Params(courseId, forceNetwork = true)
                )
                Log.i(TAG, "Got ${groups.size} assignment groups")
                formatAssignmentGroups(groups)
            }
        }
        tool("get_latest_announcement") {
            description = "Get the latest unread announcement for a course, including its title and full message content"
            parameter("course_id", "integer", "The ID of the course")
            execute { args ->
                val rawId = args["course_id"]
                Log.d(TAG, "get_latest_announcement called with course_id=$rawId (${rawId?.javaClass?.simpleName})")
                val courseId = when (rawId) {
                    is Number -> rawId.toLong()
                    is String -> rawId.toLongOrNull() ?: error("Invalid course_id: $rawId")
                    else -> error("Missing or invalid course_id: $rawId")
                }
                Log.i(TAG, "Fetching announcements for course $courseId...")
                val announcements = loadCourseAnnouncementsUseCase(
                    LoadCourseAnnouncementsUseCase.Params(courseId, forceNetwork = true)
                )
                Log.i(TAG, "Got ${announcements.size} unread announcements")
                formatLatestAnnouncement(announcements)
            }
        }
    }

    init {
        viewModelScope.launch {
            llmEngine.state.collect { engineState ->
                _uiState.update { it.fromEngineState(engineState) }
            }
        }
        autoLoadModel()
    }

    private fun autoLoadModel() {
        viewModelScope.launch {
            try {
                llmEngine.state.first { it is LlmState.Ready || it is LlmState.Error }
                if (llmEngine.state.value is LlmState.Error) return@launch

                val modelFile = withContext(Dispatchers.IO) {
                    val modelsDir = File(appContext.filesDir, "models").also { it.mkdirs() }
                    val target = File(modelsDir, MODEL_FILENAME)

                    if (target.exists() && target.length() > 0) {
                        Log.i(TAG, "Model already downloaded: ${target.length() / 1024 / 1024} MB")
                        return@withContext target
                    }

                    Log.i(TAG, "Downloading model from $MODEL_URL ...")
                    _uiState.update { it.copy(stateLabel = "Downloading model...", isLoading = true, downloadProgress = 0f) }

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
                    val contentLength = body.contentLength()
                    Log.i(TAG, "Download size: ${contentLength / 1024 / 1024} MB")

                    val tempFile = File(modelsDir, "$MODEL_FILENAME.tmp")
                    body.byteStream().use { input ->
                        FileOutputStream(tempFile).use { output ->
                            val buffer = ByteArray(256 * 1024)
                            var bytesRead: Long = 0
                            var len: Int
                            while (input.read(buffer).also { len = it } != -1) {
                                output.write(buffer, 0, len)
                                bytesRead += len
                                if (contentLength > 0) {
                                    val progress = bytesRead.toFloat() / contentLength
                                    _uiState.update { it.copy(
                                        stateLabel = "Downloading model... ${(progress * 100).toInt()}%",
                                        downloadProgress = progress
                                    )}
                                }
                            }
                        }
                    }

                    tempFile.renameTo(target)
                    Log.i(TAG, "Download complete: ${target.length() / 1024 / 1024} MB")
                    _uiState.update { it.copy(downloadProgress = null) }
                    target
                }

                _uiState.update { it.copy(modelName = modelFile.name) }
                Log.i(TAG, "Loading model: ${modelFile.name}")
                llmEngine.loadModel(modelFile.absolutePath)

                assistant = LlmAssistant(engine = llmEngine, tools = tools)
            } catch (e: Exception) {
                Log.e(TAG, "Auto-load failed", e)
                _uiState.update { it.copy(error = e.message, isLoading = false, downloadProgress = null) }
            }
        }
    }

    fun onModelSelected(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _uiState.update { it.copy(stateLabel = "Copying model...", isLoading = true, error = null) }

            try {
                val modelFile = withContext(Dispatchers.IO) {
                    val modelsDir = File(appContext.filesDir, "models").also { it.mkdirs() }
                    val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "model.gguf"
                    val target = File(modelsDir, fileName)

                    if (!target.exists()) {
                        Log.i(TAG, "Copying model to ${target.absolutePath}...")
                        contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(target).use { output -> input.copyTo(output) }
                        }
                        Log.i(TAG, "Copy done (${target.length() / 1024 / 1024} MB)")
                    }
                    target
                }

                _uiState.update { it.copy(modelName = modelFile.name) }
                llmEngine.loadModel(modelFile.absolutePath)

                assistant = LlmAssistant(engine = llmEngine, tools = tools)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load model", e)
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun generate(prompt: String) {
        val currentAssistant = assistant ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(response = "", error = null, toolLog = "") }
            try {
                currentAssistant.chatStream(prompt).collect { event ->
                    when (event) {
                        is ChatEvent.Token -> {
                            _uiState.update { it.copy(response = it.response + event.text) }
                        }
                        is ChatEvent.ToolCallDetected -> {
                            _uiState.update { it.copy(
                                toolLog = it.toolLog + "-> ${event.name}(${event.arguments})\n",
                                response = ""
                            )}
                        }
                        is ChatEvent.ToolResult -> {
                            _uiState.update { it.copy(
                                toolLog = it.toolLog + "<- ${event.name}: ${event.result.length} chars\n",
                                response = ""
                            )}
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Generation failed", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun unloadModel() {
        try {
            assistant = null
            llmEngine.unloadModel()
            _uiState.update { it.copy(modelName = null, response = "", toolLog = "") }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        assistant = null
        llmEngine.destroy()
    }

    companion object {
        private const val TAG = "LlmTestViewModel"
        private const val MODEL_URL = "https://huggingface.co/litert-community/Qwen3-0.6B/resolve/main/Qwen3-0.6B.litertlm"
        private const val MODEL_FILENAME = "Qwen3-0.6B.litertlm"

        private val DATE_FORMAT = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.US)

        private fun formatLatestAnnouncement(
            announcements: List<com.instructure.canvasapi2.models.DiscussionTopicHeader>
        ): String {
            if (announcements.isEmpty()) return "No unread announcements found."
            val latest = announcements.maxByOrNull { it.postedDate ?: it.createdDate ?: java.util.Date(0) }
                ?: return "No unread announcements found."
            return buildString {
                appendLine("Title: ${latest.title ?: "Untitled"}")
                val date = latest.postedDate ?: latest.createdDate
                if (date != null) appendLine("Posted: ${DATE_FORMAT.format(date)}")
                appendLine()
                val body = latest.message
                    ?.replace(Regex("<[^>]*>"), " ")
                    ?.replace(Regex("\\s+"), " ")
                    ?.trim()
                    ?: "No content"
                appendLine(body)
            }
        }

        private fun formatAssignmentGroups(
            groups: List<com.instructure.canvasapi2.models.AssignmentGroup>
        ): String = buildString {
            if (groups.isEmpty()) {
                append("No assignment groups found.")
                return@buildString
            }
            for (group in groups) {
                appendLine("Group: ${group.name ?: "Unnamed"} (weight: ${group.groupWeight}%)")
                val assignments = group.assignments
                if (assignments.isEmpty()) {
                    appendLine("  (no assignments)")
                } else {
                    for (a in assignments) {
                        val due = a.dueDate?.let { DATE_FORMAT.format(it) } ?: "no due date"
                        appendLine("  - \"${a.name}\" (id: ${a.id}) - Due: $due - ${a.pointsPossible} points")
                    }
                }
                appendLine()
            }
        }
    }
}

data class LlmTestUiState(
    val stateLabel: String = "Initializing...",
    val isLoading: Boolean = true,
    val isReady: Boolean = false,
    val isModelLoaded: Boolean = false,
    val isGenerating: Boolean = false,
    val isError: Boolean = false,
    val modelName: String? = null,
    val response: String = "",
    val error: String? = null,
    val downloadProgress: Float? = null,
    val toolLog: String = ""
) {
    fun fromEngineState(state: LlmState): LlmTestUiState = when (state) {
        is LlmState.Uninitialized -> copy(stateLabel = "Uninitialized", isLoading = false)
        is LlmState.Initializing -> copy(stateLabel = "Initializing native...", isLoading = true)
        is LlmState.Ready -> copy(stateLabel = "Ready (no model)", isLoading = false, isReady = true, isModelLoaded = false)
        is LlmState.LoadingModel -> copy(stateLabel = "Loading model...", isLoading = true, isReady = true)
        is LlmState.ModelLoaded -> copy(stateLabel = "Model loaded", isLoading = false, isReady = true, isModelLoaded = true, isGenerating = false)
        is LlmState.Generating -> copy(stateLabel = "Generating...", isLoading = true, isReady = true, isModelLoaded = true, isGenerating = true)
        is LlmState.Error -> copy(stateLabel = "Error", isLoading = false, isError = true, error = state.exception.message)
    }
}
