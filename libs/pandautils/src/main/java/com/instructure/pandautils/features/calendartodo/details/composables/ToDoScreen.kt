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
package com.instructure.pandautils.features.calendartodo.details.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.FullScreenError
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.OverflowMenu
import com.instructure.pandautils.compose.composables.SimpleAlertDialog
import com.instructure.pandautils.features.calendartodo.details.ToDoAction
import com.instructure.pandautils.features.calendartodo.details.ToDoUiState
import com.instructure.pandautils.features.reminder.composables.ReminderView
import com.instructure.pandautils.utils.ThemePrefs
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@Composable
internal fun ToDoScreen(
    title: String,
    toDoUiState: ToDoUiState,
    actionHandler: (ToDoAction) -> Unit,
    navigationActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CanvasTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        val localCoroutineScope = rememberCoroutineScope()
        if (toDoUiState.errorSnack != null) {
            LaunchedEffect(Unit) {
                localCoroutineScope.launch {
                    val result = snackbarHostState.showSnackbar(toDoUiState.errorSnack)
                    if (result == SnackbarResult.Dismissed) {
                        actionHandler(ToDoAction.SnackbarDismissed)
                    }
                }
            }
        }

        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                CanvasThemedAppBar(
                    title = title,
                    actions = {
                        if (toDoUiState.deleting) {
                            CircularProgressIndicator(
                                color = Color(color = ThemePrefs.primaryTextColor),
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            OverFlowMenuSegment(actionHandler)
                        }
                    },
                    navigationActionClick = navigationActionClick,
                    modifier = modifier
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            content = { padding ->
                ToDoContent(
                    actionHandler = actionHandler,
                    toDoUiState = toDoUiState,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                )
            },
            modifier = modifier
        )
    }
}

@Composable
private fun OverFlowMenuSegment(
    actionHandler: (ToDoAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    if (showDeleteConfirmationDialog) {
        SimpleAlertDialog(
            dialogTitle = stringResource(id = R.string.todoDeleteConfirmationTitle),
            dialogText = stringResource(id = R.string.todoDeleteConfirmationText),
            dismissButtonText = stringResource(id = R.string.cancel),
            confirmationButtonText = stringResource(id = R.string.delete),
            onDismissRequest = {
                showDeleteConfirmationDialog = false
            },
            onConfirmation = {
                showDeleteConfirmationDialog = false
                actionHandler(ToDoAction.DeleteToDo)
            }
        )
    }

    var showMenu by rememberSaveable { mutableStateOf(false) }
    OverflowMenu(
        modifier = modifier.background(color = colorResource(id = R.color.backgroundLightestElevated)),
        showMenu = showMenu,
        onDismissRequest = {
            showMenu = !showMenu
        }
    ) {
        DropdownMenuItem(
            onClick = {
                showMenu = !showMenu
                actionHandler(ToDoAction.EditToDo)
            }
        ) {
            Text(
                color = colorResource(id = R.color.textDarkest),
                text = stringResource(id = R.string.edit),
            )
        }
        DropdownMenuItem(
            onClick = {
                showMenu = !showMenu
                showDeleteConfirmationDialog = true
            }
        ) {
            Text(
                color = colorResource(id = R.color.textDarkest),
                text = stringResource(id = R.string.delete),
            )
        }
    }
}

@Composable
private fun ToDoContent(
    actionHandler: (ToDoAction) -> Unit,
    toDoUiState: ToDoUiState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = colorResource(id = R.color.backgroundLightest)
    ) {
        when {
            !toDoUiState.loadError.isNullOrEmpty() -> FullScreenError(errorText = toDoUiState.loadError)
            toDoUiState.loading -> Loading()
            else -> Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = toDoUiState.title,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .testTag("title"),
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
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .testTag("date"),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp
                )
                val context = LocalContext.current
                Spacer(modifier = Modifier.height(28.dp))
                Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
                ReminderView(
                    viewState = toDoUiState.reminderUiState,
                    onAddClick = { actionHandler(ToDoAction.OnReminderAddClicked) },
                    onRemoveClick = {
                        actionHandler(
                            ToDoAction.OnReminderDeleteClicked(
                                context,
                                it
                            )
                        )
                    },
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
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .testTag("description"),
                        color = colorResource(id = R.color.textDarkest),
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
private fun ToDoPreview() {
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
        actionHandler = {},
        navigationActionClick = {}
    )
}
