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

package com.instructure.parentapp.features.login.routevalidator

import android.net.Uri
import com.instructure.canvasapi2.models.AccountDomain


sealed class RouteValidatorAction {

    data object Finish : RouteValidatorAction()
    data class LoadWebViewUrl(val url: String) : RouteValidatorAction()
    data class StartMainActivity(val masqueradeId: Long? = null, val data: Uri? = null, val message: String? = null) : RouteValidatorAction()
    data class ShowToast(val message: String) : RouteValidatorAction()
    data class StartSignInActivity(val accountDomain: AccountDomain) : RouteValidatorAction()
    data object StartLoginActivity : RouteValidatorAction()
}
