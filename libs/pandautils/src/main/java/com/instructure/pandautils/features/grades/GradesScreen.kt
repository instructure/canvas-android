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

package com.instructure.pandautils.features.grades

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.Loading


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GradesScreen(
    uiState: GradesUiState,
    actionHandler: (GradesAction) -> Unit
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
        ) { padding ->
            val pullRefreshState = rememberPullRefreshState(
                refreshing = uiState.isRefreshing,
                onRefresh = {
                    actionHandler(GradesAction.Refresh)
                }
            )
            Box(
                modifier = Modifier
                    .padding(padding)
                    .pullRefresh(pullRefreshState)
            ) {
                when {
                    uiState.isError -> {
                        // Error
                    }

                    uiState.isLoading -> {
                        Loading(
                            color = Color(uiState.studentColor),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("loading")
                        )
                    }

                    uiState.items.isEmpty() -> {
                        // Empty
                    }

                    else -> {
                        GradesScreenContent(
                            uiState = uiState,
                            userColor = uiState.studentColor,
                            actionHandler = actionHandler
                        )
                    }
                }
                PullRefreshIndicator(
                    refreshing = uiState.isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .testTag("pullRefreshIndicator"),
                    contentColor = Color(uiState.studentColor)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GradesScreenContent(
    uiState: GradesUiState,
    userColor: Int,
    actionHandler: (GradesAction) -> Unit
) {
    LazyColumn {
        uiState.items.forEach {
            stickyHeader {
                Column(
                    modifier = Modifier
                        .background(colorResource(id = R.color.backgroundLightest))
                        .clickable {
                            actionHandler(GradesAction.HeaderClick(it.id))
                        }
                ) {
                    Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                    ) {
                        Text(
                            text = it.name,
                            color = colorResource(id = R.color.textDark),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_down),
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(if (it.expanded) 180f else 0f)
                        )
                    }
                    Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
                }
            }

            if (it.expanded) {
                items(it.assignments) { assignment ->
                    AssignmentItem(assignment, userColor)
                }
            }
        }
    }
}

@Composable
private fun AssignmentItem(
    uiState: AssignmentUiState,
    userColor: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Icon(
            painter = painterResource(id = uiState.iconRes),
            contentDescription = null
        )
        Column {
            Text(
                text = uiState.name,
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp
            )
            Row {
                Text(
                    text = uiState.dueDate.orEmpty(),
                    color = colorResource(id = R.color.textDark),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Graded")
            }
            Text(
                text = "${uiState.points}/${uiState.pointsPossible}",
                color = Color(userColor),
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GradesScreenPreview() {
    GradesScreen(
        uiState = GradesUiState(
            items = listOf(
                AssignmentGroupUiState(
                    id = 1,
                    name = "Assignment Group 1",
                    assignments = listOf(
                        AssignmentUiState(
                            iconRes = R.drawable.ic_assignment,
                            name = "Assignment 1",
                            dueDate = "Due Date",
                            points = "10",
                            pointsPossible = "20"
                        ),
                        AssignmentUiState(
                            iconRes = R.drawable.ic_assignment,
                            name = "Assignment 2",
                            dueDate = "Due Date",
                            points = "10",
                            pointsPossible = "20"
                        )
                    ),
                    expanded = true
                ),
            )
        ),
        actionHandler = {}
    )
}