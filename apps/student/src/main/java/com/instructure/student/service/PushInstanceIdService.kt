/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

import com.google.android.gms.iid.InstanceIDListenerService
import com.instructure.student.BuildConfig
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.services.PushNotificationRegistrationService

class PushInstanceIdService : InstanceIDListenerService() {

    override fun onTokenRefresh() {
        super.onTokenRefresh()
        PushNotificationRegistrationService.scheduleJob(applicationContext, ApiPrefs.isMasquerading, BuildConfig.PUSH_SERVICE_PROJECT_ID)
    }
}
