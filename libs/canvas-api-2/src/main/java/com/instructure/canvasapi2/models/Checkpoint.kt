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
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Checkpoint(
    val name: String? = null,
    val tag: String? = null,
    @SerializedName("points_possible")
    val pointsPossible: Double? = null,
    @SerializedName("due_at")
    val dueAt: String? = null,
    val overrides: List<AssignmentOverride>? = null,
    @SerializedName("only_visible_to_overrides")
    val onlyVisibleToOverrides: Boolean = false,
    @SerializedName("lock_at")
    val lockAt: String? = null, // Date the teacher no longer accepts submissions.
    @SerializedName("unlock_at")
    val unlockAt: String? = null,
) : CanvasModel<Checkpoint>() {
    override val comparisonDate get() = dueDate
    override val comparisonString get() = dueAt

    val dueDate: Date? get() = dueAt.toDate()
    val lockDate: Date? get() = lockAt.toDate()
    val unlockDate: Date? get() = unlockAt.toDate()
}
