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

package com.instructure.pandautils.features.settings

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
import com.instructure.pandautils.features.about.AboutFragment
import com.instructure.pandautils.utils.AppThemeSelector
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var settingsRouter: SettingsRouter

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewStyler.setStatusBarDark(requireActivity(), ThemePrefs.primaryColor)

        lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)

        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                SettingsScreen(uiState) {
                    requireActivity().onBackPressed()
                }
            }
        }
    }

    private fun handleAction(action: SettingsViewModelAction) {
        when (action) {
            is SettingsViewModelAction.Navigate -> {
                navigate(action.item)
            }
        }
    }

    private fun navigate(item: SettingsItem) {
        when (item) {
            SettingsItem.ABOUT -> {
                AboutFragment.newInstance().show(childFragmentManager, null)
            }

            SettingsItem.APP_THEME -> {
                AppThemeSelector.showAppThemeSelectorDialog(
                    requireContext(),
                    viewModel::onThemeSelected
                )
            }

            SettingsItem.PROFILE_SETTINGS -> {
                settingsRouter.navigateToProfileSettings()
            }

            SettingsItem.PUSH_NOTIFICATIONS -> {
                settingsRouter.navigateToPushNotificationsSettings()
            }

            SettingsItem.EMAIL_NOTIFICATIONS -> {
                settingsRouter.navigateToEmailNotificationsSettings()
            }

            SettingsItem.PAIR_WITH_OBSERVER -> {
                settingsRouter.navigateToPairWithObserver()
            }

            else -> {

            }
        }
    }
}
