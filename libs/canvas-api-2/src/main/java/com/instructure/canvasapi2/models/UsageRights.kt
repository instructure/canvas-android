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
 */
package com.instructure.canvasapi2.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UsageRights(
        @SerializedName("legal_copyright")
        val legalCopyright: String? = "",
        @SerializedName("use_justification")
        val useJustification: FileUsageRightsJustification = FileUsageRightsJustification.OWN_COPYRIGHT,
        val license: String? = "",
        @SerializedName("license_name")
        val licenseName: String? = "",
        val message: String? = "",
        @SerializedName("file_ids")
        val fileIds: ArrayList<Long> = ArrayList()
) : Parcelable

enum class FileUsageRightsJustification(val apiString: String) {
    @SerializedName("own_copyright")
    OWN_COPYRIGHT("own_copyright"),
    @SerializedName("used_by_permission")
    USED_BY_PERMISSION("used_by_permission"),
    @SerializedName("public_domain")
    PUBLIC_DOMAIN("public_domain"),
    @SerializedName("fair_use")
    FAIR_USE("fair_use"),
    @SerializedName("creative_commons")
    CREATIVE_COMMONS("creative_commons")
}

/**
 * Example of a License:
 * {
 *   "id": "cc_by_sa",
 *   "name": "CC Attribution Share Alike",
 *   "url": "http://creativecommons.org/licenses/by-sa/4.0"
 *  }
 */
@Parcelize
data class License(
        val id: String,
        val name: String,
        val url: String
) : Parcelable
