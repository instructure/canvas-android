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
import java.util.*

@Parcelize
data class LockInfo(
        val modulePrerequisiteNames: ArrayList<String>? = ArrayList(), // Helper variable
        @SerializedName("context_module")
        val contextModule: LockedModule? = null,
        @SerializedName("unlock_at")
        val unlockAt: String? = null
) : CanvasComparable<LockInfo>() {
    override val comparisonString get() = lockedModuleName

    val isEmpty: Boolean
        get() = (this.comparisonString == null
                && contextModule == null
                && (modulePrerequisiteNames == null || modulePrerequisiteNames.size == 0)
                && unlockAt == null)

    val lockedModuleName: String?
        get() = if (contextModule != null) {
            contextModule.name
        } else {
            ""
        }

    val unlockDate: Date? get() = unlockAt.toDate()
}
