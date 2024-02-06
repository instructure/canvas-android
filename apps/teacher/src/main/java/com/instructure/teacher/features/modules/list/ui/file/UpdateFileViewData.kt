/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 *
 */

package com.instructure.teacher.features.modules.list.ui.file

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.instructure.teacher.R
import java.util.Date

data class UpdateFileViewData(
    val selectedAvailability: FileAvailability,
    val selectedVisibility: FileVisibility,
    val lockAt: Date?,
    val unlockAt: Date?,
    val lockAtDateString: String?,
    val lockAtTimeString: String?,
    val unlockAtDateString: String?,
    val unlockAtTimeString: String?
)

enum class FileVisibility {
    INHERIT,
    CONTEXT,
    INSTITUTION,
    PUBLIC
}

enum class FileAvailability {
    PUBLISHED,
    UNPUBLISHED,
    HIDDEN,
    SCHEDULED
}

sealed class UpdateFileEvent {
    object Close : UpdateFileEvent()

    data class ShowDatePicker(
        val selectedDate: Date?,
        val minDate: Date? = null,
        val maxDate: Date? = null,
        val callback: (year: Int, month: Int, dayOfMonth: Int) -> Unit
    ) : UpdateFileEvent()

    data class ShowTimePicker(
        val selectedDate: Date?,
        val callback: (hourOfDay: Int, minute: Int) -> Unit
    ) : UpdateFileEvent()
}