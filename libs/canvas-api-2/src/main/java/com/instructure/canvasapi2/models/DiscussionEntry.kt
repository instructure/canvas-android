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

import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.utils.*
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class DiscussionEntry(
        override var id: Long = 0, // Entry id.
        var unread: Boolean = false,
        @SerializedName("updated_at")
        var updatedAt: String? = null,
        @SerializedName("created_at")
        val createdAt: String? = null,
        var author: DiscussionParticipant? = null,
        var description: String? = null, // HTML formatted string used for an edge case. Converting header to entry
        @SerializedName("user_id")
        val userId: Long = 0, // Id of the user that posted it.
        @SerializedName("parent_id")
        var parentId: Long = -1, // Parent id. -1 if there isn't one.
        var message: String? = null, // HTML message.
        var deleted: Boolean = false, // Whether the author deleted the message. If true, the message will be null.
        var totalChildren: Int = 0, // Helper variable
        var unreadChildren: Int = 0, // Helper variable
        var replies: MutableList<DiscussionEntry>? = arrayListOf(),
        var attachments: MutableList<RemoteFile>? = arrayListOf(),
        @SerializedName("rating_count")
        val ratingCount: Int = 0,
        @SerializedName("rating_sum")
        var ratingSum: Int = 0,
        // NOT USED by our code
        //@SerializedName("user_name")
        //val userName: String? = null,
        @SerializedName("editor_id")
        val editorId: Long = 0,
        var _hasRated: Boolean = false
) : CanvasModel<DiscussionEntry>() {

    @IgnoredOnParcel
    var parent: DiscussionEntry? = null // Parent of the entry;
    override val comparisonDate get() = updatedAt.toDate()
    val updatedDate get() = updatedAt.toDate()

    val depth: Int
        get() {
            var depth = 0
            var temp = this

            while (temp.parent != null) {
                depth++
                temp = temp.parent!!
            }

            return depth
        }

    fun init(topic: DiscussionTopic, parentEntry: DiscussionEntry, isOnline: Boolean = true) {
        parent = parentEntry
        // The server attaches a verifier param on the end of img src urls inside of a discussion, however
        // this happens whenever the server decides to make it happen so we need to make sure that the image
        // contains this param in it's url, or replace it with an authenticated url so we can download it for all to see
        if (isOnline && parentEntry.message?.contains("<img") == true && parentEntry.message?.contains("&verifier") != true) {
            // Entry has an image tag - find all of them and replace any that don't have a verifier param in their src url
            // Note: The following is assumed to be run inside a background thread due to a network call in ModelExtensionsKt.getImageReplacementList
            val replacementList = getImageReplacementList(parentEntry.message!!)
            parentEntry.message = replaceImgTags(replacementList, parentEntry.message!!)
        }


        val participantHashMap = topic.participantsMap
        var discussionParticipant: DiscussionParticipant? = participantHashMap[userId]
        if (userId == 0L && editorId != 0L) {
            discussionParticipant = participantHashMap[editorId]
        }
        if (discussionParticipant != null) {
            author = discussionParticipant
        }

        _hasRated = topic.hasRated(id)

        // Get whether or not the topic is unread;
        unread = topic.unreadEntriesMap().containsKey(this.id)

        replies?.forEach { reply ->
            reply.init(topic, this)

            // Handle total and unread children.
            unreadChildren += reply.unreadChildren
            if (reply.unread)
                unreadChildren++

            totalChildren++
            totalChildren += reply.totalChildren
        }
    }

    fun addReply(entry: DiscussionEntry?) {
        entry ?: return
        if (replies == null) {
            replies = ArrayList()
        }
        replies?.add(entry)
    }

    fun addInnerReply(parent: DiscussionEntry, toAdd: DiscussionEntry) {
        replies?.forEach {
            if (it.id == parent.id) {
                it.addReply(toAdd)
                return@forEach
            }
        }
    }

    fun getMessage(localizedDeletedString: String): String {
        return if (this.message == null || this.message == "null") {
            if (deleted) localizedDeletedString
            else ""
        } else this.message ?: ""
    }
}
