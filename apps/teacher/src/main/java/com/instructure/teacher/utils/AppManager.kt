/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.utils

import android.content.IntentFilter
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.MasqueradeHelper
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.R
import com.instructure.teacher.tasks.TeacherLogoutTask
import com.pspdfkit.PSPDFKit
import com.pspdfkit.exceptions.InvalidPSPDFKitLicenseException
import com.pspdfkit.exceptions.PSPDFKitInitializationFailedException
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppManager : com.instructure.canvasapi2.AppManager() {

    override fun onCreate() {
        if (MissingSplitsManagerFactory.create(this).disableAppIfMissingRequiredSplits()) {
            // Skip app initialization.
            return
        }
        super.onCreate()

        // Call it superstition, but I don't trust BuildConfig flags to be set correctly
        // in library builds.  IS_TESTING, for example, does not percolate down to libraries
        // correctly.  So I'm reading/setting these user properties here instead of canvasapi2/AppManager.
        com.instructure.canvasapi2.utils.Analytics.setUserProperty(AnalyticsEventConstants.USER_PROPERTY_BUILD_TYPE, if (BuildConfig.DEBUG) "debug" else "release")
        com.instructure.canvasapi2.utils.Analytics.setUserProperty(AnalyticsEventConstants.USER_PROPERTY_OS_VERSION, Build.VERSION.SDK_INT.toString())

        // Hold off on initializing this until we emit the user properties.
        RemoteConfigUtils.initialize()

        if (!ApiPrefs.domain.endsWith(com.instructure.loginapi.login.BuildConfig.ANONYMOUS_SCHOOL_DOMAIN)) {
            if (BuildConfig.DEBUG) {
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
            } else {
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            }
        }

        ColorKeeper.defaultColor = getColorCompat(R.color.defaultPrimary)

        try {
            PSPDFKit.initialize(this, BuildConfig.PSPDFKIT_LICENSE_KEY)
        } catch (e: PSPDFKitInitializationFailedException) {
            Logger.e("Current device is not compatible with PSPDFKIT!")
        } catch (e: InvalidPSPDFKitLicenseException) {
            Logger.e("Invalid or Trial PSPDFKIT License!")
        }

        MasqueradeHelper.masqueradeLogoutTask = Runnable { TeacherLogoutTask(LogoutTask.Type.LOGOUT).execute() }

        // SpeedGrader submission media comment
        val mediaUploadReceiver = SGPendingMediaCommentReceiver()
        val filter = IntentFilter()
        filter.addAction(Const.ACTION_MEDIA_UPLOAD_SUCCESS)
        filter.addAction(Const.ACTION_MEDIA_UPLOAD_FAIL)
        LocalBroadcastManager.getInstance(this).registerReceiver(mediaUploadReceiver, filter)
    }

    override fun performLogoutOnAuthError() {
        TeacherLogoutTask(LogoutTask.Type.LOGOUT).execute()
    }

    companion object {
        val PREF_FILE_NAME = "teacherSP"
        val MULTI_SIGN_IN_PREF_NAME = "multipleSignInTeacherSP"
        val OTHER_SIGNED_IN_USERS_PREF_NAME = "otherSignedInUsersTeacherSP"
        val PREF_NAME_PREVIOUS_DOMAINS = "teacherSP_previous_domains"
    }
}
