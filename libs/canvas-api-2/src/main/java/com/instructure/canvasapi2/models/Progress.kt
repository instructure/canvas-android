/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 *
 */

package com.instructure.canvasapi2.models

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Suppress("unused", "MemberVisibilityCanBePrivate")
@Parcelize
data class Progress(
        override var id: Long = 0,
        @SerializedName("context_id")
        val contextId: Long = 0,
        @SerializedName("context_type")
        val contextType: String = "", // The context owning the job
        @SerializedName("user_id")
        val userId: Long = 0, // The id of the user who started the job
        @SerializedName("workflow_state")
        private val workflowState: String = "", // One of 'queued', 'running', 'completed', 'failed'
        val tag: String = "", // The type of operation
        val completion: Float = 0f, // Percent completed
        val message: String? = null // Optional details about the job
) : CanvasModel<Progress>() {
    val isQueued: Boolean get() = workflowState == "queued"
    val isRunning: Boolean get() = workflowState == "running"
    val isCompleted: Boolean get() = workflowState == "completed"
    val isFailed: Boolean get() = workflowState == "failed"

    val hasRun: Boolean get() = isCompleted || isFailed
}
