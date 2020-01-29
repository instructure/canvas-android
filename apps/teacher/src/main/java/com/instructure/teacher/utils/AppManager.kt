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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
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
import io.fabric.sdk.android.Fabric

class AppManager : com.instructure.canvasapi2.AppManager() {

    override fun onCreate() {
        // Set preferences to create a pre-logged-in state. This should only be used for the 'robo' app flavor.
        if (BuildConfig.IS_ROBO_TESTING) RoboTesting.setAppStatePrefs()
        if (MissingSplitsManagerFactory.create(this).disableAppIfMissingRequiredSplits()) {
            // Skip app initialization.
            return
        }
        RemoteConfigUtils.initialize()
        super.onCreate()

        if (!ApiPrefs.domain.endsWith(com.instructure.loginapi.login.BuildConfig.ANONYMOUS_SCHOOL_DOMAIN)) {
            val crashlytics = Crashlytics.Builder()
                    .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                    .build()
            Fabric.with(this, crashlytics)
            Analytics.trackAppFlow(this)
        }

        ColorKeeper.defaultColor = getColorCompat(R.color.defaultPrimary)

        try {
            PSPDFKit.initialize(this, BuildConfig.PSPDFKIT_LICENSE_KEY)
        } catch (e: PSPDFKitInitializationFailedException) {
            Logger.e("Current device is not compatible with PSPDFKIT!")
        } catch (e: InvalidPSPDFKitLicenseException) {
            Logger.e("Invalid or Trial PSPDFKIT License!")
        }

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
