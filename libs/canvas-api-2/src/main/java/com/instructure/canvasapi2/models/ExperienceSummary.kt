/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.canvasapi2.models

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExperienceSummary(
    @SerializedName("current_app")
    val currentApp: String? = null,
    @SerializedName("available_apps")
    val availableApps: List<String>? = null,
    @SerializedName("available_roles_in_context")
    val availableRolesInContext: List<String>? = null,
): CanvasModel<ExperienceSummary>() {
    companion object {
        const val ACADEMIC_EXPERIENCE = "academic"
        const val CAREER_LEARNER_EXPERIENCE = "career_learner"
        const val ROLE_LEARNER = "learner"
        const val ROLE_LEARNING_PROVIDER = "learning_provider"
    }
}

