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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.instructure.pandautils.databinding.FragmentScheduleBinding
import com.instructure.pandautils.utils.StringArg
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_schedule.*
import javax.inject.Inject

@AndroidEntryPoint
class ScheduleFragment : Fragment() {

    @Inject
    lateinit var scheduleRouter: ScheduleRouter

    private val viewModel: ScheduleViewModel by viewModels()

    private val adapter = ScheduleRecyclerViewAdapter()

    private var startDateString by StringArg()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentScheduleBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.adapter = adapter

        viewModel.getDataForDate(startDateString)

        viewModel.events.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        })

        return binding.root
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
                scheduleRecyclerView.scrollToPosition(action.position)
            }
        }
    }

    companion object {

        fun newInstance(startDate: String) = ScheduleFragment().apply { startDateString = startDate }
    }
}