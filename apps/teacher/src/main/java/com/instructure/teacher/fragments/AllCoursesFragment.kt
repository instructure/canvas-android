/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.fragments

import android.content.Context
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.analytics.SCREEN_VIEW_DASHBOARD
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.fadeAnimationWithAction
import com.instructure.teacher.R
import com.instructure.teacher.adapters.AllCoursesAdapter
import com.instructure.teacher.decorations.VerticalGridSpacingDecoration
import com.instructure.teacher.events.CourseColorOverlayToggledEvent
import com.instructure.teacher.factory.AllCoursesPresenterFactory
import com.instructure.teacher.holders.CoursesViewHolder
import com.instructure.teacher.presenters.AllCoursesPresenter
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.TeacherPrefs
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.viewinterface.AllCoursesView
import kotlinx.android.synthetic.main.fragment_all_courses.*
import kotlinx.android.synthetic.main.fragment_all_courses.toolbar
import kotlinx.android.synthetic.main.recycler_swipe_refresh_layout.*
import kotlinx.android.synthetic.main.recycler_swipe_refresh_layout.swipeRefreshLayout
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

private const val LIST_SPAN_COUNT = 1

@ScreenView(SCREEN_VIEW_DASHBOARD)
class AllCoursesFragment : BaseSyncFragment<Course, AllCoursesPresenter, AllCoursesView, CoursesViewHolder, AllCoursesAdapter>(), AllCoursesView {

    private var mCourseBrowserCallback: CourseBrowserCallback? = null

    interface CourseBrowserCallback {
        fun onShowCourseDetails(course: Course)
        fun onPickCourseColor(course: Course)
        fun onEditCourseNickname(course: Course)
    }

    override fun layoutResId() = R.layout.fragment_all_courses

    private lateinit var mRecyclerView: RecyclerView


    override val recyclerView get() = mRecyclerView
    override fun perPageCount() = ApiPrefs.perPageCount
    override fun withPagination() = false

    override fun getPresenterFactory() = AllCoursesPresenterFactory()

    override fun onCreateView(view: View) {}

    override fun onPresenterPrepared(presenter: AllCoursesPresenter) {
        mRecyclerView = RecyclerViewUtils.buildRecyclerView(rootView, requireContext(), adapter, presenter, R.id.swipeRefreshLayout,
                R.id.recyclerView, R.id.emptyPandaView, getString(R.string.no_items_to_display_short))

        val spanCount = if (TeacherPrefs.listDashboard) LIST_SPAN_COUNT else requireContext().resources.getInteger(R.integer.course_list_span_count)
        val gridLayoutManager = GridLayoutManager(requireContext(), spanCount)
        mRecyclerView.layoutManager = gridLayoutManager
        mRecyclerView.addItemDecoration(VerticalGridSpacingDecoration(requireActivity(), gridLayoutManager))
        addSwipeToRefresh(swipeRefreshLayout)

        // Set up RecyclerView padding
        val padding = resources.getDimensionPixelSize(R.dimen.course_list_padding)
        mRecyclerView.setPaddingRelative(padding, padding, padding, padding)
        mRecyclerView.clipToPadding = false
    }

    override fun onReadySetGo(presenter: AllCoursesPresenter) {
        if(mRecyclerView.adapter == null) {
            mRecyclerView.adapter = adapter
        }
        presenter.loadData(false)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    private fun setupToolbar() {
        toolbar.setTitle(R.string.all_courses)
        toolbar.setupBackButton(this)
        toolbar.setupMenu(R.menu.menu_all_courses_fragment) { item ->
            when (item.itemId) {
                R.id.menu_dashboard_cards -> changeDashboardLayout(item)
            }
        }

        val dashboardLayoutMenuItem = toolbar.menu.findItem(R.id.menu_dashboard_cards)
        val menuIconRes = if (TeacherPrefs.listDashboard) R.drawable.ic_grid_dashboard else R.drawable.ic_list_dashboard
        dashboardLayoutMenuItem.setIcon(menuIconRes)

        val menuTitleRes = if (TeacherPrefs.listDashboard) R.string.dashboardSwitchToGridView else R.string.dashboardSwitchToListView
        dashboardLayoutMenuItem.setTitle(menuTitleRes)

        ViewStyler.themeToolbarColored(requireActivity(), toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
    }

    private fun changeDashboardLayout(item: MenuItem) {
        if (TeacherPrefs.listDashboard) {
            item.setIcon(R.drawable.ic_list_dashboard)
            item.setTitle(R.string.dashboardSwitchToListView)
            TeacherPrefs.listDashboard = false
        } else {
            item.setIcon(R.drawable.ic_grid_dashboard)
            item.setTitle(R.string.dashboardSwitchToGridView)
            TeacherPrefs.listDashboard = true
        }

        recyclerView.fadeAnimationWithAction {
            if (TeacherPrefs.listDashboard) {
                (recyclerView.layoutManager as? GridLayoutManager)?.spanCount = LIST_SPAN_COUNT
            } else {
                (recyclerView.layoutManager as? GridLayoutManager)?.spanCount = requireContext().resources.getInteger(R.integer.course_list_span_count)
            }
            view?.post { adapter.notifyDataSetChanged() }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CourseBrowserCallback) mCourseBrowserCallback = context
    }

    override fun onDetach() {
        super.onDetach()
        mCourseBrowserCallback = null
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun createAdapter() = AllCoursesAdapter(requireActivity(), presenter, mCourseBrowserCallback)

    override fun onRefreshStarted() {
        //this prevents two loading spinners from happening during pull to refresh
        if(!swipeRefreshLayout.isRefreshing) {
            emptyPandaView.visibility  = View.VISIBLE
        }
        emptyPandaView.setLoading()
    }

    override fun onRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun checkIfEmpty() {
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, mRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(sticky = true)
    fun onColorOverlayToggled(event: CourseColorOverlayToggledEvent) {
        adapter.notifyDataSetChanged()
    }

    companion object {
        fun getInstance() = AllCoursesFragment()
    }
}
