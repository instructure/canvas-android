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
package com.instructure.loginapi.login.features.acceptableusepolicy

import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import com.instructure.loginapi.login.R
import com.instructure.loginapi.login.databinding.ActivityAcceptableUsePolicyBinding
import com.instructure.pandautils.base.BaseCanvasActivity
import com.instructure.pandautils.utils.ToolbarColorizeHelper
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.setMenu
import com.instructure.pandautils.utils.setupAsCloseButton
import com.instructure.pandautils.utils.withRequireNetwork
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AcceptableUsePolicyActivity : BaseCanvasActivity() {

    private val viewModel by viewModels<AcceptableUsePolicyViewModel>()

    @Inject
    lateinit var router: AcceptableUsePolicyRouter

    private lateinit var binding: ActivityAcceptableUsePolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAcceptableUsePolicyBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)

        binding.toolbar.applyTopSystemBarInsets()
        binding.toolbar.setTitle(R.string.acceptableUsePolicyTitle)
        binding.toolbar.setupAsCloseButton {
            router.logout()
            finish()
        }
        binding.toolbar.setMenu(R.menu.menu_acceptable_use_policy, {
            if (it.itemId == R.id.submit) withRequireNetwork { viewModel.acceptPolicy() }
        })
        ViewStyler.themeToolbarLight(this, binding.toolbar)
        ViewStyler.themeSwitch(this, binding.acceptSwitch, getColor(R.color.textInfo))
        ToolbarColorizeHelper.colorizeToolbar(binding.toolbar, getColor(R.color.textDarkest), this, getColor(R.color.backgroundMedium))

        viewModel.events.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        })

        viewModel.data.observe(this, { data ->
            val submitItem = binding.toolbar.menu.getItem(0)
            submitItem.isEnabled = data.checked
        })
    }

    private fun handleAction(action: AcceptableUsePolicyAction) {
        when (action) {
            is AcceptableUsePolicyAction.OpenPolicy -> router.openPolicy(action.content)
            AcceptableUsePolicyAction.PolicyAccepted -> {
                router.startApp()
                finish()
            }
            AcceptableUsePolicyAction.AcceptFailure -> Snackbar.make(binding.root, R.string.acceptFail, Snackbar.LENGTH_SHORT).show()
            AcceptableUsePolicyAction.PolicyOpenFailed -> {}
        }
    }

    override fun onBackPressed() {
        router.logout()
        super.onBackPressed()
    }
}