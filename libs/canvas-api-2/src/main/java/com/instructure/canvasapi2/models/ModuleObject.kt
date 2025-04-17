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
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.Parcelize
import java.util.Date

@Suppress("ArrayInDataClass")
@Parcelize
data class ModuleObject(
    override var id: Long = 0,
    val position: Int = 0,
    val name: String? = null,
    @SerializedName("unlock_at")
    val unlockAt: String? = null,
    @SerializedName("require_sequential_progress")
    val sequentialProgress: Boolean = false,
    @SerializedName("prerequisite_ids")
    val prerequisiteIds: LongArray? = null,
    val state: String? = null,
    @SerializedName("completed_at")
    val completedAt: String? = null,
    val published: Boolean? = null,
    @SerializedName("items_count")
    val itemCount: Int = 0,
    @SerializedName("items_url")
    val itemsUrl: String = "",
    val items: List<ModuleItem> = emptyList(),
    @SerializedName("estimated_duration")
    val estimatedDuration: String? = null,

) : CanvasModel<ModuleObject>() {
    val unlockDate: Date? get() = unlockAt.toDate()

    enum class State(val apiString: String) {
        Completed("completed"),
        MustSubmit("must_submit"),
        MustView("must_view"),
        MustContribute("must_contribute"),
        MinScore("min_score"),
        UnlockRequirements("unlocked_requirements"),
        Unlocked("unlocked"),
        Started("started"),
        Locked("locked")
    }
}

