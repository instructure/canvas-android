/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.elementary.importantdates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.instructure.pandautils.blueprint.BaseCanvasFragment
import androidx.fragment.app.viewModels
import com.instructure.pandautils.analytics.SCREEN_VIEW_K5_IMPORTANT_DATES
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.databinding.FragmentImportantDatesBinding
import com.instructure.pandautils.features.elementary.schedule.ScheduleAction
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_K5_IMPORTANT_DATES)
@AndroidEntryPoint
class ImportantDatesFragment : BaseCanvasFragment() {

    @Inject
    lateinit var router: ImportantDatesRouter

    private val viewModel: ImportantDatesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding = FragmentImportantDatesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.events.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        })
    }

    private fun handleAction(action: ImportantDatesAction) {
        when (action) {
            is ImportantDatesAction.OpenAssignment -> router.openAssignment(action.canvasContext, action.assignmentId)
            is ImportantDatesAction.OpenCalendarEvent -> router.openCalendarEvent(
                    action.canvasContext,
                    action.scheduleItem
            )
            is ImportantDatesAction.ShowToast -> {
                Toast.makeText(requireContext(), action.toast, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance() = ImportantDatesFragment()
    }
}