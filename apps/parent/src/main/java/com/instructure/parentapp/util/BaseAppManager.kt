/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.util

import android.os.Build
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.AppManager
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.pandautils.utils.AppConfig
import com.instructure.pandautils.utils.AppConfigProvider
import com.instructure.pandautils.utils.AppTheme
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.parentapp.BuildConfig
import com.instructure.parentapp.R
import com.instructure.parentapp.features.main.MainActivity

abstract class BaseAppManager : AppManager() {

    override fun onCreate() {
        super.onCreate()
        AppConfigProvider.appConfig = AppConfig("parent", MainActivity::class.java)

        val appTheme = AppTheme.fromIndex(ThemePrefs.appTheme)
        AppCompatDelegate.setDefaultNightMode(appTheme.nightModeType)

        Analytics.setUserProperty(AnalyticsEventConstants.USER_PROPERTY_BUILD_TYPE, if (BuildConfig.DEBUG) "debug" else "release")
        Analytics.setUserProperty(AnalyticsEventConstants.USER_PROPERTY_OS_VERSION, Build.VERSION.SDK_INT.toString())

        RemoteConfigUtils.initialize()

        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        } else {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        }

        ColorKeeper.defaultColor = ContextCompat.getColor(this, R.color.textDarkest)

        try {
            WebView.setWebContentsDebuggingEnabled(true)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().log("Exception trying to setWebContentsDebuggingEnabled")
        }
    }

    override fun performLogoutOnAuthError() = Unit
}