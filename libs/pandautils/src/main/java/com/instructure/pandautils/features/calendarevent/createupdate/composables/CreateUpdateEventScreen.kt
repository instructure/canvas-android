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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.LabelValueRow
import com.instructure.pandautils.compose.composables.SelectContextScreen
import com.instructure.pandautils.compose.composables.SelectContextUiState
import com.instructure.pandautils.compose.composables.SimpleAlertDialog
import com.instructure.pandautils.compose.composables.SingleChoiceAlertDialog
import com.instructure.pandautils.compose.composables.rce.ComposeRCE
import com.instructure.pandautils.compose.getDatePickerDialog
import com.instructure.pandautils.compose.getTimePickerDialog
import com.instructure.pandautils.compose.isScreenReaderEnabled
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

    val localView = LocalView.current
    val context = LocalContext.current
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
        } else if (uiState.selectContextUiState.show) {
            SelectContextScreen(
                title = stringResource(id = R.string.selectCalendarScreenTitle),
                uiState = uiState.selectContextUiState,
                onContextSelected = {
                    localView.announceForAccessibility(
                        context.getString(R.string.a11y_calendarSelected, it.name.orEmpty())
                    )
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
    var showModifyScopeDialog by rememberSaveable { mutableStateOf(false) }
    if (showModifyScopeDialog) {
        SingleChoiceAlertDialog(
            dialogTitle = stringResource(id = R.string.eventUpdateRecurringTitle),
            items = CalendarEventAPI.ModifyEventScope.entries.take(if (uiState.isSeriesHead) 2 else 3)
                .map {
                    stringResource(id = it.stringRes)
                },
            dismissButtonText = stringResource(id = R.string.cancel),
            confirmationButtonText = stringResource(id = R.string.confirm),
            onDismissRequest = {
                showModifyScopeDialog = false
            },
            onConfirmation = {
                showModifyScopeDialog = false
                actionHandler(CreateUpdateEventAction.Save(CalendarEventAPI.ModifyEventScope.entries[it], true))
            }
        )
    }

    val saveEnabled = uiState.title.isNotBlank()
    val focusManager = LocalFocusManager.current
    TextButton(
        onClick = {
            focusManager.clearFocus()
            if (uiState.isSeriesEvent) {
                showModifyScopeDialog = true
            } else {
                actionHandler(CreateUpdateEventAction.Save(CalendarEventAPI.ModifyEventScope.ONE, false))
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CreateUpdateEventContent(
    uiState: CreateUpdateEventUiState,
    actionHandler: (CreateUpdateEventAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDatePickerDialog by rememberSaveable {
        mutableStateOf(false)
    }
    LaunchedEffect(showDatePickerDialog) {
        if (showDatePickerDialog) {
            getDatePickerDialog(
                context = context,
                date = uiState.date,
                onDateSelected = {
                    actionHandler(CreateUpdateEventAction.UpdateDate(it))
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

    var showStartTimePickerDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showEndTimePickerDialog by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(showStartTimePickerDialog) {
        if (showStartTimePickerDialog) {
            getTimePickerDialog(
                context = context,
                time = uiState.startTime ?: LocalTime.of(0, 0),
                onTimeSelected = {
                    actionHandler(CreateUpdateEventAction.UpdateStartTime(it))
                    showStartTimePickerDialog = false
                },
                onCancel = {
                    showStartTimePickerDialog = false
                },
                onDismiss = {
                    showStartTimePickerDialog = false
                }
            ).show()
        }
    }

    LaunchedEffect(showEndTimePickerDialog) {
        if (showEndTimePickerDialog) {
            getTimePickerDialog(
                context = context,
                time = uiState.endTime ?: LocalTime.of(0, 0),
                onTimeSelected = {
                    actionHandler(CreateUpdateEventAction.UpdateEndTime(it))
                    showEndTimePickerDialog = false
                },
                onCancel = {
                    showEndTimePickerDialog = false
                },
                onDismiss = {
                    showEndTimePickerDialog = false
                }
            ).show()
        }
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

            TitleInput(title = uiState.title, onFocusChanged = {
                focusedTextFields = if (it.hasFocus) {
                    focusedTextFields + TITLE
                } else {
                    focusedTextFields - TITLE
                }
            }, onTitleUpdate = {
                actionHandler(CreateUpdateEventAction.UpdateTitle(it))
            })

            LabelValueRow(
                label = stringResource(R.string.createEventDateLabel),
                value = uiState.formattedDate,
                onClick = {
                    focusManager.clearFocus()
                    showDatePickerDialog = true
                }
            )
            val preferredTimePattern = DateHelper.getPreferredTimeFormat(context).toPattern()
            LabelValueRow(
                label = stringResource(id = R.string.createEventStartTimeLabel),
                value = uiState.startTime?.format(DateTimeFormatter.ofPattern(preferredTimePattern))
                    ?: stringResource(id = R.string.createEventStartTimeNotSelected),
                onClick = {
                    focusManager.clearFocus()
                    showStartTimePickerDialog = true
                }
            )
            LabelValueRow(
                label = stringResource(id = R.string.createEventEndTimeLabel),
                value = uiState.endTime?.format(DateTimeFormatter.ofPattern(preferredTimePattern))
                    ?: stringResource(id = R.string.createEventEndTimeNotSelected),
                onClick = {
                    focusManager.clearFocus()
                    showEndTimePickerDialog = true
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
                value = uiState.selectContextUiState.selectedCanvasContext?.name.orEmpty(),
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
                modifier = Modifier
                    .onFocusChanged {
                        focusedTextFields = if (it.hasFocus) {
                            focusedTextFields + LOCATION
                        } else {
                            focusedTextFields - LOCATION
                        }
                    }
                    .testTag("locationTextField"),
            )
            LabeledTextField(
                label = stringResource(id = R.string.createEventAddressLabel),
                value = uiState.address,
                onValueChange = {
                    actionHandler(CreateUpdateEventAction.UpdateAddress(it))
                },
                modifier = Modifier
                    .onFocusChanged {
                        focusedTextFields = if (it.hasFocus) {
                            focusedTextFields + ADDRESS
                        } else {
                            focusedTextFields - ADDRESS
                        }
                    }
                    .testTag("addressTextField"),
            )
            Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
            Column(
                modifier = Modifier
                    .defaultMinSize(minHeight = 80.dp)
            ) {
                val labelText = stringResource(id = R.string.createEventDetailsLabel)

                Text(
                    text = labelText,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 12.dp)
                        .semantics { invisibleToUser() },
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val initialDetailsValue by remember { mutableStateOf(uiState.details) }
                ComposeRCE(
                    initialValue = initialDetailsValue,
                    hint = labelText.takeIf { isScreenReaderEnabled() }.orEmpty(),
                    fixedHeightInDp = 280,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("detailsComposeRCE"),
                    onTextChangeListener = {
                        actionHandler(CreateUpdateEventAction.UpdateDetails(it))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
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
            modifier = Modifier
                .padding(start = 16.dp, top = 12.dp)
                .semantics { invisibleToUser() },
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
                .focusRequester(focusRequester)
                .semantics {
                    contentDescription = label
                },
            cursorBrush = SolidColor(colorResource(id = R.color.textDarkest)),
            textStyle = MaterialTheme.typography.body1.copy(
                color = colorResource(id = R.color.textDarkest),
            )
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TitleInput(
    title: String,
    modifier: Modifier = Modifier,
    onFocusChanged: (FocusState) -> Unit,
    onTitleUpdate: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.createEventTitleLabel),
            color = colorResource(id = R.color.textDarkest),
            fontSize = 16.sp,
            modifier = Modifier
                .padding(end = 12.dp)
                .semantics { invisibleToUser() }
        )

        val hintText = stringResource(id = R.string.createEventTitleHint)

        BasicTextField(
            value = title,
            decorationBox = {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.semantics { invisibleToUser() }
                ) {
                    if (title.isEmpty()) {
                        Text(
                            text = hintText,
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
                .onFocusChanged {
                    onFocusChanged(it)
                }
                .testTag("addTitleField")
                .semantics {
                    contentDescription = hintText
                },
            cursorBrush = SolidColor(colorResource(id = R.color.textDark)),
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
            selectContextUiState = SelectContextUiState(
                selectedCanvasContext = Course(name = "Course")
            )
        ),
        actionHandler = {}
    )
}

@Preview
@Composable
private fun TitleInputPreview() {
    TitleInput("really really really really really really really really really long",
        onFocusChanged = {},
        onTitleUpdate = {})
}

@Preview
@Composable
private fun TitleInputEmptyPreview() {
    TitleInput("",
        onFocusChanged = {},
        onTitleUpdate = {})
}
