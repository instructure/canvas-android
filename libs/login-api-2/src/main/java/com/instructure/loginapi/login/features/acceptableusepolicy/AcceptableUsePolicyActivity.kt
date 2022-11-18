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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.instructure.loginapi.login.R
import com.instructure.loginapi.login.databinding.ActivityAcceptableUsePolicyBinding
import com.instructure.pandautils.utils.ToolbarColorizeHelper
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setMenu
import com.instructure.pandautils.utils.setupAsCloseButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AcceptableUsePolicyActivity : AppCompatActivity() {

    private val viewModel by viewModels<AcceptableUsePolicyViewModel>()

    @Inject
    lateinit var router: AcceptableUsePolicyRouter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAcceptableUsePolicyBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.acceptableUsePolicyTitle)
        toolbar.setupAsCloseButton {
            router.logout()
            finish()
        }
        toolbar.setMenu(R.menu.menu_acceptable_use_policy, {
            if (it.itemId == R.id.submit) viewModel.acceptPolicy()
        })
        ViewStyler.themeToolbarLight(this, toolbar)
        ViewStyler.themeSwitch(this, binding.acceptSwitch, getColor(R.color.textInfo))
        ToolbarColorizeHelper.colorizeToolbar(toolbar, getColor(R.color.textDarkest), this, getColor(R.color.backgroundMedium))

        viewModel.events.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        })

        viewModel.checked.observe(this, { checked ->
            val submitItem = toolbar.menu.getItem(0)
            submitItem.isEnabled = checked
        })
    }

    private fun handleAction(action: AcceptableUsePolicyAction) {
        when (action) {
            is AcceptableUsePolicyAction.OpenPolicy -> router.openPolicy(action.content)
            AcceptableUsePolicyAction.PolicyAccepted -> {
                router.startApp()
                finish()
            }
        }
    }

    override fun onBackPressed() {
        router.logout()
        super.onBackPressed()
    }
}