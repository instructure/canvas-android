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
package com.instructure.parentapp.features.addstudent.pairingcode

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.instructure.parentapp.R
import com.instructure.parentapp.features.addstudent.AddStudentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PairingCodeDialogFragment : DialogFragment() {


    private val addStudentViewModel: AddStudentViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.pairingCodeDialogTitle)
        builder.setMessage(R.string.pairingCodeDialogMessage)
        builder.setView(ComposeView(requireContext()).apply {
            setContent {
                val uiState by addStudentViewModel.uiState.collectAsState()
                PairingCodeScreen(uiState) {
                    dismiss()
                }
            }
        })
        return builder.create()
    }
}