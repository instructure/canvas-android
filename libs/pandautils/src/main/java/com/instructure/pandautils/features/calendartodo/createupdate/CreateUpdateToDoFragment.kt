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

package com.instructure.pandautils.features.calendartodo.createupdate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.features.calendar.CalendarSharedEvents
import com.instructure.pandautils.features.calendar.SharedCalendarAction
import com.instructure.pandautils.features.calendartodo.createupdate.composables.CreateUpdateToDoScreenWrapper
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.withArgs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CreateUpdateToDoFragment : Fragment(), NavigationCallbacks, FragmentInteractions {

    private val viewModel: CreateUpdateToDoViewModel by viewModels()

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
                CreateUpdateToDoScreenWrapper(title(), uiState, viewModel::handleAction)
            }
        }
    }

    private fun handleAction(action: CreateUpdateToDoViewModelAction) {
        when (action) {
            is CreateUpdateToDoViewModelAction.RefreshCalendarDays -> {
                navigateBack()
                sharedEvents.sendEvent(lifecycleScope, SharedCalendarAction.CloseToDoScreen)
                sharedEvents.sendEvent(lifecycleScope, SharedCalendarAction.RefreshDays(action.days))
            }

            is CreateUpdateToDoViewModelAction.NavigateBack -> navigateBack()
        }
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(
        if (arguments?.containsKey(PLANNER_ITEM).orDefault()) {
            R.string.editTodoScreenTitle
        } else {
            R.string.createTodoScreenTitle
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
        const val PLANNER_ITEM = "PLANNER_ITEM"
        const val INITIAL_DATE = "INITIAL_DATE"
        fun newInstance(route: Route) = CreateUpdateToDoFragment().withArgs(route.arguments)

        fun makeRoute(plannerItem: PlannerItem): Route {
            val bundle = bundleOf(PLANNER_ITEM to plannerItem)
            return Route(CreateUpdateToDoFragment::class.java, null, bundle)
        }

        fun makeRoute(initialDateString: String?): Route {
            val bundle = bundleOf(INITIAL_DATE to initialDateString)
            return Route(CreateUpdateToDoFragment::class.java, null, bundle)
        }
    }
}
