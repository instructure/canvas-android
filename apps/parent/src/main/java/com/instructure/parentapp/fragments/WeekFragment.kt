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

import android.annotation.TargetApi
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandarecycler.decorations.SpacesItemDecoration
import com.instructure.pandarecycler.interfaces.ViewHolderHeaderClicked
import com.instructure.pandautils.fragments.BaseExpandableSyncFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.setVisible
import com.instructure.parentapp.R
import com.instructure.parentapp.activity.DetailViewActivity
import com.instructure.parentapp.adapter.CalendarWeekRecyclerAdapter
import com.instructure.parentapp.binders.CalendarWeekBinder
import com.instructure.parentapp.factorys.WeekViewPresenterFactory
import com.instructure.parentapp.interfaces.AdapterToFragmentCallback
import com.instructure.parentapp.models.WeekHeaderItem
import com.instructure.parentapp.presenters.WeekPresenter
import com.instructure.parentapp.util.AnalyticUtils
import com.instructure.parentapp.util.ParentPrefs
import com.instructure.parentapp.util.RecyclerViewUtils
import com.instructure.parentapp.util.ViewUtils
import com.instructure.parentapp.viewinterface.WeekView
import instructure.androidblueprint.PresenterFactory
import kotlinx.android.synthetic.main.fragment_course_week.*
import kotlinx.android.synthetic.main.week_recycler_swipe_refresh_layout.*
import kotlinx.android.synthetic.main.week_view_header.*
import java.util.*

open class WeekFragment : BaseExpandableSyncFragment<WeekHeaderItem, ScheduleItem, WeekView, WeekPresenter, RecyclerView.ViewHolder, CalendarWeekRecyclerAdapter>(), WeekView {
    protected var student: User by ParcelableArg(key = Const.STUDENT)
    protected var course: Course? by NullableParcelableArg(key = Const.COURSE)

    private val adapterCallback = AdapterToFragmentCallback<ScheduleItem> { item, _, _ ->
        AnalyticUtils.trackFlow(AnalyticUtils.WEEK_FLOW, AnalyticUtils.WEEK_VIEW_SELECTED)
        if (item.assignment != null) {
            // If we're already in a detailViewActivity we don't need to add another one
            if (activity is DetailViewActivity) {
                (activity as DetailViewActivity).addFragment(AssignmentFragment.newInstance(item.assignment!!,
                        CalendarWeekBinder.getCourseById(presenter.coursesMap, item.courseId), student), false)
            } else {
                startActivity(DetailViewActivity.createIntent(requireContext(), DetailViewActivity.DETAIL_FRAGMENT.ASSIGNMENT, item.assignment!!,
                        CalendarWeekBinder.getCourseById(presenter.coursesMap, item.courseId), student))
                requireActivity().overridePendingTransition(R.anim.slide_from_bottom, android.R.anim.fade_out)
            }
        } else {
            // If we're already in a detailViewActivity we don't need to add another one
            if (activity is DetailViewActivity) {
                (activity as DetailViewActivity).addFragment(EventFragment.newInstance(item, student), false)
            } else {
                startActivity(DetailViewActivity.createIntent(requireContext(), DetailViewActivity.DETAIL_FRAGMENT.EVENT, item, student))
                requireActivity().overridePendingTransition(R.anim.slide_from_bottom, android.R.anim.fade_out)
            }
        }
    }

    private val adapterHeaderCallback = ViewHolderHeaderClicked<WeekHeaderItem> { _, _ -> }

    override fun layoutResId(): Int = R.layout.fragment_week

    private fun setupListeners() {
        prevWeek.setOnClickListener { presenter.prevWeekClicked() }
        nextWeek.setOnClickListener { presenter.nextWeekClicked() }
    }

    private fun setColors(color: Int) {
        swipeRefreshLayout.setColorSchemeColors(color, color, color, color)
        emptyPandaView.progressBar.indeterminateDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        weekViewBackground.setBackgroundColor(ViewUtils.darker(color, 0.90f))
        setStatusBarColor(color)
        setActionbarColor(color)
    }

    private fun setActionbarColor(actionBarColor: Int) {
        // The base WeekFragment does not have a toolbar, hence the safe call operator here
        toolbar?.setBackgroundColor(actionBarColor)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setStatusBarColor(statusBarColor: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && statusBarColor != Integer.MAX_VALUE) {
            // Make the status bar darker than the toolbar
            activity?.window?.statusBarColor = ViewUtils.darker(statusBarColor, 0.90f)
        }
    }

    // Sync

    override fun onCreateView(view: View?) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(toolbar != null) {
            toolbar.visibility = View.VISIBLE

            toolbar.setNavigationIcon(R.drawable.ic_close_white)
            toolbar.setNavigationContentDescription(R.string.close)
            toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        }

    }

    override fun onReadySetGo(presenter: WeekPresenter) {
        setupListeners()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        presenter.loadData(false)
        setColors(ParentPrefs.currentColor)
    }

    override fun getPresenterFactory(): PresenterFactory<WeekPresenter> {
        return WeekViewPresenterFactory(student, course)
    }

    override fun onPresenterPrepared(presenter: WeekPresenter) {
        recyclerView.addItemDecoration(SpacesItemDecoration(context, R.dimen.med_padding))
        addSwipeToRefresh(swipeRefreshLayout)
        addPagination()
    }

    override fun getAdapter(): CalendarWeekRecyclerAdapter {
        if (mAdapter == null) {
            mAdapter = CalendarWeekRecyclerAdapter(
                    requireContext(),
                    presenter,
                    presenter.courses,
                    presenter.student!!,
                    adapterCallback,
                    adapterHeaderCallback)
        }
        return mAdapter
    }

    override fun withPagination(): Boolean = true
    override fun getRecyclerView(): RecyclerView = weekRecyclerView
    override fun perPageCount(): Int = ApiPrefs.perPageCount

    override fun onRefreshStarted() {
        emptyPandaView.setVisible()
        emptyPandaView.setLoading()
    }

    override fun onRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
        // Update the adapter with the courses that the presenter retrieved
        adapter.setCourses(presenter.courses)
    }

    override fun checkIfEmpty() {
        emptyPandaView.setTitleText(getString(R.string.weekEmptyView))
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, recyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    override fun updateWeekText(dates: ArrayList<GregorianCalendar>) {
        val start = dates[0]
        val end = dates[1]

        val flags = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_YEAR or DateUtils.FORMAT_ABBREV_MONTH
        val monthAndDayText = DateUtils.formatDateTime(context, start.timeInMillis, flags)
        var monthAndDayTextEnd = DateUtils.formatDateTime(context, end.timeInMillis, flags)

        weekText.contentDescription = getString(R.string.dateRangeContentDescription, monthAndDayText, monthAndDayTextEnd)

        // If date range falls within a single month, omit the month on the end date
        if (start[Calendar.MONTH] == end[Calendar.MONTH]) monthAndDayTextEnd = end[Calendar.DAY_OF_MONTH].toString()

        weekText.text = getString(R.string.date_bar, monthAndDayText, monthAndDayTextEnd)
    }

    companion object {
        fun newInstance(student: User): WeekFragment {
            val args = Bundle()
            args.putParcelable(Const.STUDENT, student)
            val fragment = WeekFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
