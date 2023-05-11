/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.emeritus.student.activity

import android.net.Uri
import com.instructure.loginapi.login.activities.LoginWithQRActivity
import com.instructure.pandautils.analytics.SCREEN_VIEW_LOGIN_WITH_QR
import com.instructure.pandautils.analytics.ScreenView

@ScreenView(SCREEN_VIEW_LOGIN_WITH_QR)
class StudentLoginWithQRActivity : LoginWithQRActivity() {

    override fun launchApplicationWithQRLogin(loginUri: Uri) {
        // The InterwebsToApplication activity is set up to handle the QR code url from inside or outside the app
        startActivity(InterwebsToApplication.createIntent(this, loginUri))
    }

}
