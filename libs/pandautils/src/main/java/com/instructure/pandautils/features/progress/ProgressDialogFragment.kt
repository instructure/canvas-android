/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 */

package com.instructure.pandautils.features.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.instructure.pandautils.features.progress.composables.ProgressScreen
import com.instructure.pandautils.utils.Const
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProgressDialogFragment : BottomSheetDialogFragment() {

    private val viewModel: ProgressViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                ProgressScreen(uiState, viewModel::handleAction)
            }
        }
    }

    companion object {
        fun newInstance(progressId: Long, title: String, progressTitle: String, note: String? = null) =
            ProgressDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong("progressId", progressId)
                    putString("title", title)
                    putString("progressTitle", progressTitle)
                    note?.let { putString("note", it) }
                }
            }
    }
}