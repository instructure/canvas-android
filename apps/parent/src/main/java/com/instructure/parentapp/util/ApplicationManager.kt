/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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

import android.content.Context
import android.util.Log
import android.webkit.WebView
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory
import com.instructure.canvasapi2.AppManager
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.Prefs
import com.instructure.parentapp.BuildConfig
import com.instructure.parentapp.tasks.ParentLogoutTask
import io.fabric.sdk.android.Fabric
import java.util.*

class ApplicationManager : AppManager() {

    override fun onCreate() {
        // Set preferences to create a pre-logged-in state. This should only be used for the 'robo' app flavor.
        if (BuildConfig.IS_ROBO_TEST) RoboTesting.setAppStatePrefs(this)
        if (MissingSplitsManagerFactory.create(this).disableAppIfMissingRequiredSplits()) {
            // Skip app initialization.
            return
        }
        RemoteConfigUtils.initialize()
        super.onCreate()

        val crashlytics = Crashlytics.Builder()
            .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
            .build()
        Fabric.with(this, crashlytics)

        // there appears to be a bug when the user is installing/updating the android webview stuff.
        // http://code.google.com/p/android/issues/detail?id=175124
        try {
            WebView.setWebContentsDebuggingEnabled(true)
        } catch (e: Exception) {
            Log.d("ParentApp", "Exception trying to setWebContentsDebuggingEnabled")
        }

        val pref = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        // If we don't have one, generate one.
        if (!pref.contains("APID")) {
            val uuid = UUID.randomUUID().toString()

            val editor = pref.edit()
            editor.putString("APID", uuid)
            editor.apply()
        }
    }

    override fun performLogoutOnAuthError() {
        ParentLogoutTask(LogoutTask.Type.LOGOUT).execute()
    }

    companion object {

        const val PREF_NAME = "android_parent_SP"

        fun getParentId(context: Context): String {
            val prefs = Prefs(context, com.instructure.parentapp.util.Const.CANVAS_PARENT_SP)
            return prefs.load(Const.ID, "")
        }

    }

}
