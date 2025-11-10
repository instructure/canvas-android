package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.UnreadConversationCount
import com.instructure.canvasapi2.models.UnreadCount
import com.instructure.canvasapi2.models.UnreadNotificationCount
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Tag


object UnreadCountAPI {
    interface UnreadCountsInterface {
        @GET("conversations/unread_count")
        fun getUnreadConversationCount(): Call<UnreadConversationCount>

        @GET("conversations/unread_count")
        suspend fun getUnreadConversationCount(@Tag params: RestParams): DataResult<UnreadConversationCount>

        @GET("users/self/activity_stream/summary?only_active_courses=true")
        fun getNotificationsCount(): Call<List<UnreadNotificationCount>>

        @GET("users/self/activity_stream/summary?only_active_courses=true")
        suspend fun getNotificationsCount(@Tag params: RestParams): DataResult<List<UnreadNotificationCount>>

        @GET("users/self/observer_alerts/unread_count")
        fun getUnreadAlertCount(@Query("student_id") studentId: Long): Call<UnreadCount>

        @GET("users/self/observer_alerts/unread_count")
        suspend fun getUnreadAlertCount(@Query("student_id") studentId: Long, @Tag params: RestParams): DataResult<UnreadCount>
    }

    fun getUnreadConversationCount(adapter: RestBuilder, params: RestParams, callback: StatusCallback<UnreadConversationCount>) {
        callback.addCall(adapter.build(UnreadCountsInterface::class.java, params).getUnreadConversationCount()).enqueue(callback)
    }

    fun getUnreadNotificationsCount(adapter: RestBuilder, params: RestParams, callback: StatusCallback<List<UnreadNotificationCount>>) {
        callback.addCall(adapter.build(UnreadCountsInterface::class.java, params).getNotificationsCount()).enqueue(callback)
    }

    fun getUnreadAlertCount(adapter: RestBuilder, params: RestParams, studentId: Long, callback: StatusCallback<UnreadCount>) {
        callback.addCall(adapter.build(UnreadCountsInterface::class.java, params).getUnreadAlertCount(studentId)).enqueue(callback)
    }

    fun getUnreadConversationsCountSynchronous(adapter: RestBuilder, params: RestParams): String? {
        return try {
            adapter.build(UnreadCountsInterface::class.java, params).getUnreadConversationCount().execute().body()?.unreadCount
        } catch (E: Exception) {
            null
        }

    }
}
