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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.FragmentShareExtensionProgressDialogBinding
import com.instructure.pandautils.utils.NullableSerializableArg
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class ShareExtensionProgressDialogFragment : DialogFragment() {

    private val viewModel: ShareExtensionProgressDialogViewModel by viewModels()

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
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentShareExtensionProgressDialogBinding.inflate(layoutInflater, null, false)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setNegativeButton(R.string.utils_cancel, null)
            .setCancelable(false)
            .create()

        return dialog
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