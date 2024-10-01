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

package com.instructure.parentapp.features.courses.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.parentapp.util.ParentPrefs
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CourseDetailsFragment : Fragment() {

    private val viewModel: CourseDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        applyTheme()
        lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)
        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                CourseDetailsScreen(uiState, viewModel::handleAction) {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun applyTheme() {
        val student = ParentPrefs.currentStudent
        val color = ColorKeeper.getOrGenerateUserColor(student).color()
        ViewStyler.setStatusBarDark(requireActivity(), color)
    }

    private fun handleAction(action: CourseDetailsViewModelAction) {
        when (action) {
            is CourseDetailsViewModelAction.NavigateToComposeMessageScreen -> {

            }

            is CourseDetailsViewModelAction.NavigateToAssignmentDetails -> {

            }
        }
    }
}
