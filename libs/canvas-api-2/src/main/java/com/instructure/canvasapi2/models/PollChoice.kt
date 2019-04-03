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
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PollChoice(
        override var id: Long = 0,
        @SerializedName("is_correct")
        var isCorrect: Boolean = false,
        var text: String? = null,
        @SerializedName("poll_id")
        var pollId: Long = 0,
        var position: Int = 0
) : CanvasComparable<PollChoice>() {
    override fun compareTo(other: PollChoice): Int = CanvasComparable.compare(this.position, other.position)
}
