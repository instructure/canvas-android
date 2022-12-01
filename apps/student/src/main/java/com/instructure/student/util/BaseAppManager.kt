/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.util

import android.os.Build
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.heapanalytics.android.Heap
import com.heapanalytics.android.config.Options
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.canvasapi2.utils.pageview.PageViewUploadService
import com.instructure.pandautils.utils.AppTheme
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.student.BuildConfig
import com.instructure.student.R
import com.instructure.student.flutterChannels.FlutterComm
import com.instructure.student.service.StudentPageViewService
import com.pspdfkit.PSPDFKit
import com.pspdfkit.exceptions.InvalidPSPDFKitLicenseException
import com.pspdfkit.exceptions.PSPDFKitInitializationFailedException
import com.zynksoftware.documentscanner.ui.DocumentScanner
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor

abstract class BaseAppManager : com.instructure.canvasapi2.AppManager(), AnalyticsEventHandling, Configuration.Provider {

    override fun onCreate() {
        if (MissingSplitsManagerFactory.create(this).disableAppIfMissingRequiredSplits()) {
            // Skip app initialization.
            return
        }
        super.onCreate()

        val appTheme = AppTheme.fromIndex(ThemePrefs.appTheme)
        AppCompatDelegate.setDefaultNightMode(appTheme.nightModeType)

        // Call it superstition, but I don't trust BuildConfig flags to be set correctly
        // in library builds.  IS_TESTING, for example, does not percolate down to libraries
        // correctly.  So I'm reading/setting these user properties here instead of canvasapi2/AppManager.
        Analytics.setUserProperty(AnalyticsEventConstants.USER_PROPERTY_BUILD_TYPE, if(BuildConfig.DEBUG) "debug" else "release")
        Analytics.setUserProperty(AnalyticsEventConstants.USER_PROPERTY_OS_VERSION, Build.VERSION.SDK_INT.toString())

        // Hold off on initializing this until we emit the user properties.
        RemoteConfigUtils.initialize()

        initPSPDFKit()

        initDocumentScanning()

        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        } else {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        }

        ColorKeeper.defaultColor = ContextCompat.getColor(this, R.color.textDarkest)

        // There appears to be a bug when the user is installing/updating the android webview stuff.
        // http://code.google.com/p/android/issues/detail?id=175124
        try {
            WebView.setWebContentsDebuggingEnabled(true)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().log("Exception trying to setWebContentsDebuggingEnabled")
        }

        PageViewUploadService.schedule(this, StudentPageViewService::class.java)

        initFlutterEngine()

        val options = Options()
        options.disableTracking()
        Heap.init(this, BuildConfig.HEAP_APP_ID, options)
    }

    private fun initFlutterEngine() {
        flutterEngine = FlutterEngine(this)

        FlutterComm.init(flutterEngine, applicationContext)

        // Execute the 'main' entrypoint
        flutterEngine.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())

        // Cache the FlutterEngine
        FlutterEngineCache.getInstance().put(FLUTTER_ENGINE_ID, flutterEngine)
    }

    override fun onCanvasTokenRefreshed() = FlutterComm.sendUpdatedLogin()

    override fun trackButtonPressed(buttonName: String?, buttonValue: Long?) {

    }

    override fun trackScreen(screenName: String?) {

    }

    override fun trackEnrollment(enrollmentType: String?) {

    }

    override fun trackDomain(domain: String?) {

    }

    override fun trackEvent(category: String?, action: String?, label: String?, value: Long) {

    }

    override fun trackUIEvent(action: String?, label: String?, value: Long) {

    }

    override fun trackTiming(category: String?, name: String?, label: String?, duration: Long) {

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

    private fun initDocumentScanning() {
        DocumentScanner.init(this)
    }

    override fun performLogoutOnAuthError() = Unit

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(getWorkManagerFactory())
            .build()

    abstract fun getWorkManagerFactory(): WorkerFactory

    companion object {
        private const val FLUTTER_ENGINE_ID = "flutter_engine_embed"

        lateinit var flutterEngine: FlutterEngine
    }
}