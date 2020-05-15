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
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.teacher.R
import com.instructure.teacher.adapters.AllCoursesAdapter
import com.instructure.teacher.decorations.VerticalGridSpacingDecoration
import com.instructure.teacher.events.CourseColorOverlayToggledEvent
import com.instructure.teacher.factory.AllCoursesPresenterFactory
import com.instructure.teacher.holders.CoursesViewHolder
import com.instructure.teacher.presenters.AllCoursesPresenter
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.viewinterface.AllCoursesView
import kotlinx.android.synthetic.main.fragment_all_courses.*
import kotlinx.android.synthetic.main.recycler_swipe_refresh_layout.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class AllCoursesFragment : BaseSyncFragment<Course, AllCoursesPresenter, AllCoursesView, CoursesViewHolder, AllCoursesAdapter>(), AllCoursesView {

    private var mCourseBrowserCallback: CourseBrowserCallback? = null

    interface CourseBrowserCallback {
        fun onShowCourseDetails(course: Course)
        fun onPickCourseColor(course: Course)
        fun onEditCourseNickname(course: Course)
    }

    override fun layoutResId() = R.layout.fragment_all_courses

    private lateinit var mRecyclerView: RecyclerView


    override fun getList() = presenter.data
    override fun getRecyclerView() = mRecyclerView
    override fun perPageCount() = ApiPrefs.perPageCount
    override fun withPagination() = false

    override fun getPresenterFactory() = AllCoursesPresenterFactory()

    override fun onCreateView(view: View) {}

    override fun onPresenterPrepared(presenter: AllCoursesPresenter) {
        mRecyclerView = RecyclerViewUtils.buildRecyclerView(mRootView, requireContext(), adapter, presenter, R.id.swipeRefreshLayout,
                R.id.recyclerView, R.id.emptyPandaView, getString(R.string.no_items_to_display_short))
        val gridLayoutManager = GridLayoutManager(
            requireContext(),
            requireContext().resources.getInteger(R.integer.course_list_span_count)
        )
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
        toolbar.setTitle(R.string.all_courses)
        toolbar.setupBackButton(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
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

    override fun getAdapter(): AllCoursesAdapter {
        if (mAdapter == null) {
            mAdapter = AllCoursesAdapter(requireActivity(), presenter, mCourseBrowserCallback)
        }
        return mAdapter
    }

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
        mAdapter?.notifyDataSetChanged()
    }

    companion object {
        @JvmStatic
        fun getInstance() = AllCoursesFragment()
    }
}
