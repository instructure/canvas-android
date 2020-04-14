/*
 * Copyright (C) 2020 - present Instructure, Inc.
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

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

data class ConferenceList(val conferences: List<Conference> = emptyList())

@Parcelize
data class Conference(
    val id: Long = 0,
    @SerializedName("conference_key")
    val conferenceKey: String? = null,
    @SerializedName("conference_type")
    val conferenceType: String? = null,
    val description: String? = null,
    val duration: Long = 0,
    @SerializedName("ended_at")
    val endedAt: Date? = null,
    @SerializedName("has_advanced_settings")
    val hasAdvancedSettings: Boolean = false,
    @SerializedName("join_url")
    val joinUrl: String? = null,
    @SerializedName("long_running")
    val longRunning: Boolean = false,
    @SerializedName("started_at")
    val startedAt: Date? = null,
    val title: String? = null,
    val url: String? = null,
    val recordings: List<ConferenceRecording> = emptyList(),
    @SerializedName("context_type")
    val contextType: String = "",
    @SerializedName("context_id")
    val contextId: Long = 0,
    @SerializedName("user_settings")
    val userSettings: ConferenceUserSettings? = null,
    val users: List<Long> = emptyList()
) : Parcelable {
    // This field is only used temporarily and will not be serialized or parcelized
    @IgnoredOnParcel
    lateinit var canvasContext: CanvasContext
}

@Parcelize
data class ConferenceUserSettings(
    val record: Boolean = false
) : Parcelable

@Parcelize
data class ConferenceRecording(
    @SerializedName("created_at")
    val createdAtMillis: Long = 0,
    @SerializedName("duration_minutes")
    val durationMinutes: Long = 0,
    @SerializedName("playback_formats")
    val playbackFormats: List<PlaybackFormat> = emptyList(),
    @SerializedName("playback_url")
    val playbackUrl: String? = null,
    @SerializedName("recording_id")
    val recordingId: String = "",
    val title: String = ""
) : Parcelable {
    val createdAt: Date get() = Date(createdAtMillis)
}

@Parcelize
data class PlaybackFormat(
    val length: String,
    val type: String,
    val url: String
) : Parcelable
