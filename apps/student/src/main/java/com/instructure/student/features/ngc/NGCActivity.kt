/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.features.ngc

import android.content.Intent
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.base.BaseCanvasActivity
import com.instructure.pandautils.models.PushNotification
import com.instructure.pandautils.receivers.PushExternalReceiver
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.EdgeToEdgeHelper
import com.instructure.pandautils.utils.Utils
import com.instructure.pandautils.utils.WebViewAuthenticator
import com.instructure.student.features.ngc.navigation.NGCNavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NGCActivity : BaseCanvasActivity() {

    @Inject
    lateinit var webViewAuthenticator: WebViewAuthenticator

    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdgeHelper.enableEdgeToEdge(this)

        setContent {
            navController = rememberNavController()

            NGCTheme {
                NGCNavigation(navController)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        webViewAuthenticator.authenticateWebViews(lifecycleScope, this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (hasLocalNotificationLink(intent.extras) ||
            hasUnreadPushNotification(intent.extras)
        ) {
            handlePushNotification(hasUnreadPushNotification(intent.extras))
        } else {
            intent.data?.let { uri ->
                val request = NavDeepLinkRequest.Builder
                    .fromUri(uri)
                    .build()

                navController.navigate(
                    request,
                    navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
                )
            }
        }
    }

    private fun handlePushNotification(hasUnreadNotifications: Boolean) {
        val intent = intent
        if (intent != null) {
            val extras = intent.extras
            if (extras != null) {
                if (hasUnreadNotifications) {
                    setPushNotificationAsRead()
                }

                val htmlUrl = extras.getString(PushNotification.HTML_URL, "").toUri()
                val request = NavDeepLinkRequest.Builder
                    .fromUri(htmlUrl)
                    .build()

                navController.navigate(
                    request,
                    navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
                )
            }
        }
    }

    private fun hasUnreadPushNotification(extras: Bundle?): Boolean {
        return (extras != null && extras.containsKey(PushExternalReceiver.NEW_PUSH_NOTIFICATION)
                && extras.getBoolean(PushExternalReceiver.NEW_PUSH_NOTIFICATION, false))
    }

    private fun setPushNotificationAsRead() {
        intent.putExtra(PushExternalReceiver.NEW_PUSH_NOTIFICATION, false)
        PushNotification.remove(intent)
    }

    private fun hasLocalNotificationLink(extras: Bundle?): Boolean {
        val flag = extras != null && extras.containsKey(Const.LOCAL_NOTIFICATION)
                && extras.getBoolean(Const.LOCAL_NOTIFICATION, false)
        if (flag) {
            // Clear the flag if we are handling this, so subsequent app opens don't deep link again
            extras.putBoolean(Const.LOCAL_NOTIFICATION,false)
        }
        return flag
    }

    /**
     * ONLY USE FOR UI TESTING
     * Skips the traditional login process by directly setting the domain, token, and user info.
     */
    fun loginWithToken(token: String, domain: String, user: User) {
        ApiPrefs.accessToken = token
        ApiPrefs.domain = domain
        ApiPrefs.user = user
        ApiPrefs.userAgent = Utils.generateUserAgent(this, Const.STUDENT_USER_AGENT)
        finish()
        val intent = Intent(this, NGCActivity::class.java).apply {
            intent?.extras?.let { putExtras(it) }
        }
        startActivity(intent)
    }
}