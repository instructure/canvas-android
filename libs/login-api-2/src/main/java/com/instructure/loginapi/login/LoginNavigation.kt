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
import com.instructure.loginapi.login.viewmodel.Experience
import com.instructure.loginapi.login.viewmodel.LoginResultAction
import com.instructure.loginapi.login.viewmodel.LoginViewModel

const val CANVAS_FOR_ELEMENTARY = "canvas_for_elementary"
const val CANVAS_CAREER = "canvas_career"

abstract class LoginNavigation(
    private val activity: FragmentActivity
) {

    protected abstract val checkElementary: Boolean

    fun startLogin(loginViewModel: LoginViewModel, checkToken: Boolean) {
        loginViewModel.checkLogin(checkToken, checkElementary).observe(activity, { event ->
            event?.getContentIfNotHandled()?.let {
                when (it) {
                    LoginResultAction.TokenNotValid -> logout()
                    is LoginResultAction.Login -> startApp(it.experience)
                    is LoginResultAction.ShouldAcceptPolicy -> showPolicy(it.experience)
                }
            }
        })
    }

    protected abstract fun logout()

    private fun startApp(experience: Experience) {
        val intent = initMainActivityIntent(experience)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }

    protected abstract fun initMainActivityIntent(experience: Experience): Intent

    private fun showPolicy(experience: Experience) {
        val intent = Intent(activity, AcceptableUsePolicyActivity::class.java)
        if (experience is Experience.Academic) {
            intent.putExtra(CANVAS_FOR_ELEMENTARY, experience.elementary)
        } else if (experience is Experience.Career) {
            intent.putExtra(CANVAS_CAREER, true)
        }
        activity.startActivity(intent)
    }
}