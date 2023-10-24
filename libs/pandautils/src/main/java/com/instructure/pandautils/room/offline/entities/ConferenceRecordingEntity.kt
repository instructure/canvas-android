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
import com.instructure.canvasapi2.models.ConferenceRecording

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ConferenceEntity::class,
            parentColumns = ["id"],
            childColumns = ["conferenceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ConferenceRecordingEntity(
    @PrimaryKey
    val recordingId: String,
    val conferenceId: Long,
    val createdAtMillis: Long,
    val durationMinutes: Long,
    val playbackUrl: String?,
    val title: String
) {
    constructor(conferenceRecording: ConferenceRecording, conferenceId: Long) : this(
        recordingId = conferenceRecording.recordingId,
        conferenceId = conferenceId,
        createdAtMillis = conferenceRecording.createdAtMillis,
        durationMinutes = conferenceRecording.durationMinutes,
        playbackUrl = conferenceRecording.playbackUrl,
        title = conferenceRecording.title
    )

    fun toApiModel() = ConferenceRecording(
        createdAtMillis = createdAtMillis,
        durationMinutes = durationMinutes,
        playbackFormats = emptyList(),
        playbackUrl = playbackUrl,
        recordingId = recordingId,
        title = title
    )
}
