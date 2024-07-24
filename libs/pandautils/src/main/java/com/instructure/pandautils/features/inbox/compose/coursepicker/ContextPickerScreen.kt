/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.inbox.compose.coursepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.features.inbox.compose.ContextPickerActionHandler
import com.instructure.pandautils.features.inbox.compose.ContextPickerUiState
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.backgroundColor

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContextPickerScreen(
    title: String,
    uiState: ContextPickerUiState,
    actionHandler: (ContextPickerActionHandler) -> Unit,
) {
    val pullToRefreshState = rememberPullRefreshState(refreshing = false, onRefresh = {
        actionHandler(ContextPickerActionHandler.RefreshCalled)
    })

    Scaffold(
        topBar = { CanvasThemedAppBar(
            title = title,
            navigationActionClick = {
                actionHandler(
                    ContextPickerActionHandler.DoneClicked
                )
            }
        )},
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .pullRefresh(pullToRefreshState)
            ) {
                LazyColumn {
                    if (!uiState.isLoading) {
                        if (uiState.courses.isNotEmpty()) {
                            item {
                                SectionHeaderView("Courses")
                            }

                            items(uiState.courses) {
                                DataRow(it, actionHandler)
                            }
                        }

                        if (uiState.groups.isNotEmpty()) {
                            item {
                                SectionHeaderView("Groups")
                            }

                            items(uiState.groups) {
                                DataRow(it, actionHandler)
                            }
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = uiState.isLoading,
                    state = pullToRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .testTag("pullRefreshIndicator"),
                )
            }
        }
    )
}


@Composable
private fun SectionHeaderView(subTitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(colorResource(id = R.color.backgroundLight))
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(
            subTitle,
            color = colorResource(id = R.color.textDark),
            fontSize = 16.sp,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun DataRow(context: CanvasContext, actionHandler: (ContextPickerActionHandler) -> Unit) {
    val color = if (context.type == CanvasContext.Type.USER) ThemePrefs.brandColor else context.backgroundColor
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(colorResource(id = R.color.backgroundLightest))
            .fillMaxWidth()
            .clickable { actionHandler(ContextPickerActionHandler.ContextClicked(context)) }
            .height(50.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(20.dp)
                .background(Color(color), CircleShape)
        )

        Text(
            context.name ?: "",
            color = colorResource(id = R.color.textDarkest),
            fontSize = 16.sp,
            modifier = Modifier
                .padding(end = 16.dp)
                .padding(vertical = 8.dp)
        )
    }
}