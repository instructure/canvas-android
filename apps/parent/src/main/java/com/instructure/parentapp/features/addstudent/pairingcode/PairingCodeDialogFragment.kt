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
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.pandautils.analytics.SCREEN_VIEW_PAIRING_CODE
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasDialogFragment
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.parentapp.R
import com.instructure.parentapp.features.addstudent.AddStudentAction
import com.instructure.parentapp.features.addstudent.AddStudentViewModel
import com.instructure.parentapp.features.addstudent.AddStudentViewModelAction
import dagger.hilt.android.AndroidEntryPoint


@ScreenView(SCREEN_VIEW_PAIRING_CODE)
@AndroidEntryPoint
class PairingCodeDialogFragment : BaseCanvasDialogFragment() {

    private val addStudentViewModel: AddStudentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        lifecycleScope.collectOneOffEvents(addStudentViewModel.events, ::handleAddStudentAction)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun handleAddStudentAction(action: AddStudentViewModelAction) {
        when (action) {
            is AddStudentViewModelAction.PairStudentSuccess -> {
                dismiss()
            }
            else -> {}
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.AccessibleAlertDialog)
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
        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        addStudentViewModel.handleAction(AddStudentAction.ResetError)
    }
}