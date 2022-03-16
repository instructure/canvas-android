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

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.StreamItem.Type.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_NOTIFICATION_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.activity.ParentActivity
import com.instructure.student.adapter.NotificationListRecyclerAdapter
import com.instructure.student.interfaces.NotificationAdapterToFragmentCallback
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.mobius.conferences.conference_list.ui.ConferenceListFragment
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.fragment_list_notification.*
import kotlinx.android.synthetic.main.panda_recycler_refresh_layout.*

@ScreenView(SCREEN_VIEW_NOTIFICATION_LIST)
@PageView
class NotificationListFragment : ParentFragment(), Bookmarkable {

    @PageViewUrl
    @Suppress("unused")
    fun makePageViewUrl(): String {
        val url = ApiPrefs.fullDomain
        if (canvasContext.isUser) return url
        return url + canvasContext.toAPIString()
    }

    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    private lateinit var recyclerAdapter: NotificationListRecyclerAdapter

    private var adapterToFragmentCallback: NotificationAdapterToFragmentCallback<StreamItem> =
        object : NotificationAdapterToFragmentCallback<StreamItem> {
            override fun onRowClicked(streamItem: StreamItem, position: Int, isOpenDetail: Boolean) {
                recyclerAdapter.setSelectedPosition(position)
                onRowClick(streamItem)
            }

            override fun onRefreshFinished() {
                setRefreshing(false)
                editOptions.setGone()
                if (recyclerAdapter.size() == 0) {
                    setEmptyView(emptyView, R.drawable.ic_panda_noalerts, R.string.noNotifications, R.string.noNotificationsSubtext)
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        emptyView.setGuidelines(.2f, .7f, .74f, .15f, .85f)
                    } else {
                        emptyView.setGuidelines(.28f,.6f,.73f,.12f,.88f)
                    }
                }
            }

            override fun onShowEditView(isVisible: Boolean) {
                editOptions.setVisible(isVisible)
            }

            override fun onShowErrorCrouton(message: Int) {
                showToast(message)
            }
        }

    // Used to help determine if the bottom bar should be highlighted
    fun isCourseOrGroup(): Boolean = canvasContext.isCourseOrGroup


    override fun title(): String = getString(if (canvasContext.isCourse || canvasContext.isGroup) R.string.homePageIdForNotifications else R.string.notifications)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = layoutInflater.inflate(R.layout.fragment_list_notification, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerAdapter = NotificationListRecyclerAdapter(requireContext(), canvasContext, adapterToFragmentCallback)
        configureRecyclerView(
            view,
            requireContext(),
            recyclerAdapter,
            R.id.swipeRefreshLayout,
            R.id.emptyView,
            R.id.listView
        )

        listView.isSelectionEnabled = false

        confirmButton.text = getString(R.string.delete)
        confirmButton.setOnClickListener { recyclerAdapter.confirmButtonClicked() }

        cancelButton.text = getString(R.string.cancel)
        cancelButton.setOnClickListener { recyclerAdapter.cancelButtonClicked() }
    }

    override fun onDestroyView() {
        recyclerAdapter.cancel()
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toolbar.title = title()
    }

    override fun applyTheme() {
        val canvasContext = canvasContext
        if (canvasContext is Course || canvasContext is Group) {
            toolbar.setupAsBackButton(this)
            ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
        } else {
            val navigation = navigation
            navigation?.attachNavigationDrawer(this, toolbar!!)
            // Styling done in attachNavigationDrawer
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configureRecyclerView(
            requireView(),
            requireContext(),
            recyclerAdapter,
            R.id.swipeRefreshLayout,
            R.id.emptyView,
            R.id.listView,
                R.string.noNotifications
        )
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
                    // Change nothing, at least for now
                } else {
                    emptyView.setGuidelines(.2f, .7f, .74f, .15f, .85f)
                }
            }
        }
    }

    fun onRowClick(streamItem: StreamItem): Boolean {
        // This will crash if the course/group is null. This could happen if the api returns items for a concluded course.
        if (streamItem.canvasContext == null && streamItem.contextType != CanvasContext.Type.USER) {
            if (streamItem.contextType == CanvasContext.Type.COURSE) {
                toast(R.string.could_not_find_course)
            } else if (streamItem.contextType == CanvasContext.Type.GROUP) {
                toast(R.string.could_not_find_group)
            }
            return false
        }
        addFragmentForStreamItem(streamItem, activity as ParentActivity, false)
        return true
    }

    override val bookmark: Bookmarker
        get() = Bookmarker(canvasContext.isCourseOrGroup, canvasContext)

    companion object {
        fun addFragmentForStreamItem(streamItem: StreamItem, context: Context, fromWidget: Boolean) {
            if (fromWidget) {
                RouteMatcher.routeUrl(context, streamItem.url ?: streamItem.htmlUrl) // If we get null URLs, we can't route, so the behavior will just launch the app to whatever screen they were on last
                return
            }

            if (streamItem.getStreamItemType() == CONVERSATION) {
                val conversation = streamItem.conversation
                if (conversation != null) {
                    // Check to see if the conversation has been deleted.
                    if (conversation.isDeleted) {
                        Toast.makeText(context, R.string.deleteConversation, Toast.LENGTH_SHORT).show()
                    } else {
                        RouteMatcher.route(context, InboxConversationFragment.makeRoute(conversation, null))
                    }
                }
                return
            }

            val canvasContext = streamItem.canvasContext ?: return

            val route: Route? = when (streamItem.getStreamItemType()) {
                SUBMISSION -> {
                    if (canvasContext !is Course) return

                    if (streamItem.assignment == null) {
                        RouteMatcher.route(context, AssignmentDetailsFragment.makeRoute(canvasContext, streamItem.assignmentId))
                    } else {
                        // Add an empty submission with the grade to the assignment so that we can see the score.
                        streamItem.assignment?.submission = Submission(grade = streamItem.grade)
                        RouteMatcher.route(context, AssignmentDetailsFragment.makeRoute(canvasContext, streamItem.assignment!!.id))
                    }
                    null
                }
                ANNOUNCEMENT, DISCUSSION_TOPIC -> {
                    val route = DiscussionDetailsFragment.makeRoute(canvasContext, streamItem.discussionTopicId)
                    RouteMatcher.route(context, route)
                    null
                }
                MESSAGE -> {
                    if (streamItem.assignmentId > 0) {
                        AssignmentDetailsFragment.makeRoute(canvasContext, streamItem.assignmentId)
                    } else {
                        UnknownItemFragment.makeRoute(canvasContext, streamItem)
                    }
                }
                COLLABORATION -> UnsupportedTabFragment.makeRoute(canvasContext, Tab.COLLABORATIONS_ID)
                CONFERENCE -> ConferenceListFragment.makeRoute(canvasContext)
                else -> UnsupportedFeatureFragment.makeRoute(canvasContext, featureName = streamItem.type, url = streamItem.url ?: streamItem.htmlUrl)
            }

            if (route != null) RouteMatcher.route(context, route)
        }

        fun makeRoute(canvasContext: CanvasContext): Route = Route(NotificationListFragment::class.java, canvasContext, Bundle())

        private fun validateRoute(route: Route) = route.canvasContext != null

        fun newInstance(route: Route): NotificationListFragment? {
            if (!validateRoute(route)) return null
            return NotificationListFragment().withArgs(route.canvasContext!!.makeBundle())
        }
    }
}
