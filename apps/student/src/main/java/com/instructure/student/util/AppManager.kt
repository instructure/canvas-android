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

import android.os.Build
import android.webkit.WebView
import androidx.core.content.ContextCompat
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants.USER_PROPERTY_BUILD_TYPE
import com.instructure.canvasapi2.utils.AnalyticsEventConstants.USER_PROPERTY_OS_VERSION
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.MasqueradeHelper
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.canvasapi2.utils.pageview.PageViewUploadService
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.typeface.TypefaceBehavior
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.student.BuildConfig
import com.instructure.student.R
import com.instructure.student.flutterChannels.FlutterComm
import com.instructure.student.service.StudentPageViewService
import com.instructure.student.tasks.StudentLogoutTask
import com.pspdfkit.PSPDFKit
import com.pspdfkit.exceptions.InvalidPSPDFKitLicenseException
import com.pspdfkit.exceptions.PSPDFKitInitializationFailedException
import dagger.hilt.android.HiltAndroidApp
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor.DartEntrypoint
import javax.inject.Inject

@HiltAndroidApp
class AppManager : BaseAppManager() {

    @Inject
    lateinit var typefaceBehavior: TypefaceBehavior

    override fun onCreate() {
        super.onCreate()
        MasqueradeHelper.masqueradeLogoutTask = Runnable { StudentLogoutTask(LogoutTask.Type.LOGOUT, typefaceBehavior = typefaceBehavior).execute() }
    }

    override fun performLogoutOnAuthError() {
        StudentLogoutTask(LogoutTask.Type.LOGOUT, typefaceBehavior = typefaceBehavior).execute()
    }
}
