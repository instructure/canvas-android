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

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.annotation.StringRes
import com.google.android.material.appbar.AppBarLayout
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.analytics.SCREEN_VIEW_ASSIGNMENT_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.TermSpinnerAdapter
import com.instructure.student.adapter.assignment.AssignmentListByDateRecyclerAdapter
import com.instructure.student.adapter.assignment.AssignmentListByTypeRecyclerAdapter
import com.instructure.student.adapter.assignment.AssignmentListRecyclerAdapter
import com.instructure.student.interfaces.AdapterToAssignmentsCallback
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.StudentPrefs
import kotlinx.android.synthetic.main.assignment_list_layout.*

@ScreenView(SCREEN_VIEW_ASSIGNMENT_LIST)
@PageView(url = "{canvasContext}/assignments")
class AssignmentListFragment : ParentFragment(), Bookmarkable {

    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    private lateinit var recyclerAdapter: AssignmentListRecyclerAdapter
    private var termAdapter: TermSpinnerAdapter? = null

    private var sortOrder: AssignmentsSortOrder
        get() {
            val preferenceKey = StudentPrefs.getString("sortBy_${canvasContext.contextId}", AssignmentsSortOrder.SORT_BY_TIME.preferenceKey)
            return AssignmentsSortOrder.fromPreferenceKey(preferenceKey)
        }
        set(value) {
            StudentPrefs.putString("sortBy_${canvasContext.contextId}", value.preferenceKey)
        }

    private val allTermsGradingPeriod by lazy {
        GradingPeriod().apply { title = getString(R.string.assignmentsListAllGradingPeriods) }
    }

    private val adapterToAssignmentsCallback = object : AdapterToAssignmentsCallback {
        override fun assignmentLoadingFinished() {
            // If we only have one grading period we want to disable the spinner
            val termCount = termAdapter?.count ?: 0
            termSpinner?.isEnabled = termCount > 1
            termAdapter?.isLoading = false
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
                setEmptyView(emptyView, R.drawable.ic_panda_space, R.string.noAssignments, R.string.noAssignmentsSubtext)
            }
        }
    }

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)

    override fun title(): String = getString(R.string.assignments)

    override fun getSelectedParamName() = RouterParams.ASSIGNMENT_ID

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.assignment_list_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerAdapter = createRecyclerAdapter()

        sortByTextView.setText(sortOrder.buttonTextRes)
        sortByButton.contentDescription = getString(sortOrder.contentDescriptionRes)

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

        setupSortByButton()
    }

    private fun createRecyclerAdapter(): AssignmentListRecyclerAdapter {
        return if (sortOrder == AssignmentsSortOrder.SORT_BY_TIME) {
            AssignmentListByDateRecyclerAdapter(requireContext(), canvasContext, adapterToAssignmentsCallback)
        } else {
            AssignmentListByTypeRecyclerAdapter(requireContext(), canvasContext, adapterToAssignmentsCallback)
        }
    }

    private fun setupSortByButton() {
        sortByButton.onClick {
            val checkedItemIndex = sortOrder.index
            AlertDialog.Builder(context, R.style.AccentDialogTheme)
                .setTitle(R.string.sortByDialogTitle)
                .setSingleChoiceItems(R.array.assignmentsSortByOptions, checkedItemIndex, this@AssignmentListFragment::sortOrderSelected)
                .setNegativeButton(R.string.sortByDialogCancel) { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun sortOrderSelected(dialog: DialogInterface, index: Int) {
        dialog.dismiss()
        val selectedSortOrder = AssignmentsSortOrder.fromIndex(index)
        if (sortOrder != selectedSortOrder) {
            sortOrder = selectedSortOrder
            recyclerAdapter = createRecyclerAdapter()
            listView.adapter = recyclerAdapter
            sortByTextView.setText(selectedSortOrder.buttonTextRes)
            sortByButton.contentDescription = getString(selectedSortOrder.contentDescriptionRes)
            Analytics.logEvent(selectedSortOrder.analyticsKey)
            listView.announceForAccessibility(getString(selectedSortOrder.orderSelectedAnnouncement))
        }
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
        val hasGradingPeriods = periods.isNotEmpty()
        val adapter = TermSpinnerAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            periods + allTermsGradingPeriod,
            hasGradingPeriods
        )
        termSpinner.isEnabled = hasGradingPeriods
        termAdapter = adapter
        termSpinner.adapter = adapter
        termSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, l: Long) {
                if (adapter.getItem(i)!!.title == getString(R.string.assignmentsListAllGradingPeriods)) {
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
        if (hasGradingPeriods && recyclerAdapter.currentGradingPeriod != null) {
            val position = adapter.getPositionForId(recyclerAdapter.currentGradingPeriod?.id ?: 0)
            if (position != -1) {
                termSpinner.setSelection(position)
            } else {
                toast(R.string.errorOccurred)
            }
        }
    }

    override fun handleBackPressed() = toolbar.closeSearch()

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configureRecyclerView(
            requireView(),
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

        fun makeRoute(canvasContext: CanvasContext) = Route(AssignmentListFragment::class.java, canvasContext)

        fun validateRoute(route: Route) = route.canvasContext?.isCourse == true

        fun newInstance(route: Route): AssignmentListFragment? {
            if (!validateRoute(route)) return null
            return AssignmentListFragment().withArgs(route.canvasContext!!.makeBundle())
        }
    }

}

enum class AssignmentsSortOrder(
    val index: Int,
    val preferenceKey: String,
    @StringRes val buttonTextRes: Int,
    @StringRes val contentDescriptionRes: Int,
    @StringRes val orderSelectedAnnouncement: Int,
    val analyticsKey: String) {

    SORT_BY_TIME(0, "time", R.string.sortByTime, R.string.a11y_sortByTimeButton,
        R.string.a11y_assignmentsSortedByTime, AnalyticsEventConstants.ASSIGNMENT_LIST_SORT_BY_TIME_SELECTED),

    SORT_BY_TYPE(1, "type", R.string.sortByType, R.string.a11y_sortByTypeButton,
        R.string.a11y_assignmentsSortedByType, AnalyticsEventConstants.ASSIGNMENT_LIST_SORT_BY_TYPE_SELECTED);

    companion object {
        fun fromPreferenceKey(key: String?): AssignmentsSortOrder {
            return when (key) {
                SORT_BY_TIME.preferenceKey -> SORT_BY_TIME
                SORT_BY_TYPE.preferenceKey -> SORT_BY_TYPE
                else -> SORT_BY_TIME // This will be the default value
            }
        }

        fun fromIndex(key: Int): AssignmentsSortOrder {
            return when (key) {
                SORT_BY_TIME.index -> SORT_BY_TIME
                SORT_BY_TYPE.index -> SORT_BY_TYPE
                else -> SORT_BY_TIME // This will be the default value
            }
        }
    }
}
