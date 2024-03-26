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

package com.instructure.pandautils.features.calendarevent.createupdate.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableStateOf
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
import com.instructure.pandautils.compose.composables.SingleChoiceAlertDialog
import com.instructure.pandautils.compose.getDatePickerDialog
import com.instructure.pandautils.compose.getTimePickerDialog
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventAction
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventUiState
import com.instructure.pandautils.utils.ThemePrefs
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime


@Composable
internal fun CreateUpdateEventScreenWrapper(
    title: String,
    uiState: CreateUpdateEventUiState,
    actionHandler: (CreateUpdateEventAction) -> Unit,
    modifier: Modifier = Modifier
) {
    CanvasTheme {
        if (uiState.selectCalendarUiState.show) {
            SelectCalendarScreen(
                uiState = uiState.selectCalendarUiState,
                onCalendarSelected = {
                    actionHandler(CreateUpdateEventAction.UpdateCanvasContext(it))
                    actionHandler(CreateUpdateEventAction.HideSelectCalendarScreen)
                },
                navigationActionClick = {
                    actionHandler(CreateUpdateEventAction.HideSelectCalendarScreen)
                },
                modifier = modifier
            )
        } else {
            CreateUpdateEventScreen(
                title = title,
                uiState = uiState,
                actionHandler = actionHandler,
                modifier = modifier
            )
        }
    }
}

@Composable
internal fun CreateUpdateEventScreen(
    title: String,
    uiState: CreateUpdateEventUiState,
    actionHandler: (CreateUpdateEventAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val localCoroutineScope = rememberCoroutineScope()
    if (uiState.errorSnack != null) {
        LaunchedEffect(Unit) {
            localCoroutineScope.launch {
                val result = snackbarHostState.showSnackbar(uiState.errorSnack)
                if (result == SnackbarResult.Dismissed) {
                    actionHandler(CreateUpdateEventAction.SnackbarDismissed)
                }
            }
        }
    }

    Scaffold(
        backgroundColor = colorResource(id = R.color.backgroundLightest),
        topBar = {
            CreateUpdateEventTopAppBar(
                title = title,
                uiState = uiState,
                actionHandler = actionHandler
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { padding ->
            CreateUpdateEventContent(
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
private fun CreateUpdateEventTopAppBar(
    title: String,
    uiState: CreateUpdateEventUiState,
    actionHandler: (CreateUpdateEventAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.showUnsavedChangesDialog) {
        SimpleAlertDialog(
            dialogTitle = stringResource(id = R.string.exitWithoutSavingTitle),
            dialogText = stringResource(id = R.string.exitWithoutSavingMessage),
            dismissButtonText = stringResource(id = R.string.cancel),
            confirmationButtonText = stringResource(id = R.string.exitUnsaved),
            onDismissRequest = {
                actionHandler(CreateUpdateEventAction.HideUnsavedChangesDialog)
            },
            onConfirmation = {
                actionHandler(CreateUpdateEventAction.NavigateBack)
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
            actionHandler(CreateUpdateEventAction.CheckUnsavedChanges)
        },
        modifier = modifier
    )
}

@Composable
private fun ActionsSegment(
    uiState: CreateUpdateEventUiState,
    actionHandler: (CreateUpdateEventAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val saveEnabled = uiState.title.isNotEmpty()
    val focusManager = LocalFocusManager.current
    TextButton(
        onClick = {
            focusManager.clearFocus()
            actionHandler(CreateUpdateEventAction.Save)
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
private fun CreateUpdateEventContent(
    uiState: CreateUpdateEventUiState,
    actionHandler: (CreateUpdateEventAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val datePickerDialog = remember {
        getDatePickerDialog(
            context = context,
            date = uiState.date,
            onDateSelected = {
                actionHandler(CreateUpdateEventAction.UpdateDate(it))
            }
        )
    }
    val startTimePickerDialog = remember {
        getTimePickerDialog(
            context = context,
            time = uiState.startTime ?: LocalTime.of(0, 0),
            onTimeSelected = {
                actionHandler(CreateUpdateEventAction.UpdateStartTime(it))
            }
        )
    }
    val endTimePickerDialog = remember {
        getTimePickerDialog(
            context = context,
            time = uiState.endTime ?: LocalTime.of(0, 0),
            onTimeSelected = {
                actionHandler(CreateUpdateEventAction.UpdateEndTime(it))
            }
        )
    }
    val showCustomFrequencyDialog = remember { mutableStateOf(false) }
    val showFrequencyDialog = remember { mutableStateOf(false) }
    if (showFrequencyDialog.value) {
        val frequencies = uiState.frequencyDialogUiState.frequencies.keys.toList()
        SingleChoiceAlertDialog(
            dialogTitle = stringResource(id = R.string.eventFrequencyDialogTitle),
            items = frequencies,
            defaultSelection = frequencies.indexOf(uiState.frequencyDialogUiState.selectedFrequency),
            dismissButtonText = stringResource(id = R.string.cancel),
            onDismissRequest = {
                showFrequencyDialog.value = false
            },
            onItemSelected = {
                if (it == uiState.frequencyDialogUiState.frequencies.size - 1) {
                    showCustomFrequencyDialog.value = true
                } else {
                    actionHandler(CreateUpdateEventAction.UpdateFrequency(frequencies[it]))
                    showFrequencyDialog.value = false
                }
            }
        )
    }
    if (showCustomFrequencyDialog.value) {
        CustomFrequencyDialog(
            defaultRRule = uiState.frequencyDialogUiState.frequencies[uiState.frequencyDialogUiState.selectedFrequency],
            defaultDate = uiState.date,
            onConfirm = {
                actionHandler(CreateUpdateEventAction.CustomFrequencySelected(it))
                showCustomFrequencyDialog.value = false
                showFrequencyDialog.value = false
            },
            onDismissRequest = {
                showCustomFrequencyDialog.value = false
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
            val titleFocusRequester = remember { FocusRequester() }
            val focusManager = LocalFocusManager.current

            LaunchedEffect(key1 = uiState.title, block = {
                awaitFrame()
                if (uiState.title.isEmpty()) {
                    titleFocusRequester.requestFocus()
                }
            })

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(48.dp)
                    .clickable {
                        titleFocusRequester.requestFocus()
                    }
            ) {
                Text(
                    text = stringResource(id = R.string.createEventTitleLabel),
                    modifier = Modifier.padding(start = 16.dp),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp
                )
                BasicTextField(
                    value = uiState.title,
                    onValueChange = {
                        actionHandler(CreateUpdateEventAction.UpdateTitle(it))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .focusRequester(titleFocusRequester),
                    cursorBrush = SolidColor(colorResource(id = R.color.textDarkest)),
                    textStyle = TextStyle(
                        color = colorResource(id = R.color.textDarkest),
                        fontSize = 16.sp
                    ),
                    singleLine = true
                )
            }
            LabelValueRow(
                label = stringResource(R.string.createEventDateLabel),
                value = uiState.formattedDate,
                onClick = {
                    focusManager.clearFocus()
                    datePickerDialog.show()
                }
            )
            LabelValueRow(
                label = stringResource(id = R.string.createEventStartTimeLabel),
                value = uiState.startTime?.let { uiState.formattedTime(context, it) }
                    ?: stringResource(id = R.string.createEventStartTimeNotSelected),
                onClick = {
                    focusManager.clearFocus()
                    startTimePickerDialog.show()
                }
            )
            LabelValueRow(
                label = stringResource(id = R.string.createEventEndTimeLabel),
                value = uiState.endTime?.let { uiState.formattedTime(context, it) }
                    ?: stringResource(id = R.string.createEventEndTimeNotSelected),
                onClick = {
                    focusManager.clearFocus()
                    endTimePickerDialog.show()
                }
            )
            LabelValueRow(
                label = stringResource(id = R.string.createEventFrequencyLabel),
                value = uiState.frequencyDialogUiState.selectedFrequency.orEmpty(),
                onClick = {
                    focusManager.clearFocus()
                    showFrequencyDialog.value = true
                }
            )
            LabelValueRow(
                label = stringResource(id = R.string.createEventCalendarLabel),
                value = uiState.selectCalendarUiState.selectedCanvasContext?.name.orEmpty(),
                loading = uiState.loadingCanvasContexts,
                onClick = {
                    focusManager.clearFocus()
                    actionHandler(CreateUpdateEventAction.ShowSelectCalendarScreen)
                }
            )
            LabeledTextField(
                label = stringResource(id = R.string.createEventLocationLabel),
                value = uiState.location,
                onValueChange = {
                    actionHandler(CreateUpdateEventAction.UpdateLocation(it))
                }
            )
            LabeledTextField(
                label = stringResource(id = R.string.createEventAddressLabel),
                value = uiState.address,
                onValueChange = {
                    actionHandler(CreateUpdateEventAction.UpdateAddress(it))
                }
            )
            LabeledTextField(
                label = stringResource(id = R.string.createEventDetailsLabel),
                value = uiState.details,
                onValueChange = {
                    actionHandler(CreateUpdateEventAction.UpdateDetails(it))
                }
            )
        }
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 80.dp)
            .clickable {
                focusRequester.requestFocus()
            }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp),
            color = colorResource(id = R.color.textDarkest),
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            singleLine = false,
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester),
            cursorBrush = SolidColor(colorResource(id = R.color.textDarkest)),
            textStyle = TextStyle(
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp
            )
        )
    }
}

@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
private fun CreateUpdateEventPreview() {
    ContextKeeper.appContext = LocalContext.current
    AndroidThreeTen.init(LocalContext.current)
    CreateUpdateEventScreen(
        title = "New Event",
        uiState = CreateUpdateEventUiState(
            title = "Title",
            date = LocalDate.now(),
            startTime = LocalTime.now(),
            endTime = LocalTime.now(),
            details = "Details",
            saving = false,
            errorSnack = null,
            loadingCanvasContexts = false,
            selectCalendarUiState = SelectCalendarUiState(
                selectedCanvasContext = Course(name = "Course")
            )
        ),
        actionHandler = {}
    )
}
