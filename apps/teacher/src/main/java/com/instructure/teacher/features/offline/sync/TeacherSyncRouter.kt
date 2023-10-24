/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 *
 */

package com.instructure.teacher.features.offline.sync

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.instructure.pandautils.features.offline.sync.SyncRouter
import com.instructure.teacher.activities.RouteValidatorActivity

class TeacherSyncRouter : SyncRouter {
    override fun routeToSyncProgress(context: Context): PendingIntent {
        val intent = Intent(context, RouteValidatorActivity::class.java)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}