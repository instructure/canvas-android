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
 */
package com.instructure.horizon

import android.content.Intent
import android.content.pm.ShortcutManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.instructure.horizon.horizonui.HorizonTheme
import com.instructure.horizon.navigation.HorizonNavigation
import com.instructure.pandautils.base.BaseCanvasActivity
import com.instructure.pandautils.models.PushNotification
import com.instructure.pandautils.receivers.PushExternalReceiver
import com.instructure.pandautils.utils.AppTheme
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.Utils
import com.instructure.pandautils.utils.WebViewAuthenticator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HorizonActivity : BaseCanvasActivity() {

    @Inject
    lateinit var webViewAuthenticator: WebViewAuthenticator

    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val manager = getSystemService(ShortcutManager::class.java)
        manager?.removeAllDynamicShortcuts()
        if (ThemePrefs.appTheme != AppTheme.LIGHT.ordinal) {
            setLightTheme() // Force the light theme for Horizon experience to avoid any glitches.
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.Transparent.toArgb(), Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.light(Color.Transparent.toArgb(), Color.Transparent.toArgb())
        )

        setContent {
            navController = rememberNavController()

            HorizonTheme {
                HorizonNavigation(navController)
            }

            var isHandled by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                if (savedInstanceState == null && !isHandled) {
                    isHandled = true
                    if (hasUnreadPushNotification(intent.extras) || hasLocalNotificationLink(intent.extras)) {
                        handlePushNotification(hasUnreadPushNotification(intent.extras))
                    }
                }
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
        }
    }

    private fun setLightTheme() {
        val appTheme = AppTheme.LIGHT
        AppCompatDelegate.setDefaultNightMode(appTheme.nightModeType)
        ThemePrefs.appTheme = appTheme.ordinal

        val nightModeFlags: Int = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        ColorKeeper.darkTheme = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        ThemePrefs.isThemeApplied = false
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
            extras!!.putBoolean(Const.LOCAL_NOTIFICATION,false)
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
        ApiPrefs.canvasCareerView = true
        ApiPrefs.userAgent = Utils.generateUserAgent(this, Const.STUDENT_USER_AGENT)
        finish()
        val intent = Intent(this, HorizonActivity::class.java).apply {
            intent?.extras?.let { putExtras(it) }
        }
        startActivity(intent)
    }
}