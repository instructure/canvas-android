/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.student.service

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.utils.pageview.PageViewUploadService
import com.instructure.canvasapi2.utils.pageview.PandataInfo
import com.instructure.student.BuildConfig

/**
 * A [PageViewUploadService] specific to the Student application.
 *
 * To test this service, install the app on a device running Android 7.1+ and run the following command:
 *     adb shell cmd jobscheduler run -f com.instructure.candroid 188372
 */
class StudentPageViewService : PageViewUploadService() {

    override val appKey = pandataAppKey

    override val tokenAppKey = pandataTokenKey

    override fun onException(e: Throwable) = FirebaseCrashlytics.getInstance().recordException(e)


    companion object {
        val pandataAppKey = PandataInfo.AppKey(
            "CANVAS_STUDENT_ANDROID",
            "Canvas Student for Android - ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        )

        val pandataTokenKey = "CANVAS_STUDENT_ANDROID"
    }

}
