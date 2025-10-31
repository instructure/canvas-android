/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.student.features.appointmentgroups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.toast
import com.instructure.student.fragment.ParentFragment
import dagger.hilt.android.AndroidEntryPoint

@PageView(url = "{canvasContext}/appointment_groups")
@AndroidEntryPoint
class AppointmentGroupsListFragment : ParentFragment() {

    private var course: Course by ParcelableArg(key = Const.CANVAS_CONTEXT)

    private val viewModel: AppointmentGroupsListViewModel by viewModels()

    override fun title(): String = getString(R.string.appointmentGroups)

    override fun applyTheme() {
        ViewStyler.setStatusBarDark(requireActivity(), course.color)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                val context = LocalContext.current

                LaunchedEffect(uiState.showReservationSuccessSnackbar) {
                    if (uiState.showReservationSuccessSnackbar) {
                        context.toast(R.string.appointmentReserved)
                        uiState.snackbarShown()
                    }
                }

                LaunchedEffect(uiState.showReservationErrorSnackbar) {
                    if (uiState.showReservationErrorSnackbar) {
                        context.toast(R.string.appointmentReservationError)
                        uiState.snackbarShown()
                    }
                }

                LaunchedEffect(uiState.showCancellationSuccessSnackbar) {
                    if (uiState.showCancellationSuccessSnackbar) {
                        context.toast(R.string.appointmentCancelled)
                        uiState.snackbarShown()
                    }
                }

                LaunchedEffect(uiState.showCancellationErrorSnackbar) {
                    if (uiState.showCancellationErrorSnackbar) {
                        context.toast(R.string.appointmentCancellationError)
                        uiState.snackbarShown()
                    }
                }

                CanvasTheme(courseColor = Color(CanvasContext.emptyCourseContext(id = course.id).color)) {
                    AppointmentGroupsListScreen(
                        title = title(),
                        uiState = uiState,
                        onAction = { viewModel.handleAction(it) },
                        onRefresh = { viewModel.loadAppointmentGroups(isRefresh = true) },
                        onBack = { requireActivity().onBackPressed() }
                    )
                }
            }
        }
    }

    companion object {
        fun makeRoute(course: Course): Route {
            val bundle = course.makeBundle {
                putLong(AppointmentGroupsListViewModel.COURSE_ID, course.id)
            }
            return Route(null, AppointmentGroupsListFragment::class.java, course, bundle)
        }

        fun validRoute(route: Route): Boolean {
            return route.canvasContext is Course
        }

        fun newInstance(route: Route): AppointmentGroupsListFragment? {
            if (!validRoute(route)) return null
            return AppointmentGroupsListFragment().apply {
                arguments = route.arguments
            }
        }
    }
}