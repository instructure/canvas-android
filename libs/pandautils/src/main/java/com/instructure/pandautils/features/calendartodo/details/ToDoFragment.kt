/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.calendartodo.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.instructure.pandautils.analytics.SCREEN_VIEW_CALENDAR_TODO
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.features.calendar.CalendarSharedEvents
import com.instructure.pandautils.features.calendar.SharedCalendarAction
import com.instructure.pandautils.features.calendartodo.details.composables.ToDoScreen
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.withArgs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@ScreenView(SCREEN_VIEW_CALENDAR_TODO)
class ToDoFragment : Fragment(), NavigationCallbacks, FragmentInteractions {

    @Inject
    lateinit var toDoRouter: ToDoRouter

    private val viewModel: ToDoViewModel by viewModels()

    @Inject
    lateinit var sharedEvents: CalendarSharedEvents

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        applyTheme()
        viewLifecycleOwner.lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)
        viewLifecycleOwner.lifecycleScope.collectOneOffEvents(sharedEvents.events, ::handleSharedViewModelAction)

        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                ToDoScreen(title(), uiState, viewModel::handleAction, ::navigateBack)
            }
        }
    }

    private fun handleAction(action: ToDoViewModelAction) {
        when (action) {
            is ToDoViewModelAction.RefreshCalendarDay -> {
                sharedEvents.sendEvent(lifecycleScope, SharedCalendarAction.RefreshDays(listOf(action.date)))
                navigateBack()
            }

            is ToDoViewModelAction.OpenEditToDo -> {
                toDoRouter.openEditToDo(action.plannerItem)
            }
        }
    }

    private fun handleSharedViewModelAction(action: SharedCalendarAction) {
        when (action) {
            is SharedCalendarAction.CloseToDoScreen -> activity?.onBackPressed()
            else -> {}
        }
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(R.string.userCalendarToDo)

    override fun applyTheme() {
        ViewStyler.setStatusBarDark(requireActivity(), ThemePrefs.primaryColor)
    }

    override fun getFragment(): Fragment {
        return this
    }

    override fun onHandleBackPressed(): Boolean {
        return false
    }

    private fun navigateBack() {
        activity?.onBackPressed()
    }

    companion object {
        internal const val PLANNER_ITEM = "PLANNER_ITEM"
        fun newInstance(route: Route) = ToDoFragment().withArgs(route.arguments)

        fun makeRoute(plannerItem: PlannerItem): Route {
            val bundle = bundleOf(PLANNER_ITEM to plannerItem)
            return Route(ToDoFragment::class.java, null, bundle)
        }
    }
}
