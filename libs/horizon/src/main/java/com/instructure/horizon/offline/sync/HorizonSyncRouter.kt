/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.offline.sync

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.HorizonActivity
import com.instructure.pandautils.models.PushNotification
import com.instructure.pandautils.utils.Const
import javax.inject.Inject

class HorizonSyncRouter @Inject constructor(private val apiPrefs: ApiPrefs) {
    fun routeToSyncProgress(context: Context): PendingIntent {
        val path = "${apiPrefs.fullDomain}$SYNC_PROGRESS_PATH"
        val intent = Intent(context, HorizonActivity::class.java).apply {
            putExtra(Const.LOCAL_NOTIFICATION, true)
            putExtra(PushNotification.HTML_URL, path)
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val SYNC_PROGRESS_PATH = "/syncProgress"
    }
}
