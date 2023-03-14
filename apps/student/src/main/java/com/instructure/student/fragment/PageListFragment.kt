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
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.analytics.SCREEN_VIEW_PAGE_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.PageListRecyclerAdapter
import com.instructure.student.databinding.FragmentCoursePagesBinding
import com.instructure.student.events.PageUpdatedEvent
import com.instructure.student.interfaces.AdapterToFragmentCallback
import com.instructure.student.router.RouteMatcher
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

@ScreenView(SCREEN_VIEW_PAGE_LIST)
@PageView(url = "{canvasContext}/pages")
class PageListFragment : ParentFragment(), Bookmarkable {

    private val binding by viewBinding(FragmentCoursePagesBinding::bind)

    private var rootView: View? = null

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    private lateinit var recyclerAdapter: PageListRecyclerAdapter
    private var defaultSelectedPageTitle = PageListRecyclerAdapter.FRONT_PAGE_DETERMINER // blank string is used to determine front page
    private var isShowFrontPage by BooleanArg(key = SHOW_FRONT_PAGE)

    val tabId: String
        get() = Tab.PAGES_ID

    @Suppress("unused")
    @Subscribe
    fun onUpdatePage(event: PageUpdatedEvent) {
        event.once(javaClass.simpleName) {
            recyclerAdapter.refresh()
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
        recyclerAdapter.cancel()
        super.onDestroyView()
    }

    //region Fragment Lifecycle Overrides
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        rootView = layoutInflater.inflate(R.layout.fragment_course_pages, container, false)
        recyclerAdapter = PageListRecyclerAdapter(requireContext(), canvasContext, object : AdapterToFragmentCallback<Page> {
            override fun onRowClicked(page: Page, position: Int, isOpenDetail: Boolean) {
                RouteMatcher.route(requireContext(), PageDetailsFragment.makeRoute(canvasContext, page))
            }

            override fun onRefreshFinished() {
                setRefreshing(false)
                setEmptyView(binding.pagesRecyclerView.emptyView, R.drawable.ic_panda_nofiles, R.string.noPages, R.string.noPagesSubtext)
            }
        }, defaultSelectedPageTitle)

        configureRecyclerView(rootView!!, requireContext(), recyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyView, R.id.listView)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (isShowFrontPage) {
            val route = PageDetailsFragment.makeRoute(canvasContext, Page.FRONT_PAGE_NAME).apply { ignoreDebounce = true}
            RouteMatcher.route(requireContext(), route)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configureRecyclerView(rootView!!, requireContext(), recyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyView, R.id.listView)
        binding.pagesRecyclerView.emptyView.changeTextSize()
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (isTablet) {
                binding.pagesRecyclerView.emptyView.setGuidelines(.24f, .53f, .62f, .12f, .88f)
            } else {
                binding.pagesRecyclerView.emptyView.setGuidelines(.28f, .6f, .73f, .12f, .88f)

            }
        } else {
            if (isTablet) {
                //change nothing, at least for now
            } else {
                binding.pagesRecyclerView.emptyView.setGuidelines(.25f, .7f, .74f, .15f, .85f)
            }
        }
    }
    //endregion

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)

    //region Fragment Interaction Overrides

    override fun applyTheme() {
        with (binding) {
            setupToolbarMenu(toolbar)
            toolbar.title = title()
            toolbar.setupAsBackButton(this@PageListFragment)
            toolbar.addSearch(getString(R.string.searchPagesHint)) { query ->
                if (query.isBlank()) {
                    pagesRecyclerView.emptyView.emptyViewText(R.string.noItemsToDisplayShort)
                } else {
                    pagesRecyclerView.emptyView.emptyViewText(getString(R.string.noItemsMatchingQuery, query))
                }
                recyclerAdapter.searchQuery = query
            }
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, canvasContext)
        }
    }

    override fun handleBackPressed() = binding.toolbar.closeSearch()

    override fun title(): String = getString(R.string.pages)
    //endregion


    //region Parent Fragment Overrides
    override fun getSelectedParamName(): String = RouterParams.PAGE_ID
    //endregion

    companion object {
        const val SHOW_FRONT_PAGE = "isShowFrontPage"

        fun newInstance(route: Route) = if (validRoute(route)) {
            PageListFragment().apply {
                arguments = route.arguments

                route.paramsHash.let {
                    if (it.containsKey(getSelectedParamName())) {
                        defaultSelectedPageTitle = it[getSelectedParamName()]!!
                    } else {
                        isShowFrontPage = false // case when the url is /pages (only meant to go to the page list)
                    }
                }
            }
        } else null

        fun makeRoute(canvasContext: CanvasContext, showHomePage: Boolean): Route =
                Route(PageListFragment::class.java, canvasContext, canvasContext.makeBundle().apply {
                    putBoolean(Const.SHOW_FRONT_PAGE, showHomePage)
                })

        fun validRoute(route: Route) = route.canvasContext != null
    }
}
