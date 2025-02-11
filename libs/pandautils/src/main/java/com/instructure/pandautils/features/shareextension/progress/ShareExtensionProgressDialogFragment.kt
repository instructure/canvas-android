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

package com.instructure.pandautils.features.shareextension.progress

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.instructure.pandautils.base.BaseCanvasDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.FragmentShareExtensionProgressDialogBinding
import com.instructure.pandautils.features.shareextension.ShareExtensionViewModel
import com.instructure.pandautils.utils.NullableSerializableArg
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class ShareExtensionProgressDialogFragment : BaseCanvasDialogFragment() {

    private val viewModel: ShareExtensionProgressDialogViewModel by viewModels()

    private val shareExtensionViewModel: ShareExtensionViewModel by activityViewModels()

    private var uuid: UUID? by NullableSerializableArg(KEY_UUID)

    private lateinit var binding: FragmentShareExtensionProgressDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uuid?.let {
            viewModel.setUUID(it)
        }

        viewModel.events.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }
    }

    private fun handleAction(action: ShareExtensionProgressAction) {
        when (action) {
            is ShareExtensionProgressAction.ShowSuccessDialog -> {
                dismiss()
                shareExtensionViewModel.showSuccessDialog(action.fileUploadType)
            }
            is ShareExtensionProgressAction.Close -> {
                dismiss()
                shareExtensionViewModel.finish()
            }
            is ShareExtensionProgressAction.CancelUpload -> {
                cancelClicked(action.title, action.message)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentShareExtensionProgressDialogBinding.inflate(layoutInflater, null, false)

        val dialog = AlertDialog.Builder(requireContext(), R.style.AccessibleAlertDialog)
            .setView(binding.root)
            .setCancelable(false)
            .create()

        dialog.setCanceledOnTouchOutside(false)

        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        return dialog
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        requireActivity().onBackPressed()
    }

    private fun cancelClicked(title: String, message: String) {
        AlertDialog.Builder(requireContext(), R.style.AccessibleAlertDialog)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(R.string.no) { _, _ -> }
            .setPositiveButton(R.string.yes) { _, _ -> viewModel.cancelUpload() }
            .show()
    }

    companion object {
        const val TAG = "ShareExtensionProgressDialogFragment"
        const val KEY_UUID = "UUID"
        fun newInstance(uuid: UUID): ShareExtensionProgressDialogFragment {
            return ShareExtensionProgressDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_UUID, uuid)
                }
                this.uuid = uuid
            }
        }
    }

}