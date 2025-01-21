/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.firebase.messaging.FirebaseMessaging
import com.instructure.canvasapi2.utils.tryOrNull
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.parentapp.features.login.LoginActivity
import io.heap.core.Heap


class ParentLogoutTask(
    type: Type,
    uri: Uri? = null,
    private val alarmScheduler: AlarmScheduler?
) : LogoutTask(type, uri) {

    override fun onCleanup() {
        ParentPrefs.safeClearPrefs()
        Heap.stopRecording()
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
        // No-op
    }

    override suspend fun cancelAlarms() {
        alarmScheduler?.cancelAllAlarmsForCurrentUser()
    }
}