/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.util

import android.content.Context
import android.content.res.Configuration
import android.webkit.WebView
import androidx.core.content.ContextCompat
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.pageview.PageViewUploadService
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.LocaleUtils
import com.instructure.student.BuildConfig
import com.instructure.student.R
import com.instructure.student.activity.LoginActivity
import com.instructure.student.service.StudentPageViewService
import com.instructure.student.tasks.StudentLogoutTask
import com.pspdfkit.PSPDFKit
import com.pspdfkit.exceptions.InvalidPSPDFKitLicenseException
import com.pspdfkit.exceptions.PSPDFKitInitializationFailedException
import io.fabric.sdk.android.Fabric

class AppManager : com.instructure.canvasapi2.AppManager(), AnalyticsEventHandling {

    // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
    private val defaultTracker: Tracker by lazy {
        val analytics = GoogleAnalytics.getInstance(this)
        analytics.newTracker(R.xml.analytics)
    }

    override fun onCreate() {
        super.onCreate()
        initPSPDFKit()

        val crashlyticsKit = Crashlytics.Builder()
            .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
            .build()

        Fabric.with(this, crashlyticsKit)

        ColorKeeper.defaultColor = ContextCompat.getColor(this, R.color.defaultPrimary)

        // There appears to be a bug when the user is installing/updating the android webview stuff.
        // http://code.google.com/p/android/issues/detail?id=175124
        try {
            WebView.setWebContentsDebuggingEnabled(true)
        } catch (e: Exception) {
            Crashlytics.log("Exception trying to setWebContentsDebuggingEnabled")
        }

        PageViewUploadService.schedule(this, StudentPageViewService::class.java)
    }

    override fun attachBaseContext(base: Context) {
        /* Need to set context in ContextKeeper here because LocaleUtils.wrapContext() accesses shared prefs
         which uses ContextKeeper, but attachBaseContext occurs before ContentProvider init */
        ContextKeeper.appContext = base
        super.attachBaseContext(LocaleUtils.wrapContext(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleUtils.wrapContext(this)
    }

    override fun trackButtonPressed(buttonName: String?, buttonValue: Long?) {
        if (buttonName == null) return

        if (buttonValue == null) {
            defaultTracker.send(
                HitBuilders.EventBuilder()
                    .setCategory("UI Actions")
                    .setAction("Button Pressed")
                    .setLabel(buttonName)
                    .build()
            )
        } else {
            defaultTracker.send(
                HitBuilders.EventBuilder()
                    .setCategory("UI Actions")
                    .setAction("Button Pressed")
                    .setLabel(buttonName)
                    .setValue(buttonValue)
                    .build()
            )
        }
    }

    override fun trackScreen(screenName: String?) {
        if (screenName == null) return

        val tracker = defaultTracker
        tracker.setScreenName(screenName)
        tracker.send(HitBuilders.ScreenViewBuilder().build())
    }

    override fun trackEnrollment(enrollmentType: String?) {
        if (enrollmentType == null) return

        defaultTracker.send(
            HitBuilders.AppViewBuilder()
                .setCustomDimension(1, enrollmentType)
                .build()
        )
    }

    override fun trackDomain(domain: String?) {
        if (domain == null) return

        defaultTracker.send(
            HitBuilders.AppViewBuilder()
                .setCustomDimension(2, domain)
                .build()
        )
    }

    override fun trackEvent(category: String?, action: String?, label: String?, value: Long) {
        if (category == null || action == null || label == null) return

        val tracker = defaultTracker
        tracker.send(
            HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build()
        )
    }

    override fun trackUIEvent(action: String?, label: String?, value: Long) {
        if (action == null || label == null) return

        defaultTracker.send(
            HitBuilders.EventBuilder()
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build()
        )
    }

    override fun trackTiming(category: String?, name: String?, label: String?, duration: Long) {
        if (category == null || name == null || label == null) return

        val tracker = defaultTracker
        tracker.send(
            HitBuilders.TimingBuilder()
                .setCategory(category)
                .setLabel(label)
                .setVariable(name)
                .setValue(duration)
                .build()
        )
    }

    private fun initPSPDFKit() {
        try {
            PSPDFKit.initialize(this, BuildConfig.PSPDFKIT_LICENSE_KEY)
        } catch (e: PSPDFKitInitializationFailedException) {
            Logger.e("Current device is not compatible with PSPDFKIT!")
        } catch (e: InvalidPSPDFKitLicenseException) {
            Logger.e("Invalid or Trial PSPDFKIT License!")
        }
    }

    override fun performLogoutOnAuthError() {
        StudentLogoutTask(LogoutTask.Type.LOGOUT).execute()
    }

    companion object {

        fun restartApp(context: Context) {
            LocaleUtils.restartApp(context, LoginActivity::class.java)
        }

    }

}
