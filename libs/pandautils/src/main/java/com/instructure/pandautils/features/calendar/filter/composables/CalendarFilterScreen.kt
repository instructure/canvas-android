/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.calendar.filter.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.features.calendar.filter.CalendarFilterAction
import com.instructure.pandautils.features.calendar.filter.CalendarFilterItemUiState
import com.instructure.pandautils.features.calendar.filter.CalendarFilterScreenUiState
import com.instructure.pandautils.utils.ThemePrefs
import kotlinx.coroutines.launch

private const val USER_KEY = "user"
private const val COURSES_KEY = "courses"
private const val GROUPS_KEY = "groups"
private const val EXPLANATION_KEY = "explanation"
private const val HEADER_CONTENT_TYPE = "header"
private const val FILTER_ITEM_CONTENT_TYPE = "filter_item"
private const val EXPLANATION_CONTENT_TYPE = "explanation"

@Composable
fun CalendarFiltersScreen(
    uiState: CalendarFilterScreenUiState,
    actionHandler: (CalendarFilterAction) -> Unit,
    navigationActionClick: () -> Unit
) {
    CanvasTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        val localCoroutineScope = rememberCoroutineScope()
        if (uiState.snackbarMessage != null) {
            LaunchedEffect(Unit) {
                localCoroutineScope.launch {
                    val result = snackbarHostState.showSnackbar(uiState.snackbarMessage)
                    if (result == SnackbarResult.Dismissed) {
                        actionHandler(CalendarFilterAction.SnackbarDismissed)
                    }
                }
            }
        }
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                CanvasAppBar(
                    title = stringResource(id = R.string.calendarFilterTitle),
                    navigationActionClick = navigationActionClick
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            content = { padding ->
                if (uiState.error) {
                    ErrorContent(errorMessage = stringResource(id = R.string.calendarFiltersFailed), retryClick = {
                        actionHandler(CalendarFilterAction.Retry)
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight())
                } else if (uiState.loading) {
                    Loading(modifier = Modifier.fillMaxSize())
                } else {
                    CalendarFiltersContent(
                        uiState, actionHandler, modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    )
                }
            }
        )
    }
}

@Composable
private fun CalendarFiltersContent(
    uiState: CalendarFilterScreenUiState,
    actionHandler: (CalendarFilterAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        item(key = EXPLANATION_KEY, contentType = EXPLANATION_CONTENT_TYPE) {
            Spacer(modifier = Modifier.height(24.dp))
            val explanationText = if (uiState.calendarLimit != -1) {
                stringResource(id = R.string.calendarFilterExplanationLimited, uiState.calendarLimit)
            } else {
                stringResource(id = R.string.calendarFilterExplanation)
            }
            Text(
                text = explanationText,
                fontSize = 16.sp,
                color = colorResource(id = R.color.textDarkest),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        if (uiState.users.isNotEmpty()) {
            item(key = USER_KEY, contentType = HEADER_CONTENT_TYPE) {
                HeaderItem(text = stringResource(id = R.string.calendarFilterUser), modifier = Modifier.padding(bottom = 8.dp))
            }
            items(uiState.users, key = { it.contextId }, contentType = { FILTER_ITEM_CONTENT_TYPE }) { user ->
                CalendarFilterItem(user, actionHandler, Modifier.fillMaxWidth())
            }
        }
        if (uiState.courses.isNotEmpty()) {
            item(key = COURSES_KEY, contentType = HEADER_CONTENT_TYPE) {
                HeaderItem(text = stringResource(id = R.string.calendarFilterCourse), modifier = Modifier.padding(vertical = 8.dp))
            }
            items(uiState.courses, key = { it.contextId }, contentType = { FILTER_ITEM_CONTENT_TYPE }) { course ->
                CalendarFilterItem(course, actionHandler, Modifier.fillMaxWidth())
            }
        }
        if (uiState.groups.isNotEmpty()) {
            item(key = GROUPS_KEY, contentType = HEADER_CONTENT_TYPE) {
                HeaderItem(text = stringResource(id = R.string.calendarFilterGroup), modifier = Modifier.padding(vertical = 8.dp))
            }
            items(uiState.groups, key = { it.contextId }, contentType = { FILTER_ITEM_CONTENT_TYPE }) { group ->
                CalendarFilterItem(group, actionHandler, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun CalendarFilterItem(uiState: CalendarFilterItemUiState, actionHandler: (CalendarFilterAction) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 54.dp)
            .clickable {
                actionHandler(CalendarFilterAction.ToggleFilter(uiState.contextId))
            }
            .padding(start = 8.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = uiState.selected, onCheckedChange = {
                actionHandler(CalendarFilterAction.ToggleFilter(uiState.contextId))
            }, colors = CheckboxDefaults.colors(
                checkedColor = Color(ThemePrefs.brandColor),
                uncheckedColor = colorResource(id = R.color.textDarkest),
                checkmarkColor = colorResource(id = R.color.white)
            )
        )
        Text(uiState.name, color = colorResource(id = R.color.textDarkest), fontSize = 16.sp)
    }
}

@Composable
private fun HeaderItem(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        fontSize = 14.sp,
        color = colorResource(id = R.color.textDark),
        modifier = modifier.padding(horizontal = 16.dp)
    )
}

@Preview
@Composable
fun CalendarFiltersPreview() {
    ContextKeeper.appContext = LocalContext.current
    val uiState = CalendarFilterScreenUiState(
        users = listOf(
            CalendarFilterItemUiState("user_1", "User 1", true),
        ), courses = listOf(
            CalendarFilterItemUiState("course_1", "Course 1", true),
            CalendarFilterItemUiState("course_2", "Course 2", false),
        ), groups = listOf(
            CalendarFilterItemUiState("group_1", "Group 1", false),
            CalendarFilterItemUiState("group_2", "Group 2", true),
        )
    )
    CalendarFiltersScreen(uiState, {}, {})
}