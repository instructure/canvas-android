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
package com.instructure.parentapp.features.alerts.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.pandautils.analytics.SCREEN_VIEW_ALERT_SETTINGS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.showSnackbar
import com.instructure.parentapp.features.addstudent.AddStudentAction
import com.instructure.parentapp.features.addstudent.AddStudentViewModel
import com.instructure.parentapp.features.addstudent.AddStudentViewModelAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@ScreenView(SCREEN_VIEW_ALERT_SETTINGS)
@AndroidEntryPoint
class AlertSettingsFragment : BaseCanvasFragment() {

    private val addStudentViewModel: AddStudentViewModel by activityViewModels()

    private val viewModel: AlertSettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                AlertSettingsScreen(uiState) {
                    requireActivity().onBackPressed()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.uiState.collectLatest {
                ViewStyler.setStatusBarDark(requireActivity(), it.userColor)
            }
        }
        lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)

        lifecycleScope.launch {
            addStudentViewModel.events.collectLatest(::handleAddStudentEvents)
        }
    }

    private fun handleAddStudentEvents(action: AddStudentViewModelAction) {
        when (action) {
            is AddStudentViewModelAction.UnpairStudentSuccess -> {
                requireActivity().onBackPressed()
            }

            is AddStudentViewModelAction.UnpairStudentFailed -> {
                viewModel.handleAction(AlertSettingsAction.UnpairStudentFailed)
            }

            else -> {}
        }
    }

    private fun handleAction(action: AlertSettingsViewModelAction) {
        when (action) {
            is AlertSettingsViewModelAction.UnpairStudent -> {
                addStudentViewModel.handleAction(AddStudentAction.UnpairStudent(action.studentId))
            }

            is AlertSettingsViewModelAction.ShowSnackbar -> {
                view?.showSnackbar(action.message) {
                    action.actionCallback()
                }
            }
        }
    }
}
