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
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class ModuleContentDetails(
    @SerializedName("points_possible")
    val pointsPossible: String? = null,
    @SerializedName("due_at")
    val dueAt: String? = null,
    @SerializedName("unlock_at")
    val unlockAt: String? = null,
    @SerializedName("lock_at")
    val lockAt: String? = null,
    @SerializedName("locked_for_user")
    val lockedForUser: Boolean = false,
    @SerializedName("lock_explanation")
    val lockExplanation: String? = null,
    @SerializedName("lock_info")
    val lockInfo: LockInfo? = null,
    val hidden: Boolean? = null,
    val locked: Boolean? = null
) : CanvasComparable<ModuleContentDetails>() {
    @IgnoredOnParcel
    val dueDate: Date? get() = dueAt.toDate()

    @IgnoredOnParcel
    val unlockDate: Date? get() = unlockAt.toDate()

    @IgnoredOnParcel
    val lockDate: Date? get() = lockAt.toDate()
}
