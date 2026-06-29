/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.pandautils.features.discussion.details

enum class DiscussionListType {
    ANNOUNCEMENT,
    DISCUSSION
}

// After deleting a discussion/announcement in the WebView, Canvas redirects to the list index
// (e.g. .../announcements or .../discussion_topics) instead of a specific topic. Detecting this
// lets us pop back to the native list instead of showing the web index inside the detail screen.
// Returns the matched list type, or null when the url points to a specific topic.
fun discussionListRedirectType(url: String): DiscussionListType? {
    val path = url.substringBefore("?").substringBefore("#").trimEnd('/')
    return when {
        path.endsWith("/announcements") -> DiscussionListType.ANNOUNCEMENT
        path.endsWith("/discussion_topics") -> DiscussionListType.DISCUSSION
        else -> null
    }
}