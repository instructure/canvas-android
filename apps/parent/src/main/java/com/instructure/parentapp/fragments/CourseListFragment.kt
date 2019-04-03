/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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
package com.instructure.parentapp.fragments

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandarecycler.decorations.SpacesItemDecoration
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.parentapp.R
import com.instructure.parentapp.activity.DetailViewActivity
import com.instructure.parentapp.adapter.CourseListRecyclerAdapter
import com.instructure.parentapp.factorys.CourseListPresenterFactory
import com.instructure.parentapp.holders.CourseViewHolder
import com.instructure.parentapp.interfaces.BasicAdapterToFragmentCallback
import com.instructure.parentapp.presenters.CourseListPresenter
import com.instructure.parentapp.util.AnalyticUtils
import com.instructure.parentapp.util.ParentPrefs
import com.instructure.parentapp.util.RecyclerViewUtils
import com.instructure.parentapp.viewinterface.CourseListView
import instructure.androidblueprint.PresenterFactory
import kotlinx.android.synthetic.main.course_list_view.*
import kotlinx.android.synthetic.main.recycler_swipe_refresh_layout.*
import kotlinx.android.synthetic.main.recycler_swipe_refresh_layout.recyclerView as recycler

class CourseListFragment :
    BaseSyncFragment<Course, CourseListPresenter, CourseListView, CourseViewHolder, CourseListRecyclerAdapter>(),
    CourseListView {

    private var student: User by ParcelableArg(key = Const.STUDENT)

    override fun layoutResId() = R.layout.course_list_view

    override fun onCreateView(view: View?) = Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        addSwipeToRefresh(swipeRefreshLayout!!)
        recycler.addItemDecoration(SpacesItemDecoration(context, R.dimen.med_padding))
    }

    override fun onReadySetGo(presenter: CourseListPresenter) {
        recyclerView.adapter = adapter
        presenter.loadData(false)
        setupColor(ParentPrefs.currentColor)
    }

    private fun setupColor(color: Int) {
        swipeRefreshLayout.setColorSchemeColors(color, color, color, color)
        emptyPandaView.progressBar.indeterminateDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    override fun getPresenterFactory(): PresenterFactory<CourseListPresenter> = CourseListPresenterFactory(student)

    override fun onPresenterPrepared(presenter: CourseListPresenter) {
        RecyclerViewUtils.buildRecyclerView(
            mRootView, context, adapter,
            presenter, R.id.swipeRefreshLayout, R.id.recyclerView, R.id.emptyPandaView, getString(R.string.noCourses)
        )
        addSwipeToRefresh(swipeRefreshLayout!!)
        addPagination()
    }

    override fun getAdapter(): CourseListRecyclerAdapter {
        if (mAdapter == null) {
            mAdapter = CourseListRecyclerAdapter(activity, presenter,
                BasicAdapterToFragmentCallback { position, _ ->
                    val course = adapter.getItemAtPosition(position)
                    if (course != null) {
                        AnalyticUtils.trackFlow(AnalyticUtils.COURSE_FLOW, AnalyticUtils.COURSE_SELECTED)
                        startActivity(
                            DetailViewActivity.createIntent(
                                requireContext(), DetailViewActivity.DETAIL_FRAGMENT.WEEK, presenter.student, course
                            )
                        )
                        requireActivity().overridePendingTransition(R.anim.slide_from_bottom, android.R.anim.fade_out)
                    }
                })
        }
        return mAdapter
    }

    override fun withPagination() = true

    override fun getRecyclerView(): RecyclerView = recycler

    override fun perPageCount() = ApiPrefs.perPageCount

    override fun onRefreshStarted() {
        emptyPandaView.setLoading()
    }

    override fun onRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun checkIfEmpty() {
        val isEmpty = RecyclerViewUtils.checkIfEmpty(emptyPandaView, recyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
        emptyPandaView.setGone() // Using the 'noCoursesView' in place of the traditional emptyPandaView
        noCoursesView.setVisible(isEmpty)
    }

    companion object {
        fun newInstance(student: User): CourseListFragment = CourseListFragment().apply {
            arguments = Bundle().apply {
                putParcelable(Const.STUDENT, student)
            }
        }
    }
}
