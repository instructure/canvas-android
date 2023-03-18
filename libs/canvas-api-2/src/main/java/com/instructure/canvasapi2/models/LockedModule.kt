/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class LockedModule(
        override val id: Long = 0,
        @SerializedName("context_id")
        val contextId: Long = 0,
        @SerializedName("context_type")
        val contextType: String? = null,
        val name: String? = null,
        @SerializedName("unlock_at")
        val unlockAt: String? = null,
        @SerializedName("require_sequential_progress")
        val isRequireSequentialProgress: Boolean = false,
        val prerequisites: List<ModuleName>? = arrayListOf(),
        @SerializedName("completion_requirements")
        val completionRequirements: List<ModuleCompletionRequirement> = ArrayList()
) : CanvasModel<LockedModule>() {
    override val comparisonString get() = name


    companion object {
        fun isLockedModuleValid(lockedModule: LockedModule): Boolean {
            if (lockedModule.contextId <= 0) return false
            if (lockedModule.name == null) return false
            if (lockedModule.unlockAt == null) return false
            if (lockedModule.prerequisites == null) return false

            for (i in 0 until lockedModule.prerequisites.size) {
                if (lockedModule.prerequisites[i].name == null) {
                    return false
                }
            }
            return true
        }
    }
}


@Parcelize
data class ModuleName(
        var name: String? = null
) : Parcelable
