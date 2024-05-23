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
import java.util.*

@Parcelize
data class DiscussionTopic(
        // The user can't see it unless they post a high level reply (requireinitialpost).
        // NOT USED in our code
        //@SerializedName("forbidden")
        //val isForbidden: Boolean = false,
        // List of all the ids of the unread discussion entries.
        @SerializedName("unread_entries")
        val unreadEntries: MutableList<Long> = arrayListOf(),
        val participants: List<DiscussionParticipant>? = arrayListOf(),
        @SerializedName("unread_entriesMap")
        val unreadEntriesMap: HashMap<Long, Boolean> = hashMapOf(),
        @SerializedName("entry_ratings")
        val entryRatings: HashMap<Long, Int> = hashMapOf(),

        // List of all the discussion entries (views)
        @SerializedName("view")
        val views: MutableList<DiscussionEntry> = arrayListOf()
) : Parcelable {
    // This should only have to get built once.
    // MUCH faster for lookups.
    // So instead of n linear operations, we have 1 linear operations and (n-1) constant ones.
    fun unreadEntriesMap(): HashMap<Long, Boolean> {
        if (unreadEntries.size != unreadEntriesMap.size) {
            for (unreadEntry in unreadEntries) {
                unreadEntriesMap[unreadEntry] = true
            }
        }
        return unreadEntriesMap
    }

    // This should only have to get built once.
    // MUCH faster for lookups.
    // So instead of n linear operations, we have 1 linear operations and (n-1) constant ones.
    var participantsMap = hashMapOf<Long, DiscussionParticipant>()
        get(): HashMap<Long, DiscussionParticipant> {
        if (field.isEmpty()) {
            if (participants != null) {
                for (discussionParticipant in participants) {
                    field[discussionParticipant.id] = discussionParticipant
                }
            }
        }
        return field
    }

    fun hasRated(entryId: Long): Boolean = entryRatings.containsKey(entryId) && entryRatings[entryId] == 1

    companion object {
        fun getDiscussionURL(api_protocol: String, domain: String, courseId: Long, topicId: Long): String {
            //https://mobiledev.instructure.com/api/v1/courses/24219/discussion_topics/1129998/
            return "$api_protocol://$domain/courses/$courseId/discussion_topics/$topicId"
        }
    }
}
