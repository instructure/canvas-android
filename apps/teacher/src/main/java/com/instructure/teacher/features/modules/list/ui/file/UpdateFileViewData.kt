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
    val title: String,
    val selectedAvailability: FileAvailability,
    val selectedVisibility: FileVisibility,
    val lockAt: Date?,
    val unlockAt: Date?
)

enum class FileVisibility(@StringRes val resource: Int) {
    INHERIT(R.string.inherit_from_course),
    CONTEXT(R.string.course_members),
    INSTITUTION(R.string.institution_members),
    PUBLIC(R.string.public_title)
}

enum class FileAvailability(@StringRes val resource: Int, @DrawableRes iconRes: Int) {
    PUBLISHED(R.string.publish, R.drawable.ic_publish),
    UNPUBLISHED(R.string.unpublish, R.drawable.ic_unpublish),
    HIDDEN(R.string.only_available_with_link, R.drawable.ic_eye_off),
    SCHEDULED(R.string.schedule_availability, R.drawable.ic_calendar_month)
}

sealed class UpdateFileEvent {
    object Close: UpdateFileEvent()
}