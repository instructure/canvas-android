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
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.StreamItem
import com.instructure.canvasapi2.models.StreamItem.Type.ANNOUNCEMENT
import com.instructure.canvasapi2.models.StreamItem.Type.COLLABORATION
import com.instructure.canvasapi2.models.StreamItem.Type.CONFERENCE
import com.instructure.canvasapi2.models.StreamItem.Type.CONVERSATION
import com.instructure.canvasapi2.models.StreamItem.Type.DISCUSSION_MENTION
import com.instructure.canvasapi2.models.StreamItem.Type.DISCUSSION_TOPIC
import com.instructure.canvasapi2.models.StreamItem.Type.MESSAGE
import com.instructure.canvasapi2.models.StreamItem.Type.SUBMISSION
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_NOTIFICATION_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.features.inbox.details.InboxDetailsFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyBottomSystemBarInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.isCourse
import com.instructure.pandautils.utils.isCourseOrGroup
import com.instructure.pandautils.utils.isGroup
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.isUser
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.R
import com.instructure.student.activity.ParentActivity
import com.instructure.student.adapter.NotificationListRecyclerAdapter
import com.instructure.student.databinding.FragmentListNotificationBinding
import com.instructure.student.databinding.PandaRecyclerRefreshLayoutBinding
import com.instructure.student.interfaces.NotificationAdapterToFragmentCallback
import com.instructure.student.mobius.conferences.conference_list.ui.ConferenceListRepositoryFragment
import com.instructure.student.router.RouteMatcher

@ScreenView(SCREEN_VIEW_NOTIFICATION_LIST)
@PageView
class NotificationListFragment : ParentFragment(), Bookmarkable, FragmentManager.OnBackStackChangedListener {

    private val binding by viewBinding(FragmentListNotificationBinding::bind)
    private lateinit var recyclerBinding: PandaRecyclerRefreshLayoutBinding

    @PageViewUrl
    @Suppress("unused")
    fun makePageViewUrl(): String {
        val url = ApiPrefs.fullDomain
        if (canvasContext.isUser) return url
        return url + canvasContext.toAPIString()
    }

    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    private var recyclerAdapter: NotificationListRecyclerAdapter? = null

    private var adapterToFragmentCallback: NotificationAdapterToFragmentCallback<StreamItem> =
        object : NotificationAdapterToFragmentCallback<StreamItem> {
            override fun onRowClicked(streamItem: StreamItem, position: Int, isOpenDetail: Boolean) {
                recyclerAdapter?.setSelectedPosition(position)
                onRowClick(streamItem)
            }

            override fun onRefreshFinished() {
                setRefreshing(false)
                binding.editOptions.setGone()
                if (recyclerAdapter?.size() == 0) {
                    setEmptyView(recyclerBinding.emptyView, R.drawable.ic_panda_noalerts, R.string.noNotifications, R.string.noNotificationsSubtext)
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        recyclerBinding.emptyView.setGuidelines(.2f, .7f, .74f, .15f, .85f)
                    } else {
                        recyclerBinding.emptyView.setGuidelines(.28f,.6f,.73f,.12f,.88f)
                    }
                }
                (activity as? OnNotificationCountInvalidated)?.invalidateNotificationCount()
            }

            override fun onShowEditView(isVisible: Boolean) {
                binding.editOptions.setVisible(isVisible)
            }

            override fun onShowErrorCrouton(message: Int) {
                showToast(message)
            }

            override fun onItemRemoved() {
                (activity as? OnNotificationCountInvalidated)?.invalidateNotificationCount()
            }
        }

    // Used to help determine if the bottom bar should be highlighted
    fun isCourseOrGroup(): Boolean = canvasContext.isCourseOrGroup

    override fun title(): String = getString(if (canvasContext.isCourse || canvasContext.isGroup) R.string.homePageIdForNotifications else R.string.notifications)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = layoutInflater.inflate(R.layout.fragment_list_notification, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerBinding = PandaRecyclerRefreshLayoutBinding.bind(binding.root)
        recyclerAdapter = NotificationListRecyclerAdapter(requireContext(), canvasContext, adapterToFragmentCallback)
        recyclerAdapter?.let {
            configureRecyclerView(
                view,
                requireContext(),
                it,
                R.id.swipeRefreshLayout,
                R.id.emptyView,
                R.id.listView
            )
        }

        recyclerBinding.listView.isSelectionEnabled = false
        recyclerBinding.swipeRefreshLayout.applyBottomSystemBarInsets()

        binding.confirmButton.text = getString(R.string.delete)
        binding.confirmButton.setOnClickListener { recyclerAdapter?.confirmButtonClicked() }
        binding.cancelButton.text = getString(R.string.cancel)
        binding.cancelButton.setOnClickListener { recyclerAdapter?.cancelButtonClicked() }

        applyTheme()

        activity?.supportFragmentManager?.addOnBackStackChangedListener(this)
    }

    private var shouldRefreshOnResume = false

    override fun onBackStackChanged() {
        if (activity?.supportFragmentManager?.fragments?.lastOrNull()?.javaClass == this.javaClass) {
            if (shouldRefreshOnResume) {
                recyclerBinding.swipeRefreshLayout.isRefreshing = true
                recyclerAdapter?.refresh()
                shouldRefreshOnResume = false
            }
        }
    }

    override fun onDestroyView() {
        recyclerAdapter?.cancel()
        activity?.supportFragmentManager?.removeOnBackStackChangedListener(this)
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.toolbar.title = title()
    }

    override fun applyTheme() {
        val canvasContext = canvasContext
        if (canvasContext is Course || canvasContext is Group) {
            binding.toolbar.setupAsBackButton(this)
            binding.toolbar.applyTopSystemBarInsets()
            ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, canvasContext)
        } else {
            val navigation = navigation
            navigation?.attachNavigationDrawer(this, binding.toolbar)
            binding.toolbar.applyTopSystemBarInsets()
            // Styling done in attachNavigationDrawer
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recyclerAdapter?.let {
            configureRecyclerView(
                requireView(),
                requireContext(),
                it,
                R.id.swipeRefreshLayout,
                R.id.emptyView,
                R.id.listView,
                R.string.noNotifications
            )
        }
        if (recyclerAdapter?.size() == 0) {
            recyclerBinding.emptyView.changeTextSize()
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (isTablet) {
                    recyclerBinding.emptyView.setGuidelines(.24f, .53f, .62f, .12f, .88f)
                } else {
                    recyclerBinding.emptyView.setGuidelines(.28f, .6f, .73f, .12f, .88f)

                }
            } else {
                if (isTablet) {
                    // Change nothing, at least for now
                } else {
                    recyclerBinding.emptyView.setGuidelines(.2f, .7f, .74f, .15f, .85f)
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
        shouldRefreshOnResume = !streamItem.isReadState
        return true
    }

    override val bookmark: Bookmarker
        get() = Bookmarker(canvasContext.isCourseOrGroup, canvasContext)

    interface OnNotificationCountInvalidated {
        fun invalidateNotificationCount()
    }

    companion object {
        fun addFragmentForStreamItem(streamItem: StreamItem, activity: FragmentActivity, fromWidget: Boolean) {
            if (fromWidget) {
                RouteMatcher.routeUrl(activity, streamItem.url ?: streamItem.htmlUrl)
                // If we get null URLs, we can't route, so the behavior will just launch the app to whatever screen they were on last
                return
            }

            if (streamItem.getStreamItemType() == CONVERSATION) {
                val conversation = streamItem.conversation
                if (conversation != null) {
                    // Check to see if the conversation has been deleted.
                    if (conversation.isDeleted) {
                        Toast.makeText(activity, R.string.deleteConversation, Toast.LENGTH_SHORT).show()
                    } else {
                        RouteMatcher.route(activity, InboxDetailsFragment.makeRoute(conversation.id))
                    }
                }
                return
            }

            val canvasContext = streamItem.canvasContext ?: return

            val route: Route? = when (streamItem.getStreamItemType()) {
                SUBMISSION -> {
                    if (canvasContext !is Course) return

                    val assignment = streamItem.assignment

                    if (assignment == null) {
                        RouteMatcher.route(activity, AssignmentDetailsFragment.makeRoute(canvasContext, streamItem.assignmentId))
                    } else {
                        // Add an empty submission with the grade to the assignment so that we can see the score.
                        assignment.submission = Submission(grade = streamItem.grade)
                        val assignmentId = assignment.discussionTopicHeader?.assignmentId ?: assignment.id
                        RouteMatcher.route(activity, AssignmentDetailsFragment.makeRoute(canvasContext, assignmentId))
                    }
                    null
                }
                ANNOUNCEMENT, DISCUSSION_TOPIC -> {
                    val route = DiscussionRouterFragment.makeRoute(canvasContext, streamItem.discussionTopicId)
                    RouteMatcher.route(activity, route)
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
                CONFERENCE -> ConferenceListRepositoryFragment.makeRoute(canvasContext)
                DISCUSSION_MENTION -> {
                    if (streamItem.htmlUrl.isNotEmpty()) {
                        RouteMatcher.getInternalRoute(streamItem.htmlUrl, ApiPrefs.domain)
                    } else {
                        UnknownItemFragment.makeRoute(canvasContext, streamItem)
                    }
                }
                else -> UnsupportedFeatureFragment.makeRoute(canvasContext, featureName = streamItem.type, url = streamItem.url ?: streamItem.htmlUrl)
            }

            if (route != null) RouteMatcher.route(activity, route)
        }

        fun makeRoute(canvasContext: CanvasContext): Route = Route(NotificationListFragment::class.java, canvasContext, Bundle())

        private fun validateRoute(route: Route) = route.canvasContext != null

        fun newInstance(route: Route): NotificationListFragment? {
            if (!validateRoute(route)) return null
            return NotificationListFragment().withArgs(route.canvasContext!!.makeBundle())
        }
    }
}
