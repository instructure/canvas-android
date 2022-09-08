/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.shareextension.status

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.instructure.pandautils.databinding.FragmentShareExtensionStatusDialogBinding
import com.instructure.pandautils.features.shareextension.ShareExtensionViewModel
import com.instructure.pandautils.utils.ThemePrefs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareExtensionStatusDialogFragment : DialogFragment() {

    private var state: ShareExtensionStatus = ShareExtensionStatus.SUCCEEDED

    private val viewModel: ShareExtensionStatusDialogViewModel by viewModels()

    private val shareExtensionViewModel: ShareExtensionViewModel by activityViewModels()

    private lateinit var binding: FragmentShareExtensionStatusDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentShareExtensionStatusDialogBinding.inflate(layoutInflater, null, false)

        val alertDialog = AlertDialog.Builder(requireContext())
                .setView(binding.root)
                .setCancelable(true)
                .create()

        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(true)

        return alertDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.initData(shareExtensionViewModel.uploadType, state)

        viewModel.events.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }

        binding.doneButton.setTextColor(ThemePrefs.buttonColor)
    }

    private fun handleAction(action: ShareExtensionStatusAction) {
        when (action) {
            is ShareExtensionStatusAction.Done -> {
                dismissAllowingStateLoss()
                shareExtensionViewModel.finish()
            }
            is ShareExtensionStatusAction.ShowConfetti -> {
                shareExtensionViewModel.showConfetti()
            }
        }
    }

    companion object {
        const val TAG = "ShareExtensionSuccessDialogFragment"

        fun newInstance(state: ShareExtensionStatus): ShareExtensionStatusDialogFragment {
            return ShareExtensionStatusDialogFragment()
                .apply {
                    this.state = state
                }
        }
    }
}