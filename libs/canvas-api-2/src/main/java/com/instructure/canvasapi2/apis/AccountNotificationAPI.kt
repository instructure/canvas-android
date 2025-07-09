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
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.utils.DataResult

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag
import retrofit2.http.Url

/**
 * APIs for working with account notifications (aka global announcements)
 */
object AccountNotificationAPI {

    interface AccountNotificationInterface {

        @GET("accounts/self/users/self/account_notifications")
        fun getAccountNotifications(): Call<List<AccountNotification>>

        @GET("accounts/self/users/self/account_notifications")
        suspend fun getAccountNotifications(@Tag params: RestParams, @Query("include_past") includePast: Boolean = false, @Query("show_is_closed") showIsClosed: Boolean = false): DataResult<List<AccountNotification>>

        @GET
        fun getNextPageNotifications(@Url url: String): Call<List<AccountNotification>>

        @GET
        suspend fun getNextPageNotifications(@Url url: String, @Tag params: RestParams): DataResult<List<AccountNotification>>

        @DELETE("accounts/self/users/self/account_notifications/{accountNotificationId}")
        fun deleteAccountNotification(@Path("accountNotificationId") accountNotificationId: Long): Call<AccountNotification>

        @GET("accounts/self/users/self/account_notifications/{accountNotificationId}")
        fun getAccountNotification(@Path("accountNotificationId") accountNotificationId: Long): Call<AccountNotification>

        @GET("accounts/self/users/self/account_notifications/{accountNotificationId}")
        suspend fun getAccountNotification(@Path("accountNotificationId") accountNotificationId: Long, @Tag params: RestParams): DataResult<AccountNotification>
    }

    fun getAccountNotifications(adapter: RestBuilder, params: RestParams, callback: StatusCallback<List<AccountNotification>>) {
        if (callback.isFirstPage) {
            callback.addCall(adapter.build(AccountNotificationInterface::class.java, params).getAccountNotifications()).enqueue(callback)
        } else if (callback.moreCallsExist()) {
            callback.addCall(adapter.build(AccountNotificationInterface::class.java, params).getNextPageNotifications(callback.linkHeaders!!.nextUrl!!)).enqueue(callback)
        }
    }

    fun deleteAccountNotification(notificationId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<AccountNotification>) {
        callback.addCall(adapter.build(AccountNotificationInterface::class.java, params).deleteAccountNotification(notificationId)).enqueue(callback)
    }

    fun getAccountNotification(notificationId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<AccountNotification>) {
        callback.addCall(adapter.build(AccountNotificationInterface::class.java, params).getAccountNotification(notificationId)).enqueue(callback)
    }
}
