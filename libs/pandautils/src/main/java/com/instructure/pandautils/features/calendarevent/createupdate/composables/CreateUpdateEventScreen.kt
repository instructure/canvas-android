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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.LabelValueRow
import com.instructure.pandautils.compose.composables.SelectCalendarScreen
import com.instructure.pandautils.compose.composables.SelectCalendarUiState
import com.instructure.pandautils.compose.composables.SimpleAlertDialog
import com.instructure.pandautils.compose.composables.SingleChoiceAlertDialog
import com.instructure.pandautils.compose.composables.rce.ComposeRCE
import com.instructure.pandautils.compose.getDatePickerDialog
import com.instructure.pandautils.compose.getTimePickerDialog
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventAction
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventUiState
import com.instructure.pandautils.utils.ThemePrefs
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

private const val TITLE = 1
private const val LOCATION = 2
private const val ADDRESS = 3

@Composable
internal fun CreateUpdateEventScreenWrapper(
    title: String,
    uiState: CreateUpdateEventUiState,
    actionHandler: (CreateUpdateEventAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    CanvasTheme {
        if (uiState.selectFrequencyUiState.customFrequencyUiState.show) {
            CustomFrequencyScreen(
                uiState = uiState.selectFrequencyUiState.customFrequencyUiState,
                actionHandler = actionHandler,
                navigationActionClick = {
                    actionHandler(CreateUpdateEventAction.HideCustomFrequencyScreen)
                },
                modifier = modifier
            )
        } else if (uiState.selectCalendarUiState.show) {
            SelectCalendarScreen(
                uiState = uiState.selectCalendarUiState,
                onCalendarSelected = {
                    actionHandler(CreateUpdateEventAction.UpdateCanvasContext(it))
                    coroutineScope.launch {
                        // We need to add this delay to give the user some feedback about the selection before closing the screen
                        delay(100)
                        actionHandler(CreateUpdateEventAction.HideSelectCalendarScreen)
                    }
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
    val showModifyScopeDialog = remember { mutableStateOf(false) }
    if (showModifyScopeDialog.value) {
        SingleChoiceAlertDialog(
            dialogTitle = stringResource(id = R.string.eventUpdateRecurringTitle),
            items = CalendarEventAPI.ModifyEventScope.entries.take(if (uiState.isSeriesHead) 2 else 3).map {
                stringResource(id = it.stringRes)
            },
            dismissButtonText = stringResource(id = R.string.cancel),
            confirmationButtonText = stringResource(id = R.string.confirm),
            onDismissRequest = {
                showModifyScopeDialog.value = false
            },
            onConfirmation = {
                showModifyScopeDialog.value = false
                actionHandler(CreateUpdateEventAction.Save(CalendarEventAPI.ModifyEventScope.entries[it]))
            }
        )
    }

    val saveEnabled = uiState.title.isNotEmpty()
    val focusManager = LocalFocusManager.current
    TextButton(
        onClick = {
            focusManager.clearFocus()
            if (uiState.isSeriesEvent) {
                showModifyScopeDialog.value = true
            } else {
                actionHandler(CreateUpdateEventAction.Save(CalendarEventAPI.ModifyEventScope.ONE))
            }
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
    if (uiState.selectFrequencyUiState.showFrequencyDialog) {
        val frequencies = uiState.selectFrequencyUiState.frequencies.keys.toList()
        SingleChoiceAlertDialog(
            dialogTitle = stringResource(id = R.string.eventFrequencyDialogTitle),
            items = frequencies,
            defaultSelection = frequencies.indexOf(uiState.selectFrequencyUiState.selectedFrequency),
            dismissButtonText = stringResource(id = R.string.cancel),
            onDismissRequest = {
                actionHandler(CreateUpdateEventAction.HideFrequencyDialog)
            },
            onItemSelected = {
                if (it == frequencies.lastIndex) {
                    actionHandler(CreateUpdateEventAction.ShowCustomFrequencyScreen)
                } else {
                    actionHandler(CreateUpdateEventAction.UpdateFrequency(frequencies[it]))
                    actionHandler(CreateUpdateEventAction.HideFrequencyDialog)
                }
            }
        )
    }

    Surface(
        modifier = modifier,
        color = colorResource(id = R.color.backgroundLightest)
    ) {
        val scrollState = rememberScrollState()
        var focusedTextFields by remember { mutableStateOf(emptySet<Int>()) }

        LaunchedEffect(Unit) {
            // Since we cannot track the focus of the RCE correctly we track the focus of all the other text fields.
            // When no text field is focused but the scroll state maxValue is changed that means that the RCE is focused.
            // In this case we just scroll to the bottom.
            // We need to drop the first value in case the screen is not tall enough and the initial scroll state is not 0.
            snapshotFlow { scrollState.maxValue }.drop(1).collect { maxValue ->
                if (maxValue != 0 && maxValue != Int.MAX_VALUE && focusedTextFields.isEmpty()) {
                    scrollState.scrollTo((maxValue))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            val focusManager = LocalFocusManager.current

            BasicTextField(
                value = uiState.title,
                decorationBox = {
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (uiState.title.isEmpty()) {
                            Text(
                                text = stringResource(id = R.string.createEventTitleHint),
                                color = colorResource(id = R.color.textDarkest).copy(alpha = .4f),
                                fontSize = 16.sp
                            )
                        }
                        it()
                    }
                },
                onValueChange = {
                    actionHandler(CreateUpdateEventAction.UpdateTitle(it))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp)
                    .onFocusChanged {
                        focusedTextFields = if (it.hasFocus) {
                            focusedTextFields + TITLE
                        } else {
                            focusedTextFields - TITLE
                        }
                    }
                    .testTag("addTitleField"),
                cursorBrush = SolidColor(colorResource(id = R.color.textDarkest)),
                textStyle = TextStyle(
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp
                ),
                singleLine = true
            )
            LabelValueRow(
                label = stringResource(R.string.createEventDateLabel),
                value = uiState.formattedDate,
                onClick = {
                    focusManager.clearFocus()
                    datePickerDialog.show()
                }
            )
            val preferredTimePattern = DateHelper.getPreferredTimeFormat(context).toPattern()
            LabelValueRow(
                label = stringResource(id = R.string.createEventStartTimeLabel),
                value = uiState.startTime?.format(DateTimeFormatter.ofPattern(preferredTimePattern))
                    ?: stringResource(id = R.string.createEventStartTimeNotSelected),
                onClick = {
                    focusManager.clearFocus()
                    startTimePickerDialog.show()
                }
            )
            LabelValueRow(
                label = stringResource(id = R.string.createEventEndTimeLabel),
                value = uiState.endTime?.format(DateTimeFormatter.ofPattern(preferredTimePattern))
                    ?: stringResource(id = R.string.createEventEndTimeNotSelected),
                onClick = {
                    focusManager.clearFocus()
                    endTimePickerDialog.show()
                }
            )
            LabelValueRow(
                label = stringResource(id = R.string.createEventFrequencyLabel),
                value = uiState.selectFrequencyUiState.selectedFrequency.orEmpty(),
                onClick = {
                    focusManager.clearFocus()
                    actionHandler(CreateUpdateEventAction.ShowFrequencyDialog)
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
                },
                modifier = Modifier.onFocusChanged {
                    focusedTextFields = if (it.hasFocus) {
                        focusedTextFields + LOCATION
                    } else {
                        focusedTextFields - LOCATION
                    }
                }.testTag("locationTextField"),
            )
            LabeledTextField(
                label = stringResource(id = R.string.createEventAddressLabel),
                value = uiState.address,
                onValueChange = {
                    actionHandler(CreateUpdateEventAction.UpdateAddress(it))
                },
                modifier = Modifier.onFocusChanged {
                    focusedTextFields = if (it.hasFocus) {
                        focusedTextFields + ADDRESS
                    } else {
                        focusedTextFields - ADDRESS
                    }
                }.testTag("addressTextField"),
            )
            Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
            Column(
                modifier = Modifier
                    .defaultMinSize(minHeight = 80.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.createEventDetailsLabel),
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                ComposeRCE(
                    html = uiState.details,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp).testTag("detailsComposeRCE"),
                ) {
                    actionHandler(CreateUpdateEventAction.UpdateDetails(it))
                }
            }
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
