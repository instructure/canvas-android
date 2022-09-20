//
// Copyright (C) 2022-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


package com.instructure.dataseeding.model

import com.google.gson.annotations.SerializedName

/**
 * Used to create conferences.
 */
data class ConferencesRequestApiModel(
        @SerializedName("title")
        val title: String = "",
        @SerializedName("description")
        val description: String? = null,
        @SerializedName("conference_type")
        val conferenceType: String = "",
        @SerializedName("long_running")
        val longRunning: Boolean = false,
        @SerializedName("duration")
        val duration: Int = 60,
        @SerializedName("users")
        val userIds: List<Long>? = null
)

/**
 * Wrapper class above ConferencesRequestApiModel because it is wrapped within a 'web_conference' object in the request.
 */
data class WebConferenceWrapper(
        @SerializedName("web_conference")
        val webConference: ConferencesRequestApiModel
)