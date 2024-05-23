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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.LabelValueRow
import com.instructure.pandautils.compose.composables.SelectCalendarScreen
import com.instructure.pandautils.compose.composables.SelectCalendarUiState
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

@ExperimentalFoundationApi
@Composable
internal fun CreateUpdateToDoScreenWrapper(
    title: String,
    uiState: CreateUpdateToDoUiState,
    actionHandler: (CreateUpdateToDoAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    CanvasTheme {
        if (uiState.selectCalendarUiState.show) {
            SelectCalendarScreen(
                uiState = uiState.selectCalendarUiState,
                onCalendarSelected = {
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
                    modifier = Modifier.size(32.dp)
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
    val saveEnabled = uiState.title.isNotEmpty()
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

@Composable
private fun CreateUpdateToDoContent(
    uiState: CreateUpdateToDoUiState,
    actionHandler: (CreateUpdateToDoAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val datePickerDialog = remember {
        getDatePickerDialog(
            context = context,
            date = uiState.date,
            onDateSelected = {
                actionHandler(CreateUpdateToDoAction.UpdateDate(it))
            }
        )
    }
    val timePickerDialog = remember {
        getTimePickerDialog(
            context = context,
            time = uiState.time,
            onTimeSelected = {
                actionHandler(CreateUpdateToDoAction.UpdateTime(it))
            }
        )
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

            BasicTextField(
                value = uiState.title,
                decorationBox = {
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (uiState.title.isEmpty()) {
                            Text(
                                text = stringResource(id = R.string.createToDoTitleHint),
                                color = colorResource(id = R.color.textDarkest).copy(alpha = .4f),
                                fontSize = 16.sp
                            )
                        }
                        it()
                    }
                },
                onValueChange = {
                    actionHandler(CreateUpdateToDoAction.UpdateTitle(it))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
                cursorBrush = SolidColor(colorResource(id = R.color.textDarkest)),
                textStyle = TextStyle(
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp
                ),
                singleLine = true
            )
            LabelValueRow(
                label = stringResource(id = R.string.createToDoDateLabel),
                value = uiState.formattedDate,
                onClick = {
                    focusManager.clearFocus()
                    datePickerDialog.show()
                }
            )
            LabelValueRow(
                label = stringResource(id = R.string.createToDoTimeLabel),
                value = uiState.formattedTime(LocalContext.current),
                onClick = {
                    focusManager.clearFocus()
                    timePickerDialog.show()
                }
            )
            LabelValueRow(
                label = stringResource(id = R.string.createToDoCalendarLabel),
                value = uiState.selectCalendarUiState.selectedCanvasContext?.name.orEmpty(),
                loading = uiState.loadingCanvasContexts,
                onClick = {
                    focusManager.clearFocus()
                    actionHandler(CreateUpdateToDoAction.ShowSelectCalendarScreen)
                }
            )
            Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
            Column(
                modifier = Modifier
                    .defaultMinSize(minHeight = 120.dp)
                    .clickable {
                        detailsFocusRequester.requestFocus()
                    }
            ) {
                Text(
                    text = stringResource(id = R.string.createToDoDetailsLabel),
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp),
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
                        .focusRequester(detailsFocusRequester),
                    cursorBrush = SolidColor(colorResource(id = R.color.textDarkest)),
                    textStyle = TextStyle(
                        color = colorResource(id = R.color.textDarkest),
                        fontSize = 16.sp
                    )
                )
            }
        }
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
            selectCalendarUiState = SelectCalendarUiState(
                selectedCanvasContext = Course(name = "Course")
            )
        ),
        actionHandler = {}
    )
}
