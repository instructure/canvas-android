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

package com.instructure.pandautils.features.notification.preferences

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.pandautils.analytics.SCREEN_VIEW_NOTIFICATION_PREFERENCES
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.databinding.FragmentPushPreferencesBinding
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setupAsBackButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_push_preferences.*

@ScreenView(SCREEN_VIEW_NOTIFICATION_PREFERENCES)
@PageView(url = "profile/communication")
@AndroidEntryPoint
class NotificationPreferencesFragment : Fragment() {

    private val viewModel: NotificationPreferencesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding = FragmentPushPreferencesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this.viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()

        viewModel.events.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }
    }

    private fun handleAction(action: NotificationPreferencesAction) {
        when (action) {
            is NotificationPreferencesAction.ShowSnackbar -> Snackbar.make(requireView(), action.snackbar, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupToolbar() {
        toolbar.setupAsBackButton { requireActivity().onBackPressed() }
        ViewStyler.themeToolbar(requireActivity(), toolbar, Color.WHITE, Color.BLACK, false)
    }

    companion object {
        fun newInstance() = NotificationPreferencesFragment()
    }
}