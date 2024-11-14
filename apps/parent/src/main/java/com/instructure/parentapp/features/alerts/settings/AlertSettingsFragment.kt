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
import com.instructure.pandautils.blueprint.BaseCanvasFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.parentapp.R
import com.instructure.parentapp.features.addstudent.AddStudentAction
import com.instructure.parentapp.features.addstudent.AddStudentViewModel
import com.instructure.parentapp.features.addstudent.AddStudentViewModelAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

            else -> {}
        }
    }

    private fun handleAction(action: AlertSettingsViewModelAction) {
        when (action) {
            is AlertSettingsViewModelAction.UnpairStudent -> {
                addStudentViewModel.handleAction(AddStudentAction.UnpairStudent(action.studentId))
            }

            is AlertSettingsViewModelAction.ShowSnackbar -> {
                Snackbar.make(requireView(), action.message, Snackbar.LENGTH_SHORT).apply {
                    setAction(R.string.retry) { action.actionCallback() }
                    setActionTextColor(resources.getColor(R.color.white, resources.newTheme()))
                }.show()
            }
        }
    }
}