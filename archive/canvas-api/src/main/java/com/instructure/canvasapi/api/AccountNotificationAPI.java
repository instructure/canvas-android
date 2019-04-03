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

package com.instructure.canvasapi.api;

import com.instructure.canvasapi.model.AccountNotification;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import retrofit.RestAdapter;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Path;

public class AccountNotificationAPI extends BuildInterfaceAPI {

    public interface AccountNotificationInterface {

        @GET("/accounts/self/users/self/account_notifications")
        void getAccountNotifications(CanvasCallback<AccountNotification[]> callback);

        @DELETE("/accounts/self/users/self/account_notifications/{account_notification_id}")
        void deleteAccountNotification(@Path("account_notification_id") long account_notification_id, CanvasCallback<AccountNotification> callback);

    }

    public static void getAccountNotifications(final CanvasCallback<AccountNotification[]> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildCacheInterface(AccountNotificationInterface.class, callback, null).getAccountNotifications(callback);
        buildInterface(AccountNotificationInterface.class, callback, null).getAccountNotifications(callback);
    }

    public static void getAccountNotificationsChained(final CanvasCallback<AccountNotification[]> callback, boolean isCached) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        if (isCached) {
            buildCacheInterface(AccountNotificationInterface.class, callback, null).getAccountNotifications(callback);
        } else {
            buildInterface(AccountNotificationInterface.class, callback, null).getAccountNotifications(callback);
        }
    }

    public static void deleteAccountNotification(long accountNotificationId, CanvasCallback<AccountNotification> callback) {
        if (APIHelpers.paramIsNull(callback)) { return; }

        buildInterface(AccountNotificationInterface.class, callback, null).deleteAccountNotification(accountNotificationId, callback);
    }
}
