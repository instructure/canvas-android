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
import com.google.firebase.messaging.FirebaseMessaging
import com.instructure.canvasapi2.utils.tryOrNull
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.typeface.TypefaceBehavior
import com.instructure.student.activity.LoginActivity
import com.instructure.student.flutterChannels.FlutterComm
import com.instructure.student.util.StudentPrefs
import com.instructure.student.widget.WidgetUpdater

class StudentLogoutTask(
    type: Type,
    uri: Uri? = null,
    canvasForElementaryFeatureFlag: Boolean = false,
    typefaceBehavior: TypefaceBehavior? = null
) : LogoutTask(type, uri, canvasForElementaryFeatureFlag, typefaceBehavior) {

    override fun onCleanup() {
        FlutterComm.reset()
        StudentPrefs.safeClearPrefs()
        WidgetUpdater.updateWidgets()
    }

    override fun createLoginIntent(context: Context): Intent {
        return LoginActivity.createIntent(context)
    }

    override fun createQRLoginIntent(context: Context, uri: Uri): Intent? {
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
}
