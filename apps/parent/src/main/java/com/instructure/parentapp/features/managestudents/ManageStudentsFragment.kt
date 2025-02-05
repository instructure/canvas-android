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

package com.instructure.parentapp.features.managestudents

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
import androidx.navigation.fragment.findNavController
import com.instructure.pandautils.analytics.SCREEN_VIEW_MANAGE_STUDENTS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.parentapp.features.addstudent.AddStudentBottomSheetDialogFragment
import com.instructure.parentapp.features.addstudent.AddStudentViewModel
import com.instructure.parentapp.features.addstudent.AddStudentViewModelAction
import com.instructure.parentapp.util.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@ScreenView(SCREEN_VIEW_MANAGE_STUDENTS)
@AndroidEntryPoint
class ManageStudentsFragment : BaseCanvasFragment() {

    @Inject
    lateinit var navigation: Navigation

    private val viewModel: ManageStudentViewModel by viewModels()
    private val addStudentViewModel: AddStudentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewStyler.setStatusBarDark(requireActivity(), ThemePrefs.primaryColor)

        lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)
        lifecycleScope.launch {
            addStudentViewModel.events.collectLatest(::handleAddStudentAction)
        }

        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                ManageStudentsScreen(
                    uiState,
                    viewModel::handleAction,
                    navigationActionClick = {
                        findNavController().popBackStack()
                    }
                )
            }
        }
    }

    private fun handleAddStudentAction(action: AddStudentViewModelAction) {
        when (action) {
            is AddStudentViewModelAction.PairStudentSuccess -> {
                viewModel.handleAction(ManageStudentsAction.Refresh)
            }
            is AddStudentViewModelAction.UnpairStudentSuccess -> {
                viewModel.handleAction(ManageStudentsAction.Refresh)
            }
        }
    }

    private fun handleAction(action: ManageStudentsViewModelAction) {
        when (action) {
            is ManageStudentsViewModelAction.NavigateToAlertSettings -> {
                navigation.navigate(requireActivity(), navigation.alertSettingsRoute(action.student))
            }

            is ManageStudentsViewModelAction.AddStudent -> {
                AddStudentBottomSheetDialogFragment().show(
                    childFragmentManager,
                    AddStudentBottomSheetDialogFragment::class.java.simpleName
                )
            }
        }
    }
}
