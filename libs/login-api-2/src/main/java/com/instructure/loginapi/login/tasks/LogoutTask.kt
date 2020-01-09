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
package com.instructure.loginapi.login.tasks

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.managers.CommunicationChannelsManager
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.MasqueradeHelper
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.loginapi.login.util.PreviousUsersUtils
import com.instructure.pandautils.models.PushNotification
import com.instructure.pandautils.utils.FilePrefs
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.Utils
import java.io.File
import java.lang.Exception

abstract class LogoutTask(val type: Type) {

    enum class Type {
        SWITCH_USERS,
        LOGOUT,
        LOGOUT_NO_LOGIN_FLOW
    }

    protected abstract fun onCleanup()
    protected abstract fun createLoginIntent(context: Context): Intent
    protected abstract fun getFcmToken(listener: (registrationId: String?) -> Unit)

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun execute() {
        try {
            // Get the fcm token to delete the comm channel, then resume logout
            getFcmToken { registrationId ->
                handleLogoutTask(registrationId)
            }
        } catch (e: Exception) {
            // Fallback to null in case anything bad happens, that way the logout still goes through
            handleLogoutTask(null)
        }
    }

    private fun handleLogoutTask(registrationId: String?) {
        weave {
            inBackground {
                // Clear push notifications
                if (registrationId != null) {
                    // Synchronously delete channel, has to be done before we clear the user as it makes an API call
                    CommunicationChannelsManager.deletePushCommunicationChannelSynchronous(registrationId)
                }
                (ContextKeeper.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .cancel(PushNotification.NOTIFY_ID)
                PushNotification.clearPushHistory()

                when (type) {
                    Type.LOGOUT, Type.LOGOUT_NO_LOGIN_FLOW -> removeUser()
                    Type.SWITCH_USERS -> updateUser()
                }

                // Clean up masquerading
                MasqueradeHelper.stopMasquerading<Activity>()
                File(ContextKeeper.appContext.filesDir, "cache_masquerade").deleteRecursively()

                // Clear caches
                CanvasRestAdapter.okHttpClient.cache()?.evictAll()
                RestBuilder.clearCacheDirectory()
                Utils.getAttachmentsDirectory(ContextKeeper.appContext).deleteRecursively()
                File(ContextKeeper.appContext.filesDir, "cache").deleteRecursively()

                // Clear prefs
                ApiPrefs.clearAllData()
                FilePrefs.clearPrefs()
                ThemePrefs.clearPrefs()

                // Cookies are cleared in BaseLoginSignInActivity
            }

            // Perform additional, app-specific cleanup
            onCleanup()

            // Go to login page
            if (type != Type.LOGOUT_NO_LOGIN_FLOW) {
                val intent = createLoginIntent(ContextKeeper.appContext)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                ContextKeeper.appContext.startActivity(intent)
            }
        }
    }

    private fun removeUser() {
        // Remove SignedInUser
        PreviousUsersUtils.removeByToken(ContextKeeper.appContext, ApiPrefs.token, ApiPrefs.refreshToken)
        // Delete token from server. Fire and forget.
        if (ApiPrefs.getValidToken().isNotEmpty()) OAuthManager.deleteToken()
    }

    private fun updateUser() {
        // Update SignedInUser to preserve changes to name, locale, etc
        val currentUser = ApiPrefs.user
        val signedInUser = PreviousUsersUtils.getSignedInUser(
            ContextKeeper.appContext,
            ApiPrefs.domain,
            currentUser?.id ?: 0
        )
        if (currentUser != null && signedInUser != null) {
            signedInUser.user = currentUser
            PreviousUsersUtils.add(ContextKeeper.appContext, signedInUser)
        }
    }


}
