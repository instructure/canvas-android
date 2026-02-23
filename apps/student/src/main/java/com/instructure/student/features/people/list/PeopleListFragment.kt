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

package com.instructure.student.features.people.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_PEOPLE_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyBottomSystemBarInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.isCourse
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.R
import com.instructure.student.databinding.FragmentPeopleListBinding
import com.instructure.student.features.people.details.PeopleDetailsFragment
import com.instructure.student.fragment.ParentFragment
import com.instructure.student.interfaces.AdapterToFragmentCallback
import com.instructure.student.router.RouteMatcher
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_PEOPLE_LIST)
@PageView(url = "{canvasContext}/users")
@AndroidEntryPoint
class PeopleListFragment : ParentFragment(), Bookmarkable {

    @Inject
    lateinit var repository: PeopleListRepository

    private val binding by viewBinding(FragmentPeopleListBinding::bind)

    @get:PageViewUrlParam("canvasContext")
    var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    private var recyclerAdapter: PeopleListRecyclerAdapter? = null

    private var adapterToFragmentCallback = object : AdapterToFragmentCallback<User> {
        override fun onRowClicked(user: User, position: Int, isOpenDetail: Boolean) {
            if (canvasContext.isCourse) {
                RouteMatcher.route(requireActivity(), PeopleDetailsFragment.makeRoute(user.id, canvasContext))
            } else {
                RouteMatcher.route(requireActivity(), PeopleDetailsFragment.makeRoute(user, canvasContext))
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
        super.onViewCreated(view, savedInstanceState)
        recyclerAdapter = PeopleListRecyclerAdapter(requireContext(), lifecycleScope, repository, canvasContext, adapterToFragmentCallback)
        configureRecyclerView(
            view,
            requireContext(),
            recyclerAdapter!!,
            R.id.swipeRefreshLayout,
            R.id.emptyView,
            R.id.listView
        )
        view.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefreshLayout)?.applyBottomSystemBarInsets()
    }

    override fun applyTheme() {
        with(binding) {
            toolbar.title = title()
            toolbar.setupAsBackButton(this@PeopleListFragment)
            toolbar.applyTopSystemBarInsets()
            setupToolbarMenu(toolbar)
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, canvasContext)
        }
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
