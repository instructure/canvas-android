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
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.FragmentInboxNewBinding
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.addListener
import com.instructure.pandautils.utils.isVisible
import com.instructure.pandautils.utils.setMenu
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.showThemed
import com.instructure.pandautils.utils.withArgs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NewInboxFragment : Fragment() {

    private val viewModel: InboxViewModel by viewModels()

    @Inject
    lateinit var inboxRouter: InboxRouter

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
        binding.toolbar.setMenu(R.menu.menu_inbox) {}
        binding.editToolbar.setupAsBackButton(this)
        binding.editToolbar.setMenu(R.menu.menu_inbox_edit) {
            when (it.itemId) {
                R.id.inboxStarSelected -> viewModel.starSelected()
                R.id.inboxUnstarSelected -> viewModel.unstarSelected()
                R.id.inboxMarkAsReadSelected -> viewModel.markAsReadSelected()
                R.id.inboxMarkAsUnreadSelected -> viewModel.markAsUnreadSelected()
                R.id.inboxDeleteSelected -> deleteSelected()
                R.id.inboxArchiveSelected -> viewModel.archiveSelected()
            }
        }
        applyTheme()

        viewModel.events.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }

        viewModel.data.observe(viewLifecycleOwner) { data ->
            animateToolbars(data.selectionMode)
        }
    }

    private fun deleteSelected() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.deleteConfirmation)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteSelected() }
            .setNegativeButton(R.string.cancel, null)
            .showThemed()
    }

    // TODO Move to data binding?
    private fun animateToolbars(selectionMode: Boolean) {
        if (selectionMode && binding.editToolbar.isVisible) return
        if (!selectionMode && binding.toolbar.isVisible) return

        var currentToolbar: Toolbar
        var newToolbar: Toolbar
        if (selectionMode) {
            currentToolbar = binding.toolbar
            newToolbar = binding.editToolbar
        } else {
            currentToolbar = binding.editToolbar
            newToolbar = binding.toolbar
        }

        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.duration = 150
        fadeOut.addListener(onEnd = { currentToolbar.setVisible(false) })

        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 150
        fadeIn.startOffset = 150
        fadeIn.addListener(onStart = { newToolbar.setVisible(true) })

        currentToolbar.startAnimation(fadeOut)
        newToolbar.startAnimation(fadeIn)
    }

    private fun applyTheme() {
        ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        ViewStyler.themeToolbarColored(requireActivity(), binding.editToolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        binding.toolbarWrapper.setBackgroundColor(ThemePrefs.primaryColor)
        binding.addMessage.backgroundTintList = ViewStyler.makeColorStateListForButton()
        inboxRouter.attachNavigationIcon(binding.toolbar)
    }

    private fun handleAction(action: InboxAction) {
        when (action) {
            is InboxAction.OpenConversation -> inboxRouter.openConversation(action.conversation, action.scope)
            InboxAction.OpenScopeSelector -> openScopeSelector()
            is InboxAction.ItemSelectionChanged -> animateAvatar(action.view, action.selected)
            is InboxAction.ShowConfirmationSnackbar -> Snackbar.make(requireView(), action.text, Snackbar.LENGTH_LONG).show()
        }
    }

    // TODO Move to data binding?
    private fun animateAvatar(view: View, selected: Boolean) {
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val avatarSelected: ImageView = view.findViewById(R.id.avatarSelected)
        avatarSelected.setColorFilter(ThemePrefs.buttonColor)

        var outView: View
        var inView: View
        if (selected) {
            outView = avatar
            inView = avatarSelected
        } else {
            outView = avatarSelected
            inView = avatar
        }

        val outAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.flip_out_anim)
        outAnimation.duration = 150
        outAnimation.addListener(onEnd = { outView.setVisible(false) })

        val inAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.flip_in_anim)
        inAnimation.duration = 150
        inAnimation.startOffset = 150
        inAnimation.addListener(onStart = { inView.setVisible(true) })

        outView.startAnimation(outAnimation)
        inView.startAnimation(inAnimation)
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

    fun handleBackPress(): Boolean {
        return viewModel.handleBackPressed()
    }

    companion object {
        fun makeRoute() = Route(NewInboxFragment::class.java, null)

        fun newInstance(route: Route) = if (validateRoute(route)) NewInboxFragment().withArgs(route.arguments) else null

        private fun validateRoute(route: Route) = route.primaryClass == NewInboxFragment::class.java
    }
}