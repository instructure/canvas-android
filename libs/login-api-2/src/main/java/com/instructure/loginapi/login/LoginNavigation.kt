/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.loginapi.login

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.instructure.loginapi.login.features.acceptableusepolicy.AcceptableUsePolicyActivity
import com.instructure.loginapi.login.viewmodel.LoginResultAction
import com.instructure.loginapi.login.viewmodel.LoginViewModel

private const val CANVAS_FOR_ELEMENTARY = "canvas_for_elementary"

abstract class LoginNavigation(
    private val activity: FragmentActivity
) {

    protected abstract val checkElementary: Boolean

    fun startLogin(loginViewModel: LoginViewModel, checkToken: Boolean) {
        loginViewModel.checkLogin(checkToken, checkElementary).observe(activity, { event ->
            event?.getContentIfNotHandled()?.let {
                when (it) {
                    LoginResultAction.TokenNotValid -> logout()
                    is LoginResultAction.Login -> startApp(it.elementary)
                    is LoginResultAction.ShouldAcceptPolicy -> showPolicy(it.elementary)
                }
            }
        })
    }

    protected abstract fun logout()

    private fun startApp(elementary: Boolean) {
        val intent = initMainActivityIntent()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(CANVAS_FOR_ELEMENTARY, elementary)
        activity.startActivity(intent)
        activity.finish()
    }

    protected abstract fun initMainActivityIntent(): Intent

    private fun showPolicy(elementary: Boolean) {
        val intent = Intent(activity, AcceptableUsePolicyActivity::class.java)
        intent.putExtra(CANVAS_FOR_ELEMENTARY, elementary)
        activity.startActivity(intent)
    }
}