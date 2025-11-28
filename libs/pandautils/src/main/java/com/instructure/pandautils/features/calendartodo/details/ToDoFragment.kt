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

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_CALENDAR_TODO
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
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
class ToDoFragment : BaseCanvasFragment(), NavigationCallbacks, FragmentInteractions {

    @Inject
    lateinit var toDoRouter: ToDoRouter

    private val viewModel: ToDoViewModel by viewModels()

    @Inject
    lateinit var sharedEvents: CalendarSharedEvents

    private val notificationsPermissionContract = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

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
                sharedEvents.sendEvent(lifecycleScope, SharedCalendarAction.RefreshToDoList)
                navigateBack()
            }

            is ToDoViewModelAction.OpenEditToDo -> {
                toDoRouter.openEditToDo(action.plannerItem)
            }

            is ToDoViewModelAction.OnReminderAddClicked -> {
                checkAlarmPermission()
            }
        }
    }

    private fun handleSharedViewModelAction(action: SharedCalendarAction) {
        when (action) {
            is SharedCalendarAction.CloseToDoScreen -> activity?.onBackPressed()
            else -> {}
        }
    }

    private fun checkAlarmPermission() {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && requireActivity().checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            viewModel.checkingNotificationPermission = true
            notificationsPermissionContract.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                viewModel.showCreateReminderDialog(requireContext(), ThemePrefs.textButtonColor)
            } else {
                viewModel.checkingReminderPermission = true
                startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + requireContext().packageName)
                    )
                )
            }
        } else {
            viewModel.showCreateReminderDialog(requireContext(), ThemePrefs.textButtonColor)
        }
    }

    private fun checkAlarmPermissionResult() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && viewModel.checkingNotificationPermission) {
            if (requireActivity().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                checkAlarmPermission()
            } else {
                Snackbar.make(requireView(), getString(R.string.notificationPermissionNotGrantedError), Snackbar.LENGTH_LONG).show()
            }
            viewModel.checkingNotificationPermission = false
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && viewModel.checkingReminderPermission) {
            if ((requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()) {
                viewModel.showCreateReminderDialog(requireContext(), ThemePrefs.textButtonColor)
            } else {
                Snackbar.make(requireView(), getString(R.string.reminderPermissionNotGrantedError), Snackbar.LENGTH_LONG).show()
            }
            viewModel.checkingReminderPermission = false
        }
    }

    override fun onResume() {
        super.onResume()
        checkAlarmPermissionResult()
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
        const val PLANNER_ITEM = "PLANNER_ITEM"
        const val PLANNABLE_ID = "PLANNABLE_ID"
        fun newInstance(route: Route): ToDoFragment {
            return ToDoFragment().withArgs(
                route.arguments.apply {
                    if (route.paramsHash.containsKey(PLANNABLE_ID)) {
                        putLong(PLANNABLE_ID, route.paramsHash[PLANNABLE_ID]?.toLongOrNull() ?: 0)
                    }
                }
            )
        }

        fun makeRoute(plannerItem: PlannerItem): Route {
            val bundle = bundleOf(PLANNER_ITEM to plannerItem)
            return Route(ToDoFragment::class.java, null, bundle)
        }
    }
}
