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

package com.instructure.pandautils.features.offline.sync.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.pandautils.R
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.databinding.FragmentSyncSettingsBinding
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.showThemed
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SyncSettingsFragment : Fragment(), FragmentInteractions {

    private val binding by viewBinding(FragmentSyncSettingsBinding::bind)

    private val viewModel: SyncSettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sync_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        applyTheme()

        viewModel.events.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }
    }

    private fun handleAction(action: SyncSettingsAction) {
        when (action) {
            is SyncSettingsAction.ShowFrequencySelector -> showFrequencySelector(
                action.items,
                action.selectedItemPosition,
                action.onItemSelected
            )
            is SyncSettingsAction.ShowWifiConfirmation -> showWifiConfirmation(action.confirmationCallback)
        }
    }

    private fun showWifiConfirmation(confirmationCallback: (Boolean) -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.syncSettings_wifiConfirmationTitle)
            .setMessage(R.string.synySettings_wifiConfirmationMessage)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                confirmationCallback(false)
                dialog.dismiss()
            }
            .setPositiveButton(R.string.syncSettings_wifiConfirmationPositiveButton) { dialog, _ ->
                confirmationCallback(true)
                dialog.dismiss()
            }
            .setOnDismissListener {
                confirmationCallback(false)
            }
            .showThemed()
    }

    private fun showFrequencySelector(items: List<String>, selectedItemPosition: Int, onItemSelected: (Int) -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.syncSettings_syncFrequencyDialogTitle)
            .setSingleChoiceItems(items.toTypedArray(), selectedItemPosition) { dialog, selected ->
                onItemSelected(selected)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .showThemed()
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(R.string.syncSettings_toolbarTitle)

    override fun applyTheme() {
        ViewStyler.themeToolbarColored(
            requireActivity(),
            binding.toolbar,
            ThemePrefs.primaryColor,
            ThemePrefs.primaryTextColor
        )

        binding.toolbar.setupAsBackButton(this@SyncSettingsFragment)
    }

    override fun getFragment(): Fragment = this

    companion object {
        fun newInstance() = SyncSettingsFragment()
    }
}