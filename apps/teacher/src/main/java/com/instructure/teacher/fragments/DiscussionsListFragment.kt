/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.fragments

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.pandautils.analytics.SCREEN_VIEW_DISCUSSION_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.discussion.DiscussionSharedAction
import com.instructure.pandautils.features.discussion.DiscussionSharedEvents
import com.instructure.pandautils.features.discussion.create.CreateDiscussionWebViewFragment
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragment
import com.instructure.pandautils.fragments.BaseExpandableSyncFragment
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.addSearch
import com.instructure.pandautils.utils.closeSearch
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getDrawableCompat
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.showThemed
import com.instructure.pandautils.utils.toast
import com.instructure.teacher.R
import com.instructure.teacher.adapters.DiscussionListAdapter
import com.instructure.teacher.databinding.FragmentDiscussionListBinding
import com.instructure.teacher.dialog.DiscussionsMoveToDialog
import com.instructure.teacher.events.DiscussionCreatedEvent
import com.instructure.teacher.events.DiscussionTopicHeaderDeletedEvent
import com.instructure.teacher.events.DiscussionTopicHeaderEvent
import com.instructure.teacher.events.DiscussionUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.factory.DiscussionListPresenterFactory
import com.instructure.teacher.presenters.DiscussionListPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.viewinterface.DiscussionListView
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@PageView(url = "{canvasContext}/{type}")
@ScreenView(SCREEN_VIEW_DISCUSSION_LIST)
@AndroidEntryPoint
open class DiscussionsListFragment : BaseExpandableSyncFragment<
        String,
        DiscussionTopicHeader,
        DiscussionListView,
        DiscussionListPresenter,
        RecyclerView.ViewHolder,
        DiscussionListAdapter>(), DiscussionListView {

    private val binding by viewBinding(FragmentDiscussionListBinding::bind)

    @get:PageViewUrlParam("canvasContext")
    var canvasContext: CanvasContext by ParcelableArg(default = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, -1L, ""))

    @Inject
    lateinit var discussionSharedEvents: DiscussionSharedEvents

    private val linearLayoutManager by lazy { LinearLayoutManager(requireContext()) }
    private lateinit var mRecyclerView: RecyclerView

    private var needToForceNetwork = false
    private var forceRefresh = false
    protected var isAnnouncements by BooleanArg()

    override fun layoutResId(): Int = R.layout.fragment_discussion_list
    override val recyclerView: RecyclerView get() = binding.discussionRecyclerView

    override fun getPresenterFactory() = DiscussionListPresenterFactory(canvasContext, isAnnouncements)

    override fun onPresenterPrepared(presenter: DiscussionListPresenter) = with(binding) {
        val emptyTitle = getString(if (isAnnouncements) R.string.noAnnouncements else R.string.noDiscussions)
        mRecyclerView = RecyclerViewUtils.buildRecyclerView(
            rootView = rootView,
            context = requireContext(),
            recyclerAdapter = adapter,
            presenter = presenter,
            swipeToRefreshLayoutResId = R.id.swipeRefreshLayout,
            recyclerViewResId = R.id.discussionRecyclerView,
            emptyViewResId = R.id.emptyPandaView,
            emptyViewText = emptyTitle
        )

        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && createNewDiscussion.visibility == View.VISIBLE) {
                    createNewDiscussion.hide()
                } else if (dy < 0 && createNewDiscussion.visibility != View.VISIBLE) {
                    createNewDiscussion.show()
                }
            }
        })

        setupViews()

        lifecycleScope.collectOneOffEvents(discussionSharedEvents.events, ::handleSharedAction)
    }

    private fun handleSharedAction(action: DiscussionSharedAction) {
        when (action) {
            is DiscussionSharedAction.RefreshListScreen -> {
                presenter.refresh(true)
            }
        }
    }

    override fun onCreateView(view: View) {
        linearLayoutManager.orientation = RecyclerView.VERTICAL
    }

    override fun onReadySetGo(presenter: DiscussionListPresenter) {
        mRecyclerView.adapter = adapter
        if (forceRefresh) {
            presenter.refresh(true)
            forceRefresh = false
        } else {
            presenter.loadData(needToForceNetwork)
            needToForceNetwork = false
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun createAdapter(): DiscussionListAdapter {
        return DiscussionListAdapter(requireContext(), presenter, canvasContext.color, isAnnouncements,
            { discussionTopicHeader ->
                val route = presenter.getDetailsRoute(discussionTopicHeader)
                RouteMatcher.route(
                    requireActivity(),
                    route
                )
            },
            { group, discussionTopicHeaderOverflow ->
                if (group != null) {
                    DiscussionsMoveToDialog.show(
                        parentFragmentManager,
                        group,
                        discussionTopicHeaderOverflow
                    ) { newGroup ->
                        presenter.requestMoveDiscussionTopicToGroup(newGroup, group, discussionTopicHeaderOverflow)
                    }
                }
            })
    }

    override fun perPageCount() = ApiPrefs.perPageCount

    override fun onRefreshStarted(): Unit = with(binding) {
        //this prevents two loading spinners from happening during pull to refresh
        if(!swipeRefreshLayout.isRefreshing) {
            emptyPandaView.setVisible()
        }
        emptyPandaView.setLoading()
        createNewDiscussion.setGone()
    }

    override fun onRefreshFinished(): Unit = with(binding) {
        emptyPandaView.setGone()
        swipeRefreshLayout.isRefreshing = false

        EventBus.getDefault().getStickyEvent(DiscussionTopicHeaderEvent::class.java)?.get {
            // After we load the data we look to see if anything is out of date and update it. Only used on phones typically.
            adapter.addOrUpdateItem(it)
        }

        // Fix for when loading groups and pinned items don't show up in the first api call.
        recyclerView.scrollToPosition(0)
        createNewDiscussion.setVisible()
    }

    override fun checkIfEmpty() = with(binding) {
        // We don't want to leave the fab hidden if the list is empty
        if(presenter.isEmpty) {
            createNewDiscussion.show()
            if (isAnnouncements) {
                emptyPandaView.setEmptyViewImage(requireContext().getDrawableCompat(R.drawable.ic_panda_noannouncements))
                emptyPandaView.setMessageText(R.string.noAnnouncementsSubtext)
            } else {
                emptyPandaView.setEmptyViewImage(requireContext().getDrawableCompat(R.drawable.ic_panda_nodiscussions))
                emptyPandaView.setMessageText(R.string.noDiscussionsTeacher)
            }
        }
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, mRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    private fun setupToolbar() = with(binding) {
        discussionListToolbar.title = if(isAnnouncements) getString(R.string.tab_announcements) else getString(R.string.tab_discussions)
        discussionListToolbar.subtitle = canvasContext.name
        discussionListToolbar.setupBackButton(this@DiscussionsListFragment)
        val searchHint = getString(if (isAnnouncements) R.string.searchAnnouncementsHint else R.string.searchDiscussionsHint)
        discussionListToolbar.addSearch(searchHint) { query ->
            if (query.isBlank()) {
                emptyPandaView.emptyViewText(R.string.no_items_to_display_short)
            } else {
                emptyPandaView.emptyViewText(getString(R.string.noItemsMatchingQuery, query))
            }
            presenter.searchQuery = query
        }
        ViewStyler.themeToolbarColored(requireActivity(), discussionListToolbar, canvasContext.color, requireContext().getColor(R.color.textLightest))
    }

    private fun setupViews() = with(binding) {
        createNewDiscussion.setGone()
        createNewDiscussion.backgroundTintList = ViewStyler.makeColorStateListForButton()
        createNewDiscussion.setImageDrawable(ColorUtils.colorIt(ThemePrefs.buttonTextColor, createNewDiscussion.drawable))
        createNewDiscussion.onClickWithRequireNetwork {
            if(isAnnouncements) {
                val route = CreateDiscussionWebViewFragment.makeRoute(canvasContext, true)
                RouteMatcher.route(requireActivity(), route)
            } else {
                val route = CreateDiscussionWebViewFragment.makeRoute(canvasContext, false)
                RouteMatcher.route(requireActivity(), route)
            }
        }
    }

    override fun askToDeleteDiscussionTopicHeader(discussionTopicHeader: DiscussionTopicHeader) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.discussions_delete_title)
        builder.setMessage(R.string.discussions_delete_message)
        builder.setPositiveButton(R.string.delete) { _, _ ->
            presenter.deleteDiscussionTopicHeader(discussionTopicHeader)
        }
        builder.setNegativeButton(R.string.cancel, null)
        builder.showThemed()
    }

    override fun moveToGroup(group: String, discussionTopicHeader: DiscussionTopicHeader) {
        adapter.addOrUpdateItem(group, discussionTopicHeader)
    }

    override fun discussionDeletedSuccessfully(discussionTopicHeader: DiscussionTopicHeader) {
        DiscussionTopicHeaderDeletedEvent(discussionTopicHeader.id, (DiscussionDetailsWebViewFragment::class.java.toString() + ".onPost()")).post()
    }

    override fun displayLoadingError() = toast(R.string.errorOccurred)

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDiscussionCreated(event: DiscussionCreatedEvent) {
        event.once(javaClass.simpleName) {
            // need to set a flag here. Because we use the event bus in the fragment instead of the presenter for unit testing purposes,
            // when we come back to this fragment it will go through the life cycle events again and the cached data will immediately
            // overwrite the data from the network if we refresh the presenter from here.
            needToForceNetwork = true
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDiscussionTopicCountChange(event: DiscussionTopicHeaderEvent) {
        event.get {
            //Gets written over on phones - added also to {@link #onRefreshFinished()}
            adapter.addOrUpdateItem(it)
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDiscussionUpdated(event: DiscussionUpdatedEvent) {
        event.once(javaClass.simpleName) {
            // need to set a flag here. Because we use the event bus in the fragment instead of the presenter for unit testing purposes,
            // when we come back to this fragment it will go through the life cycle events again and the cached data will immediately
            // overwrite the data from the network if we refresh the presenter from here.
            needToForceNetwork = true
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDiscussionTopicHeaderDeleted(event: DiscussionTopicHeaderDeletedEvent) {
        event.get {
            val discussionTopicHeader = adapter.getItem(it)
            if (discussionTopicHeader != null) {
                adapter.removeItem(discussionTopicHeader, true)
                needToForceNetwork = true
                if (adapter.itemCount == 0) {
                    presenter.refresh(true)
                }
            }
        }
    }

    override fun onHandleBackPressed() = binding.discussionListToolbar.closeSearch()

    @PageViewUrlParam("type")
    open fun makePageViewUrl(): String = "discussion_topics"

    companion object {
        fun newInstance(canvasContext: CanvasContext) = DiscussionsListFragment().apply {
            this.canvasContext = canvasContext
        }
    }
}
