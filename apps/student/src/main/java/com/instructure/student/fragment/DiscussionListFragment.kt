/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.analytics.SCREEN_VIEW_DISCUSSION_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.DiscussionListRecyclerAdapter
import com.instructure.student.events.DiscussionCreatedEvent
import com.instructure.student.events.DiscussionTopicHeaderDeletedEvent
import com.instructure.student.events.DiscussionTopicHeaderEvent
import com.instructure.student.events.DiscussionUpdatedEvent
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.course_discussion_topic.*
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@ScreenView(SCREEN_VIEW_DISCUSSION_LIST)
@PageView(url = "{canvasContext}/discussion_topics")
open class DiscussionListFragment : ParentFragment(), Bookmarkable {
    protected var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    private lateinit var recyclerAdapter: DiscussionListRecyclerAdapter

    private val linearLayoutManager by lazy { LinearLayoutManager(requireContext()) }
    private lateinit var discussionRecyclerView: RecyclerView
    private lateinit var rootView: View
    private var permissionJob: Job? = null
    private var canPost: Boolean = false
    private var groupsJob: WeaveJob? = null

    protected open val isAnnouncement: Boolean
        get() = false

    //region Fragment Lifecycle Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkForPermission()
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        rootView = layoutInflater.inflate(R.layout.course_discussion_topic, container, false)
        recyclerAdapter = DiscussionListRecyclerAdapter(requireContext(), canvasContext, !isAnnouncement, object: DiscussionListRecyclerAdapter.AdapterToDiscussionsCallback{
            override fun onRowClicked(model: DiscussionTopicHeader, position: Int, isOpenDetail: Boolean) {
                // If the discussion/announcement hasn't been published take them back to the publish screen
                if (model.groupTopicChildren.isNotEmpty()) {
                    groupsJob = tryWeave {
                        DiscussionDetailsFragment.getDiscussionGroup(model)?.let {
                            RouteMatcher.route(requireActivity(), DiscussionDetailsFragment.makeRoute(it.first, it.second, groupDiscussion = true))
                        } ?: RouteMatcher.route(requireActivity(), DiscussionDetailsFragment.makeRoute(canvasContext, model))
                    }.catch {  }
                } else {
                    RouteMatcher.route(requireActivity(), DiscussionDetailsFragment.makeRoute(canvasContext, model))
                }
            }

            override fun onRefreshFinished() {
                setRefreshing(false)
                // Show the FAB.
                if(canPost) createNewDiscussion?.show()
                if (recyclerAdapter.size() == 0) {
                    if (isAnnouncement) {
                        setEmptyView(emptyView, R.drawable.ic_panda_noannouncements, R.string.noAnnouncements, R.string.noAnnouncementsSubtext)
                    } else {
                        setEmptyView(emptyView, R.drawable.ic_panda_nodiscussions, R.string.noDiscussions, R.string.noDiscussionsSubtext)
                    }
                }
            }

            override fun onRefreshStarted() {
                setRefreshing(true)
                // Hide the FAB.
                if(canPost) createNewDiscussion?.hide()
            }

            override fun discussionOverflow(group: String?, discussionTopicHeader: DiscussionTopicHeader) {
                if(group != null) {
                    // TODO - Blocked by COMMS-868
//                    DiscussionsMoveToDialog.show(requireFragmentManager(), group, discussionTopicHeader, { newGroup ->
//                        recyclerAdapter.requestMoveDiscussionTopicToGroup(newGroup, group, discussionTopicHeader)
//                    })
                }
            }

            override fun askToDeleteDiscussion(discussionTopicHeader: DiscussionTopicHeader) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.utils_discussionsDeleteTitle)
                    .setMessage(R.string.utils_discussionsDeleteMessage)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        recyclerAdapter.deleteDiscussionTopicHeader(discussionTopicHeader)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .showThemed()
            }
        })

        discussionRecyclerView = configureRecyclerView(
                rootView,
                requireContext(),
                recyclerAdapter,
                R.id.swipeRefreshLayout,
                R.id.emptyView,
                R.id.discussionRecyclerView
                )
        linearLayoutManager.orientation = RecyclerView.VERTICAL

        discussionRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(canPost) {
                    if (dy > 0 && createNewDiscussion.visibility == View.VISIBLE) {
                        createNewDiscussion?.hide()
                    } else if (dy < 0 && createNewDiscussion.visibility != View.VISIBLE) {
                        createNewDiscussion?.show()
                    }
                }
            }
        })

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createNewDiscussion.setGone()
        createNewDiscussion.backgroundTintList = ViewStyler.makeColorStateListForButton()
        createNewDiscussion.setImageDrawable(ColorUtils.colorIt(ThemePrefs.buttonTextColor, createNewDiscussion.drawable))
        createNewDiscussion.onClickWithRequireNetwork {
            if(isAnnouncement) {
                val route = CreateAnnouncementFragment.makeRoute(canvasContext, null)
                RouteMatcher.route(requireActivity(), route)
            } else {
                val route = CreateDiscussionFragment.makeRoute(canvasContext)
                RouteMatcher.route(requireActivity(), route)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (recyclerAdapter.size() == 0) {
            emptyView.changeTextSize()
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (isTablet) {
                    emptyView.setGuidelines(.24f, .53f, .62f, .12f, .88f)
                } else {
                    emptyView.setGuidelines(.28f, .6f, .73f, .12f, .88f)

                }
            } else {
                if (isTablet) {
                    //change nothing, at least for now
                } else {
                    emptyView.setGuidelines(.25f, .7f, .74f, .15f, .85f)
                }
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        permissionJob?.cancel()
        recyclerAdapter.cancel()
    }
    //endregion

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)

    //region Fragment Interaction Overrides

    override fun applyTheme() {
        setupToolbarMenu(discussionListToolbar)
        discussionListToolbar.title = title()
        discussionListToolbar.setupAsBackButton(this)
        val searchHint = getString(if (isAnnouncement) R.string.searchAnnouncementsHint else R.string.searchDiscussionsHint)
        discussionListToolbar.addSearch(searchHint) { query ->
            if (query.isBlank()) {
                emptyView?.emptyViewText(R.string.noItemsToDisplayShort)
            } else {
                emptyView?.emptyViewText(getString(R.string.noItemsMatchingQuery, query))
            }
            recyclerAdapter.searchQuery = query
        }
        ViewStyler.themeToolbar(requireActivity(), discussionListToolbar, canvasContext)
    }

    override fun title(): String = getString(R.string.discussion)
    //endregion

    //region Parent Fragment Overrides
    override fun getSelectedParamName(): String = RouterParams.MESSAGE_ID

    //endregion

    private fun checkForPermission() {
        permissionJob = tryWeave {
            val permission = if(canvasContext.isCourse) {
                awaitApi<Course> { CourseManager.getCourse(
                        canvasContext.id,
                        it,
                        true
                )}.permissions
            } else {
                awaitApi<Group> { GroupManager.getDetailedGroup(
                        canvasContext.id,
                        it,
                        true
                )}.permissions
            }

            this@DiscussionListFragment.canvasContext.permissions = permission
            canPost = if(isAnnouncement) {
                permission?.canCreateAnnouncement ?: false
            } else {
                permission?.canCreateDiscussionTopic ?: false
            }
            if(canPost) {
                createNewDiscussion?.show()
            }
        } catch {
            Logger.e("Error getting permissions for discussion permissions. " + it.message)
            createNewDiscussion?.hide()
        }
    }

    //region Bus Events
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDiscussionUpdated(event: DiscussionUpdatedEvent) {
        event.once(javaClass.simpleName) {
            recyclerAdapter.refresh()
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDiscussionTopicHeaderDeleted(event: DiscussionTopicHeaderDeletedEvent) {
        event.get {
            // TODO - COMMS-868
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDiscussionTopicCountChange(event: DiscussionTopicHeaderEvent) {
        event.get {
            // Gets written over on phones - added also to {@link #onRefreshFinished()}
            when {
                it.pinned -> {
                    recyclerAdapter.addOrUpdateItem(DiscussionListRecyclerAdapter.PINNED, it)
                }
                it.locked -> {
                    recyclerAdapter.addOrUpdateItem(DiscussionListRecyclerAdapter.CLOSED_FOR_COMMENTS, it)
                }
                else -> {
                    recyclerAdapter.addOrUpdateItem(DiscussionListRecyclerAdapter.UNPINNED, it)
                }
            }
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDiscussionCreated(event: DiscussionCreatedEvent) {
        event.once(javaClass.simpleName) {
            recyclerAdapter.refresh()
        }
    }
    //endregion

    override fun handleBackPressed() = discussionListToolbar.closeSearch()

    companion object {
        fun newInstance(route: Route) =
                if (validateRoute(route)) {
                    DiscussionListFragment().apply {
                        arguments = route.canvasContext!!.makeBundle(route.arguments)
                    }
                } else null

        fun makeRoute(canvasContext: CanvasContext?) =
                Route(DiscussionListFragment::class.java, canvasContext)

        private fun validateRoute(route: Route) = route.canvasContext != null
    }
}
