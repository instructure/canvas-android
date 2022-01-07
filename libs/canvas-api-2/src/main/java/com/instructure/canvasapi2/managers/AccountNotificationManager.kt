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
import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.utils.ExhaustiveListCallback
import com.instructure.canvasapi2.utils.weave.apiAsync

/**
 * Manager for working with account notifications (aka global announcements)
 */
object AccountNotificationManager {

    fun getAllAccountNotifications(callback: StatusCallback<List<AccountNotification>>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<AccountNotification>(callback) {
            override fun getNextPage(
                callback: StatusCallback<List<AccountNotification>>,
                nextUrl: String,
                isCached: Boolean
            ) {
                AccountNotificationAPI.getAccountNotifications(adapter, params, callback)
            }
        }

        adapter.statusCallback = depaginatedCallback
        AccountNotificationAPI.getAccountNotifications(adapter, params, depaginatedCallback)
    }

    fun getAllAccountNotificationsAsync(forceNetwork: Boolean) =
        apiAsync<List<AccountNotification>> { getAllAccountNotifications(it, forceNetwork) }

    fun deleteAccountNotification(notificationId: Long, callback: StatusCallback<AccountNotification>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()

        AccountNotificationAPI.deleteAccountNotification(notificationId, adapter, params, callback)
    }

    fun deleteAccountNotificationsAsync(notificationId: Long) = apiAsync<AccountNotification> { deleteAccountNotification(notificationId, it) }

    fun getAccountNotification(
        accountNotificationId: Long,
        callback: StatusCallback<AccountNotification>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        AccountNotificationAPI.getAccountNotification(accountNotificationId, adapter, params, callback)
    }

}
