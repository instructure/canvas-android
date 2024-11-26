/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

import com.google.gson.annotations.SerializedName

data class SmartSearchResult(
    @SerializedName("content_id")
    val contentId: Long,
    @SerializedName("content_type")
    val contentType: SmartSearchContentType,
    val title: String,
    val body: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    val distance: Double,
    val relevance: Int
)

enum class SmartSearchContentType {
    @SerializedName("Announcement")
    ANNOUNCEMENT,
    @SerializedName("DiscussionTopic")
    DISCUSSION_TOPIC,
    @SerializedName("Assignment")
    ASSIGNMENT,
    @SerializedName("WikiPage")
    WIKI_PAGE,
}