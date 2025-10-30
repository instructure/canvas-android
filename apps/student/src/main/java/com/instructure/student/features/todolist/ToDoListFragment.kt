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
package com.instructure.student.features.todolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_TO_DO_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.features.todolist.ToDoListRouter
import com.instructure.pandautils.features.todolist.ToDoListScreen
import com.instructure.pandautils.features.todolist.ToDoListViewModel
import com.instructure.pandautils.features.todolist.ToDoListViewModelAction
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@PageView
@ScreenView(SCREEN_VIEW_TO_DO_LIST)
@AndroidEntryPoint
class ToDoListFragment : BaseCanvasFragment(), FragmentInteractions, NavigationCallbacks {

    private val viewModel: ToDoListViewModel by viewModels()

    @Inject
    lateinit var toDoListRouter: ToDoListRouter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        applyTheme()
        viewLifecycleOwner.lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)

        return ComposeView(requireActivity()).apply {
            setContent {
                CanvasTheme {
                    val uiState by viewModel.uiState.collectAsState()

                    ToDoListScreen(
                        uiState = uiState,
                        actionHandler = viewModel::handleAction,
                        navigationIconClick = { toDoListRouter.openNavigationDrawer() }
                    )
                }
            }
        }
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(R.string.Todo)

    override fun applyTheme() {
        ViewStyler.setStatusBarDark(requireActivity(), ThemePrefs.primaryColor)
        toDoListRouter.attachNavigationDrawer()
    }

    override fun getFragment(): Fragment = this

    private fun handleAction(action: ToDoListViewModelAction) {
        when (action) {
            is ToDoListViewModelAction.OpenToDoItem -> toDoListRouter.openToDoItem(action.itemId)
        }
    }

    override fun onHandleBackPressed(): Boolean {
        return false
    }

    companion object {
        fun makeRoute(canvasContext: CanvasContext): Route = Route(ToDoListFragment::class.java, canvasContext, Bundle())

        private fun validateRoute(route: Route) = route.canvasContext != null

        fun newInstance(route: Route): ToDoListFragment? {
            if (!validateRoute(route)) return null
            return ToDoListFragment().withArgs(route.canvasContext!!.makeBundle())
        }
    }
}