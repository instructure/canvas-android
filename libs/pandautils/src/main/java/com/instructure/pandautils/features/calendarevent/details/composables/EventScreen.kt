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

package com.instructure.pandautils.features.calendarevent.details.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.compose.composables.FullScreenError
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.OverflowMenu
import com.instructure.pandautils.compose.composables.SimpleAlertDialog
import com.instructure.pandautils.compose.composables.SingleChoiceAlertDialog
import com.instructure.pandautils.features.calendarevent.details.EventAction
import com.instructure.pandautils.features.calendarevent.details.EventUiState
import com.instructure.pandautils.features.calendarevent.details.ToolbarUiState
import com.instructure.pandautils.features.reminder.composables.ReminderView
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.views.CanvasWebView
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.launch

@Composable
internal fun EventScreen(
    title: String,
    eventUiState: EventUiState,
    actionHandler: (EventAction) -> Unit,
    navigationAction: () -> Unit,
    applyOnWebView: (CanvasWebView.() -> Unit),
    modifier: Modifier = Modifier
) {
    CanvasTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        val localCoroutineScope = rememberCoroutineScope()
        if (eventUiState.errorSnack != null) {
            LaunchedEffect(Unit) {
                localCoroutineScope.launch {
                    val result = snackbarHostState.showSnackbar(eventUiState.errorSnack)
                    if (result == SnackbarResult.Dismissed) {
                        actionHandler(EventAction.SnackbarDismissed)
                    }
                }
            }
        }

        val toolbarTextColor = if (eventUiState.toolbarUiState.isUserContext) {
            Color(color = ThemePrefs.primaryTextColor)
        } else {
            colorResource(id = R.color.textLightest)
        }

        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                CanvasThemedAppBar(
                    title = title,
                    subtitle = eventUiState.toolbarUiState.subtitle,
                    actions = {
                        if (eventUiState.toolbarUiState.deleting) {
                            CircularProgressIndicator(
                                color = Color(color = ThemePrefs.primaryTextColor),
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        } else if (eventUiState.toolbarUiState.editAllowed || eventUiState.toolbarUiState.deleteAllowed) {
                            OverFlowMenuSegment(
                                eventUiState = eventUiState,
                                actionHandler = actionHandler,
                                toolbarTextColor = toolbarTextColor,
                                modifier.testTag("overFlowMenuSegment")
                            )
                        }
                    },
                    backgroundColor = Color(color = eventUiState.toolbarUiState.toolbarColor),
                    contentColor = toolbarTextColor,
                    navigationActionClick = {
                        navigationAction()
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            content = { padding ->
                EventContent(
                    uiState = eventUiState,
                    actionHandler = actionHandler,
                    applyOnWebView = applyOnWebView,
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
    eventUiState: EventUiState,
    actionHandler: (EventAction) -> Unit,
    toolbarTextColor: Color,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmationDialog by rememberSaveable {
        mutableStateOf(false)
    }
    if (showDeleteConfirmationDialog) {
        SimpleAlertDialog(
            dialogTitle = stringResource(id = R.string.eventDeleteConfirmationTitle),
            dialogText = stringResource(id = R.string.eventDeleteConfirmationText),
            dismissButtonText = stringResource(id = R.string.cancel),
            confirmationButtonText = stringResource(id = R.string.delete),
            onDismissRequest = {
                showDeleteConfirmationDialog = false
            },
            onConfirmation = {
                showDeleteConfirmationDialog = false
                actionHandler(EventAction.DeleteEvent(CalendarEventAPI.ModifyEventScope.ONE))
            }
        )
    }

    var showDeleteScopeDialog by rememberSaveable { mutableStateOf(false) }
    var defaultSelection by rememberSaveable { mutableIntStateOf(-1) }
    if (showDeleteScopeDialog) {
        SingleChoiceAlertDialog(
            defaultSelection = defaultSelection,
            dialogTitle = stringResource(id = R.string.eventDeleteRecurringConfirmationTitle),
            items = CalendarEventAPI.ModifyEventScope.entries.take(if (eventUiState.isSeriesHead) 2 else 3).map {
                stringResource(id = it.stringRes)
            },
            dismissButtonText = stringResource(id = R.string.cancel),
            confirmationButtonText = stringResource(id = R.string.delete),
            onDismissRequest = {
                showDeleteScopeDialog = false
            },
            onConfirmation = {
                showDeleteScopeDialog = false
                actionHandler(EventAction.DeleteEvent(CalendarEventAPI.ModifyEventScope.entries[it]))
            },
            onItemSelected = {
                defaultSelection = it
            }
        )
    }

    var showMenu by rememberSaveable { mutableStateOf(false) }
    OverflowMenu(
        modifier = modifier
            .background(color = colorResource(id = R.color.backgroundLightestElevated))
            .testTag("overFlowMenu"),
        showMenu = showMenu,
        iconColor = toolbarTextColor,
        onDismissRequest = {
            showMenu = !showMenu
        }
    ) {
        if (eventUiState.toolbarUiState.editAllowed) {
            DropdownMenuItem(
                onClick = {
                    showMenu = !showMenu
                    actionHandler(EventAction.EditEvent)
                }
            ) {
                Text(
                    color = colorResource(id = R.color.textDarkest),
                    text = stringResource(id = R.string.edit),
                )
            }
        }
        DropdownMenuItem(
            onClick = {
                showMenu = !showMenu
                if (eventUiState.isSeriesEvent) {
                    showDeleteScopeDialog = true
                } else {
                    showDeleteConfirmationDialog = true
                }
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
private fun EventContent(
    uiState: EventUiState,
    actionHandler: (EventAction) -> Unit,
    applyOnWebView: (CanvasWebView.() -> Unit),
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = colorResource(id = R.color.backgroundLightest)
    ) {
        when {
            !uiState.loadError.isNullOrEmpty() -> FullScreenError(errorText = uiState.loadError)
            uiState.loading -> Loading()
            else -> Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = uiState.title,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .testTag("eventTitle"),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = uiState.date,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .testTag("eventDate"),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp
                )
                if (uiState.recurrence.isNotEmpty()) {
                    Text(
                        text = uiState.recurrence,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .testTag("recurrence"),
                        color = colorResource(id = R.color.textDarkest),
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
                Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
                val context = LocalContext.current
                ReminderView(
                    viewState = uiState.reminderUiState,
                    onAddClick = { actionHandler(EventAction.OnReminderAddClicked) },
                    onRemoveClick = { actionHandler(EventAction.OnReminderDeleteClicked(context, it)) },
                )
                Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
                if (uiState.location.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(id = R.string.eventLocationLabel),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .testTag("locationLabel"),
                        color = colorResource(id = R.color.textDark),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.location,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .testTag("location"),
                        color = colorResource(id = R.color.textDarkest),
                        fontSize = 16.sp
                    )
                }
                if (uiState.address.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(id = R.string.eventAddressLabel),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .testTag("addressLabel"),
                        color = colorResource(id = R.color.textDark),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.address,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .testTag("address"),
                        color = colorResource(id = R.color.textDarkest),
                        fontSize = 16.sp
                    )
                }
                if (uiState.formattedDescription.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(id = R.string.eventDetailsLabel),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .testTag("detailsLabel"),
                        color = colorResource(id = R.color.textDark),
                        fontSize = 14.sp
                    )
                    ComposeCanvasWebViewWrapper(
                        html = uiState.formattedDescription,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .testTag("composeCanvasWebViewWrapper"),
                        onLtiButtonPressed = {
                            actionHandler(EventAction.OnLtiClicked(it))
                        },
                        applyOnWebView = applyOnWebView
                    )
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
private fun EventPreview() {
    ContextKeeper.appContext = LocalContext.current
    AndroidThreeTen.init(LocalContext.current)
    EventScreen(
        title = "Event",
        eventUiState = EventUiState(
            toolbarUiState = ToolbarUiState(
                toolbarColor = ThemePrefs.primaryColor,
                subtitle = "Subtitle",
                editAllowed = true
            ),
            loading = false,
            title = "Creative Machines and Innovative Instrumentation Conference",
            date = "2023. March 31. 12:00 PM - 1:00 PM",
            recurrence = "Weekly on Wed, 52 times",
            location = "UCF Department of Mechanical and Aerospace Engineering",
            address = "12760 Pegasus Dr, Orlando, FL 32816, USA",
            formattedDescription = "Description"
        ),
        actionHandler = {},
        navigationAction = {},
        applyOnWebView = {}
    )
}
