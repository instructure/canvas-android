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
import android.widget.AdapterView
import com.google.android.material.appbar.AppBarLayout
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.AssignmentDateListRecyclerAdapter
import com.instructure.student.adapter.TermSpinnerAdapter
import com.instructure.student.interfaces.AdapterToAssignmentsCallback
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.assignment_list_layout.*

@PageView(url = "{canvasContext}/assignments")
class AssignmentListFragment : ParentFragment(), Bookmarkable {

    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    private lateinit var recyclerAdapter: AssignmentDateListRecyclerAdapter
    private var termAdapter: TermSpinnerAdapter? = null

    private val allTermsGradingPeriod by lazy {
        GradingPeriod().apply { title = getString(R.string.allGradingPeriods) }
    }

    private val adapterToAssignmentsCallback = object : AdapterToAssignmentsCallback {
        override fun setTermSpinnerState(isEnabled: Boolean) {
            termSpinner?.isEnabled = isEnabled
            termAdapter?.isLoading = !isEnabled
            termAdapter?.notifyDataSetChanged()
        }

        override fun gradingPeriodsFetched(periods: List<GradingPeriod>) {
            setupGradingPeriods(periods)
        }

        override fun onRowClicked(assignment: Assignment, position: Int, isOpenDetail: Boolean) {
            RouteMatcher.route(requireContext(), AssignmentDetailsFragment.makeRoute(canvasContext, assignment.id))
        }

        override fun onRefreshFinished() {
            if(!isAdded) return // Refresh can finish after user has left screen, causing emptyView to be null
            setRefreshing(false)
            if (recyclerAdapter.size() == 0) {
                setEmptyView(emptyView, R.drawable.vd_panda_space, R.string.noAssignments, R.string.noAssignmentsSubtext)
            }
        }
    }

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)

    override fun title(): String = getString(R.string.assignments)

    override fun getSelectedParamName() = RouterParams.ASSIGNMENT_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        TelemetryUtils.setInteractionName(this::class.java.simpleName)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.assignment_list_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerAdapter = AssignmentDateListRecyclerAdapter(
            requireContext(),
            canvasContext,
            adapterToAssignmentsCallback
        )

        configureRecyclerView(
            view,
            requireContext(),
            recyclerAdapter,
            R.id.swipeRefreshLayout,
            R.id.emptyView,
            R.id.listView
        )

        appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, i ->
            // Workaround for Toolbar not showing with swipe to refresh
            if (i == 0) {
                setRefreshingEnabled(true)
            } else {
                setRefreshingEnabled(false)
            }
        })
    }

    override fun applyTheme() {
        setupToolbarMenu(toolbar)
        toolbar.title = title()
        toolbar.setupAsBackButton(this)
        toolbar.addSearch(getString(R.string.searchAssignmentsHint)) { query ->
            if (query.isBlank()) {
                emptyView?.emptyViewText(R.string.noItemsToDisplayShort)
            } else {
                emptyView?.emptyViewText(getString(R.string.noItemsMatchingQuery, query))
            }
            recyclerAdapter.searchQuery = query
        }
        ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
    }

    private fun setupGradingPeriods(periods: List<GradingPeriod>) {
        val adapter = TermSpinnerAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            periods + allTermsGradingPeriod
        )
        termAdapter = adapter
        termSpinner.adapter = adapter
        termSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, l: Long) {
                if (adapter.getItem(i)!!.title == getString(R.string.allGradingPeriods)) {
                    recyclerAdapter.loadAssignment()
                } else {
                    recyclerAdapter.loadAssignmentsForGradingPeriod(adapter.getItem(i)!!.id, true)
                    termSpinner.isEnabled = false
                    adapter.isLoading = true
                    adapter.notifyDataSetChanged()
                }
                recyclerAdapter.currentGradingPeriod = adapter.getItem(i)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        // If we have a "current" grading period select it
        if (recyclerAdapter.currentGradingPeriod != null) {
            val position = adapter.getPositionForId(recyclerAdapter.currentGradingPeriod?.id ?: 0)
            if (position != -1) {
                termSpinner.setSelection(position)
            } else {
                toast(R.string.errorOccurred)
            }
        }

        termSpinnerLayout.setVisible()
    }

    override fun handleBackPressed() = toolbar.closeSearch()

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configureRecyclerView(
            view!!,
            requireContext(),
            recyclerAdapter,
            R.id.swipeRefreshLayout,
            R.id.emptyView,
            R.id.listView,
                R.string.noAssignments
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
                    //change nothing, at least for now
                } else {
                    emptyView.setGuidelines(.25f, .7f, .74f, .15f, .85f)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerAdapter.cancel()
    }

    companion object {

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext) = Route(AssignmentListFragment::class.java, canvasContext)

        @JvmStatic
        fun validateRoute(route: Route) = route.canvasContext?.isCourse == true

        @JvmStatic
        fun newInstance(route: Route): AssignmentListFragment? {
            if (!validateRoute(route)) return null
            return AssignmentListFragment().withArgs(route.canvasContext!!.makeBundle())
        }
    }

}
