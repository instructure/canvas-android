/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.pandautils.features.offline.sync.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.instructure.pandautils.base.BaseCanvasFragment
import androidx.fragment.app.viewModels
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.FragmentSyncProgressBinding
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.items
import com.instructure.pandautils.utils.setMenu
import com.instructure.pandautils.utils.setupAsBackButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SyncProgressFragment : BaseCanvasFragment() {

    private val viewModel: SyncProgressViewModel by viewModels()

    private lateinit var binding: FragmentSyncProgressBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSyncProgressBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()

        viewModel.progressData.observe(viewLifecycleOwner) {
            when (it?.progressState) {
                ProgressState.IN_PROGRESS -> {
                    setActionTitle(getString(R.string.cancel))
                }

                ProgressState.ERROR -> {
                    setActionTitle(getString(R.string.retry))
                }

                else -> {
                    setActionGone()
                }
            }
        }

        viewModel.events.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }
    }

    private fun applyTheme() {
        ViewStyler.themeToolbarColored(
            requireActivity(),
            binding.toolbar,
            ThemePrefs.primaryColor,
            ThemePrefs.primaryTextColor
        )
        binding.toolbar.apply {
            setBackgroundColor(ThemePrefs.primaryColor)
            setupAsBackButton(this@SyncProgressFragment)
            setMenu(R.menu.menu_sync_progress) {
                viewModel.onActionClicked()
            }
        }
    }

    private fun setActionTitle(title: String) {
        binding.toolbar.menu.items.firstOrNull()?.isVisible = true
        binding.toolbar.menu.items.firstOrNull()?.title = title
    }

    private fun setActionGone() {
        binding.toolbar.menu.items.firstOrNull()?.isVisible = false
    }

    private fun handleAction(action: SyncProgressAction) {
        when (action) {
            is SyncProgressAction.CancelConfirmation -> {
                showCancelConfirmation()
            }

            is SyncProgressAction.Back -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun showCancelConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.syncProgress_cancelConfirmationTitle)
            .setMessage(R.string.syncProgress_cancelConfirmationMessage)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.cancel()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    companion object {
        fun newInstance() = SyncProgressFragment()

        fun makeRoute() = Route(SyncProgressFragment::class.java, null)
    }

}