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

package com.instructure.pandautils.features.todo.createupdate

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
import com.instructure.pandautils.features.calendar.CalendarSharedViewModel
import com.instructure.pandautils.features.calendar.ComposeCalendarFragment
import com.instructure.pandautils.features.calendar.SharedCalendarAction
import com.instructure.pandautils.features.todo.createupdate.composables.CreateUpdateToDoScreenWrapper
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.withArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class CreateUpdateToDoFragment : Fragment(), NavigationCallbacks, FragmentInteractions {

    private val viewModel: CreateUpdateToDoViewModel by viewModels()
    private val sharedViewModel: CalendarSharedViewModel by viewModels(ownerProducer = {
        requireActivity()
    })

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireActivity()).apply {
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            CreateUpdateToDoScreenWrapper(title(), uiState, viewModel::handleAction, ::navigateBack)
        }

        sharedViewModel.sendEvent(SharedCalendarAction.RequestSelectedDay {
            viewModel.handleAction(CreateUpdateToDoAction.SetInitialDate(it))
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            withContext(Dispatchers.Main.immediate) {
                viewModel.events.collect { action ->
                    when (action) {
                        is CreateUpdateToDoViewModelAction.RefreshCalendarDays -> {
                            sharedViewModel.sendEvent(SharedCalendarAction.RefreshDays(action.days))
                            activity?.supportFragmentManager?.popBackStack(ComposeCalendarFragment::class.java.name, 0)
                        }
                    }
                }
            }
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
        ViewStyler.setStatusBarLight(requireActivity())
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
        fun newInstance(route: Route) = CreateUpdateToDoFragment().withArgs(route.arguments)

        fun makeRoute(plannerItem: PlannerItem? = null): Route {
            val bundle = bundleOf(PLANNER_ITEM to plannerItem)
            return Route(CreateUpdateToDoFragment::class.java, null, bundle)
        }
    }
}
