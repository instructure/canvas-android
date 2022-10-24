/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.fragments

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.parcelCopy
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_INBOX
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.activities.InitActivity
import com.instructure.teacher.adapters.InboxAdapter
import com.instructure.teacher.dialog.CanvasContextListDialog
import com.instructure.teacher.events.ConversationDeletedEvent
import com.instructure.teacher.events.ConversationUpdatedEvent
import com.instructure.teacher.events.ConversationUpdatedEventTablet
import com.instructure.teacher.factory.InboxPresenterFactory
import com.instructure.teacher.holders.InboxViewHolder
import com.instructure.teacher.interfaces.AdapterToFragmentCallback
import com.instructure.teacher.presenters.InboxPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.getColorCompat
import com.instructure.teacher.utils.setupBackButtonAsBackPressedOnly
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.viewinterface.InboxView
import kotlinx.android.synthetic.main.fragment_inbox.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@ScreenView(SCREEN_VIEW_INBOX)
class InboxFragment : BaseSyncFragment<Conversation, InboxPresenter, InboxView, InboxViewHolder, InboxAdapter>(), InboxView {

    private val CANVAS_CONTEXT = "canvas_context"
    private var canvasContextSelected: CanvasContext? = null
    //used to keep track of scope for configuration changes
    private var currentScope = InboxApi.Scope.ALL

    override fun layoutResId(): Int = R.layout.fragment_inbox
    override fun withPagination() = true
    override val recyclerView: RecyclerView get() = inboxRecyclerView
    override fun checkIfEmpty() {
        // We don't want to leave the fab hidden if the list is empty
        if(presenter.isEmpty) {
            addMessage.show()
        }
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, recyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }
    override fun getPresenterFactory(): InboxPresenterFactory = InboxPresenterFactory()
    override fun onCreateView(view: View) {}

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(CANVAS_CONTEXT, canvasContextSelected)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState != null) {
            canvasContextSelected = savedInstanceState.getParcelable(CANVAS_CONTEXT)
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onPresenterPrepared(presenter: InboxPresenter) {
        RecyclerViewUtils.buildRecyclerView(rootView, requireContext(), adapter,
                presenter, R.id.swipeRefreshLayout, R.id.inboxRecyclerView, R.id.emptyPandaView, getString(R.string.nothingUnread))
        onScopeChanged(currentScope)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && addMessage.visibility == View.VISIBLE) {
                    addMessage.hide()
                } else if (dy < 0 && addMessage.visibility != View.VISIBLE) {
                    addMessage.show()
                }
            }
        })

        addSwipeToRefresh(swipeRefreshLayout)
        addPagination()
    }

    //pagination
    override fun hitRockBottom() {
        presenter.nextPage()
    }

    override fun onReadySetGo(presenter: InboxPresenter) {
        if(recyclerView.adapter == null) {
            recyclerView.adapter = adapter
        }

        presenter.canvasContext = canvasContextSelected
        setFilterText()
        presenter.loadData(false)
        setupFilter(presenter)
        setupToolbar()
        setupListeners()
        setupConversationEvents()
    }

    private fun setupConversationEvents() {
        //phone specific event for updates (archives/read/unread/stars)
        val event = EventBus.getDefault().getStickyEvent(ConversationUpdatedEvent::class.java)
        event?.once(javaClass.simpleName) {
            if((presenter.scope == event.scope && presenter.scope != InboxApi.Scope.UNREAD) || presenter.scope == InboxApi.Scope.ALL)
            //for removed stars and archives, we need to update the list completely
                presenter.refresh(true)
            else
                presenter.data.addOrUpdate(it)
        }

        //phone specific event for deletion
        EventBus.getDefault().getStickyEvent(ConversationDeletedEvent::class.java)?.once(javaClass.simpleName + ".onResume()") {
            // The presenter's data could be cleared on a refresh, then a race condition here will remove an item from an empty list
            if (presenter.data.size() > it) {
                presenter.data.removeItemAt(it)
            }
        }

    }

    private fun setupToolbar() {
        toolbar.setupMenu(R.menu.menu_filter_inbox, menuItemCallback)
        val activity = requireActivity()
        if (activity is InitActivity) {
            activity.attachNavigationDrawer(toolbar)
        } else {
            toolbar.setupBackButtonAsBackPressedOnly(this)
        }

        ViewStyler.themeToolbarColored(requireActivity(), toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        addMessage.backgroundTintList = ViewStyler.makeColorStateListForButton()
        addMessage.setImageDrawable(ColorUtils.colorIt(ThemePrefs.buttonTextColor, addMessage.drawable))
        toolbar.requestAccessibilityFocus()
    }

    private fun setupListeners() {
        addMessage.setOnClickListener {
            val args = AddMessageFragment.createBundle()
            RouteMatcher.route(requireContext(), Route(AddMessageFragment::class.java, null, args))
        }

        clearFilterTextView.setOnClickListener {
            presenter.canvasContext = null
            canvasContextSelected = null
            courseFilter.setText(R.string.all_courses)
            clearFilterTextView.setGone()
            presenter.refresh(true)
        }
    }
    public override fun createAdapter(): InboxAdapter {
        return InboxAdapter(requireActivity(), presenter, mAdapterCallback)
    }

    private val mAdapterCallback = object : AdapterToFragmentCallback<Conversation> {
        override fun onRowClicked(model: Conversation, position: Int) {
            //we send a parcel copy so that we can properly propagate updates through our events
            if (resources.getBoolean(R.bool.isDeviceTablet)) { //but tablets need reference, since the detail view remains in view
                val args = MessageThreadFragment.createBundle(model, position, InboxApi.conversationScopeToString(presenter.scope))
                RouteMatcher.route(requireContext(), Route(null, MessageThreadFragment::class.java, null, args))
            } else { //phones use the parcel copy
                val args = MessageThreadFragment.createBundle(model.parcelCopy(), position, InboxApi.conversationScopeToString(presenter.scope))
                RouteMatcher.route(requireContext(), Route(null, MessageThreadFragment::class.java, null, args))
            }
        }

    }

    override fun onRefreshStarted() {
        //this prevents two loading spinners from happening during pull to refresh
        if(!swipeRefreshLayout.isRefreshing) {
            emptyPandaView.visibility  = View.VISIBLE
        }
        emptyPandaView.setLoading()
    }

    override fun onRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
        setFilterText()
    }

    private fun setFilterText() {
        clearFilterTextView.setTextColor(ThemePrefs.textButtonColor)
        if (presenter.canvasContext != null) {
            courseFilter.text = (presenter.canvasContext as CanvasContext).name
            clearFilterTextView.setVisible()
        }
    }

    private fun setupFilter(presenter: InboxPresenter) {
        filterText.text = getTextByScope(presenter.scope)
        filterIndicator.setImageDrawable(ColorUtils.colorIt(requireContext().getColorCompat(R.color.textDarkest), filterIndicator.drawable))
        filterButton.setOnClickListener(View.OnClickListener {
            if (context == null) return@OnClickListener

            val popup = PopupMenu(requireContext(), popupViewPosition)
            popup.menuInflater.inflate(R.menu.conversation_scope, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.inbox_all -> onScopeChanged(InboxApi.Scope.ALL)
                    R.id.inbox_unread -> onScopeChanged(InboxApi.Scope.UNREAD)
                    R.id.inbox_starred -> onScopeChanged(InboxApi.Scope.STARRED)
                    R.id.inbox_sent -> onScopeChanged(InboxApi.Scope.SENT)
                    R.id.inbox_archived -> onScopeChanged(InboxApi.Scope.ARCHIVED)
                }

                true
            }

            popup.show()
        })
    }

    override fun unreadCountUpdated(unreadCount: Int) {
        val activity = requireActivity()
        if (activity is InitActivity) {
            activity.updateInboxUnreadCount(unreadCount)
        }
    }

    val menuItemCallback: (MenuItem) -> Unit = { item ->
        when (item.itemId) {
            R.id.inboxFilter -> {
                //let the user select the course/group they want to see
                val dialog = CanvasContextListDialog.getInstance(requireActivity().supportFragmentManager) { canvasContext: CanvasContext ->
                    canvasContextSelected = canvasContext
                    if (presenter.canvasContext?.id != canvasContextSelected?.id) {
                        //we only want to change this up if they are selecting a new requireContext()
                        presenter.canvasContext = canvasContext
                        presenter.refresh(true)
                    }
                }

                dialog.show(requireActivity().supportFragmentManager, CanvasContextListDialog::class.java.simpleName)
            }
        }
    }

    private fun onScopeChanged(scope: InboxApi.Scope) {
        currentScope = scope
        filterText.text = getTextByScope(scope)
        presenter.scope = scope

        when (scope) {
            InboxApi.Scope.STARRED -> {
                emptyPandaView.setEmptyViewImage(requireContext().getDrawableCompat(R.drawable.ic_panda_inboxstarred))
                emptyPandaView.setMessageText(R.string.nothingStarredSubtext)
                emptyPandaView.setTitleText(R.string.nothingStarred)
            }
            InboxApi.Scope.SENT -> {
                emptyPandaView.setEmptyViewImage(requireContext().getDrawableCompat(R.drawable.ic_panda_inboxsent))
                emptyPandaView.setMessageText(R.string.nothingSentSubtext)
                emptyPandaView.setTitleText(R.string.nothingSent)
            }
            InboxApi.Scope.ARCHIVED -> {
                emptyPandaView.setEmptyViewImage(requireContext().getDrawableCompat(R.drawable.ic_panda_inboxarchived))
                emptyPandaView.setMessageText(R.string.nothingArchivedSubtext)
                emptyPandaView.setTitleText(R.string.nothingArchived)
            }
            else -> {
                emptyPandaView.setEmptyViewImage(requireContext().getDrawableCompat(R.drawable.ic_panda_inboxzero))
                emptyPandaView.setMessageText(R.string.nothingUnreadSubtext)
                emptyPandaView.getMessage().importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                emptyPandaView.setTitleText(R.string.nothingUnread)
            }
        }
    }

    private fun getTextByScope(scope: InboxApi.Scope): String {
        return when (scope) {
            InboxApi.Scope.ALL -> getString(R.string.inbox_all_messages)
            InboxApi.Scope.UNREAD -> getString(R.string.inbox_unread)
            InboxApi.Scope.STARRED -> getString(R.string.inbox_starred)
            InboxApi.Scope.SENT -> getString(R.string.inbox_sent)
            InboxApi.Scope.ARCHIVED -> getString(R.string.inbox_archived)
            else -> getString(R.string.inbox_all_messages)
        }
    }

    //tablet specific event for updates
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onConversationUpdated(event: ConversationUpdatedEventTablet) {
        event.once(javaClass.simpleName) {
            if(presenter.scope == event.scope && presenter.scope != InboxApi.Scope.UNREAD)
                //for removed stars and archives, we need to update the list completely
                presenter.refresh(true)
            else
                adapter.notifyItemChanged(it)
        }
    }

    //tablet specific event for deletion
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onConversationDeleted(event: ConversationDeletedEvent) {
        event.once(javaClass.simpleName + ".onPost()") {
            presenter.data.removeItemAt(it)
            //pop current detail fragment if tablet
            if (resources.getBoolean(R.bool.isDeviceTablet)) {
                val currentFrag = requireFragmentManager().findFragmentById(R.id.detail)
                if(currentFrag != null) {
                    val transaction = requireFragmentManager().beginTransaction()
                    transaction.remove(currentFrag)
                    transaction.commit()
                    requireFragmentManager().popBackStack()
                }
            }
        }
    }

    override fun perPageCount(): Int = ApiPrefs.perPageCount

    companion object {
        fun newInstance() = InboxFragment()
    }
}
