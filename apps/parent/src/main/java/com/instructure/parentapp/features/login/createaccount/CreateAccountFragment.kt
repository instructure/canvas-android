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
 */package com.instructure.parentapp.features.login.createaccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.features.legal.LegalRouter
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.parentapp.features.login.SignInActivity
import com.instructure.parentapp.features.webview.HtmlContentActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CreateAccountFragment : BaseCanvasFragment() {

    @Inject
    lateinit var legalRouter: LegalRouter

    private val viewModel: CreateAccountViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                CreateAccountScreen(uiState, viewModel::handleAction)
            }
        }
    }

    private fun handleAction(action: CreateAccountViewModelAction) {
        when (action) {
            is CreateAccountViewModelAction.NavigateToSignIn -> {
                val intent = SignInActivity.createIntent(requireActivity(), action.accountDomain)
                requireActivity().startActivity(intent)
                if (action.closeActivity) {
                    requireActivity().finish()
                }
            }

            is CreateAccountViewModelAction.NavigateToHtmlContent -> {
                val intent = HtmlContentActivity.createIntent(requireActivity(), action.title, action.html, true)
                requireActivity().startActivity(intent)
            }

            is CreateAccountViewModelAction.NavigateToPrivacyPolicy -> {
                legalRouter.routeToPrivacyPolicy()
            }

            CreateAccountViewModelAction.NavigateToTermsOfService -> {
                legalRouter.routeToTermsOfService("")
            }
        }
    }

    companion object {
        const val DOMAIN = "domain"
        const val ACCOUNT_ID = "account_id"
        const val PAIRING_CODE = "pairing_code"
    }
}
