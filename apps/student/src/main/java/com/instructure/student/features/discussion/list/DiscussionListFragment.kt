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

package com.instructure.student.features.discussion.list

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.analytics.SCREEN_VIEW_DISCUSSION_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.discussion.DiscussionSharedAction
import com.instructure.pandautils.features.discussion.DiscussionSharedEvents
import com.instructure.pandautils.features.discussion.create.CreateDiscussionWebViewFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.addSearch
import com.instructure.pandautils.utils.closeSearch
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.student.R
import com.instructure.student.databinding.CourseDiscussionTopicBinding
import com.instructure.student.features.discussion.list.adapter.DiscussionListRecyclerAdapter
import com.instructure.student.fragment.ParentFragment
import com.instructure.student.router.RouteMatcher
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_DISCUSSION_LIST)
@PageView(url = "{canvasContext}/discussion_topics")
@AndroidEntryPoint
open class DiscussionListFragment : ParentFragment(), Bookmarkable {

    private val binding by viewBinding(CourseDiscussionTopicBinding::bind)

    @Inject
    lateinit var featureFlagProvider: FeatureFlagProvider

    @Inject
    lateinit var repository: DiscussionListRepository

    @Inject
    lateinit var discussionSharedEvents: DiscussionSharedEvents

    @get:PageViewUrlParam("canvasContext")
    var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    private var recyclerAdapter: DiscussionListRecyclerAdapter? = null

    private val linearLayoutManager by lazy { LinearLayoutManager(requireContext()) }
    private lateinit var discussionRecyclerView: RecyclerView
    private var canPost: Boolean = false
    private var groupsJob: WeaveJob? = null
    private var featureFlagsJob: WeaveJob? = null

    protected open val isAnnouncement: Boolean
        get() = false

    //region Fragment Lifecycle Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkForPermission()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return layoutInflater.inflate(R.layout.course_discussion_topic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            recyclerAdapter = DiscussionListRecyclerAdapter(
                requireContext(),
                canvasContext,
                !isAnnouncement,
                repository,
                lifecycleScope,
                object : DiscussionListRecyclerAdapter.AdapterToDiscussionsCallback {
                    override fun onRowClicked(model: DiscussionTopicHeader, position: Int, isOpenDetail: Boolean) {
                        RouteMatcher.route(
                            requireActivity(),
                            DiscussionRouterFragment.makeRoute(canvasContext, model, isAnnouncement)
                        )
                    }

                    override fun onRefreshFinished() {
                        setRefreshing(false)
                        // Show the FAB.
                        if (canPost) createNewDiscussion.show()
                        if (recyclerAdapter == null || recyclerAdapter?.size() == 0) {
                            emptyView.let {
                                if (isAnnouncement) {
                                    setEmptyView(
                                        it,
                                        R.drawable.ic_panda_noannouncements,
                                        R.string.noAnnouncements,
                                        R.string.noAnnouncementsSubtext
                                    )
                                } else {
                                    setEmptyView(
                                        it,
                                        R.drawable.ic_panda_nodiscussions,
                                        R.string.noDiscussions,
                                        R.string.noDiscussionsSubtext
                                    )
                                }
                            }
                        }
                    }

                    override fun onRefreshStarted() {
                        setRefreshing(true)
                        // Hide the FAB.
                        if (canPost) binding.createNewDiscussion.hide()
                    }
                })

            recyclerAdapter?.let { adapter ->
                this@DiscussionListFragment.discussionRecyclerView = configureRecyclerView(
                    binding.root,
                    requireContext(),
                    adapter,
                    R.id.swipeRefreshLayout,
                    R.id.emptyView,
                    R.id.discussionRecyclerView
                )
            }
            linearLayoutManager.orientation = RecyclerView.VERTICAL

            discussionRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (canPost) {
                        if (dy > 0 && createNewDiscussion.visibility == View.VISIBLE) {
                            createNewDiscussion.hide()
                        } else if (dy < 0 && createNewDiscussion.visibility != View.VISIBLE) {
                            createNewDiscussion.show()
                        }
                    }
                }
            })

            createNewDiscussion.apply {
                setGone()
                backgroundTintList = ViewStyler.makeColorStateListForButton()
                setImageDrawable(ColorUtils.colorIt(ThemePrefs.buttonTextColor, drawable))
                onClickWithRequireNetwork {
                    if (isAnnouncement) {
                        val route = CreateDiscussionWebViewFragment.makeRoute(canvasContext, true)
                        RouteMatcher.route(requireActivity(), route)
                    } else {
                        val route = CreateDiscussionWebViewFragment.makeRoute(canvasContext, false)
                        RouteMatcher.route(requireActivity(), route)
                    }
                }
            }
        }

        lifecycleScope.collectOneOffEvents(discussionSharedEvents.events.flowWithLifecycle(lifecycle)) { handleSharedAction(it) }
    }

    private fun handleSharedAction(action: DiscussionSharedAction) {
        when (action) {
            is DiscussionSharedAction.RefreshListScreen -> {
                recyclerAdapter?.refresh()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) = with(binding) {
        super.onConfigurationChanged(newConfig)
        if (recyclerAdapter == null || recyclerAdapter?.size() == 0) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        featureFlagsJob?.cancel()
        groupsJob?.cancel()
        recyclerAdapter?.cancel()
    }
    //endregion

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)

    //region Fragment Interaction Overrides

    override fun applyTheme() {
        with(binding) {
            setupToolbarMenu(discussionListToolbar)
            discussionListToolbar.title = title()
            discussionListToolbar.setupAsBackButton(this@DiscussionListFragment)
            val searchHint =
                getString(if (isAnnouncement) R.string.searchAnnouncementsHint else R.string.searchDiscussionsHint)
            discussionListToolbar.addSearch(searchHint) { query ->
                if (query.isBlank()) {
                    emptyView.emptyViewText(R.string.noItemsToDisplayShort)
                } else {
                    emptyView.emptyViewText(getString(R.string.noItemsMatchingQuery, query))
                }
                recyclerAdapter?.searchQuery = query
            }
            ViewStyler.themeToolbarColored(requireActivity(), discussionListToolbar, canvasContext)
        }
    }

    override fun title(): String = getString(R.string.discussion)
    //endregion

    //region Parent Fragment Overrides
    override fun getSelectedParamName(): String = RouterParams.MESSAGE_ID

    //endregion

    private fun checkForPermission() {
        lifecycleScope.tryLaunch {
            canPost = repository.getCreationPermission(canvasContext, isAnnouncement)
            if (canPost) {
                if (view != null) binding.createNewDiscussion.show()
            }
        } catch {
            Logger.e("Error getting permissions for discussion permissions. " + it.message)
            if (view != null) binding.createNewDiscussion.hide()
        }
    }

    override fun handleBackPressed() = binding.discussionListToolbar.closeSearch()

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
