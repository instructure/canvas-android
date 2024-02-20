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

data class DiscussionApiModel (
        val id: Long,
        val title: String,
        val message: String,
        @SerializedName("is_announcement")
        val isAnnouncement: Boolean,
        @SerializedName("locked_for_user")
        val lockedForUser: Boolean,
        @SerializedName("locked")
        val locked: Boolean,
        @SerializedName("published")
        val published: Boolean? = true
)

data class CreateDiscussionTopic(
        val title: String,
        val message: String,
        @SerializedName("is_announcement")
        val isAnnouncement: Boolean = false,
        @SerializedName("locked_for_user")
        val lockedForUser: Boolean = false,
        @SerializedName("locked")
        val locked: Boolean = true,
        val published: Boolean = true
)