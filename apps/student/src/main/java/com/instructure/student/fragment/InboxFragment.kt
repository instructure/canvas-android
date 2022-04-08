/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.student.fragment

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.InboxApi.Scope
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_INBOX
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.InboxAdapter
import com.instructure.student.decorations.DividerDecoration
import com.instructure.student.dialog.CanvasContextListDialog
import com.instructure.student.events.ConversationUpdatedEvent
import com.instructure.student.interfaces.AdapterToFragmentCallback
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.fragment_inbox.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

@ScreenView(SCREEN_VIEW_INBOX)
@PageView(url = "conversations")
class InboxFragment : ParentFragment() {

    private var messageType: Scope = Scope.ALL

    private lateinit var adapter: InboxAdapter

    private var onUnreadCountInvalidated: OnUnreadCountInvalidated? = null

   data class ScopeParams(val titleText: Int,
                          val messageText: Int,
                          val image: Int)

    interface OnUnreadCountInvalidated {
        fun invalidateUnreadCount()
    }

    private var adapterToFragmentCallback = object : AdapterToFragmentCallback<Conversation> {
        override fun onRowClicked(conversation: Conversation, position: Int, isOpenDetail: Boolean) {
            showConversation(conversation)
        }

        override fun onRefreshFinished() {
            setRefreshing(false)

            // update the unread count
            onUnreadCountInvalidated?.invalidateUnreadCount()
        }
    }

    override fun title(): String = getString(R.string.inbox)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navigation?.attachNavigationDrawer(this, toolbar)
        toolbar.title = title()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(requireContext())

        try {
            onUnreadCountInvalidated = context as OnUnreadCountInvalidated?
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement OnUnreadCountInvalidated")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = layoutInflater.inflate(R.layout.fragment_inbox, container, false)
        adapter = InboxAdapter(requireContext(), adapterToFragmentCallback)
        val recyclerView = configureRecyclerView(rootView, requireContext(), adapter, R.id.swipeRefreshLayout, R.id.emptyInboxView, R.id.inboxRecyclerView)
        recyclerView.addItemDecoration(DividerDecoration(requireContext()))
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupFilter()
        setupFilterText()
        emptyInboxView.setEmptyViewImage(requireContext().getDrawableCompat(R.drawable.ic_panda_inboxzero))
        emptyInboxView.setMessageText(R.string.nothingUnreadSubtext)
        emptyInboxView.getMessage().importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        emptyInboxView.setTitleText(R.string.nothingUnread)
        inboxRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && addMessage.visibility == View.VISIBLE) {
                    addMessage.hide()
                } else if (dy < 0 && addMessage.visibility != View.VISIBLE) {
                    addMessage.show()
                }
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        emptyInboxView.changeTextSize()
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (isTablet) {
                emptyInboxView.setGuidelines(.24f, .53f, .62f, .12f, .88f)
            } else {
                emptyInboxView.setGuidelines(.15f, .55f, .65f, .12f, .88f)

            }
        } else {
            if (isTablet) {
                //change nothing, at least for now
            } else {
                emptyInboxView.setGuidelines(.12f, .65f, .7f, .12f, .88f)
            }
        }
    }

    private fun setupFilterText() {
        clearFilterTextView.setTextColor(ThemePrefs.buttonColor)
        adapter.canvasContext?.let {
            courseFilter.text = it.name
            clearFilterTextView.setVisible()
        }
    }

    private fun setupListeners() {
        addMessage.onClickWithRequireNetwork {
            val route = InboxComposeMessageFragment.makeRoute()
            RouteMatcher.route(requireContext(), route)
        }

        clearFilterTextView.setOnClickListener {
            adapter.canvasContext = null
            courseFilter.setText(R.string.allCourses)
            clearFilterTextView.setGone()
            reloadData()
        }
    }

    private fun setupFilter() {
        filterText.text = getTextByScope(adapter.scope)
        filterButton.setOnClickListener(View.OnClickListener {
            if (context == null) return@OnClickListener

            val popup = PopupMenu(requireContext(), popupViewPosition)
            popup.menuInflater.inflate(R.menu.inbox_scope, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.inbox_all -> onScopeChanged(Scope.ALL)
                    R.id.inbox_unread -> onScopeChanged(Scope.UNREAD)
                    R.id.inbox_starred -> onScopeChanged(Scope.STARRED)
                    R.id.inbox_sent -> onScopeChanged(Scope.SENT)
                    R.id.inbox_archived -> onScopeChanged(Scope.ARCHIVED)
                }
                true
            }
            popup.show()
        })
    }


    private fun getScopeParams(scope: Scope): ScopeParams {
        return when (scope) {
            Scope.ALL -> ScopeParams(R.string.nothingUnread,
                    R.string.nothingUnreadSubtext,
                    R.drawable.ic_panda_inboxzero)
            Scope.UNREAD -> ScopeParams(R.string.nothingUnread,
                    R.string.nothingUnreadSubtext,
                    R.drawable.ic_panda_inboxzero)
            Scope.STARRED -> ScopeParams(R.string.nothingStarred,
                    R.string.nothingStarredSubtext,
                    R.drawable.ic_panda_inboxstarred)
            Scope.SENT -> ScopeParams(R.string.nothingSent,
                   R.string.nothingSentSubtext,
                    R.drawable.ic_panda_inboxsent)
            Scope.ARCHIVED -> ScopeParams(R.string.nothingArchived,
                    R.string.nothingArchivedSubtext,
                    R.drawable.ic_panda_inboxarchived)
        }
    }

    private fun setupScopeView(scopeParams: ScopeParams) {
        emptyInboxView.setEmptyViewImage(requireContext().getDrawableCompat(scopeParams.image))
        emptyInboxView.setMessageText(scopeParams.messageText)
        emptyInboxView.setTitleText(scopeParams.titleText)
    }

    private fun getTextByScope(scope: Scope): String {
        return when (scope) {
            Scope.ALL -> getString(R.string.inboxAllMessages)
            Scope.UNREAD -> getString(R.string.inbox_unread)
            Scope.STARRED -> getString(R.string.inbox_starred)
            Scope.SENT -> getString(R.string.inbox_sent)
            Scope.ARCHIVED -> getString(R.string.inbox_archived)
        }
    }

    private fun onScopeChanged(scope: Scope) {
        filterText.text = getTextByScope(scope)
        adapter.scope = scope
        val scopeParams = getScopeParams(scope)
        setupScopeView(scopeParams)
    }

    override fun applyTheme() {
        setupToolbarMenu(toolbar, R.menu.menu_filter_inbox)
        ViewStyler.themeToolbar(requireActivity(), toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        addMessage.backgroundTintList = ViewStyler.makeColorStateListForButton()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.inboxFilter -> {
                // Let the user select the course/group they want to see
                val dialog = CanvasContextListDialog.getInstance(requireActivity().supportFragmentManager) { canvasContext ->
                    if (adapter.canvasContext?.contextId != canvasContext.contextId) {
                        // We only want to change this up if they are selecting a new context
                        adapter.canvasContext = canvasContext
                        reloadData()
                    }
                }

                dialog.show(requireActivity().supportFragmentManager, CanvasContextListDialog::class.java.simpleName)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showConversation(conversation: Conversation) {
        val route = InboxConversationFragment.makeRoute(conversation, InboxApi.conversationScopeToString(messageType))
        RouteMatcher.route(requireContext(), route)
    }

    @Suppress("unused")
    @Subscribe(sticky = true)
    fun onUpdateConversation(event: ConversationUpdatedEvent) {
        event.get {
            reloadData()

            // update the unread count
            onUnreadCountInvalidated?.invalidateUnreadCount()
        }
    }

    private fun reloadData() {
        adapter.refresh()
        setupFilterText()
    }

    companion object {

        fun makeRoute() = Route(InboxFragment::class.java, null)

        private fun validateRoute(route: Route) = route.primaryClass == InboxFragment::class.java

        fun newInstance(route: Route) = if (validateRoute(route)) InboxFragment().withArgs(route.arguments) else null
    }
}
