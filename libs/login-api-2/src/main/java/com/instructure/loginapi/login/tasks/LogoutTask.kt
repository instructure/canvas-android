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
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.managers.CommunicationChannelsManager
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.MasqueradeHelper
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.loginapi.login.util.PreviousUsersUtils
import com.instructure.pandautils.models.PushNotification
import com.instructure.pandautils.typeface.TypefaceBehavior
import com.instructure.pandautils.utils.FilePrefs
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.Utils
import java.io.File

abstract class LogoutTask(
    val type: Type,
    val uri: Uri? = null,
    private val canvasForElementaryFeatureFlag: Boolean = false,
    private val typefaceBehavior: TypefaceBehavior? = null
) {

    enum class Type {
        SWITCH_USERS,
        LOGOUT,
        LOGOUT_NO_LOGIN_FLOW,
        QR_CODE_SWITCH
    }

    protected abstract fun onCleanup()
    protected abstract fun createLoginIntent(context: Context): Intent
    protected abstract fun createQRLoginIntent(context: Context, uri: Uri): Intent?
    protected abstract fun getFcmToken(listener: (registrationId: String?) -> Unit)
    protected abstract fun removeOfflineData(userId: Long?)

    protected open fun stopOfflineSync() = Unit

    protected open suspend fun cancelAlarms() = Unit

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
                typefaceBehavior?.resetFonts()
                // Clear push notifications
                if (registrationId != null) {
                    // Synchronously delete channel, has to be done before we clear the user as it makes an API call
                    CommunicationChannelsManager.deletePushCommunicationChannelSynchronous(registrationId)
                }
                PushNotification.clearPushHistory()

                stopOfflineSync()

                cancelAlarms()

                when (type) {
                    Type.LOGOUT, Type.LOGOUT_NO_LOGIN_FLOW -> {
                        removeOfflineData(ApiPrefs.user?.id)
                        removeUser()
                    }

                    Type.SWITCH_USERS, Type.QR_CODE_SWITCH -> updateUser()
                }

                // Clean up masquerading
                MasqueradeHelper.stopMasquerading<Activity>()
                File(ContextKeeper.appContext.filesDir, "cache_masquerade").deleteRecursively()

                // Clear caches
                CanvasRestAdapter.okHttpClient.cache?.evictAll()
                RestBuilder.clearCacheDirectory()
                Utils.getAttachmentsDirectory(ContextKeeper.appContext).deleteRecursively()
                File(ContextKeeper.appContext.filesDir, "cache").deleteRecursively()

                // Clear prefs
                ApiPrefs.clearAllData()
                FilePrefs.clearPrefs()
                ThemePrefs.safeClearPrefs()

                // Cookies are cleared in BaseLoginSignInActivity
            }

            // Perform additional, app-specific cleanup
            onCleanup()

            // Go to login page
            if (type != Type.LOGOUT_NO_LOGIN_FLOW) {
                // If this was triggered by a QR switch, we need a different intent to include the URI
                val intent = if (type == Type.QR_CODE_SWITCH && uri != null)
                    createQRLoginIntent(ContextKeeper.appContext, uri)
                else
                    createLoginIntent(ContextKeeper.appContext)
                ContextKeeper.appContext.startActivity(intent)
            }
        }
    }

    private fun removeUser() {
        // Don't want to inadvertently invalidate a Teacher's token
        if (ApiPrefs.isStudentView) return

        // Remove SignedInUser
        PreviousUsersUtils.removeByToken(ContextKeeper.appContext, ApiPrefs.getValidToken(), ApiPrefs.refreshToken)
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
            signedInUser.canvasForElementary = canvasForElementaryFeatureFlag
            signedInUser.user = currentUser
            signedInUser.clientId = ApiPrefs.clientId
            signedInUser.clientSecret = ApiPrefs.clientSecret
            PreviousUsersUtils.add(ContextKeeper.appContext, signedInUser)
        }
    }
}
