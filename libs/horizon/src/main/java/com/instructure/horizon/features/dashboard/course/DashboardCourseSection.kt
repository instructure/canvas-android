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
package com.instructure.horizon.features.dashboard.course

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.course.card.CardClickAction
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardContent
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardError
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardLoading
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardState
import com.instructure.horizon.features.home.HomeNavigationRoute
import com.instructure.horizon.navigation.MainNavigationRoute

@Composable
fun DashboardCourseSection(
    mainNavController: NavHostController,
    homeNavController: NavHostController
) {
    val viewModel = hiltViewModel<DashboardCourseViewModel>()
    val state by viewModel.uiState.collectAsState()

    DashboardCourseSection(state, mainNavController, homeNavController)
}

@Composable
private fun DashboardCourseSection(
    state: DashboardCourseUiState,
    mainNavController: NavHostController,
    homeNavController: NavHostController
) {
    when(state.state) {
        DashboardItemState.LOADING -> {
            DashboardCourseCardLoading()
        }
        DashboardItemState.ERROR -> {
            DashboardCourseCardError(state.onRefresh)
        }
        DashboardItemState.SUCCESS -> {
            DashboardCourseSectionContent(state, mainNavController, homeNavController)
        }
    }
}

@Composable
private fun DashboardCourseSectionContent(
    state: DashboardCourseUiState,
    mainNavController: NavHostController,
    homeNavController: NavHostController
) {
    val pagerstate = rememberPagerState { state.courses.size }
    HorizontalPager(
        pagerstate,
        verticalAlignment = Alignment.Top
    ) {
        DashboardCourseItem(state.courses[it], mainNavController, homeNavController)
    }
}

@Composable
private fun DashboardCourseItem(
    cardState: DashboardCourseCardState,
    mainNavController: NavHostController,
    homeNavController: NavHostController
) {
    DashboardCourseCardContent(
        cardState, { handleClickAction(it, mainNavController, homeNavController) }
    )
}

private fun handleClickAction(
    action: CardClickAction?,
    mainNavController: NavHostController,
    homeNavController: NavHostController
) {
    when(action) {
        is CardClickAction.Action -> {
            action.onClick()
        }
        is CardClickAction.NavigateToCourse -> {
            homeNavController.navigate(HomeNavigationRoute.Learn.withCourse(action.courseId)) {
                popUpTo(homeNavController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = false
            }
        }
        is CardClickAction.NavigateToModuleItem -> {
            mainNavController.navigate(
                MainNavigationRoute.ModuleItemSequence(
                    action.courseId,
                    action.moduleItemId
                )
            )
        }
        is CardClickAction.NavigateToProgram -> {
            homeNavController.navigate(HomeNavigationRoute.Learn.withProgram(action.programId)) {
                popUpTo(homeNavController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = false
            }
        }
        else -> {}
    }
}