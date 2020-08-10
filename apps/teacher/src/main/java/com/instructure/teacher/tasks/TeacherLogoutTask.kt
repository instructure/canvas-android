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
package com.instructure.teacher.tasks

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.firebase.iid.FirebaseInstanceId
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.teacher.activities.LoginActivity
import com.instructure.teacher.utils.TeacherPrefs
import com.instructure.teacher.view.CanvasRecipientManager

class TeacherLogoutTask(type: Type, uri: Uri? = null) : LogoutTask(type, uri) {

    override fun onCleanup() {
        TeacherPrefs.safeClearPrefs()
        CanvasRecipientManager.getInstance().clearCache()
    }

    override fun createLoginIntent(context: Context): Intent {
        return LoginActivity.createIntent(context)
    }

    override fun createQRLoginIntent(context: Context, uri: Uri): Intent? {
        return LoginActivity.createIntent(context, uri)
    }

    override fun getFcmToken(listener: (registrationId: String?) -> Unit) {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task -> listener(task.result?.token) }
    }
}
