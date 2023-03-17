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

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MasteryPathAssignment(
        override val id: Long = 0,
        @SerializedName("assignment_id")
        val assignmentId: Long = 0,
        @SerializedName("created_at")
        val createdAt: String? = null,
        @SerializedName("updated_at")
        val updatedAt: String? = null,
        @SerializedName("override_id")
        val overrideId: Long = 0,
        @SerializedName("assignment_set_id")
        val assignmentSetId: Long = 0,
        val position: Int = 0,
        val model: Assignment? = null
) : CanvasModel<MasteryPathAssignment>() {
    // Additional getter for assignment
    val assignment get() = model
}
