/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
 */
package com.instructure.student.tasks

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessaging
import com.heapanalytics.android.Heap
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.tryOrNull
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.features.offline.sync.OfflineSyncWorker
import com.instructure.pandautils.room.offline.DatabaseProvider
import com.instructure.pandautils.typeface.TypefaceBehavior
import com.instructure.student.activity.LoginActivity
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.student.util.StudentPrefs
import com.instructure.student.widget.WidgetUpdater
import java.io.File

class StudentLogoutTask(
    type: Type,
    uri: Uri? = null,
    canvasForElementaryFeatureFlag: Boolean = false,
    typefaceBehavior: TypefaceBehavior? = null,
    private val databaseProvider: DatabaseProvider? = null,
    private val alarmScheduler: AlarmScheduler? = null
) : LogoutTask(type, uri, canvasForElementaryFeatureFlag, typefaceBehavior) {

    override fun onCleanup() {
        StudentPrefs.safeClearPrefs()
        WidgetUpdater.updateWidgets()
        Heap.setTrackingEnabled(false)
    }

    override fun createLoginIntent(context: Context): Intent {
        return LoginActivity.createIntent(context)
    }

    override fun createQRLoginIntent(context: Context, uri: Uri): Intent {
        return LoginActivity.createIntent(context, uri)
    }

    override fun getFcmToken(listener: (registrationId: String?) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            // Task.getResult() can throw exceptions, such as java.io.IOException: SERVICE_NOT_AVAILABLE. We want
            // to catch the exception here and pass a null string to the listener to allow the LogoutTask to continue
            // with the remaining logout and cleanup tasks.
            val registrationId: String? = tryOrNull { task.result }
            listener(registrationId)
        }
    }

    override fun removeOfflineData(userId: Long?) {
        userId?.let {
            val dir = File(ContextKeeper.appContext.filesDir, it.toString())
            dir.deleteRecursively()
            databaseProvider?.clearDatabase(it)
        }
    }

    override fun stopOfflineSync() {
        val workManager = WorkManager.getInstance(ContextKeeper.appContext)
        workManager.apply {
            cancelAllWorkByTag(OfflineSyncWorker.PERIODIC_TAG)
            cancelAllWorkByTag(OfflineSyncWorker.ONE_TIME_TAG)
        }
    }

    override suspend fun cancelAlarms() {
        alarmScheduler?.cancelAllAlarmsForCurrentUser()
    }
}
