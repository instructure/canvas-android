/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.canvas.espresso.mockCanvas.endpoints

import com.instructure.canvas.espresso.mockCanvas.Endpoint
import com.instructure.canvas.espresso.mockCanvas.endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.*
import com.instructure.canvasapi2.models.Account
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.LaunchDefinition

/**
 * Endpoint that can return a list of [Account]s. We currently assume that only one account is supported at a time and that
 * no users are admins (only admins can list account), so this will always return an empty list.
 *
 * ROUTES:
 * - `{accountId}` -> [AccountEndpoint]
 */
object AccountListEndpoint : Endpoint(
    AccountId() to AccountEndpoint,
    response = {
        // NOTE: Only supporting one account for now and we'll assume no users are admins, so return an empty list
        GET { request.successPaginatedResponse(emptyList<Account>()) }
    }
)

/**
 * Endpoint that can return the [Account] specified by [PathVars.accountId]. We currently assume that no users are
 * admins (only admins can view accounts) so this will always return an unauthorized response.
 *
 * ROUTES:
 * - `users/{userId}/account_notifications` -> [AccountNotificationListEndpoint]
 * - `lti_apps/launch_notifications` -> [LaunchDefinitionsEndpoint]
 */
object AccountEndpoint : Endpoint(
    Segment("users") to endpoint(
        UserId() to endpoint(
            Segment("account_notifications") to AccountNotificationListEndpoint
        )
    ),
    Segment("lti_apps") to endpoint(
        Segment("launch_definitions") to LaunchDefinitionsEndpoint
    ),
    response = {
        // NOTE: Only supporting one account for now and we'll assume no users are admins, so return a 401
        GET { request.unauthorizedResponse() }
    }
)

/**
 * Endpoint that can return a list of [LaunchDefinition]s
 */
object LaunchDefinitionsEndpoint : Endpoint(response = {
    GET { request.successPaginatedResponse(data.launchDefinitions.values.toList()) }
})

/**
 * Endpoint that can return a list of [AccountNotification]s
 *
 * ROUTES:
 * - `{accountNotificationId}` -> [AccountNotificationEndpoint]
 */
object AccountNotificationListEndpoint : Endpoint(
    LongId(PathVars::accountNotificationId) to AccountNotificationEndpoint,
    response = {
        GET { request.successPaginatedResponse(data.accountNotifications.values.toList()) }
    }
)

/**
 * Endpoint that can return or delete the [AccountNotification] specified by [PathVars.accountNotificationId]
 */
object AccountNotificationEndpoint : Endpoint(response = {
    GET { request.successResponse(data.accountNotifications[pathVars.accountNotificationId]!!) }
    DELETE {
        val notification = data.accountNotifications[pathVars.accountNotificationId]!!
        data.accountNotifications.remove(pathVars.accountNotificationId)
        request.successResponse(notification)
    }
})
