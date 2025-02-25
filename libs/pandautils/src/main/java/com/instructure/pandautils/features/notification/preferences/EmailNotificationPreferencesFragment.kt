/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.pandautils.features.notification.preferences

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.managers.NotificationPreferencesFrequency
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_NOTIFICATION_PREFERENCES
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.databinding.FragmentNotificationPreferencesBinding
import com.instructure.pandautils.utils.ToolbarSetupBehavior
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@PageView(url = "profile/communication")
@ScreenView(SCREEN_VIEW_NOTIFICATION_PREFERENCES)
@AndroidEntryPoint
class EmailNotificationPreferencesFragment : BaseCanvasFragment() {

    @Inject
    lateinit var toolbarSetupBehavior: ToolbarSetupBehavior

    private val viewModel: EmailNotificationPreferencesViewModel by viewModels()

    private lateinit var binding: FragmentNotificationPreferencesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentNotificationPreferencesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this.viewLifecycleOwner
        binding.viewModel = viewModel
        binding.title = resources.getString(R.string.emailNotifications)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarSetupBehavior.setupToolbar(binding.toolbar)
        viewModel.events.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }
    }

    private fun handleAction(action: NotificationPreferencesAction) {
        when (action) {
            is NotificationPreferencesAction.ShowSnackbar -> Snackbar.make(requireView(), action.snackbar, Snackbar.LENGTH_LONG).show()
            is NotificationPreferencesAction.ShowFrequencySelectionDialog -> showFrequencySelectionDialog(action.categoryName, action.selectedFrequency)
        }
    }

    private fun showFrequencySelectionDialog(categoryName: String, selectedFrequency: NotificationPreferencesFrequency) {
        val items = NotificationPreferencesFrequency.values().map { resources.getString(it.stringRes) }.toTypedArray()
        val selectedIndex = NotificationPreferencesFrequency.values().indexOf(selectedFrequency)
        AlertDialog.Builder(requireContext(), R.style.AccessibleAccentDialogTheme)
            .setTitle(R.string.selectFrequency)
            .setSingleChoiceItems(items, selectedIndex) { dialog, index -> frequencySelected(dialog, index, categoryName) }
            .setNegativeButton(R.string.sortByDialogCancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun frequencySelected(dialog: DialogInterface, index: Int, categoryName: String) {
        val selectedFrequency = NotificationPreferencesFrequency.values()[index]
        viewModel.updateFrequency(categoryName, selectedFrequency)
        dialog.dismiss()
    }

    companion object {
        fun newInstance() = EmailNotificationPreferencesFragment()
    }
}