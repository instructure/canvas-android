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

import android.content.Context
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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_INBOX
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.databinding.FragmentInboxBinding
import com.instructure.pandautils.databinding.ItemInboxEntryBinding
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.addListener
import com.instructure.pandautils.utils.isVisible
import com.instructure.pandautils.utils.items
import com.instructure.pandautils.utils.setHidden
import com.instructure.pandautils.utils.setMenu
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.showThemed
import com.instructure.pandautils.utils.withArgs
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

private const val ANIM_DURATION = 150L

@ScreenView(SCREEN_VIEW_INBOX)
@PageView(url = "conversations")
@AndroidEntryPoint
class InboxFragment : Fragment(), NavigationCallbacks, FragmentInteractions {

    private val viewModel: InboxViewModel by viewModels()

    @Inject
    lateinit var inboxRouter: InboxRouter

    private lateinit var binding: FragmentInboxBinding

    private var onUnreadCountInvalidated: OnUnreadCountInvalidated? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInboxBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this.viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editToolbar.setupAsBackButton(this)
        binding.editToolbar.setMenu(R.menu.menu_inbox_edit) {
            when (it.itemId) {
                R.id.inboxStarSelected -> viewModel.starSelected()
                R.id.inboxUnstarSelected -> viewModel.unstarSelected()
                R.id.inboxMarkAsReadSelected -> viewModel.markAsReadSelected()
                R.id.inboxMarkAsUnreadSelected -> viewModel.markAsUnreadSelected()
                R.id.inboxDeleteSelected -> deleteSelected()
                R.id.inboxArchiveSelected -> viewModel.archiveSelected()
                R.id.inboxUnarchiveSelected -> viewModel.unarchiveSelected()
            }
        }
        applyTheme()
        setUpScrollingBehavior()

        viewModel.events.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }

        viewModel.data.observe(viewLifecycleOwner) { data ->
            setMenuItems(data.editMenuItems)
            animateToolbars(data.selectionMode)
        }
    }

    private fun setMenuItems(editMenuItems: Set<InboxMenuItem>) {
        binding.editToolbar.menu.items.forEach {
            it.isVisible = editMenuItems.map { it.id }.contains(it.itemId)
        }
    }

    private fun setUpScrollingBehavior() {
        binding.inboxRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && binding.addMessage.visibility == View.VISIBLE) {
                    binding.addMessage.hide()
                } else if (dy < 0 && binding.addMessage.visibility != View.VISIBLE) {
                    binding.addMessage.show()
                }
            }
        })
    }

    private fun deleteSelected() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.deleteConfirmation)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteSelected() }
            .setNegativeButton(R.string.cancel, null)
            .showThemed()
    }

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
        fadeOut.duration = ANIM_DURATION
        fadeOut.addListener(onEnd = { currentToolbar.setVisible(false) })

        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = ANIM_DURATION
        fadeIn.startOffset = ANIM_DURATION
        fadeIn.addListener(onStart = { newToolbar.setVisible(true) })

        currentToolbar.startAnimation(fadeOut)
        newToolbar.startAnimation(fadeIn)
    }

    override val navigation: Navigation?
        get() = if (activity is Navigation) activity as Navigation else null

    override fun title(): String = getString(R.string.inbox)

    override fun applyTheme() {
        ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        ViewStyler.themeToolbarColored(requireActivity(), binding.editToolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        binding.toolbarWrapper.setBackgroundColor(ThemePrefs.primaryColor)
        binding.addMessage.backgroundTintList = ViewStyler.makeColorStateListForButton()
        inboxRouter.attachNavigationIcon(binding.toolbar)
    }

    override fun getFragment(): Fragment? = this

    private fun handleAction(action: InboxAction) {
        when (action) {
            is InboxAction.OpenConversation -> inboxRouter.openConversation(action.conversation, action.scope)
            InboxAction.OpenScopeSelector -> openScopeSelector()
            is InboxAction.ItemSelectionChanged -> animateAvatar(action.view, action.selected)
            is InboxAction.ShowConfirmationSnackbar -> Snackbar.make(requireView(), action.text, Snackbar.LENGTH_LONG).show()
            InboxAction.CreateNewMessage -> inboxRouter.routeToNewMessage()
            InboxAction.FailedToLoadNextPage -> Snackbar.make(requireView(), R.string.failedToLoadNextPage, Snackbar.LENGTH_LONG).show()
            InboxAction.UpdateUnreadCount -> onUnreadCountInvalidated?.invalidateUnreadCount()
        }
    }

    private fun animateAvatar(view: View, selected: Boolean) {
        val itemBinding = DataBindingUtil.findBinding<ItemInboxEntryBinding>(view)
        if (itemBinding == null) return

        val avatar: ImageView = itemBinding.avatar
        val avatarSelected: ImageView = itemBinding.avatarSelected

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
        outAnimation.duration = ANIM_DURATION
        outAnimation.addListener(onEnd = { outView.setHidden(true) })

        val inAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.flip_in_anim)
        inAnimation.duration = ANIM_DURATION
        inAnimation.startOffset = ANIM_DURATION
        inAnimation.addListener(onStart = { inView.setHidden(false) })

        outView.startAnimation(outAnimation)
        inView.startAnimation(inAnimation)
    }

    private fun openScopeSelector() {
        val popup = PopupMenu(requireContext(), binding.scopeFilter)
        popup.menuInflater.inflate(R.menu.menu_conversation_scope, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.inbox_all -> viewModel.scopeChanged(InboxApi.Scope.INBOX)
                R.id.inbox_unread -> viewModel.scopeChanged(InboxApi.Scope.UNREAD)
                R.id.inbox_starred -> viewModel.scopeChanged(InboxApi.Scope.STARRED)
                R.id.inbox_sent -> viewModel.scopeChanged(InboxApi.Scope.SENT)
                R.id.inbox_archived -> viewModel.scopeChanged(InboxApi.Scope.ARCHIVED)
            }

            true
        }

        popup.show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(requireContext())
        try {
            onUnreadCountInvalidated = context as OnUnreadCountInvalidated?
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement OnUnreadCountInvalidated")
        }
    }

    override fun onHandleBackPressed(): Boolean {
        return viewModel.handleBackPressed()
    }

    fun conversationUpdated() {
        viewModel.invalidateCache()
        viewModel.refresh()
        onUnreadCountInvalidated?.invalidateUnreadCount()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(inboxRouter)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(inboxRouter)
    }

    companion object {
        fun makeRoute() = Route(InboxFragment::class.java, null)

        fun newInstance(route: Route) = if (validateRoute(route)) InboxFragment().withArgs(route.arguments) else null

        private fun validateRoute(route: Route) = route.primaryClass == InboxFragment::class.java
    }
}

interface OnUnreadCountInvalidated {
    fun invalidateUnreadCount()
}