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

/**
 * Used to update the users' settings.
 */
data class UserSettingsApiModel(
        @SerializedName("manual_mark_as_read")
        val manualMarkAsRead: Boolean? = false,
        @SerializedName("collapse_global_nav")
        val collapseGlobalNav: Boolean? = false,
        @SerializedName("hide_dashcard_color_overlays")
        val hideDashCardColorOverlays: Boolean? = false,
        @SerializedName("comment_library_suggestions_enabled")
        val commentLibrarySuggestions: Boolean? = false
)