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
package com.instructure.horizon.features.learn.course.lti

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.foundation.horizonShadow
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.launchCustomTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseToolsScreen(
    courseId: Long,
    modifier: Modifier = Modifier,
    viewModel: CourseToolsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var previousCourseId: Long? by rememberSaveable { mutableStateOf(null) }
    LaunchedEffect(courseId) {
        if (courseId != previousCourseId) {
            previousCourseId = courseId
            viewModel.loadState(courseId)
        }
    }

    LoadingStateWrapper(state.screenState, modifier = modifier.padding(horizontal = 8.dp)) {
        CourseToolsContent(state)
    }
}

@Composable
private fun CourseToolsContent(uiState: CourseToolsUiState, modifier: Modifier = Modifier) {
    LazyColumn(contentPadding = PaddingValues(top = 8.dp), modifier = modifier) {
        items(uiState.ltiTools) { ltiTool ->
            LtiToolItem(ltiTool)
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun LtiToolItem(
    ltiTool: LtiToolItem,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.getActivityOrNull()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(bottom = 16.dp, start = 8.dp, end = 8.dp)
            .defaultMinSize(minHeight = 64.dp)
            .horizonShadow(HorizonElevation.level4, shape = HorizonCornerRadius.level6, clip = false)
            .background(HorizonColors.Surface.pageSecondary(), shape = HorizonCornerRadius.level6)
            .clip(HorizonCornerRadius.level6)
            .clickable {
                activity?.launchCustomTab(ltiTool.url, ThemePrefs.brandColor)
            }
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        GlideImage(
            model = ltiTool.iconUrl,
            contentDescription = ltiTool.title,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        HorizonSpace(SpaceSize.SPACE_8)
        Text(
            text = ltiTool.title,
            style = HorizonTypography.p2,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painterResource(R.drawable.open_in_new),
            contentDescription = null,
            tint = HorizonColors.Icon.default(),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview
@Composable
private fun CourseToolsScreenPreview() {
    CourseToolsContent(
        uiState = CourseToolsUiState(
            ltiTools = listOf(
                LtiToolItem("Tool 1", "https://tool1.com/icon.png", "https://tool1.com/launch"),
                LtiToolItem("Tool 2", "https://tool2.com/icon.png", "https://tool2.com/launch"),
                LtiToolItem("Tool 3", "https://tool3.com/icon.png", "https://tool3.com/launch"),
            )
        )
    )
}