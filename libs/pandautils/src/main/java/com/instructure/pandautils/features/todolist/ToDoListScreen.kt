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
package com.instructure.pandautils.features.todolist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.courseOrUserColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ToDoListScreen(
    uiState: ToDoListUiState,
    actionHandler: (ToDoListActionHandler) -> Unit,
    modifier: Modifier = Modifier,
    navigationIconClick: () -> Unit = {}
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = { actionHandler(ToDoListActionHandler.Refresh) }
    )

    Scaffold(
        backgroundColor = colorResource(R.color.backgroundLightest),
        topBar = {
            CanvasThemedAppBar(
                title = stringResource(id = R.string.Todo),
                navIconRes = R.drawable.ic_hamburger,
                navIconContentDescription = stringResource(id = R.string.navigation_drawer_open),
                navigationActionClick = navigationIconClick,
                actions = {
                    IconButton(onClick = { actionHandler(ToDoListActionHandler.FilterClicked) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filter_outline),
                            contentDescription = stringResource(id = R.string.a11y_contentDescriptionToDoFilter)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            when {
                uiState.isLoading -> {
                    Loading(modifier = Modifier.align(Alignment.Center))
                }

                uiState.isError -> {
                    ErrorContent(
                        errorMessage = stringResource(id = R.string.errorLoadingToDos),
                        retryClick = { actionHandler(ToDoListActionHandler.Refresh) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.itemsByDate.isEmpty() -> {
                    EmptyContent(
                        emptyTitle = stringResource(id = R.string.noToDosForNow),
                        emptyMessage = stringResource(id = R.string.noToDosForNowSubtext),
                        imageRes = R.drawable.ic_no_events,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    ToDoListContent(
                        itemsByDate = uiState.itemsByDate,
                        actionHandler = actionHandler
                    )
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = colorResource(id = R.color.backgroundLightest),
                contentColor = Color(ThemePrefs.brandColor)
            )
        }
    }
}

@Composable
private fun ToDoListContent(
    itemsByDate: Map<Date, List<ToDoItemUiState>>,
    actionHandler: (ToDoListActionHandler) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateGroups = itemsByDate.entries.toList()
    LazyColumn(modifier = modifier.fillMaxSize()) {
        dateGroups.forEachIndexed { groupIndex, (date, items) ->
            items.forEachIndexed { index, item ->
                item(key = item.id) {
                    ToDoItem(
                        item = item,
                        showDateBadge = index == 0,
                        onCheckedChange = { actionHandler(ToDoListActionHandler.ToggleItemChecked(item.id)) },
                        onClick = { actionHandler(ToDoListActionHandler.ItemClicked(item.id)) }
                    )
                }
            }

            if (groupIndex < dateGroups.size - 1) {
                item(key = "divider_$date") {
                    CanvasDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ToDoItem(
    item: ToDoItemUiState,
    showDateBadge: Boolean,
    onCheckedChange: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance().apply {
        time = item.date
    }
    val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(item.date)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = SimpleDateFormat("MMM", Locale.getDefault()).format(item.date)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .padding(end = 12.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (showDateBadge) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayOfWeek,
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.textDark)
                    )
                    Text(
                        text = day.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.textDark)
                    )
                    Text(
                        text = month,
                        fontSize = 10.sp,
                        color = colorResource(id = R.color.textDark)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val contextColor = Color(item.canvasContext.courseOrUserColor)
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = null,
                        tint = contextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    CanvasDivider(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.contextLabel,
                        fontSize = 14.sp,
                        color = contextColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.textDarkest),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                item.dateLabel?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.textDark),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onCheckedChange() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(ThemePrefs.brandColor),
                    uncheckedColor = colorResource(id = R.color.textDark)
                )
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ToDoListScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    val calendar = Calendar.getInstance()
    CanvasTheme {
        ToDoListScreen(
            uiState = ToDoListUiState(
                itemsByDate = mapOf(
                    Date(10) to listOf(
                        ToDoItemUiState(
                            id = "1",
                            title = "Short title",
                            date = calendar.apply { set(2024, 9, 22, 7, 59) }.time,
                            dateLabel = "7:59 AM",
                            contextLabel = "COURSE",
                            canvasContext = CanvasContext.emptyCourseContext(1),
                            itemType = ToDoItemType.ASSIGNMENT,
                            isChecked = false
                        ),
                        ToDoItemUiState(
                            id = "2",
                            title = "Levitate an object without crushing it, bonus points if you don't scratch the paint",
                            date = calendar.apply { set(2024, 9, 22, 11, 59) }.time,
                            dateLabel = "11:59 AM",
                            contextLabel = "Introduction to Advanced Galactic Force Manipulation and Control Techniques for Beginners",
                            canvasContext = CanvasContext.emptyCourseContext(1),
                            itemType = ToDoItemType.QUIZ,
                            isChecked = false
                        ),
                        ToDoItemUiState(
                            id = "3",
                            title = "Identify which emotions lead to Jedi calmness vs. a full Darth Vader office meltdown situation",
                            date = calendar.apply { set(2024, 9, 22, 14, 30) }.time,
                            dateLabel = "2:30 PM",
                            contextLabel = "FORC 101",
                            canvasContext = CanvasContext.emptyCourseContext(1),
                            itemType = ToDoItemType.ASSIGNMENT,
                            isChecked = true
                        )
                    ),
                    Date(1000) to listOf(
                        ToDoItemUiState(
                            id = "4",
                            title = "Essay - Why Force-choking co-workers is frowned upon in most galactic workplaces",
                            date = calendar.apply { set(2024, 9, 23, 19, 0) }.time,
                            dateLabel = "7:00 PM",
                            contextLabel = "Professional Jedi Ethics and Workplace Communication",
                            canvasContext = CanvasContext.emptyCourseContext(1),
                            itemType = ToDoItemType.DISCUSSION,
                            isChecked = false
                        ),
                        ToDoItemUiState(
                            id = "5",
                            title = "Q",
                            date = calendar.apply { set(2024, 9, 23, 23, 59) }.time,
                            dateLabel = "11:59 PM",
                            contextLabel = "PHY",
                            canvasContext = CanvasContext.emptyCourseContext(1),
                            itemType = ToDoItemType.PLANNER_NOTE,
                            isChecked = false
                        ),
                        ToDoItemUiState(
                            id = "6",
                            title = "Write a comprehensive research paper analyzing the psychological and physiological effects of prolonged exposure to the Dark Side of the Force on Jedi Knights and their ability to maintain emotional equilibrium",
                            date = calendar.apply { set(2024, 9, 23, 23, 59) }.time,
                            dateLabel = "11:59 PM",
                            contextLabel = "Advanced Force Psychology",
                            canvasContext = CanvasContext.emptyCourseContext(1),
                            itemType = ToDoItemType.ASSIGNMENT,
                            isChecked = false
                        )
                    )
                )
            ),
            actionHandler = {}
        )
    }
}

@Preview(name = "Empty Light Mode", showBackground = true)
@Preview(name = "Empty Dark Mode", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ToDoListScreenEmptyPreview() {
    ContextKeeper.appContext = LocalContext.current
    CanvasTheme {
        ToDoListScreen(
            uiState = ToDoListUiState(),
            actionHandler = {}
        )
    }
}
