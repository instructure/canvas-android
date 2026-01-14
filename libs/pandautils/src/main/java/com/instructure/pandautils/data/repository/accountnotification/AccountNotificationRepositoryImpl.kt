/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.data.repository.accountnotification

import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.utils.DataResult

class AccountNotificationRepositoryImpl(
    private val accountNotificationApi: AccountNotificationAPI.AccountNotificationInterface
) : AccountNotificationRepository {

    override suspend fun getAccountNotifications(
        forceRefresh: Boolean
    ): DataResult<List<AccountNotification>> {
        val params = RestParams(
            isForceReadFromNetwork = forceRefresh,
            usePerPageQueryParam = true
        )
        return accountNotificationApi.getAccountNotifications(
            params = params,
            includePast = false,
            showIsClosed = false
        )
    }

    override suspend fun deleteAccountNotification(
        accountNotificationId: Long
    ): DataResult<AccountNotification> {
        val params = RestParams()
        return accountNotificationApi.deleteAccountNotification(accountNotificationId, params)
    }
}