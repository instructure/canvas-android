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


package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.CreateDiscussionTopic
import com.instructure.dataseeding.model.DiscussionApiModel
import com.instructure.dataseeding.model.DiscussionTopicEntryReplyRequest
import com.instructure.dataseeding.model.DiscussionTopicEntryReplyResponse
import com.instructure.dataseeding.model.DiscussionTopicEntryRequest
import com.instructure.dataseeding.model.DiscussionTopicEntryResponse
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.dataseeding.util.Randomizer
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

object DiscussionTopicsApi {
    interface DiscussionTopicsService {
        @POST("courses/{courseId}/discussion_topics")
        fun createDiscussionTopic(@Path("courseId") courseId: Long, @Body createDiscussionTopic: CreateDiscussionTopic): Call<DiscussionApiModel>

        @POST("courses/{courseId}/discussion_topics/{discussionId}/entries")
        fun createEntryToDiscussionTopic(@Path("courseId") courseId: Long, @Path("discussionId") discussionId: Long, @Body discussionTopicEntry: DiscussionTopicEntryRequest): Call<DiscussionTopicEntryResponse>

        @POST("courses/{courseId}/discussion_topics/{discussionId}/entries/{entryId}/replies")
        fun createReplyToDiscussionTopicEntry(@Path("courseId") courseId: Long, @Path("discussionId") discussionId: Long, @Path("entryId") entryId: Long, @Body discussionTopicEntry: DiscussionTopicEntryReplyRequest): Call<DiscussionTopicEntryReplyResponse>

    }

    private fun discussionTopicsService(token: String): DiscussionTopicsService
            = CanvasNetworkAdapter.retrofitWithToken(token).create(DiscussionTopicsService::class.java)

    fun createEntryToDiscussionTopic(token: String, courseId: Long, discussionId: Long, replyMessage: String): DiscussionTopicEntryResponse {
        val discussionTopicEntry = DiscussionTopicEntryRequest(replyMessage)
        return discussionTopicsService(token)
            .createEntryToDiscussionTopic(courseId, discussionId, discussionTopicEntry)
            .execute()
            .body()!!
    }

    fun createReplyToDiscussionTopicEntry(token: String, courseId: Long, discussionId: Long, entryId: Long, replyMessage: String): DiscussionTopicEntryReplyResponse {
        val discussionTopicEntryReply = DiscussionTopicEntryReplyRequest(replyMessage)
        return discussionTopicsService(token)
            .createReplyToDiscussionTopicEntry(courseId, discussionId, entryId, discussionTopicEntryReply)
            .execute()
            .body()!!
    }

    fun createDiscussion(courseId: Long, token: String, isAnnouncement: Boolean = false, lockedForUser: Boolean = false, locked: Boolean = false, discussionTitle: String? = null): DiscussionApiModel {
        val discussionTopic = Randomizer.randomDiscussion(discussionTitle, isAnnouncement, lockedForUser, locked)
        return discussionTopicsService(token)
                .createDiscussionTopic(courseId, discussionTopic)
                .execute()
                .body()!!
    }

    fun createAnnouncement(courseId: Long, token: String, lockedForUser: Boolean = false, locked: Boolean = false, announcementTitle: String? = null): DiscussionApiModel {
        val discussion = createDiscussion(courseId, token, true, lockedForUser, locked, announcementTitle)

        if(!lockedForUser) {
            return DiscussionApiModel(
                id = discussion.id,
                title = discussion.title,
                message = discussion.message,
                isAnnouncement = true,
                lockedForUser = false,
                locked = false
            )
        }

        return DiscussionApiModel(
            id = discussion.id,
            title = discussion.title,
            message = discussion.message,
            isAnnouncement = true,
            lockedForUser = false,
            locked = true
        )
    }
}
