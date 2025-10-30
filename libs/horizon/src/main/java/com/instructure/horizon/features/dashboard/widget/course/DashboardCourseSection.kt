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
package com.instructure.horizon.features.dashboard.widget.course

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardCard
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCard
import com.instructure.horizon.features.dashboard.widget.course.card.CardClickAction
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardContent
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardError
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardLoading
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardState
import com.instructure.horizon.features.home.HomeNavigationRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.organisms.AnimatedHorizontalPager
import com.instructure.horizon.horizonui.organisms.AnimatedHorizontalPagerIndicator
import com.instructure.horizon.navigation.MainNavigationRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@Composable
fun DashboardCourseSection(
    mainNavController: NavHostController,
    homeNavController: NavHostController,
    shouldRefresh: Boolean,
    refreshState: MutableStateFlow<List<Boolean>>
) {
    val viewModel = hiltViewModel<DashboardCourseViewModel>()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            refreshState.update { it + true }
            state.onRefresh {
                refreshState.update { it - true }
            }
        }
    }

    DashboardCourseSection(state, mainNavController, homeNavController)
}

@Composable
fun DashboardCourseSection(
    state: DashboardCourseUiState,
    mainNavController: NavHostController,
    homeNavController: NavHostController
) {
    when(state.state) {
        DashboardItemState.LOADING -> {
            DashboardCourseCardLoading(Modifier.padding(horizontal = 16.dp))
        }
        DashboardItemState.ERROR -> {
            DashboardCourseCardError({state.onRefresh {} }, Modifier.padding(horizontal = 16.dp))
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DashboardPaginatedWidgetCard(
            state.programs,
            mainNavController,
            homeNavController,
        )

        if (state.courses.isNotEmpty()) {
            AnimatedHorizontalPager(
                pagerstate,
                beyondViewportPageCount = pagerstate.pageCount,
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 4.dp,
                verticalAlignment = Alignment.CenterVertically,
            ) { index, modifier ->
                DashboardCourseItem(
                    state.courses[index],
                    mainNavController,
                    homeNavController,
                    modifier
                )
            }

            if (pagerstate.pageCount >= 4) {
                AnimatedHorizontalPagerIndicator(pagerstate)
            }

            Spacer(Modifier.height(16.dp))
        } else {
            DashboardCard(
                Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    stringResource(R.string.dashboardNoCoursesMessage),
                    style = HorizonTypography.h4,
                    color = HorizonColors.Text.body(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardCourseItem(
    cardState: DashboardCourseCardState,
    mainNavController: NavHostController,
    homeNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth()
    ){
        DashboardCourseCardContent(
            cardState, { handleClickAction(it, mainNavController, homeNavController) }
        )
    }
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