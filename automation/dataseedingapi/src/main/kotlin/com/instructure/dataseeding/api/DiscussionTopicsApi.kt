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
    }

    private fun discussionTopicsService(token: String): DiscussionTopicsService
            = CanvasNetworkAdapter.retrofitWithToken(token).create(DiscussionTopicsService::class.java)

    fun createDiscussion(courseId: Long, isAnnouncement: Boolean = false, token: String): DiscussionApiModel {
        val discussionTopic = Randomizer.randomDiscussion(isAnnouncement)
        return discussionTopicsService(token)
                .createDiscussionTopic(courseId, discussionTopic)
                .execute()
                .body()!!
    }

    fun createAnnouncement(courseId: Long, token: String): DiscussionApiModel {
        val discussion = createDiscussion(courseId,true, token)

        // MBL-11670: It appears that the createDiscussion call does not properly set the isAnnouncement field.
        // So let's do that manually.
        return DiscussionApiModel(
                id = discussion.id,
                title = discussion.title,
                message = discussion.message,
                isAnnouncement = true
        )
    }
}
