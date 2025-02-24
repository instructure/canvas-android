/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 */
package com.instructure.pandautils.features.settings.inboxsignature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.pandautils.R
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.features.settings.SettingsSharedAction
import com.instructure.pandautils.features.settings.SettingsSharedEvents
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InboxSignatureFragment : BaseCanvasFragment() {

    @Inject
    lateinit var sharedEvents: SettingsSharedEvents

    private val viewModel: InboxSignatureViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewStyler.setStatusBarDark(requireActivity(), ThemePrefs.primaryColor)
        viewLifecycleOwner.lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)
        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                InboxSignatureScreen(uiState = uiState, actionHandler = {
                    viewModel.handleAction(it)
                }, navigationActionClick = {
                    requireActivity().onBackPressed()
                })
            }
        }
    }

    private fun handleAction(action: InboxSignatureViewModelAction) {
        when (action) {
            is InboxSignatureViewModelAction.CloseAndUpdateSettings -> {
                requireActivity().onBackPressed()
                sharedEvents.sendEvent(lifecycleScope, SettingsSharedAction.UpdateSignatureSettings(action.enabled))
                toast(R.string.inboxSignatureSettingsUpdated)
            }

            InboxSignatureViewModelAction.ShowErrorToast -> toast(R.string.inboxSignatureSettingsError)
        }
    }
}