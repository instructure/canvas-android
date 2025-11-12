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
package com.instructure.horizon.features.dashboard.widget.course.list

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardCard
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCourseListScreen(
    state: DashboardCourseListUiState,
    mainNavController: NavHostController,
) {
    LoadingStateWrapper(state.loadingState) {
        Scaffold(
            containerColor = HorizonColors.Surface.pagePrimary(),
            topBar = { DashboardCourseListTopBar(mainNavController) }
        ) {
            LazyColumn {
                items(state.courses) {
                    CourseItemCard(it)
                }
            }
        }
    }
}

@Composable
private fun CourseItemCard(courseState: DashboardCourseListCourseState) {
    DashboardCard {
        
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardCourseListTopBar(mainNavController: NavHostController) {
    CenterAlignedTopAppBar(
       colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HorizonColors.Surface.pagePrimary(),
            titleContentColor = HorizonColors.Text.title(),
            navigationIconContentColor = HorizonColors.Icon.default()
        ),
        title = {
            Text(
                stringResource(R.string.dashboardCourseListTitle),
                style = HorizonTypography.h3,
                color = HorizonColors.Text.title()
            )
        },
        navigationIcon = {
            IconButton(
                iconRes = R.drawable.arrow_back,
                contentDescription = stringResource(R.string.a11yNavigateBack),
                color = IconButtonColor.Inverse,
                size = IconButtonSize.SMALL,
                elevation = HorizonElevation.level4,
                onClick = { mainNavController.popBackStack() },
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        },
    )
}