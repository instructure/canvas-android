/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

package com.instructure.student.features.grades

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.analytics.SCREEN_VIEW_GRADES_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsFragment
import com.instructure.pandautils.features.grades.AppBarUiState
import com.instructure.pandautils.features.grades.COURSE_ID_KEY
import com.instructure.pandautils.features.grades.GradesScreen
import com.instructure.pandautils.features.grades.GradesViewModel
import com.instructure.pandautils.features.grades.GradesViewModelAction
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.R
import com.instructure.student.activity.NavigationActivity
import com.instructure.student.fragment.ParentFragment
import com.instructure.student.router.RouteMatcher
import dagger.hilt.android.AndroidEntryPoint

@ScreenView(SCREEN_VIEW_GRADES_LIST)
@PageView(url = "{canvasContext}/grades")
@AndroidEntryPoint
class GradesFragment : ParentFragment(), Bookmarkable {

    private val viewModel: GradesViewModel by viewModels()

    @get:PageViewUrlParam("canvasContext")
    var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)

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
                GradesScreen(
                    uiState = uiState,
                    actionHandler = viewModel::handleAction,
                    appBarUiState = AppBarUiState(
                        title = title(),
                        subtitle = canvasContext.name.orEmpty(),
                        navigationActionClick = {
                            activity?.onBackPressed()
                        },
                        bookmarkable = (activity as? NavigationActivity)?.canBookmark().orDefault(),
                        addBookmarkClick = {
                            if (APIHelper.hasNetworkConnection()) {
                                (activity as? NavigationActivity)?.addBookmark()
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show()
                            }
                        }
                    ),
                    canvasContextColor = canvasContext.color
                )
            }
        }
    }

    override fun applyTheme() {
        ViewStyler.setStatusBarDark(requireActivity(), canvasContext.color)
    }

    override fun title(): String = getString(R.string.grades)

    override fun getSelectedParamName(): String = RouterParams.ASSIGNMENT_ID

    private fun handleAction(action: GradesViewModelAction) {
        when (action) {
            is GradesViewModelAction.NavigateToAssignmentDetails -> {
                RouteMatcher.route(
                    requireActivity(),
                    AssignmentDetailsFragment.makeRoute(canvasContext, action.assignmentId)
                )
            }
        }
    }

    companion object {
        fun newInstance(route: Route): GradesFragment? {
            val canvasContext = route.canvasContext
            if (!validRoute(route) || canvasContext == null) return null
            route.arguments.putLong(COURSE_ID_KEY, canvasContext.id)
            return GradesFragment().withArgs(route.arguments)
        }

        private fun validRoute(route: Route): Boolean {
            return route.canvasContext != null && route.canvasContext is Course
        }

        fun makeRoute(canvasContext: CanvasContext): Route {
            return Route(
                null,
                GradesFragment::class.java,
                canvasContext,
                canvasContext.makeBundle()
            )
        }
    }
}
