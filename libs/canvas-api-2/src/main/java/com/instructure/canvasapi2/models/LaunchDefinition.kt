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

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LaunchDefinition(
        @SerializedName("definition_type")
        var definitionType: String,
        @SerializedName("definition_id")
        var definitionId: Long? = null,
        var name: String?,
        var description: String?,
        var domain: String?,
        var placements: Placements?,
        var url: String?
) : Parcelable {

    companion object {
        const val GAUGE_DOMAIN = "gauge.instructure.com"
        const val STUDIO_DOMAIN = "arc.instructure.com" // NOTE: The subdomain hasn't changed to reflect the rebranding of Arc -> Studio yet
        const val MASTERY_DOMAIN = "app.masteryconnect.com"
        const val PORTFOLIO_DOMAIN = "iad.portfolio.instructure.com"
    }
}
