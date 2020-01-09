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
import com.google.firebase.iid.FirebaseInstanceId
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.student.activity.LoginActivity
import com.instructure.student.util.StudentPrefs
import com.instructure.student.view.CanvasRecipientManager
import com.instructure.student.widget.WidgetUpdater

class StudentLogoutTask(type: Type) : LogoutTask(type) {

    override fun onCleanup() {
        StudentPrefs.safeClearPrefs()
        CanvasRecipientManager.getInstance(ContextKeeper.appContext).clearCache()
        WidgetUpdater.updateWidgets()
    }

    override fun createLoginIntent(context: Context): Intent {
        return LoginActivity.createIntent(context)
    }

    override fun getFcmToken(listener: (registrationId: String?) -> Unit) {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task -> listener(task.result?.token) }
    }
}
