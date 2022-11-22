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
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.postmodels.DiscussionEntryPostBody
import com.instructure.canvasapi2.models.postmodels.DiscussionTopicPostBody
import com.instructure.canvasapi2.utils.ExhaustiveListCallback
import com.instructure.canvasapi2.utils.weave.apiAsync
import okhttp3.MultipartBody
import java.io.File

object DiscussionManager {

    fun createDiscussion(
        canvasContext: CanvasContext,
        newDiscussionHeader: DiscussionTopicHeader,
        attachment: MultipartBody.Part?,
        callback: StatusCallback<DiscussionTopicHeader>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.createDiscussion(adapter, params, canvasContext, newDiscussionHeader, attachment, callback)
    }

    fun createStudentDiscussion(
        canvasContext: CanvasContext,
        newDiscussionHeader: DiscussionTopicHeader,
        attachment: MultipartBody.Part?,
        callback: StatusCallback<DiscussionTopicHeader>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.createStudentDiscussion(
            adapter,
            params,
            canvasContext,
            newDiscussionHeader,
            attachment,
            callback
        )
    }

    fun editDiscussionTopic(
        canvasContext: CanvasContext,
        discussionHeaderId: Long,
        discussionTopicPostBody: DiscussionTopicPostBody,
        callback: StatusCallback<DiscussionTopicHeader>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.editDiscussionTopic(
            canvasContext,
            discussionHeaderId,
            discussionTopicPostBody,
            adapter,
            callback,
            params
        )
    }

    fun getAllDiscussionTopicHeaders(
        canvasContext: CanvasContext,
        forceNetwork: Boolean,
        callback: StatusCallback<List<DiscussionTopicHeader>>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<DiscussionTopicHeader>(callback) {
            override fun getNextPage(
                callback: StatusCallback<List<DiscussionTopicHeader>>,
                nextUrl: String,
                isCached: Boolean
            ) {
                DiscussionAPI.getNextPage(nextUrl, adapter, callback, params)
            }
        }
        adapter.statusCallback = depaginatedCallback
        DiscussionAPI.getFirstPageDiscussionTopicHeaders(canvasContext, adapter, depaginatedCallback, params)
    }

    fun getFullDiscussionTopic(
        canvasContext: CanvasContext,
        topicId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<DiscussionTopic>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        DiscussionAPI.getFullDiscussionTopic(adapter, canvasContext, topicId, callback, params)
    }

    fun getDetailedDiscussion(
        canvasContext: CanvasContext,
        topicId: Long,
        callback: StatusCallback<DiscussionTopicHeader>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        DiscussionAPI.getDetailedDiscussion(adapter, canvasContext, topicId, callback, params)
    }

    fun rateDiscussionEntry(
        canvasContext: CanvasContext,
        topicId: Long,
        entryId: Long,
        rating: Int,
        callback: StatusCallback<Void>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.rateDiscussionEntry(adapter, canvasContext, topicId, entryId, rating, callback, params)
    }

    fun markDiscussionTopicRead(
        canvasContext: CanvasContext,
        topicId: Long,
        callback: StatusCallback<Void>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.markDiscussionTopicRead(adapter, canvasContext, topicId, callback, params)
    }

    fun markDiscussionTopicEntryRead(
        canvasContext: CanvasContext,
        topicId: Long,
        entryId: Long,
        callback: StatusCallback<Void>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.markDiscussionTopicEntryRead(adapter, canvasContext, topicId, entryId, callback, params)
    }

    fun markDiscussionTopicEntryUnread(
        canvasContext: CanvasContext,
        topicId: Long,
        entryId: Long,
        callback: StatusCallback<Void>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.markDiscussionTopicEntryUnread(adapter, canvasContext, topicId, entryId, callback, params)
    }

    fun replyToDiscussionEntry(
        canvasContext: CanvasContext,
        topicId: Long,
        entryId: Long,
        message: String,
        callback: StatusCallback<DiscussionEntry>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.replyToDiscussionEntry(adapter, canvasContext, topicId, entryId, message, callback, params)
    }

    fun replyToDiscussionEntry(
        canvasContext: CanvasContext,
        topicId: Long,
        entryId: Long,
        message: String,
        attachment: File,
        mimeType: String,
        callback: StatusCallback<DiscussionEntry>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.replyToDiscussionEntryWithAttachment(
            adapter,
            canvasContext,
            topicId,
            entryId,
            message,
            attachment,
            mimeType,
            callback,
            params
        )
    }

    fun updateDiscussionEntry(
        canvasContext: CanvasContext,
        topicId: Long,
        entryId: Long,
        updatedEntry: DiscussionEntryPostBody,
        callback: StatusCallback<DiscussionEntry>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.updateDiscussionEntry(
            adapter,
            canvasContext,
            topicId,
            entryId,
            updatedEntry,
            callback,
            params
        )
    }

    fun postToDiscussionTopic(
        canvasContext: CanvasContext,
        topicId: Long,
        message: String,
        callback: StatusCallback<DiscussionEntry>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.postToDiscussionTopic(adapter, canvasContext, topicId, message, callback, params)
    }

    fun postToDiscussionTopic(
        canvasContext: CanvasContext,
        topicId: Long,
        message: String,
        attachment: File,
        mimeType: String,
        callback: StatusCallback<DiscussionEntry>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.postToDiscussionTopicWithAttachment(
            adapter,
            canvasContext,
            topicId,
            message,
            attachment,
            mimeType,
            callback,
            params
        )
    }

    fun pinDiscussionTopicHeader(
        canvasContext: CanvasContext,
        topicId: Long,
        callback: StatusCallback<DiscussionTopicHeader>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.pinDiscussion(adapter, canvasContext, topicId, callback, params)
    }

    fun unpinDiscussionTopicHeader(
        canvasContext: CanvasContext,
        topicId: Long,
        callback: StatusCallback<DiscussionTopicHeader>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.unpinDiscussion(adapter, canvasContext, topicId, callback, params)
    }

    fun lockDiscussionTopicHeader(
        canvasContext: CanvasContext,
        topicId: Long,
        callback: StatusCallback<DiscussionTopicHeader>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.lockDiscussion(adapter, canvasContext, topicId, callback, params)
    }

    fun unlockDiscussionTopicHeader(
        canvasContext: CanvasContext,
        topicId: Long,
        callback: StatusCallback<DiscussionTopicHeader>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.unlockDiscussion(adapter, canvasContext, topicId, callback, params)
    }

    fun deleteDiscussionTopicHeader(canvasContext: CanvasContext, topicId: Long, callback: StatusCallback<Void>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.deleteDiscussionTopicHeader(adapter, canvasContext, topicId, callback, params)
    }

    fun deleteDiscussionEntry(
        canvasContext: CanvasContext,
        topicId: Long,
        entryId: Long,
        callback: StatusCallback<Void>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.deleteDiscussionEntry(adapter, canvasContext, topicId, entryId, callback, params)
    }

    fun createDiscussion(
        canvasContext: CanvasContext,
        title: String,
        message: String,
        isThreaded: Boolean,
        isAnnouncement: Boolean,
        isPublished: Boolean,
        callback: StatusCallback<DiscussionTopicHeader>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        DiscussionAPI.createDiscussion(
            adapter,
            params,
            canvasContext,
            title,
            message,
            isThreaded,
            isAnnouncement,
            isPublished,
            callback
        )
    }

    fun getDiscussionTopicHeader(
        canvasContext: CanvasContext,
        topicId: Long,
        forceNetwork: Boolean,
        callback: StatusCallback<DiscussionTopicHeader>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        DiscussionAPI.getDiscussionTopicHeader(adapter, canvasContext, topicId, callback, params)
    }

    fun getDiscussionTopicHeaderAsync(
        canvasContext: CanvasContext,
        topicId: Long,
        forceNetwork: Boolean
    ) = apiAsync<DiscussionTopicHeader> { getDiscussionTopicHeader(canvasContext, topicId, forceNetwork, it) }

}
