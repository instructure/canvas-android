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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.features.calendar.filter.CalendarFilterAction
import com.instructure.pandautils.features.calendar.filter.CalendarFilterItemUiState
import com.instructure.pandautils.features.calendar.filter.CalendarFilterScreenUiState
import com.instructure.pandautils.utils.ThemePrefs

@Composable
fun CalendarFiltersScreen(
    uiState: CalendarFilterScreenUiState,
    actionHandler: (CalendarFilterAction) -> Unit,
    navigationActionClick: () -> Unit
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                TopAppBarContent(
                    title = stringResource(id = R.string.calendarFilterTitle),
                    navigationActionClick = navigationActionClick
                )
            },
            content = { padding ->
                CalendarFiltersContent(
                    uiState, actionHandler, modifier = Modifier
                        .padding(padding)
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }
        )
    }
}

@Composable
private fun TopAppBarContent(
    title: String,
    navigationActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(text = title)
        },
        elevation = 2.dp,
        backgroundColor = colorResource(id = R.color.backgroundLightestElevated),
        contentColor = colorResource(id = R.color.textDarkest),
        navigationIcon = {
            IconButton(onClick = navigationActionClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = stringResource(id = R.string.back)
                )
            }
        },
        modifier = modifier
    )
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
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.calendarFilterExplanation),
                fontSize = 16.sp,
                color = colorResource(id = R.color.textDarkest),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            if (uiState.users.isNotEmpty()) {
                Text(
                    stringResource(id = R.string.calendarFilterUser),
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.textDark),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                )
            }
        }
        items(uiState.users) { user ->
            CalendarFilterItem(user, actionHandler, Modifier.fillMaxWidth())
        }
        item {
            if (uiState.courses.isNotEmpty()) {
                Text(
                    stringResource(id = R.string.calendarFilterCourse),
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.textDark),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                )
            }
        }
        items(uiState.courses) { course ->
            CalendarFilterItem(course, actionHandler, Modifier.fillMaxWidth())
        }
        item {
            if (uiState.groups.isNotEmpty()) {
                Text(
                    stringResource(id = R.string.calendarFilterGroup),
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.textDark),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                )
            }
        }
        items(uiState.groups) { group ->
            CalendarFilterItem(group, actionHandler, Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun CalendarFilterItem(uiState: CalendarFilterItemUiState, actionHandler: (CalendarFilterAction) -> Unit, modifier: Modifier = Modifier) {
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

@Preview
@Composable
fun CalendarFiltersPreview() {
    ContextKeeper.appContext = LocalContext.current
    val uiState = CalendarFilterScreenUiState(
        users = listOf(
            CalendarFilterItemUiState("user_1", "User 1", true),
        ), courses = listOf(
            CalendarFilterItemUiState("course_1", "Course 1", true),
            CalendarFilterItemUiState("course_1", "Course 1", false),
        ), groups = listOf(
            CalendarFilterItemUiState("group_1", "Group 1", false),
            CalendarFilterItemUiState("group_1", "Group 1", true),
        )
    )
    CalendarFiltersScreen(uiState, {}, {})
}