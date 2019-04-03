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

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.pageview.BeforePageView
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.pageview.PageViewUrlQuery
import com.instructure.interactions.router.Route
import com.instructure.pandarecycler.PandaRecyclerView
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.getModuleItemId
import com.instructure.pandautils.utils.makeBundle
import com.instructure.student.R
import com.instructure.student.adapter.RubricRecyclerAdapter
import com.instructure.student.decorations.RubricDecorator
import com.instructure.student.interfaces.AdapterToFragmentCallback

@PageView(url = "{canvasContext}/assignments/{assignmentId}/submissions/{submissionId}")
class RubricFragment : ParentFragment() {

    // Bundle Args
    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    private var rubricRecyclerAdapter: RubricRecyclerAdapter? = null

    //region Analytics
    @PageViewUrlParam(name = "assignmentId")
    private val assignmentId: Long =
            if (rubricRecyclerAdapter?.assignment != null) {
                rubricRecyclerAdapter!!.assignment.id
            } else {
                0
            }
    @PageViewUrlParam(name = "submissionId")
    private val submissionId: String =
            if (rubricRecyclerAdapter?.assignment != null && rubricRecyclerAdapter?.assignment?.submission != null) {
                val submissionId = rubricRecyclerAdapter!!.assignment.submission!!.id
                if (submissionId > 0) submissionId.toString() else ""
            } else {
                ""
            }

    @PageViewUrlQuery(name = "module_item_id")
    private val moduleItemId: Long? = this.getModuleItemId()
    //endregion

    //region Fragment Lifecycle Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_list, container, false)
        rootView.findViewById<View>(R.id.fragment_container).setBackgroundColor(Color.WHITE)

        rubricRecyclerAdapter = RubricRecyclerAdapter(requireContext(), canvasContext, object : AdapterToFragmentCallback<Any> {
            override fun onRowClicked(o: Any, position: Int, isOpenDetail: Boolean) {}

            override fun onRefreshFinished() {
                setRefreshing(false)
            }
        })

        configureRecyclerView(rootView, requireContext(), rubricRecyclerAdapter!!, R.id.swipeRefreshLayout, R.id.emptyView, R.id.listView)
        val pandaRecyclerView = rootView.findViewById<View>(R.id.listView) as PandaRecyclerView
        pandaRecyclerView.addItemDecoration(RubricDecorator(context))

        return rootView
    }
    //endregion

    //region Fragment Interacton Overrides
    override fun title(): String = getString(R.string.grades)

    override fun applyTheme() {}

    //endregion

    //region Setup
    /**
     * Since this is a nested fragment, the ViewPager calls this method
     *
     * For explanation of isWithinAnotherCallback and isCached refer to comment in [com.instructure.student.activity.CallbackActivity]
     */
    @BeforePageView
    fun setAssignment(assignment: Assignment) {
        rubricRecyclerAdapter?.let {
            it.assignment = assignment
            it.loadDataChained()
        }
    }
    //endregion

    companion object {
        val tabTitle: Int
            get() = R.string.assignmentTabGrade

        fun makeRoute(canvasContext: CanvasContext): Route = Route(null, canvasContext, Bundle())

        fun newInstance(route: Route) = if (validRoute(route)) {
            RubricFragment().apply {
                arguments = route.canvasContext!!.makeBundle(route.arguments)
                canvasContext = route.canvasContext!!
            }
        } else null

        private fun validRoute(route: Route) = route.canvasContext != null

    }
}
