/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.assignments.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.analytics.SCREEN_VIEW_ASSIGNMENT_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.withArgs
import dagger.hilt.android.AndroidEntryPoint

@ScreenView(SCREEN_VIEW_ASSIGNMENT_LIST)
@PageView(url = "assignments")
@AndroidEntryPoint
class AssignmentListFragment: BaseCanvasFragment() {
    private val viewModel: AssignmentListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        applyTheme()

        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                val contextColor = Color(uiState.course.color)

                AssignmentListScreen(uiState, contextColor)
            }
        }
    }

    private fun applyTheme() {
        ViewStyler.setStatusBarDark(requireActivity(), ThemePrefs.primaryColor)
    }

    companion object {
        fun newInstance(): AssignmentListFragment {
            return AssignmentListFragment()
        }

        fun newInstance(route: Route): AssignmentListFragment {
            route.paramsHash[RouterParams.COURSE_ID]?.let {
                route.arguments.putLong(Const.COURSE_ID, it.toLong())
            }
            return AssignmentListFragment().withArgs(route.arguments)
        }

        fun makeRoute(courseId: Long): Route {
            val bundle = bundleOf().apply {
                putLong(Const.COURSE_ID, courseId)
            }
            return Route(null, AssignmentListFragment::class.java, null, bundle)
        }
    }
}