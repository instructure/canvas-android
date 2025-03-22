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
@file:OptIn(ExperimentalGlideComposeApi::class)

package com.instructure.horizon.features.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.design.foundation.Colors
import com.instructure.horizon.design.molecules.ButtonPrimary
import com.instructure.horizon.design.molecules.ProgressBar
import com.instructure.horizon.design.organisms.LearningObjectCard
import com.instructure.horizon.design.organisms.LearningObjectCardState

@Composable
fun DashboardScreen(uiState: DashboardUiState) {
    Scaffold(containerColor = Colors.Surface.pagePrimary(), topBar = {
        HomeScreenTopBar(uiState, modifier = Modifier.height(48.dp))
    }, modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp)) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues), content = {
            items(uiState.coursesUiState) { courseItem ->
                DashboardCourseItem(courseItem)
            }
        })
    }
}

@Composable
private fun HomeScreenTopBar(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    Row(modifier) {
        GlideImage(
            model = uiState.logoUrl,
            contentDescription = null,
            modifier = Modifier.width(118.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        ButtonPrimary(iconRes = R.drawable.menu_book_notebook, onClick = uiState.onNotebookClick)
        Spacer(modifier = Modifier.width(8.dp))
        ButtonPrimary(iconRes = R.drawable.notifications, onClick = uiState.onNotificationsClick)
        Spacer(modifier = Modifier.width(8.dp))
        ButtonPrimary(iconRes = R.drawable.mail, onClick = uiState.onInboxClick)
    }
}

@Composable
private fun DashboardCourseItem(courseItem: DashboardCourseUiState, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(text = courseItem.courseName)
        Spacer(modifier = Modifier.height(24.dp))
        ProgressBar(progress = courseItem.courseProgress)
        Spacer(modifier = Modifier.height(36.dp))
        Text(text = courseItem.nextModuleName)
        Spacer(modifier = Modifier.height(12.dp))
        LearningObjectCard(
            LearningObjectCardState(
                moduleTitle = courseItem.nextModuleName,
                learningObjectTitle = courseItem.nextModuleItemName,
                progressLabel = courseItem.progressLabel,
                remainingTime = courseItem.remainingTime,
                dueDate = courseItem.dueDate
            )
        )
    }
}

@Composable
@Preview
private fun DashboardScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    DashboardScreen(DashboardUiState())
}