/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.student.features.dashboard.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.router.Route
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.features.dashboard.notifications.DashboardRouter
import com.instructure.student.fragment.ParentFragment
import com.instructure.student.features.dashboard.notifications.StudentDashboardRouter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : ParentFragment() {

    private val router: DashboardRouter by lazy { StudentDashboardRouter(requireActivity()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        applyTheme()
        return ComposeView(requireContext()).apply {
            setContent {
                CanvasTheme {
                    DashboardScreen(router = router)
                }
            }
        }
    }

    override fun title(): String = ""

    override fun applyTheme() {
        navigation?.attachNavigationDrawer(this, null)
    }

    companion object {
        fun makeRoute(canvasContext: CanvasContext?) =
            Route(DashboardFragment::class.java, canvasContext)

        fun newInstance(route: Route): DashboardFragment {
            val fragment = DashboardFragment()
            fragment.arguments = route.arguments
            return fragment
        }
    }
}