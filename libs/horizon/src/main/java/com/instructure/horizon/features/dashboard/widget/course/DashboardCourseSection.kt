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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardCard
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCard
import com.instructure.horizon.features.dashboard.widget.course.card.CardClickAction
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardContent
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardError
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardState
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardMoreCourseCard
import com.instructure.horizon.features.home.HomeNavigationRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.horizonShadow
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonIconPosition
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.organisms.AnimatedHorizontalPager
import com.instructure.horizon.navigation.MainNavigationRoute
import com.instructure.pandautils.utils.toDp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.min

@Composable
fun DashboardCourseSection(
    mainNavController: NavHostController,
    homeNavController: NavHostController,
    shouldRefresh: Boolean,
    refreshState: MutableStateFlow<List<Boolean>>,
    modifier: Modifier = Modifier,
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

    DashboardCourseSection(state, mainNavController, homeNavController, modifier)
}

@Composable
fun DashboardCourseSection(
    state: DashboardCourseUiState,
    mainNavController: NavHostController,
    homeNavController: NavHostController,
    modifier: Modifier = Modifier,
) {
    when(state.state) {
        DashboardItemState.LOADING -> {
            DashboardCourseCardContent(
                DashboardCourseCardState.Loading,
                { handleClickAction(it, mainNavController, homeNavController) },
                true,
                modifier = modifier.padding(horizontal = 24.dp)
            )
        }
        DashboardItemState.ERROR -> {
            DashboardCourseCardError({state.onRefresh {} }, modifier.padding(horizontal = 24.dp))
        }
        DashboardItemState.SUCCESS -> {
            DashboardCourseSectionContent(state, mainNavController, homeNavController, modifier)
        }
    }
}

@Composable
private fun DashboardCourseSectionContent(
    state: DashboardCourseUiState,
    mainNavController: NavHostController,
    homeNavController: NavHostController,
    modifier: Modifier = Modifier,
) {
    // Display 4 cards at most
    val pagerState = rememberPagerState { min(4, state.courses.size) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (state.programs.items.isNotEmpty()) {
            DashboardPaginatedWidgetCard(
                state.programs,
                mainNavController,
                homeNavController,
            )
        }

        if (state.courses.isNotEmpty()) {
            var maxCardHeight by remember { mutableStateOf(0) }
            AnimatedHorizontalPager(
                pagerState,
                beyondViewportPageCount = pagerState.pageCount,
                contentPadding = PaddingValues(horizontal = 24.dp),
                pageSpacing = 12.dp,
                verticalAlignment = Alignment.CenterVertically,
            ) { index, modifier ->
                when (index) {
                    in 0..2 -> {
                        DashboardCourseItem(
                            state.courses[index],
                            mainNavController,
                            homeNavController,
                            modifier.padding(bottom = 12.dp)
                                .onGloballyPositioned { coordinates ->
                                    val cardHeight = coordinates.size.height
                                    if (cardHeight > maxCardHeight) { maxCardHeight = cardHeight }
                                }
                                .semantics {
                                    contentDescription = ""
                                }
                        )
                    }
                    else -> {
                        DashboardMoreCourseCard(
                            state.courses.size,
                            modifier
                                .padding(bottom = 12.dp)
                                .height(maxCardHeight.toDp.dp)
                        ) {
                            homeNavController.navigate(HomeNavigationRoute.CourseList.route)
                        }
                    }
                }
            }

            Button(
                stringResource(R.string.dashboardSeeAllCoursesLabel),
                onClick = {
                    homeNavController.navigate(HomeNavigationRoute.CourseList.route)
                },
                width = ButtonWidth.FILL,
                color = ButtonColor.WhiteWithOutline,
                iconPosition = ButtonIconPosition.End(R.drawable.arrow_forward),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .horizonShadow(HorizonElevation.level4, HorizonCornerRadius.level6)
            )
        } else {
            DashboardCard(
                Modifier.padding(horizontal = 24.dp)
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
            cardState,
            { handleClickAction(it, mainNavController, homeNavController) },
            false
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
            homeNavController.navigate(HomeNavigationRoute.Learn.route) {
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
            homeNavController.navigate(HomeNavigationRoute.Learn.route) {
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