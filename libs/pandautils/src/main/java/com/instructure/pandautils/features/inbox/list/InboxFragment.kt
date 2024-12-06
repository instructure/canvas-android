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
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_INBOX
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.binding.BindableViewHolder
import com.instructure.pandautils.databinding.FragmentInboxBinding
import com.instructure.pandautils.databinding.ItemInboxEntryBinding
import com.instructure.pandautils.features.inbox.list.filter.ContextFilterFragment
import com.instructure.pandautils.features.inbox.list.itemviewmodels.InboxEntryItemViewModel
import com.instructure.pandautils.features.inbox.utils.InboxSharedAction
import com.instructure.pandautils.features.inbox.utils.InboxSharedEvents
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.addListener
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.isVisible
import com.instructure.pandautils.utils.items
import com.instructure.pandautils.utils.setHidden
import com.instructure.pandautils.utils.setMenu
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.showThemed
import com.instructure.pandautils.utils.toPx
import com.instructure.pandautils.utils.withArgs
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

private const val ANIM_DURATION = 150L

@ScreenView(SCREEN_VIEW_INBOX)
@PageView(url = "conversations")
@AndroidEntryPoint
class InboxFragment : BaseCanvasFragment(), NavigationCallbacks, FragmentInteractions {

    private val viewModel: InboxViewModel by viewModels()

    private val sharedViewModel: InboxSharedViewModel by activityViewModels()

    @Inject
    lateinit var inboxRouter: InboxRouter

    @Inject
    lateinit var sharedEvents: InboxSharedEvents

    private lateinit var binding: FragmentInboxBinding

    private var onUnreadCountInvalidated: OnUnreadCountInvalidated? = null

    private var touchCallback: ItemTouchHelper.SimpleCallback? = null

    private var confirmationSnackbar: Snackbar? = null

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
        setUpEditToolbar()
        applyTheme()
        lifecycleScope.collectOneOffEvents(sharedEvents.events, ::handleSharedViewModelAction)

        viewModel.events.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }

        viewModel.data.observe(viewLifecycleOwner) { data ->
            setMenuItems(data.editMenuItems)
            animateToolbars(data.selectionMode)
            if (!data.selectionMode) animateBackAvatars()

            if (data.scope == getString(R.string.inbox_sent)) {
                touchCallback?.setDefaultSwipeDirs(ItemTouchHelper.END)
            } else {
                touchCallback?.setDefaultSwipeDirs(ItemTouchHelper.START or ItemTouchHelper.END)
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            handleAppBarBehavior(state)
        }

        sharedViewModel.events.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                handleSharedAction(it)
            }
        }

        configureItemTouchHelper()
    }

    private fun handleSharedViewModelAction(action: InboxSharedAction) {
        when (action) {
            is InboxSharedAction.RefreshListScreen -> {
                viewModel.refresh()
            }
            else -> {}
        }
    }

    private fun configureItemTouchHelper() {
        val markAsColor = requireContext().getColor(R.color.backgroundInfo)
        val archiveColor = requireContext().getColor(R.color.ash)
        val paint = Paint().apply { style = Paint.Style.FILL }

        touchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START or ItemTouchHelper.END) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = true

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val itemViewModel = (viewHolder as BindableViewHolder).itemViewModel as? InboxEntryItemViewModel
                if (itemViewModel == null) return
                if (direction == ItemTouchHelper.END) {
                    if (itemViewModel.data.unread) {
                        viewModel.markConversationAsRead(itemViewModel.data.id)
                    } else {
                        viewModel.markConversationAsUnread(itemViewModel.data.id)
                    }
                } else {
                    when (viewModel.data.value?.scope) {
                        getString(R.string.inbox_archived) -> viewModel.unarchiveConversation(itemViewModel.data.id)
                        getString(R.string.inbox_starred) -> viewModel.unstarConversation(itemViewModel.data.id)
                        else -> viewModel.archiveConversation(itemViewModel.data.id)
                    }
                }
                binding.inboxRecyclerView.adapter?.notifyItemChanged(viewHolder.bindingAdapterPosition)
            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemViewModel = (viewHolder as BindableViewHolder).itemViewModel as? InboxEntryItemViewModel
                if (itemViewModel == null) return

                val bounds = Rect(viewHolder.itemView.left, viewHolder.itemView.top, viewHolder.itemView.right, viewHolder.itemView.bottom)
                val height = viewHolder.itemView.height
                val margin = 16.toPx

                val iconColor = requireContext().getColor(R.color.textLightest)

                if (dX > 0) {
                    paint.color = markAsColor
                    canvas.drawRect(bounds, paint)

                    val drawableId = if (itemViewModel.data.unread) R.drawable.ic_mark_as_read else R.drawable.ic_mark_as_unread
                    val markAsIcon = resources.getDrawable(drawableId, null)
                    markAsIcon.colorFilter = PorterDuffColorFilter(iconColor,PorterDuff.Mode.SRC_ATOP)
                    markAsIcon.bounds = Rect(
                        margin,
                        bounds.top + height/2 - markAsIcon.intrinsicHeight/2,
                        margin + markAsIcon.intrinsicWidth,
                        bounds.top + height/2 + markAsIcon.intrinsicHeight/2)
                    markAsIcon.draw(canvas)
                } else {
                    paint.color = archiveColor
                    canvas.drawRect(bounds, paint)

                    val archiveIcon = when (viewModel.data.value?.scope) {
                        getString(R.string.inbox_archived) -> resources.getDrawable(R.drawable.ic_unarchive, null)
                        getString(R.string.inbox_starred) -> resources.getDrawable(R.drawable.ic_star_outline, null)
                        else -> resources.getDrawable(R.drawable.ic_archive, null)
                    }
                    archiveIcon.colorFilter = PorterDuffColorFilter(iconColor,PorterDuff.Mode.SRC_ATOP)

                    val width = resources.displayMetrics.widthPixels
                    archiveIcon.bounds = Rect(
                        width - margin - archiveIcon.intrinsicWidth,
                        bounds.top + height/2 - archiveIcon.intrinsicHeight/2,
                        width - margin,
                        bounds.top + height/2 + archiveIcon.intrinsicHeight/2)
                    archiveIcon.draw(canvas)
                }

                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(touchCallback!!)

        itemTouchHelper.attachToRecyclerView(binding.inboxRecyclerView)
    }

    private fun handleAppBarBehavior(state: ViewState?) {
        val params = binding.filterSection.layoutParams as? AppBarLayout.LayoutParams
        params?.scrollFlags = if (state is ViewState.Empty || state is ViewState.Error) {
            0
        } else {
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        }
        binding.filterSection.layoutParams = params
    }

    private fun setUpEditToolbar() {
        binding.editToolbar.setupAsBackButton(this)
        binding.editToolbar.setNavigationContentDescription(R.string.a11y_exitSelectionMode)
        binding.editToolbar.setMenu(R.menu.menu_inbox_edit) {
            when (it.itemId) {
                R.id.inboxStarSelected -> viewModel.starSelected()
                R.id.inboxUnstarSelected -> viewModel.unstarSelected()
                R.id.inboxMarkAsReadSelected -> viewModel.markAsReadSelected()
                R.id.inboxMarkAsUnreadSelected -> viewModel.markAsUnreadSelected()
                R.id.inboxDeleteSelected -> viewModel.confirmDelete()
                R.id.inboxArchiveSelected -> viewModel.archiveSelected()
                R.id.inboxUnarchiveSelected -> viewModel.unarchiveSelected()
            }
        }
    }

    private fun setMenuItems(editMenuItems: Set<InboxMenuItem>) {
        binding.editToolbar.menu.items.forEach {
            it.isVisible = editMenuItems.map { it.id }.contains(it.itemId)
        }
    }

    private fun deleteSelected(count: Int) {
        val message = resources.getQuantityString(R.plurals.inboxConfirmDelete, count)
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteSelected() }
            .setNegativeButton(R.string.cancel, null)
            .showThemed()
    }

    private fun animateToolbars(selectionMode: Boolean) {
        if (selectionMode && binding.editToolbar.isVisible) return
        if (!selectionMode && binding.toolbar.isVisible) return

        val currentToolbar: Toolbar
        val newToolbar: Toolbar
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
        fadeIn.addListener(onStart = { newToolbar.setVisible(true) }, onEnd = {
            if (selectionMode) {
                binding.editToolbar.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            } else {
                newToolbar.announceForAccessibility(getString(R.string.a11y_selectionModeDeactivated))
            }
        })

        currentToolbar.startAnimation(fadeOut)
        newToolbar.startAnimation(fadeIn)
    }

    private fun animateBackAvatars() {
        binding.inboxRecyclerView.children.forEach {
            val itemBinding = DataBindingUtil.findBinding<ViewDataBinding>(it) as? ItemInboxEntryBinding
            if (itemBinding?.avatarSelected?.visibility == View.VISIBLE) {
                animateAvatar(it, false)
            }
        }
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(R.string.inbox)

    override fun applyTheme() {
        ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        ViewStyler.themeToolbarColored(requireActivity(), binding.editToolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        binding.toolbarWrapper.setBackgroundColor(ThemePrefs.primaryColor)
        ViewStyler.themeFAB(binding.addMessage)
        binding.scopeFilterText.setTextColor(ThemePrefs.textButtonColor)
        binding.scopeFilterIcon.setColorFilter(ThemePrefs.textButtonColor)
        inboxRouter.attachNavigationIcon(binding.toolbar)
    }

    override fun getFragment(): Fragment? = this

    private fun handleAction(action: InboxAction) {
        when (action) {
            is InboxAction.OpenConversation -> inboxRouter.openConversation(action.conversation, action.scope)
            InboxAction.OpenScopeSelector -> openScopeSelector()
            is InboxAction.ItemSelectionChanged -> animateAvatar(action.view, action.selected)
            is InboxAction.ShowConfirmationSnackbar -> showConfirmation(action)
            InboxAction.CreateNewMessage -> inboxRouter.routeToNewMessage(requireActivity())
            InboxAction.FailedToLoadNextPage -> Snackbar.make(requireView(), R.string.failedToLoadNextPage, Snackbar.LENGTH_LONG).show()
            InboxAction.UpdateUnreadCount -> onUnreadCountInvalidated?.invalidateUnreadCount()
            is InboxAction.OpenContextFilterSelector -> openContextFilterSelector(action.canvasContexts)
            InboxAction.RefreshFailed -> Snackbar.make(requireView(), R.string.conversationsRefreshFailed, Snackbar.LENGTH_LONG).show()
            is InboxAction.ConfirmDelete -> deleteSelected(action.count)
            is InboxAction.AvatarClickedCallback -> inboxRouter.avatarClicked(action.conversation, action.scope)
            InboxAction.DismissSnackbar -> confirmationSnackbar?.dismiss()
        }
    }

    private fun showConfirmation(action: InboxAction.ShowConfirmationSnackbar) {
        confirmationSnackbar = Snackbar.make(requireView(), action.text, Snackbar.LENGTH_LONG)

        if (action.undoAction != null) {
            confirmationSnackbar
                ?.setActionTextColor(ThemePrefs.textButtonColor)
                ?.setAction(R.string.inboxUndo) {
                    action.undoAction.invoke()
                }
        }

        confirmationSnackbar?.show()
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
        inAnimation.addListener(onStart = { inView.setHidden(false) }, onEnd = {
            val textResource = if (selected) R.string.a11y_conversationSelected else R.string.a11y_conversationDeselected
            inView.announceForAccessibility(getString(textResource))
        })

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

    private fun openContextFilterSelector(canvasContexts: List<CanvasContext>) {
        val contextFilterDialog = ContextFilterFragment.newInstance(canvasContexts)
        contextFilterDialog.show(requireActivity().supportFragmentManager, ContextFilterFragment::javaClass.name)
    }

    private fun handleSharedAction(it: InboxFilterAction) {
        when (it) {
            InboxFilterAction.FilterCleared -> viewModel.allCoursesSelected()
            is InboxFilterAction.FilterSelected -> viewModel.canvasContextFilterSelected(it.id)
            InboxFilterAction.FilterDialogDismissed -> ViewStyler.setStatusBarDark(requireActivity(), ThemePrefs.primaryColor)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(requireContext())
        onUnreadCountInvalidated = context as? OnUnreadCountInvalidated
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

    // We might need to change this for the teacher implementation
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.emptyInboxView.handleConfigChange(isTablet, resources.configuration.orientation)
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