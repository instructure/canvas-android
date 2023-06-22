/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.ConferenceRecording
import com.instructure.canvasapi2.models.ConferenceUserSettings
import java.util.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ConferenceEntity(
    @PrimaryKey
    val id: Long,
    val courseId: Long,
    val conferenceKey: String?,
    val conferenceType: String?,
    val description: String?,
    val duration: Long,
    val endedAt: Date?,
    val hasAdvancedSettings: Boolean,
    val joinUrl: String?,
    val longRunning: Boolean,
    val startedAt: Date?,
    val title: String?,
    val url: String?,
    val contextType: String,
    val contextId: Long,
    val record: Boolean?,
    val users: List<Long>
) {
    constructor(conference: Conference, courseId: Long) : this(
        id = conference.id,
        courseId = courseId,
        conferenceKey = conference.conferenceKey,
        conferenceType = conference.conferenceType,
        description = conference.description,
        duration = conference.duration,
        endedAt = conference.endedAt,
        hasAdvancedSettings = conference.hasAdvancedSettings,
        joinUrl = conference.joinUrl,
        longRunning = conference.longRunning,
        startedAt = conference.startedAt,
        title = conference.title,
        url = conference.url,
        contextType = conference.contextType,
        contextId = conference.contextId,
        record = conference.userSettings?.record,
        users = conference.users
    )

    fun toApiModel(recordings: List<ConferenceRecording> = emptyList()) = Conference(
        id = id,
        conferenceKey = conferenceKey,
        conferenceType = conferenceType,
        description = description,
        duration = duration,
        endedAt = endedAt,
        hasAdvancedSettings = hasAdvancedSettings,
        joinUrl = joinUrl,
        longRunning = longRunning,
        startedAt = startedAt,
        title = title,
        url = url,
        recordings = recordings,
        contextType = contextType,
        contextId = contextId,
        userSettings = record?.let { ConferenceUserSettings(it) },
        users = users
    )
}
