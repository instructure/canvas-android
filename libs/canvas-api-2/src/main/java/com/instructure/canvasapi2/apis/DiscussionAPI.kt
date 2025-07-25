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
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.postmodels.DiscussionEntryPostBody
import com.instructure.canvasapi2.models.postmodels.DiscussionTopicPostBody
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.DataResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.http.*
import java.io.File

object DiscussionAPI {

    interface DiscussionInterface {

        @Multipart
        @POST("{contextType}/{contextId}/discussion_topics")
        fun createCourseDiscussion(
                @Path("contextType") contextType: String,
                @Path("contextId") contextId: Long,
                @Part("title") title: RequestBody,
                @Part("message") message: RequestBody,
                @Part("is_announcement") isAnnouncement: Boolean,
                @Part("delayed_post_at") delayedPostAt: RequestBody?,
                @Part("published") isPublished: Boolean,
                @Part("discussion_type") discussionType: RequestBody,
                @Part("require_initial_post") isUsersMustPost: Boolean,
                @Part("locked") locked: Boolean,
                @Part("lock_at") lockAt: RequestBody?,
                @Part attachment: MultipartBody.Part?,
                @Part("specific_sections") specificSections: RequestBody?): Call<DiscussionTopicHeader>

        @Multipart
        @POST("{contextType}/{contextId}/discussion_topics")
        fun createStudentCourseDiscussion(
                @Path("contextType") contextType: String,
                @Path("contextId") contextId: Long,
                @Part("title") title: RequestBody,
                @Part("message") message: RequestBody,
                @Part("is_announcement") isAnnouncement: Boolean,
                @Part("delayed_post_at") delayedPostAt: RequestBody?,
                @Part("discussion_type") discussionType: RequestBody,
                @Part("require_initial_post") isUsersMustPost: Boolean,
                @Part("locked") locked: Boolean,
                @Part("lock_at") lockAt: RequestBody?,
                @Part attachment: MultipartBody.Part?): Call<DiscussionTopicHeader>

        @POST("{contextType}/{contextId}/discussion_topics/")
        fun createNewDiscussion(@Path("contextType") contextType: String, @Path("contextId") courseId: Long, @Query("title") title: String, @Query("message") message: String, @Query("is_announcement") announcement: Int, @Query("published") published: Int, @Query("discussion_type") discussionType: String): Call<DiscussionTopicHeader>

        @GET("{contextType}/{contextId}/discussion_topics?override_assignment_dates=true&include[]=all_dates&include[]=overrides&include[]=sections")
        fun getFirstPageDiscussionTopicHeaders(@Path("contextType") contextType: String, @Path("contextId") contextId: Long): Call<List<DiscussionTopicHeader>>

        @GET("{contextType}/{contextId}/discussion_topics?override_assignment_dates=true&include[]=all_dates&include[]=overrides&include[]=sections")
        suspend fun getFirstPageDiscussionTopicHeaders(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Tag params: RestParams): DataResult<List<DiscussionTopicHeader>>

        @GET("{contextType}/{contextId}/discussion_topics/{topicId}?include[]=sections")
        suspend fun getDetailedDiscussion(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("topicId") topicId: Long, @Tag params: RestParams): DataResult<DiscussionTopicHeader>

        @GET("{contextType}/{contextId}/discussion_topics/{topicId}/view")
        fun getFullDiscussionTopic(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("topicId") topicId: Long, @Query("include_new_entries") includeNewEntries: Int): Call<DiscussionTopic>

        @GET("{contextType}/{contextId}/discussion_topics/{topicId}/view")
        suspend fun getFullDiscussionTopic(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("topicId") topicId: Long, @Query("include_new_entries") includeNewEntries: Int, @Tag params: RestParams): DataResult<DiscussionTopic>

        @POST("{contextType}/{contextId}/discussion_topics/{topicId}/entries/{entryId}/rating")
        suspend fun rateDiscussionEntry(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("topicId") topicId: Long, @Path("entryId") entryId: Long, @Query("rating") rating: Int, @Tag params: RestParams): DataResult<Unit>

        @PUT("{contextType}/{contextId}/discussion_topics/{topicId}/read")
        fun markDiscussionTopicRead(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("topicId") topicId: Long): Call<Void>

        @PUT("{contextType}/{contextId}/discussion_topics/{topicId}/read")
        suspend fun markDiscussionTopicRead(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("topicId") topicId: Long, @Tag params: RestParams): DataResult<Unit>

        @PUT("{contextType}/{contextId}/discussion_topics/{topicId}/entries/{entryId}/read")
        suspend fun markDiscussionTopicEntryRead(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("topicId") topicId: Long, @Path("entryId") entryId: Long, @Tag params: RestParams): DataResult<Unit>

        @DELETE("{contextType}/{contextId}/discussion_topics/{topicId}/entries/{entryId}/read")
        fun markDiscussionTopicEntryUnread(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("topicId") topicId: Long, @Path("entryId") entryId: Long): Call<Void>

        @PUT("{contextType}/{contextId}/discussion_topics/{topicId}")
        fun pinDiscussion(@Path("contextType") contextType: String, @Path("contextId") courseId: Long, @Path("topicId") topicId: Long, @Query("pinned") pinned: Boolean, @Body body: String): Call<DiscussionTopicHeader>

        @PUT("{contextType}/{contextId}/discussion_topics/{topicId}")
        fun lockDiscussion(@Path("contextType") contextType: String, @Path("contextId") courseId: Long, @Path("topicId") topicId: Long, @Query("locked") locked: Boolean, @Body body: String): Call<DiscussionTopicHeader>

        @DELETE("{contextType}/{contextId}/discussion_topics/{topicId}")
        fun deleteDiscussionTopic(@Path("contextType") contextType: String, @Path("contextId") courseId: Long, @Path("topicId") topicId: Long): Call<Void>

        @DELETE("{contextType}/{contextId}/discussion_topics/{topicId}/entries/{entryId}")
        suspend fun deleteDiscussionEntry(@Path("contextType") contextType: String, @Path("contextId") courseId: Long, @Path("topicId") topicId: Long, @Path("entryId") entryId: Long, @Tag params: RestParams): DataResult<Unit>

        @Multipart
        @POST("{contextType}/{contextId}/discussion_topics/{topicId}/entries/{entryId}/replies")
        fun postDiscussionReply(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("topicId") topicId: Long, @Path("entryId") entryId: Long, @Part("message") message: RequestBody): Call<DiscussionEntry>

        @Multipart
        @POST("{contextType}/{contextId}/discussion_topics/{topicId}/entries")
        fun postDiscussionEntry(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("topicId") topicId: Long, @Part("message") message: RequestBody): Call<DiscussionEntry>

        @Multipart
        @POST("{contextType}/{contextId}/discussion_topics/{topicId}/entries/{entryId}/replies")
        fun postDiscussionReplyWithAttachment(@Path("contextType") contextType: String, @Path("contextId") contextId: Long,
                                              @Path("topicId") topicId: Long, @Path("entryId") entryId: Long,
                                              @Part("message") message: RequestBody, @Part attachment: MultipartBody.Part): Call<DiscussionEntry>

        @Multipart
        @POST("{contextType}/{contextId}/discussion_topics/{topicId}/entries")
        fun postDiscussionEntryWithAttachment(@Path("contextType") contextType: String, @Path("contextId") contextId: Long,
                                              @Path("topicId") topicId: Long, @Part("message") message: RequestBody, @Part attachment: MultipartBody.Part): Call<DiscussionEntry>

        @GET
        fun getNextPage(@Url nextUrl: String): Call<List<DiscussionTopicHeader>>

        @GET
        suspend fun getNextPage(@Url nextUrl: String, @Tag params: RestParams): DataResult<List<DiscussionTopicHeader>>

        @PUT("{contextType}/{contextId}/discussion_topics/{topicId}/entries/{entryId}")
        fun updateDiscussionEntry(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("topicId") topicId: Long, @Path("entryId") entryId: Long, @Body entry: DiscussionEntryPostBody): Call<DiscussionEntry>

        @PUT("{contextType}/{contextId}/discussion_topics/{topicId}?include[]=sections")
        fun editDiscussionTopic(
                @Path("contextType") contextType: String,
                @Path("contextId") contextId: Long,
                @Path("topicId") topicId: Long,
                @Body body: DiscussionTopicPostBody): Call<DiscussionTopicHeader>

        @GET("{contextType}/{contextId}/discussion_topics/{topicId}")
        fun getDiscussionTopicHeader(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("topicId") topicId: Long): Call<DiscussionTopicHeader>

        @GET("{contextType}/{contextId}/discussion_topics/{topicId}")
        suspend fun getDiscussionTopicHeader(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("topicId") topicId: Long, @Tag params: RestParams): DataResult<DiscussionTopicHeader>
    }

    fun createDiscussion(adapter: RestBuilder,
                         params: RestParams,
                         canvasContext: CanvasContext,
                         newDiscussionHeader: DiscussionTopicHeader,
                         attachment: MultipartBody.Part?,
                         callback: StatusCallback<DiscussionTopicHeader>) {
        callback.addCall(adapter.build(DiscussionInterface::class.java, params)
                .createCourseDiscussion(
                        CanvasContext.getApiContext(canvasContext),
                        canvasContext.id,
                        APIHelper.makeRequestBody(newDiscussionHeader.title),
                        APIHelper.makeRequestBody(newDiscussionHeader.message),
                        newDiscussionHeader.announcement,
                        if (newDiscussionHeader.delayedPostDate == null) null else APIHelper.makeRequestBody(newDiscussionHeader.delayedPostDate!!.toString()),
                        newDiscussionHeader.published,
                        APIHelper.makeRequestBody(newDiscussionHeader.discussionType),
                        newDiscussionHeader.requireInitialPost,
                        newDiscussionHeader.locked,
                        if (newDiscussionHeader.lockAt == null) null else APIHelper.makeRequestBody(newDiscussionHeader.lockAt.toString()),
                        attachment,
                        if (newDiscussionHeader.specificSections == null) null else APIHelper.makeRequestBody(newDiscussionHeader.specificSections)
                )).enqueue(callback)

    }

    fun createStudentDiscussion(adapter: RestBuilder,
                                params: RestParams,
                                canvasContext: CanvasContext,
                                newDiscussionHeader: DiscussionTopicHeader,
                                attachment: MultipartBody.Part?,
                                callback: StatusCallback<DiscussionTopicHeader>) {
        callback.addCall(adapter.build(DiscussionInterface::class.java, params)
                .createStudentCourseDiscussion(
                        CanvasContext.getApiContext(canvasContext),
                        canvasContext.id,
                        APIHelper.makeRequestBody(newDiscussionHeader.title),
                        APIHelper.makeRequestBody(newDiscussionHeader.message),
                        newDiscussionHeader.announcement,
                        if (newDiscussionHeader.delayedPostDate == null) null else APIHelper.makeRequestBody(newDiscussionHeader.delayedPostDate!!.toString()),
                        APIHelper.makeRequestBody(newDiscussionHeader.discussionType),
                        newDiscussionHeader.requireInitialPost,
                        newDiscussionHeader.locked,
                        if (newDiscussionHeader.lockAt == null) null else APIHelper.makeRequestBody(newDiscussionHeader.lockAt.toString()),
                        attachment
                )).enqueue(callback)

    }

    fun createDiscussion(adapter: RestBuilder,
                         params: RestParams,
                         canvasContext: CanvasContext,
                         title: String,
                         message: String,
                         isThreaded: Boolean,
                         isAnnouncement: Boolean,
                         isPublished: Boolean,
                         callback: StatusCallback<DiscussionTopicHeader>) {
        val type = if (isThreaded)
            "threaded"
        else
            "side_comment"
        val contextType = CanvasContext.getApiContext(canvasContext)
        val announcement = APIHelper.booleanToInt(isAnnouncement)
        val publish = APIHelper.booleanToInt(isPublished)
        callback.addCall(adapter.build(DiscussionInterface::class.java, params).createNewDiscussion(contextType, canvasContext.id, title, message, announcement, publish, type)).enqueue(callback)
    }

    fun getFirstPageDiscussionTopicHeaders(canvasContext: CanvasContext, adapter: RestBuilder, callback: StatusCallback<List<DiscussionTopicHeader>>, params: RestParams) {
        val contextType = CanvasContext.getApiContext(canvasContext)
        callback.addCall(adapter.build(DiscussionInterface::class.java, params).getFirstPageDiscussionTopicHeaders(contextType, canvasContext.id)).enqueue(callback)
    }

    fun getNextPage(nextUrl: String, adapter: RestBuilder, callback: StatusCallback<List<DiscussionTopicHeader>>, params: RestParams) {
        callback.addCall(adapter.build(DiscussionInterface::class.java, params).getNextPage(nextUrl)).enqueue(callback)
    }

    fun replyToDiscussionEntry(adapter: RestBuilder, canvasContext: CanvasContext, topicId: Long, entryId: Long, message: String, callback: StatusCallback<DiscussionEntry>, params: RestParams) {
        val contextType = CanvasContext.getApiContext(canvasContext)

        val messagePart = message.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        callback.addCall(adapter.build(DiscussionInterface::class.java, params).postDiscussionReply(contextType, canvasContext.id, topicId, entryId, messagePart)).enqueue(callback)
    }

    fun replyToDiscussionEntryWithAttachment(adapter: RestBuilder, canvasContext: CanvasContext, topicId: Long, entryId: Long,
                                             message: String, attachment: File, mimeType: String, callback: StatusCallback<DiscussionEntry>, params: RestParams) {
        val contextType = CanvasContext.getApiContext(canvasContext)
        val messagePart = message.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val requestFile = attachment.asRequestBody(mimeType.toMediaTypeOrNull())
        val attachmentPart = MultipartBody.Part.createFormData("attachment", attachment.name, requestFile)

        callback.addCall(adapter.build(DiscussionInterface::class.java, params).postDiscussionReplyWithAttachment(contextType, canvasContext.id, topicId, entryId, messagePart, attachmentPart)).enqueue(callback)
    }

    fun updateDiscussionEntry(adapter: RestBuilder, canvasContext: CanvasContext, topicId: Long, entryId: Long, updatedEntry: DiscussionEntryPostBody, callback: StatusCallback<DiscussionEntry>, params: RestParams) {
        val contextType = CanvasContext.getApiContext(canvasContext)
        callback.addCall(adapter.buildSerializeNulls(DiscussionInterface::class.java, params).updateDiscussionEntry(contextType, canvasContext.id, topicId, entryId, updatedEntry)).enqueue(callback)
    }

    fun postToDiscussionTopic(adapter: RestBuilder, canvasContext: CanvasContext, topicId: Long, message: String, callback: StatusCallback<DiscussionEntry>, params: RestParams) {
        val contextType = CanvasContext.getApiContext(canvasContext)

        val messagePart = message.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        callback.addCall(adapter.build(DiscussionInterface::class.java, params).postDiscussionEntry(contextType, canvasContext.id, topicId, messagePart)).enqueue(callback)
    }

    fun postToDiscussionTopicWithAttachment(adapter: RestBuilder, canvasContext: CanvasContext, topicId: Long, message: String, attachment: File, mimeType: String, callback: StatusCallback<DiscussionEntry>, params: RestParams) {
        val contextType = CanvasContext.getApiContext(canvasContext)

        val messagePart = message.toRequestBody("multipart/form-data".toMediaTypeOrNull())

        val requestFile = attachment.asRequestBody(mimeType.toMediaTypeOrNull())
        val attachmentPart = MultipartBody.Part.createFormData("attachment", attachment.name, requestFile)

        callback.addCall(adapter.build(DiscussionInterface::class.java, params).postDiscussionEntryWithAttachment(contextType, canvasContext.id, topicId, messagePart, attachmentPart)).enqueue(callback)
    }

    fun markDiscussionTopicRead(adapter: RestBuilder, canvasContext: CanvasContext, topicId: Long, callback: StatusCallback<Void>, params: RestParams) {
        val contextType = CanvasContext.getApiContext(canvasContext)
        callback.addCall(adapter.build(DiscussionInterface::class.java, params).markDiscussionTopicRead(contextType, canvasContext.id, topicId)).enqueue(callback)
    }

    fun pinDiscussion(adapter: RestBuilder, canvasContext: CanvasContext, topicId: Long, callback: StatusCallback<DiscussionTopicHeader>, params: RestParams) {
        callback.addCall(adapter.build(DiscussionInterface::class.java, params).pinDiscussion(CanvasContext.getApiContext(canvasContext), canvasContext.id, topicId, true, "")).enqueue(callback)
    }

    fun unpinDiscussion(adapter: RestBuilder, canvasContext: CanvasContext, topicId: Long, callback: StatusCallback<DiscussionTopicHeader>, params: RestParams) {
        callback.addCall(adapter.build(DiscussionInterface::class.java, params).pinDiscussion(CanvasContext.getApiContext(canvasContext), canvasContext.id, topicId, false, "")).enqueue(callback)
    }

    fun lockDiscussion(adapter: RestBuilder, canvasContext: CanvasContext, topicId: Long, callback: StatusCallback<DiscussionTopicHeader>, params: RestParams) {
        callback.addCall(adapter.build(DiscussionInterface::class.java, params).lockDiscussion(CanvasContext.getApiContext(canvasContext), canvasContext.id, topicId, true, "")).enqueue(callback)
    }

    fun unlockDiscussion(adapter: RestBuilder, canvasContext: CanvasContext, topicId: Long, callback: StatusCallback<DiscussionTopicHeader>, params: RestParams) {
        callback.addCall(adapter.build(DiscussionInterface::class.java, params).lockDiscussion(CanvasContext.getApiContext(canvasContext), canvasContext.id, topicId, false, "")).enqueue(callback)
    }

    fun deleteDiscussionTopicHeader(adapter: RestBuilder, canvasContext: CanvasContext, topicId: Long, callback: StatusCallback<Void>, params: RestParams) {
        callback.addCall(adapter.build(DiscussionInterface::class.java, params).deleteDiscussionTopic(CanvasContext.getApiContext(canvasContext), canvasContext.id, topicId)).enqueue(callback)
    }

    fun editDiscussionTopic(canvasContext: CanvasContext, topicId: Long, body: DiscussionTopicPostBody, adapter: RestBuilder, callback: StatusCallback<DiscussionTopicHeader>, params: RestParams) {
        callback.addCall(adapter.build(DiscussionInterface::class.java, params).editDiscussionTopic(CanvasContext.getApiContext(canvasContext), canvasContext.id, topicId, body)).enqueue(callback)
    }

    fun getDiscussionTopicHeader(adapter: RestBuilder, canvasContext: CanvasContext, topicId: Long, callback: StatusCallback<DiscussionTopicHeader>, params: RestParams) {
        callback.addCall(adapter.build(DiscussionInterface::class.java, params).getDiscussionTopicHeader(CanvasContext.getApiContext(canvasContext), canvasContext.id, topicId)).enqueue(callback)
    }
}
