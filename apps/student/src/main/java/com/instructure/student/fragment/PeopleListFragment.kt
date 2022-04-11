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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_PEOPLE_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.PeopleListRecyclerAdapter
import com.instructure.student.interfaces.AdapterToFragmentCallback
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.fragment_people_list.*

@ScreenView(SCREEN_VIEW_PEOPLE_LIST)
@PageView(url = "{canvasContext}/users")
class PeopleListFragment : ParentFragment(), Bookmarkable {

    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    private var recyclerAdapter: PeopleListRecyclerAdapter? = null

    private var adapterToFragmentCallback = object : AdapterToFragmentCallback<User> {
        override fun onRowClicked(user: User, position: Int, isOpenDetail: Boolean) {
            if (canvasContext.isCourse) {
                RouteMatcher.route(requireContext(), PeopleDetailsFragment.makeRoute(user.id, canvasContext))
            } else {
                RouteMatcher.route(requireContext(), PeopleDetailsFragment.makeRoute(user, canvasContext))
            }
        }

        override fun onRefreshFinished() {
            setRefreshing(false)
        }
    }

    override fun title(): String = getString(R.string.coursePeople)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_people_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerAdapter = PeopleListRecyclerAdapter(requireContext(), canvasContext, adapterToFragmentCallback)
        configureRecyclerView(
            view,
            requireContext(),
            recyclerAdapter!!,
            R.id.swipeRefreshLayout,
            R.id.emptyView,
            R.id.listView
        )
    }

    override fun applyTheme() {
        toolbar.title = title()
        toolbar.setupAsBackButton(this)
        setupToolbarMenu(toolbar)
        ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
    }

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)

    override fun onDestroyView() {
        recyclerAdapter?.cancel()
        super.onDestroyView()
    }

    companion object {

        fun makeRoute(canvasContext: CanvasContext): Route {
            return Route(PeopleListFragment::class.java, canvasContext, Bundle())
        }

        private fun validateRoute(route: Route) = route.canvasContext != null

        fun newInstance(route: Route): PeopleListFragment? {
            if (!validateRoute(route)) return null
            return PeopleListFragment().withArgs(route.canvasContext!!.makeBundle())
        }
    }
}
