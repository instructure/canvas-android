/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.pandautils.features.dashboard.widget.courses.customize

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.router.Route
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.withArgs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomizeCourseFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CanvasTheme {
                    CustomizeCourseScreen(
                        onNavigateBack = requireActivity()::onBackPressed
                    )
                }
            }
        }
    }

    companion object {
        fun makeRoute(course: Course): Route {
            val bundle = Bundle().apply {
                putParcelable(Const.COURSE, course)
            }
            return Route(CustomizeCourseFragment::class.java, null, bundle)
        }

        fun newInstance(route: Route): CustomizeCourseFragment {
            val fragment = CustomizeCourseFragment().withArgs(route.arguments)
            return fragment
        }
    }
}
