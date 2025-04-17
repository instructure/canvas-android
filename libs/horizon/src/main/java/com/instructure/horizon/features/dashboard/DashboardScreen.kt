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
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)

package com.instructure.horizon.features.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.ProgressBar
import com.instructure.horizon.horizonui.organisms.cards.LearningObjectCard
import com.instructure.horizon.horizonui.organisms.cards.LearningObjectCardState
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.horizon.navigation.MainNavigationRoute

const val SHOULD_REFRESH_DASHBOARD = "shouldRefreshDashboard"

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun DashboardScreen(uiState: DashboardUiState, mainNavController: NavHostController) {

    val parentEntry = remember { mainNavController.getBackStackEntry("home") }
    val savedStateHandle = parentEntry.savedStateHandle

    val refreshFlow = remember { savedStateHandle.getStateFlow("shouldRefreshDashboard", false) }

    val shouldRefresh by refreshFlow.collectAsState()

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            uiState.loadingState.onRefresh()
            savedStateHandle[SHOULD_REFRESH_DASHBOARD] = false
        }
    }

    Scaffold(containerColor = HorizonColors.Surface.pagePrimary()) { paddingValues ->
        LoadingStateWrapper(loadingState = uiState.loadingState) {
            LazyColumn(contentPadding = PaddingValues(start = 24.dp, end = 24.dp), modifier = Modifier.padding(paddingValues), content = {
                item {
                    HomeScreenTopBar(uiState, modifier = Modifier.height(56.dp))
                    HorizonSpace(SpaceSize.SPACE_36)
                }
                items(uiState.coursesUiState) { courseItem ->
                    DashboardCourseItem(courseItem, onClick = {
                        mainNavController.navigate(MainNavigationRoute.ModuleItemSequence(courseItem.courseId, courseItem.nextModuleItemId))
                    })
                }
            })
        }
    }
}

@Composable
private fun HomeScreenTopBar(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.Bottom, modifier = modifier) {
        GlideImage(
            model = uiState.logoUrl,
            contentDescription = stringResource(R.string.a11y_institutionLogoContentDescription),
            modifier = Modifier.width(118.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            iconRes = R.drawable.menu_book_notebook,
            onClick = uiState.onNotebookClick,
            color = IconButtonColor.INVERSE,
            elevation = HorizonElevation.level4,
        )
        HorizonSpace(SpaceSize.SPACE_8)
        IconButton(
            iconRes = R.drawable.notifications,
            onClick = uiState.onNotificationsClick,
            elevation = HorizonElevation.level4,
            color = IconButtonColor.INVERSE
        )
        HorizonSpace(SpaceSize.SPACE_8)
        IconButton(
            iconRes = R.drawable.mail,
            onClick = uiState.onInboxClick,
            elevation = HorizonElevation.level4,
            color = IconButtonColor.INVERSE
        )
    }
}

@Composable
private fun DashboardCourseItem(courseItem: DashboardCourseUiState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(text = courseItem.courseName, style = HorizonTypography.h1)
        HorizonSpace(SpaceSize.SPACE_24)
        ProgressBar(progress = courseItem.courseProgress)
        HorizonSpace(SpaceSize.SPACE_36)
        Text(text = stringResource(R.string.dashboard_resumeLearning), style = HorizonTypography.h3)
        HorizonSpace(SpaceSize.SPACE_12)
        LearningObjectCard(
            LearningObjectCardState(
                moduleTitle = courseItem.nextModuleName,
                learningObjectTitle = courseItem.nextModuleItemName,
                progressLabel = courseItem.progressLabel,
                remainingTime = courseItem.remainingTime,
                dueDate = courseItem.dueDate,
                learningObjectType = courseItem.learningObjectType,
                onClick = onClick
            )
        )
        HorizonSpace(SpaceSize.SPACE_24)
    }
}

@Composable
@Preview
private fun DashboardScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    DashboardScreen(DashboardUiState(), mainNavController = rememberNavController())
}