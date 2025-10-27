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

package com.instructure.pandautils.features.calendartodo.createupdate.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.LabelValueRow
import com.instructure.pandautils.compose.composables.SelectContextScreen
import com.instructure.pandautils.compose.composables.SelectContextUiState
import com.instructure.pandautils.compose.composables.SimpleAlertDialog
import com.instructure.pandautils.compose.getDatePickerDialog
import com.instructure.pandautils.compose.getTimePickerDialog
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoAction
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoUiState
import com.instructure.pandautils.utils.ThemePrefs
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

@Composable
internal fun CreateUpdateToDoScreenWrapper(
    title: String,
    uiState: CreateUpdateToDoUiState,
    actionHandler: (CreateUpdateToDoAction) -> Unit,
    modifier: Modifier = Modifier
) {

    val localView = LocalView.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    CanvasTheme {
        if (uiState.selectContextUiState.show) {
            SelectContextScreen(
                title = stringResource(id = R.string.selectCalendarScreenTitle),
                uiState = uiState.selectContextUiState,
                onContextSelected = {
                    localView.announceForAccessibility(
                        context.getString(R.string.a11y_calendarSelected, it.name.orEmpty())
                    )
                    actionHandler(CreateUpdateToDoAction.UpdateCanvasContext(it))
                    coroutineScope.launch {
                        // We need to add this delay to give the user some feedback about the selection before closing the screen
                        delay(100)
                        actionHandler(CreateUpdateToDoAction.HideSelectCalendarScreen)
                    }
                },
                navigationActionClick = {
                    actionHandler(CreateUpdateToDoAction.HideSelectCalendarScreen)
                },
                modifier = modifier
            )
        } else {
            CreateUpdateToDoScreen(
                title = title,
                uiState = uiState,
                actionHandler = actionHandler,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun CreateUpdateToDoScreen(
    title: String,
    uiState: CreateUpdateToDoUiState,
    actionHandler: (CreateUpdateToDoAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val localCoroutineScope = rememberCoroutineScope()
    if (uiState.errorSnack != null) {
        LaunchedEffect(Unit) {
            localCoroutineScope.launch {
                val result = snackbarHostState.showSnackbar(uiState.errorSnack)
                if (result == SnackbarResult.Dismissed) {
                    actionHandler(CreateUpdateToDoAction.SnackbarDismissed)
                }
            }
        }
    }

    Scaffold(
        backgroundColor = colorResource(id = R.color.backgroundLightest),
        contentWindowInsets = WindowInsets.ime.union(WindowInsets.navigationBars),
        topBar = {
            CreateUpdateToDoTopAppBar(
                title = title,
                uiState = uiState,
                actionHandler = actionHandler,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { padding ->
            CreateUpdateToDoContent(
                uiState = uiState,
                actionHandler = actionHandler,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            )
        },
        modifier = modifier
    )
}

@Composable
private fun CreateUpdateToDoTopAppBar(
    title: String,
    uiState: CreateUpdateToDoUiState,
    actionHandler: (CreateUpdateToDoAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.showUnsavedChangesDialog) {
        SimpleAlertDialog(
            dialogTitle = stringResource(id = R.string.exitWithoutSavingTitle),
            dialogText = stringResource(id = R.string.exitWithoutSavingMessage),
            dismissButtonText = stringResource(id = R.string.cancel),
            confirmationButtonText = stringResource(id = R.string.exitUnsaved),
            onDismissRequest = {
                actionHandler(CreateUpdateToDoAction.HideUnsavedChangesDialog)
            },
            onConfirmation = {
                actionHandler(CreateUpdateToDoAction.NavigateBack)
            }
        )
    }

    CanvasAppBar(
        title = title,
        actions = {
            if (uiState.saving) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.textDarkest),
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("savingProgressIndicator")
                )
            } else {
                ActionsSegment(
                    uiState = uiState,
                    actionHandler = actionHandler
                )
            }
        },
        navigationActionClick = {
            actionHandler(CreateUpdateToDoAction.CheckUnsavedChanges)
        },
        modifier = modifier
    )
}

@Composable
private fun ActionsSegment(
    uiState: CreateUpdateToDoUiState,
    actionHandler: (CreateUpdateToDoAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val saveEnabled = uiState.title.isNotBlank()
    val focusManager = LocalFocusManager.current
    TextButton(
        onClick = {
            focusManager.clearFocus()
            actionHandler(CreateUpdateToDoAction.Save)
        },
        enabled = saveEnabled,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.save),
            color = Color(color = ThemePrefs.textButtonColor),
            fontSize = 14.sp,
            modifier = Modifier.alpha(if (saveEnabled) 1f else .4f)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CreateUpdateToDoContent(
    uiState: CreateUpdateToDoUiState,
    actionHandler: (CreateUpdateToDoAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(showDatePickerDialog) {
        if (showDatePickerDialog) {
            getDatePickerDialog(
                context = context,
                date = uiState.date,
                onDateSelected = {
                    actionHandler(CreateUpdateToDoAction.UpdateDate(it))
                    showDatePickerDialog = false
                },
                onCancel = {
                    showDatePickerDialog = false
                },
                onDismiss = {
                    showDatePickerDialog = false
                }
            ).show()
        }
    }

    var showTimePickerDialog by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(showTimePickerDialog) {
        if (showTimePickerDialog) {
            getTimePickerDialog(
                context = context,
                time = uiState.time,
                onTimeSelected = {
                    actionHandler(CreateUpdateToDoAction.UpdateTime(it))
                    showTimePickerDialog = false
                },
                onCancel = {
                    showTimePickerDialog = false
                },
                onDismiss = {
                    showTimePickerDialog = false
                }
            ).show()
        }
    }

    Surface(
        modifier = modifier,
        color = colorResource(id = R.color.backgroundLightest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            val detailsFocusRequester = remember { FocusRequester() }
            val focusManager = LocalFocusManager.current

            TitleInput(title = uiState.title) {
                actionHandler(CreateUpdateToDoAction.UpdateTitle(it))
            }
            LabelValueRow(
                label = stringResource(id = R.string.createToDoDateLabel),
                value = uiState.formattedDate,
                onClick = {
                    focusManager.clearFocus()
                    showDatePickerDialog = true
                },
                modifier = Modifier.testTag("dateRow")
            )
            LabelValueRow(
                label = stringResource(id = R.string.createToDoTimeLabel),
                value = uiState.formattedTime(LocalContext.current),
                onClick = {
                    focusManager.clearFocus()
                    showTimePickerDialog = true
                },
                modifier = Modifier.testTag("timeRow")
            )
            LabelValueRow(
                label = stringResource(id = R.string.createToDoCalendarLabel),
                value = uiState.selectContextUiState.selectedCanvasContext?.name.orEmpty(),
                loading = uiState.loadingCanvasContexts,
                onClick = {
                    focusManager.clearFocus()
                    actionHandler(CreateUpdateToDoAction.ShowSelectCalendarScreen)
                },
                modifier = Modifier.testTag("canvasContextRow")
            )
            Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
            Column(
                modifier = Modifier
                    .defaultMinSize(minHeight = 120.dp)
                    .clickable {
                        detailsFocusRequester.requestFocus()
                    }
            ) {
                val detailsText = stringResource(id = R.string.createToDoDetailsLabel)
                Text(
                    text = detailsText,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 12.dp)
                        .semantics { invisibleToUser() },
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    singleLine = false,
                    value = uiState.details,
                    onValueChange = {
                        actionHandler(CreateUpdateToDoAction.UpdateDetails(it))
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .focusRequester(detailsFocusRequester)
                        .testTag("todoDetailsTextField")
                        .semantics {
                            contentDescription = detailsText
                        },
                    cursorBrush = SolidColor(colorResource(id = R.color.textDarkest)),
                    textStyle = MaterialTheme.typography.body1.copy(
                        color = colorResource(id = R.color.textDarkest),
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TitleInput(
    title: String,
    modifier: Modifier = Modifier,
    onTitleUpdate: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.createToDoTitleLabel),
            color = colorResource(id = R.color.textDarkest),
            fontSize = 16.sp,
            modifier = Modifier
                .padding(end = 12.dp)
                .semantics { invisibleToUser() },
        )

        val hintText = stringResource(id = R.string.createToDoTitleHint)

        BasicTextField(
            value = title,
            decorationBox = {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.semantics { invisibleToUser() }
                ) {
                    if (title.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.createToDoTitleHint),
                            color = colorResource(id = R.color.textDark),
                            fontSize = 16.sp
                        )
                    }
                    it()
                }
            },
            onValueChange = {
                onTitleUpdate(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("addTitleField")
                .semantics {
                    contentDescription = hintText
                },
            cursorBrush = SolidColor(colorResource(id = R.color.textDarkest)),
            textStyle = MaterialTheme.typography.body1.copy(
                color = colorResource(id = R.color.textDarkest),
            ),
            maxLines = 2
        )
    }
}

@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
private fun CreateUpdateToDoPreview() {
    ContextKeeper.appContext = LocalContext.current
    AndroidThreeTen.init(LocalContext.current)
    CreateUpdateToDoScreen(
        title = "New To Do",
        uiState = CreateUpdateToDoUiState(
            title = "Title",
            date = LocalDate.now(),
            time = LocalTime.now(),
            details = "Details",
            saving = false,
            errorSnack = null,
            loadingCanvasContexts = true,
            selectContextUiState = SelectContextUiState(
                selectedCanvasContext = Course(name = "Course")
            )
        ),
        actionHandler = {}
    )
}
