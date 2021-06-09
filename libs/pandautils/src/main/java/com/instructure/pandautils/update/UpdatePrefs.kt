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

import com.instructure.canvasapi2.utils.IntPref
import com.instructure.canvasapi2.utils.PrefManager
import com.instructure.canvasapi2.utils.StringPref


const val UPDATE_PREFS_FILE_NAME = "updatePreferences"
const val FLEXIBLE_UPDATE_NOTIFICATION_MAX_COUNT = 2
const val FLEXIBLE_UPDATE_NOTIFICATION_INTERVAL_DAYS = 1

object UpdatePrefs : PrefManager(UPDATE_PREFS_FILE_NAME) {

    var lastUpdateNotificationDate by StringPref()
    var lastUpdateNotificationVersionCode by IntPref()
    var lastUpdateNotificationCount by IntPref()
    var hasShownThisStart = false

}