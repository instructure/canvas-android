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

package com.instructure.pandautils.update

import com.google.android.play.core.appupdate.AppUpdateInfo
import com.instructure.canvasapi2.utils.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs


const val UPDATE_PREFS_FILE_NAME = "updatePreferences"
const val FLEXIBLE_UPDATE_NOTIFICATION_MAX_COUNT = 2
const val FLEXIBLE_UPDATE_NOTIFICATION_INTERVAL_DAYS = 1

class UpdatePrefs : PrefManager(UPDATE_PREFS_FILE_NAME) {

    private var lastUpdateNotificationDate by StringPref()
    private var lastUpdateNotificationVersionCode by IntPref()
    private var lastUpdateNotificationCount by IntPref()
    private var hasShownThisStart = false

    fun shouldShowUpdateNotification(appUpdateInfo: AppUpdateInfo): Boolean {

        if (lastUpdateNotificationDate.isBlank() || lastUpdateNotificationVersionCode != appUpdateInfo.availableVersionCode()) {
            lastUpdateNotificationDate = Date().toApiString() ?: ""
            lastUpdateNotificationVersionCode = appUpdateInfo.availableVersionCode()
            lastUpdateNotificationCount = 1
            hasShownThisStart = true
            return true
        }

        if (appUpdateInfo.updatePriority() >= IMMEDIATE_THRESHOLD && !hasShownThisStart) {
            hasShownThisStart = true
            return true
        }

        if (appUpdateInfo.updatePriority() in FLEXIBLE_THRESHOLD until IMMEDIATE_THRESHOLD) {
            val lastUpdateDate = lastUpdateNotificationDate.toDate()
            val currentDate = Date()
            val diff = TimeUnit.DAYS.convert(abs(currentDate.time - lastUpdateDate!!.time), TimeUnit.MILLISECONDS)

            if (diff >= FLEXIBLE_UPDATE_NOTIFICATION_INTERVAL_DAYS && lastUpdateNotificationCount <= FLEXIBLE_UPDATE_NOTIFICATION_MAX_COUNT) {
                lastUpdateNotificationCount += 1
                lastUpdateNotificationDate = Date().toApiString() ?: ""
                return true
            }
        }

        return false
    }

}