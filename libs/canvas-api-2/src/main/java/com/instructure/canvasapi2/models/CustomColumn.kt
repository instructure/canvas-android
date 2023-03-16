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
data class CustomColumn(
        override val id: Long = 0,
        val title: String, // Header text
        val position: Int, //Column order
        val hidden: Boolean, // Won't be displayed if hidden is true
        @SerializedName("teacher_notes")
        val teacherNotes: Boolean  // Is it the teacher's note column?
) : CanvasModel<CustomColumn>() {
    override val comparisonString get() = title
}
