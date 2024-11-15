/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.features.elementary.schedule

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.pandautils.blueprint.BaseCanvasFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.pandautils.analytics.SCREEN_VIEW_K5_SCHEDULE
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.databinding.FragmentScheduleBinding
import com.instructure.pandautils.features.elementary.schedule.pager.SchedulePagerFragment
import com.instructure.pandautils.utils.StringArg
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@PageView
@ScreenView(SCREEN_VIEW_K5_SCHEDULE)
@AndroidEntryPoint
class ScheduleFragment : BaseCanvasFragment() {

    @Inject
    lateinit var scheduleRouter: ScheduleRouter

    private val viewModel: ScheduleViewModel by viewModels()

    private val adapter = ScheduleRecyclerViewAdapter()

    private var startDateString by StringArg()

    private var recyclerView: RecyclerView? = null

    private lateinit var binding: FragmentScheduleBinding

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            checkFirstPosition()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentScheduleBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.adapter = adapter
        recyclerView = binding.scheduleRecyclerView

        viewModel.getDataForDate(startDateString)

        viewModel.events.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        recyclerView?.addOnScrollListener(onScrollListener)
    }

    override fun onPause() {
        super.onPause()
        recyclerView?.removeOnScrollListener(onScrollListener)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        viewModel.refresh(false)
    }

    private fun handleAction(action: ScheduleAction) {
        when (action) {
            is ScheduleAction.OpenCourse -> scheduleRouter.openCourse(action.course)
            is ScheduleAction.OpenAssignment -> scheduleRouter.openAssignment(action.canvasContext, action.assignmentId)
            is ScheduleAction.OpenCalendarEvent -> scheduleRouter.openCalendarEvent(
                action.canvasContext,
                action.scheduleItemId
            )
            is ScheduleAction.OpenQuiz -> {
                scheduleRouter.openQuiz(action.canvasContext, action.htmlUrl)
            }
            is ScheduleAction.OpenDiscussion -> {
                scheduleRouter.openDiscussion(action.canvasContext, action.id, action.title)
            }
            is ScheduleAction.JumpToToday -> {
                jumpToToday()
            }
            is ScheduleAction.AnnounceForAccessibility -> {
                binding.root.announceForAccessibility(action.announcement)
            }
        }
    }

    fun jumpToToday() {
        if (recyclerView?.layoutManager is LinearLayoutManager) {
            (recyclerView?.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                viewModel.todayPosition,
                0
            )
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        if (menuVisible) {
            checkFirstPosition()
        }
        super.setMenuVisibility(menuVisible)
    }

    fun checkFirstPosition() {
        val firstItemPosition =
            (binding.scheduleRecyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        val todayRange = viewModel.getTodayRange()
        if (todayRange != null) {
            toggleJumpToTodayButton(firstItemPosition !in todayRange)
        }
    }

    private fun toggleJumpToTodayButton(visible: Boolean) {
        (requireParentFragment() as SchedulePagerFragment).setTodayButtonVisibility(visible)
    }

    @PageViewUrl
    fun makePageViewUrl() = "${ApiPrefs.fullDomain}#schedule"

    companion object {

        fun newInstance(startDate: String) = ScheduleFragment().apply { startDateString = startDate }
    }
}