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
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandarecycler.BaseRecyclerAdapter
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.SyllabusRecyclerAdapter
import com.instructure.student.interfaces.AdapterToFragmentCallback
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.FeatureFlags
import kotlinx.android.synthetic.main.fragment_list_syllabus.*

@PageView(url = "{canvasContext}/assignments/syllabus")
class ScheduleListFragment : ParentFragment(), Bookmarkable {

    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)
    private var isSyllabus by BooleanArg(key = Const.ADD_SYLLABUS)

    private var mAdapterToFragmentCallback: AdapterToFragmentCallback<ScheduleItem>? = null
    private var mRecyclerAdapter: BaseRecyclerAdapter<*>? = null

    val tabId: String
        get() = Tab.SYLLABUS_ID

    override fun title(): String = getString(R.string.syllabus)

    override fun getSelectedParamName(): String = RouterParams.EVENT_ID

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_list_syllabus, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mAdapterToFragmentCallback = object : AdapterToFragmentCallback<ScheduleItem> {
            override fun onRowClicked(scheduleItem: ScheduleItem, position: Int, isOpenDetail: Boolean) {
                when {
                    scheduleItem.assignment != null -> {
                        if (FeatureFlags.newAssignmentPage) {
                            RouteMatcher.route(requireContext(), AssignmentDetailsFragment.makeRoute(canvasContext, scheduleItem.assignment!!.id))
                        } else {
                            RouteMatcher.route(requireContext(), AssignmentFragment.makeRoute(canvasContext, scheduleItem.assignment!!))
                        }
                    }
                    scheduleItem.itemType == ScheduleItem.Type.TYPE_SYLLABUS -> RouteMatcher.route(requireContext(), SyllabusFragment.makeRoute(canvasContext, scheduleItem))
                    else -> RouteMatcher.route(requireContext(), CalendarEventFragment.makeRoute(canvasContext, scheduleItem))
                }
            }

            override fun onRefreshFinished() {
                setRefreshing(false)
            }
        }

        mRecyclerAdapter = SyllabusRecyclerAdapter(requireContext(), canvasContext, mAdapterToFragmentCallback)
        configureRecyclerView(view, requireContext(), mRecyclerAdapter!!, R.id.swipeRefreshLayout, R.id.emptyView, R.id.listView)
    }

    override fun applyTheme() {
        setupToolbarMenu(toolbar)
        toolbar.title = title()
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        view?.let { configureRecyclerView(it, requireContext(), mRecyclerAdapter!!, R.id.swipeRefreshLayout, R.id.emptyView, R.id.listView) }
    }

    override fun onResume() {
        super.onResume()
        (mRecyclerAdapter as? SyllabusRecyclerAdapter)?.setupCallbacks()
    }

    override fun onPause() {
        super.onPause()
        (mRecyclerAdapter as? SyllabusRecyclerAdapter)?.removeCallbacks()
    }

    override val bookmark: Bookmarker
        get() = Bookmarker(isSyllabus, canvasContext)

    companion object {

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext, tab: Tab): Route = makeRoute(canvasContext, tab.tabId == Tab.SYLLABUS_ID)

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext, addSyllabus: Boolean): Route {
            val bundle = Bundle().apply { putBoolean(Const.ADD_SYLLABUS, addSyllabus) }
            return Route(null, ScheduleListFragment::class.java, canvasContext, bundle)
        }

        private fun validateRoute(route: Route): Boolean = route.canvasContext != null

        @JvmStatic
        fun newInstance(route: Route): ScheduleListFragment? {
            if (!validateRoute(route)) return null
            return ScheduleListFragment().withArgs(route.canvasContext!!.makeBundle(route.arguments))
        }
    }
}
