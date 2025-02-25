/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.features.calendarevent.createupdate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.instructure.pandautils.base.BaseCanvasFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.features.calendar.CalendarSharedEvents
import com.instructure.pandautils.features.calendar.SharedCalendarAction
import com.instructure.pandautils.features.calendarevent.createupdate.composables.CreateUpdateEventScreenWrapper
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.announceAccessibilityText
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.withArgs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class CreateUpdateEventFragment : BaseCanvasFragment(), NavigationCallbacks, FragmentInteractions {

    private val viewModel: CreateUpdateEventViewModel by viewModels()

    @Inject
    lateinit var sharedEvents: CalendarSharedEvents

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        applyTheme()

        viewLifecycleOwner.lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)

        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                CreateUpdateEventScreenWrapper(title(), uiState, viewModel::handleAction)
            }
        }
    }

    private fun handleAction(action: CreateUpdateEventViewModelAction) {
        when (action) {
            is CreateUpdateEventViewModelAction.RefreshCalendarDays -> {
                navigateBack()
                sharedEvents.sendEvent(lifecycleScope, SharedCalendarAction.RefreshDays(action.days))
                sharedEvents.sendEvent(lifecycleScope, SharedCalendarAction.CloseEventScreen)
            }

            is CreateUpdateEventViewModelAction.RefreshCalendar -> {
                navigateBack()
                sharedEvents.sendEvent(lifecycleScope, SharedCalendarAction.RefreshCalendar)
                sharedEvents.sendEvent(lifecycleScope, SharedCalendarAction.CloseEventScreen)
            }

            is CreateUpdateEventViewModelAction.NavigateBack -> navigateBack()
            is CreateUpdateEventViewModelAction.AnnounceEventCreation -> announceEventCreation(action.title)
            is CreateUpdateEventViewModelAction.AnnounceEventUpdate -> announceEventUpdate(action.title)
        }
    }

    private fun announceEventCreation(title: String) {
        val textToAnnounce = getString(R.string.a11y_eventCreatedAnnouncement, title)
        announceAccessibilityText(requireContext(), textToAnnounce)
    }

    private fun announceEventUpdate(title: String) {
        val textToAnnounce = getString(R.string.a11y_eventUpdatedAnnouncement, title)
        announceAccessibilityText(requireContext(), textToAnnounce)
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(
        if (arguments?.containsKey(SCHEDULE_ITEM).orDefault()) {
            R.string.editEventScreenTitle
        } else {
            R.string.createEventScreenTitle
        }
    )

    override fun applyTheme() {
        ViewStyler.themeStatusBar(requireActivity())
    }

    override fun getFragment(): Fragment {
        return this
    }

    override fun onHandleBackPressed(): Boolean {
        return viewModel.onBackPressed()
    }

    private fun navigateBack() {
        activity?.onBackPressed()
    }

    companion object {
        const val INITIAL_DATE = "INITIAL_DATE"
        const val SCHEDULE_ITEM = "SCHEDULE_ITEM"

        fun newInstance(route: Route) = CreateUpdateEventFragment().withArgs(route.arguments)

        fun makeRoute(scheduleItem: ScheduleItem): Route {
            val bundle = bundleOf(SCHEDULE_ITEM to scheduleItem)
            return Route(CreateUpdateEventFragment::class.java, null, bundle)
        }

        fun makeRoute(initialDateString: String?): Route {
            val bundle = bundleOf(INITIAL_DATE to initialDateString)
            return Route(CreateUpdateEventFragment::class.java, null, bundle)
        }
    }
}
