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
package com.instructure.pandautils.features.inbox.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.FragmentInboxNewBinding
import com.instructure.pandautils.features.notification.preferences.NotificationPreferencesAction
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.withArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewInboxFragment : Fragment() {

    private val viewModel: InboxViewModel by viewModels()

    private lateinit var binding: FragmentInboxNewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInboxNewBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this.viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()

        viewModel.events.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }
    }

    private fun applyTheme() {
        ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        binding.addMessage.backgroundTintList = ViewStyler.makeColorStateListForButton()
    }

    private fun handleAction(action: InboxAction) {
        when (action) {
            is InboxAction.OpenConversation -> TODO()
            InboxAction.OpenScopeSelector -> openScopeSelector()
        }
    }

    private fun openScopeSelector() {
        val popup = PopupMenu(requireContext(), binding.popupViewPosition)
        popup.menuInflater.inflate(R.menu.menu_conversation_scope, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.inbox_all -> viewModel.scopeChanged(InboxApi.Scope.ALL)
                R.id.inbox_unread -> viewModel.scopeChanged(InboxApi.Scope.UNREAD)
                R.id.inbox_starred -> viewModel.scopeChanged(InboxApi.Scope.STARRED)
                R.id.inbox_sent -> viewModel.scopeChanged(InboxApi.Scope.SENT)
                R.id.inbox_archived -> viewModel.scopeChanged(InboxApi.Scope.ARCHIVED)
            }

            true
        }

        popup.show()
    }

    companion object {
        fun makeRoute() = Route(NewInboxFragment::class.java, null)

        fun newInstance(route: Route) = if (validateRoute(route)) NewInboxFragment().withArgs(route.arguments) else null

        private fun validateRoute(route: Route) = route.primaryClass == NewInboxFragment::class.java
    }
}