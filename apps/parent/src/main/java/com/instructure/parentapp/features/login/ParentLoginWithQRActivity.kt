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

package com.instructure.parentapp.features.login

import android.net.Uri
import com.instructure.loginapi.login.activities.LoginWithQRActivity
import com.instructure.parentapp.features.login.routevalidator.RouteValidatorActivity


class ParentLoginWithQRActivity : LoginWithQRActivity() {

    override fun launchApplicationWithQRLogin(loginUri: Uri) {
        // The RouteValidatorActivity is set up to handle the QR code url from inside or outside the app
        startActivity(RouteValidatorActivity.createIntent(this, loginUri))
    }
}
