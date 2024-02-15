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
@file:OptIn(ExperimentalFoundationApi::class)

package com.instructure.pandautils.features.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_CALENDAR
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.features.calendar.composables.CalendarScreen
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
@ScreenView(SCREEN_VIEW_CALENDAR)
@PageView(url = "calendar")
class ComposeCalendarFragment : Fragment(), NavigationCallbacks, FragmentInteractions {

    private val viewModel: CalendarViewModel by viewModels()
    private val sharedViewModel: CalendarSharedViewModel by viewModels(ownerProducer = {
        requireActivity()
    })

    @Inject
    lateinit var calendarRouter: CalendarRouter

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    event.getContentIfNotHandled()?.let {
                        handleAction(it)
                    }
                }
            }
        }

        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                CalendarScreen(title(), uiState, actionHandler = {
                    viewModel.handleAction(it)
                }) {
                    calendarRouter.openNavigationDrawer()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            withContext(Dispatchers.Main.immediate) {
                sharedViewModel.events.collect { action ->
                    when (action) {
                        is SharedCalendarAction.RefreshDay -> {
                            viewModel.handleAction(CalendarAction.RefreshDay(action.date))
                        }
                    }
                }
            }
        }
    }

    private fun handleAction(action: CalendarViewModelAction) {
        when (action) {
            is CalendarViewModelAction.OpenAssignment -> calendarRouter.openAssignment(action.canvasContext, action.assignmentId)
            is CalendarViewModelAction.OpenDiscussion -> calendarRouter.openDiscussion(action.canvasContext, action.discussionId)
            is CalendarViewModelAction.OpenQuiz -> calendarRouter.openQuiz(action.canvasContext, action.htmlUrl)
            is CalendarViewModelAction.OpenCalendarEvent -> calendarRouter.openCalendarEvent(action.canvasContext, action.eventId)
            is CalendarViewModelAction.OpenToDo -> calendarRouter.openToDo(action.plannerItem)
        }
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(R.string.calendar)

    override fun applyTheme() {
        ViewStyler.setStatusBarDark(requireActivity(), ThemePrefs.primaryColor)
    }

    override fun getFragment(): Fragment? {
        return this
    }

    override fun onHandleBackPressed(): Boolean {
        return false
    }

    companion object {
        fun newInstance(route: Route) = ComposeCalendarFragment()

        fun makeRoute() = Route(ComposeCalendarFragment::class.java, null)
    }
}