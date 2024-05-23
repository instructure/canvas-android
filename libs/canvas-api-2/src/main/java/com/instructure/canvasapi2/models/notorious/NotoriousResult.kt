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

package com.instructure.canvasapi2.models.notorious

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simpleframework.xml.Element

@Parcelize
@Element
data class NotoriousResult(
        @field:Element(required = false)
        var objectType: String? = null,
        @field:Element(required = false)
        var id: String? = null,
        @field:Element(required = false)
        var partnerId: Long = 0,
        @field:Element(required = false)
        var userId: String? = null,
        @field:Element(required = false)
        var status: String? = null,
        @field:Element(required = false)
        var fileName: String? = null,
        @field:Element(required = false)
        var fileSize: String? = null,
        @field:Element(required = false)
        var uploadedFileSize: Long = 0,
        @field:Element(required = false)
        var createdAt: Long = 0,
        @field:Element(required = false)
        var updatedAt: Long = 0,
        @field:Element(required = false)
        var name: String? = null,
        @field:Element(required = false)
        var description: String? = null,
        @field:Element(required = false)
        var tags: String? = null,
        @field:Element(required = false)
        var adminTags: String? = null,
        @field:Element(required = false)
        var categories: String? = null,
        @field:Element(required = false)
        var partnerData: String? = null,
        @field:Element(required = false)
        var downloadUrl: String? = null,
        @field:Element(required = false)
        var moderationStatus: Long = 0,
        @field:Element(required = false)
        var moderationCount: Long = 0,
        @field:Element(required = false)
        var type: Long = 0,
        @field:Element(required = false)
        var totalRank: Long = 0,
        @field:Element(required = false)
        var rank: Long = 0,
        @field:Element(required = false)
        var votes: Long = 0,
        @field:Element(required = false)
        var groupId: Long = 0,
        @field:Element(required = false)
        var searchText: String? = null,
        @field:Element(required = false)
        var licenseType: Long = 0,
        @field:Element(required = false)
        var version: Long = 0,
        @field:Element(required = false)
        var thumbnailUrl: String? = null,
        @field:Element(required = false)
        var accessControlId: Long = 0,
        @field:Element(required = false)
        var startDate: Long = 0,
        @field:Element(required = false)
        var endDate: Long = 0,
        @field:Element(required = false)
        var plays: Long = 0,
        @field:Element(required = false)
        var views: Long = 0,
        @field:Element(required = false)
        var width: Long = 0,
        @field:Element(required = false)
        var height: Long = 0,
        @field:Element(required = false)
        var duration: Double = 0.0,
        @field:Element(required = false)
        var durationType: Long = 0,
        @field:Element(required = false)
        var mediaType: Long = 0,
        @field:Element(required = false)
        var conversionQuality: Long = 0,
        @field:Element(required = false)
        var sourceType: Long = 0,
        @field:Element(required = false)
        var searchProviderType: Long = 0,
        @field:Element(required = false)
        var searchProviderId: Long = 0,
        @field:Element(required = false)
        var creditUserName: String? = null,
        @field:Element(required = false)
        var creditUrl: String? = null,
        @field:Element(required = false)
        var mediaDate: String? = null,
        @field:Element(required = false)
        var dataUrl: String? = null,
        @field:Element(required = false)
        var flavorParamsIds: String? = null,
        @field:Element(required = false)
        var catoriesIds: String? = null, // Misspelled on Notorious' side
        @field:Element(required = false)
        var msDuration: Double = 0.0,
        @field:Element(required = false)
        var error: NotoriousError? = null
) : Parcelable

