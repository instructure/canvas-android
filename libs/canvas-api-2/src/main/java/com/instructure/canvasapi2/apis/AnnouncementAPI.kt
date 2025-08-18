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

package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag
import retrofit2.http.Url


object AnnouncementAPI {

    interface AnnouncementInterface {
        @GET("{contextType}/{contextId}/discussion_topics?only_announcements=1&include[]=sections")
        fun getFirstPageAnnouncementsList(@Path("contextType") contextType: String, @Path("contextId") contextId: Long): Call<List<DiscussionTopicHeader>>

        @GET("{contextType}/{contextId}/discussion_topics?only_announcements=1&include[]=sections")
        suspend fun getFirstPageAnnouncementsList(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Tag params: RestParams): DataResult<List<DiscussionTopicHeader>>

        @GET("announcements")
        suspend fun getFirstPageAnnouncements(
            @Query("context_codes[]") vararg courseCode: String,
            @Query("start_date") startDate: String,
            @Query("end_date") endDate: String,
            @Tag params: RestParams
        ): DataResult<List<DiscussionTopicHeader>>

        @GET
        fun getNextPageAnnouncementsList(@Url nextUrl: String): Call<List<DiscussionTopicHeader>>

        @GET
        suspend fun getNextPageAnnouncementsList(@Url nextUrl: String, @Tag params: RestParams): DataResult<List<DiscussionTopicHeader>>

        /**
         * This API call returns the latest announcement. The current implementation is the latest announcement from the last 14 days.
         */
        @GET("announcements?include[]=sections&active_only=true&per_page=1")
        fun getLatestAnnouncement(@Query("context_codes[]") courseCode: String): Call<List<DiscussionTopicHeader>>

        @GET("courses/{courseId}/discussion_topics/{announcementId}")
        suspend fun getCourseAnnouncement(
            @Path("courseId") courseId: Long,
            @Path("announcementId") announcementId: Long,
            @Tag restParams: RestParams
        ): DataResult<DiscussionTopicHeader>
    }

    fun getFirstPageAnnouncements(canvasContext: CanvasContext, adapter: RestBuilder, callback: StatusCallback<List<DiscussionTopicHeader>>, params: RestParams) {
        val contextType = CanvasContext.getApiContext(canvasContext)
        callback.addCall(adapter.build(AnnouncementInterface::class.java, params).getFirstPageAnnouncementsList(contextType, canvasContext.id)).enqueue(callback)
    }

    fun getNextPage(nextUrl: String, adapter: RestBuilder, callback: StatusCallback<List<DiscussionTopicHeader>>, params: RestParams) {
        callback.addCall(adapter.build(AnnouncementInterface::class.java, params).getNextPageAnnouncementsList(nextUrl)).enqueue(callback)
    }

    fun getLatestAnnouncement(canvasContext: CanvasContext, adapter: RestBuilder, callback: StatusCallback<List<DiscussionTopicHeader>>, params: RestParams) {
        callback.addCall(adapter.build(AnnouncementInterface::class.java, params).getLatestAnnouncement(canvasContext.contextId)).enqueue(callback)
    }
}
