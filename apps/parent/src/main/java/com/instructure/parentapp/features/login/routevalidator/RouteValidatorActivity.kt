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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import instructure.androidblueprint.BaseCanvasActivity
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.parentapp.databinding.ActivityRouteValidatorBinding
import com.instructure.parentapp.features.login.LoginActivity
import com.instructure.parentapp.features.login.SignInActivity
import com.instructure.parentapp.features.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RouteValidatorActivity : BaseCanvasActivity() {

    private val binding by viewBinding(ActivityRouteValidatorBinding::inflate)

    private val viewModel by viewModels<RouteValidatorViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)

        viewModel.loadRoute(intent.dataString)
    }

    private fun handleAction(action: RouteValidatorAction) {
        when (action) {
            is RouteValidatorAction.Finish -> finish()
            is RouteValidatorAction.LoadWebViewUrl -> binding.dummyWebView.loadUrl(action.url)
            is RouteValidatorAction.StartMainActivity -> startMainActivity(action.masqueradeId, action.data)
            is RouteValidatorAction.ShowToast -> Toast.makeText(this, action.message, Toast.LENGTH_LONG).show()
            is RouteValidatorAction.StartSignInActivity -> startSignInActivity(action.accountDomain)
            is RouteValidatorAction.StartLoginActivity -> startLoginActivity()
        }
    }

    private fun startMainActivity(masqueradeId: Long?, data: Uri?) {
        if (data != null) {
            val intent = MainActivity.createIntent(this, data)
            startActivity(intent)
        } else {
            val intent = if (masqueradeId != null) {
                MainActivity.createIntent(this, masqueradeId)
            } else {
                Intent(this, MainActivity::class.java)
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        finish()
    }

    private fun startSignInActivity(accountDomain: AccountDomain) {
        val intent = SignInActivity.createIntent(this, accountDomain)
        startActivity(intent)
        finish()
    }

    private fun startLoginActivity() {
        val intent = LoginActivity.createIntent(this)
        startActivity(intent)
        finish()
    }

    companion object {
        fun createIntent(context: Context, uri: Uri): Intent {
            val intent = Intent(context, RouteValidatorActivity::class.java)
            intent.data = uri
            return intent
        }
    }
}