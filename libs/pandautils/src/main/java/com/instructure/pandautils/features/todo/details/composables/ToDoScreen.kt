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
package com.instructure.pandautils.features.todo.details.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
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
import com.instructure.pandautils.features.todo.details.ToDoUiState
import com.instructure.pandautils.utils.ThemePrefs
import com.jakewharton.threetenabp.AndroidThreeTen

@ExperimentalFoundationApi
@Composable
fun ToDoScreen(
    title: String,
    toDoUiState: ToDoUiState,
    modifier: Modifier = Modifier,
    navigationActionClick: () -> Unit
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = title)
                    },
                    actions = {
                        OverflowMenu {
                            DropdownMenuItem(
                                onClick = {

                                }
                            ) {
                                Text(
                                    color = colorResource(id = R.color.textDarkest),
                                    text = stringResource(id = R.string.edit),
                                )
                            }
                            DropdownMenuItem(
                                onClick = {

                                }
                            ) {
                                Text(
                                    color = colorResource(id = R.color.textDarkest),
                                    text = stringResource(id = R.string.delete),
                                )
                            }
                        }
                    },
                    backgroundColor = Color(ThemePrefs.primaryColor),
                    contentColor = Color(ThemePrefs.primaryTextColor),
                    navigationIcon = {
                        IconButton(onClick = navigationActionClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back_arrow),
                                contentDescription = stringResource(id = R.string.back)
                            )
                        }
                    }
                )
            },
            content = { padding ->
                Surface(
                    modifier = modifier
                        .padding(padding)
                        .fillMaxSize(),
                    color = colorResource(id = R.color.backgroundLightest)
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = toDoUiState.title,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = colorResource(id = R.color.textDarkest),
                            fontSize = 22.sp
                        )
                        if (!toDoUiState.contextName.isNullOrEmpty() && toDoUiState.contextColor != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = toDoUiState.contextName,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color(toDoUiState.contextColor),
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                        Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(id = R.string.todoDateLabel),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = colorResource(id = R.color.textDark),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = toDoUiState.date,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = colorResource(id = R.color.textDarkest),
                            fontSize = 16.sp
                        )
                        if (toDoUiState.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(28.dp))
                            Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = stringResource(id = R.string.todoDescriptionLabel),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = colorResource(id = R.color.textDark),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = toDoUiState.description,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = colorResource(id = R.color.textDarkest),
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        )
    }
}

@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
    ContextKeeper.appContext = LocalContext.current
    AndroidThreeTen.init(LocalContext.current)
    ToDoScreen(
        title = "To Do",
        toDoUiState = ToDoUiState(
            title = "Submit Creative Machines and Innovative Instrumentation - ASTR 21400",
            contextName = "Course",
            contextColor = android.graphics.Color.RED,
            date = "2023. March 31. 23:59",
            description = "The Assignment Details page displays the assignment title, points possible, submission status, and due date [1]. You can also view the assignment's submission types [2], as well as acceptable file types for file uploads if restricted by your instructor [3]."
        ),
        navigationActionClick = {}
    )
}