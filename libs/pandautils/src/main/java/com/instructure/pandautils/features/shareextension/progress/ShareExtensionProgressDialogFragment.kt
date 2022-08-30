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

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.Const
import java.util.*

class ShareExtensionProgressDialogFragment : DialogFragment() {

    private val viewModel: ShareExtensionProgressDialogViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_share_extension_progress_dialog, container, false)
    }

    companion object {
        const val UUID = "UUID"
        fun newInstance(uuid: UUID) : ShareExtensionProgressDialogFragment {
            return ShareExtensionProgressDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(UUID, uuid)
                }
            }
        }
    }

}