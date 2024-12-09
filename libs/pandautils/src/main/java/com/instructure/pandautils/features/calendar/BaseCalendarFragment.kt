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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_CALENDAR
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.features.calendar.composables.CalendarScreen
import com.instructure.pandautils.features.calendar.filter.CalendarFilterFragment
import com.instructure.pandautils.features.inbox.list.filter.ContextFilterFragment
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@ScreenView(SCREEN_VIEW_CALENDAR)
@PageView(url = "calendar")
open class BaseCalendarFragment : BaseCanvasFragment(), NavigationCallbacks, FragmentInteractions {

    private val viewModel: CalendarViewModel by viewModels()

    @Inject
    lateinit var sharedViewModel: CalendarSharedEvents

    @Inject
    lateinit var calendarRouter: CalendarRouter

    // This is needed to trigger accessibility focus on the calendar screen when the tab is selected
    private var triggerCalendarScreenAccessibilityFocus = mutableStateOf(false)

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        applyTheme()
        viewLifecycleOwner.lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)
        viewLifecycleOwner.lifecycleScope.collectOneOffEvents(
            sharedViewModel.events,
            ::handleSharedViewModelAction
        )

        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                val actionHandler = { action: CalendarAction -> viewModel.handleAction(action) }
                CalendarScreen(title(), uiState, triggerCalendarScreenAccessibilityFocus.value, showToolbar(), actionHandler) {
                    calendarRouter.openNavigationDrawer()
                }
            }
        }
    }

    fun calendarTabSelected() {
        triggerCalendarScreenAccessibilityFocus.value = !triggerCalendarScreenAccessibilityFocus.value
    }

    private fun handleAction(action: CalendarViewModelAction) {
        when (action) {
            is CalendarViewModelAction.OpenAssignment -> calendarRouter.openAssignment(action.canvasContext, action.assignmentId)
            is CalendarViewModelAction.OpenDiscussion -> calendarRouter.openDiscussion(action.canvasContext, action.discussionId, action.assignmentId)
            is CalendarViewModelAction.OpenQuiz -> calendarRouter.openQuiz(action.canvasContext, action.htmlUrl)
            is CalendarViewModelAction.OpenCalendarEvent -> calendarRouter.openCalendarEvent(action.canvasContext, action.eventId)
            is CalendarViewModelAction.OpenToDo -> calendarRouter.openToDo(action.plannerItem)
            is CalendarViewModelAction.OpenCreateToDo -> calendarRouter.openCreateToDo(action.initialDateString)
            CalendarViewModelAction.OpenFilters -> {
                val calendarFilterFragment = CalendarFilterFragment.newInstance()
                calendarFilterFragment.show(requireActivity().supportFragmentManager, ContextFilterFragment::javaClass.name)
            }

            is CalendarViewModelAction.OpenCreateEvent -> calendarRouter.openCreateEvent(action.initialDateString)
        }
    }

    private fun handleSharedViewModelAction(action: SharedCalendarAction) {
        when (action) {
            is SharedCalendarAction.RefreshDays -> action.days.forEach {
                viewModel.handleAction(CalendarAction.RefreshDay(it))
            }

            is SharedCalendarAction.RefreshCalendar -> viewModel.handleAction(CalendarAction.RefreshCalendar)

            is SharedCalendarAction.FiltersClosed -> {
                applyTheme()
                if (action.changed) {
                    viewModel.handleAction(CalendarAction.FiltersRefreshed)
                }
            }

            SharedCalendarAction.TodayButtonTapped -> viewModel.handleAction(CalendarAction.TodayTapped)

            else -> {}
        }
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(R.string.calendar)

    override fun applyTheme() {
        ViewStyler.setStatusBarDark(requireActivity(), ThemePrefs.primaryColor)
        calendarRouter.attachNavigationDrawer()
    }

    override fun getFragment(): Fragment? {
        return this
    }

    override fun onHandleBackPressed(): Boolean {
        return false
    }

    open fun showToolbar(): Boolean = true

    protected fun refreshCalendar() {
        viewModel.handleAction(CalendarAction.RefreshCalendar)
    }
}