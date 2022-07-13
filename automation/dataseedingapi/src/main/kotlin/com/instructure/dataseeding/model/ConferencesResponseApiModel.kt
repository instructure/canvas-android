//
// Copyright (C) 2018-present Instructure, Inc.
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

data class ConferencesResponseApiModel(
        @SerializedName("id")
        val id: Long? = null,
        @SerializedName("description")
        val description: String? = null,
        @SerializedName("conference_type")
        val conferenceType: String = "",
        @SerializedName("long_running")
        val longRunning: Int? = null,
        @SerializedName("duration")
        val duration: Double? = null,
        @SerializedName("user_ids")
        val userIds: List<Long>? = null
)