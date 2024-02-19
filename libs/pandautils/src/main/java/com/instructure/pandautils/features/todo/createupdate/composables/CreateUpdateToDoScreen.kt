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

package com.instructure.pandautils.features.todo.createupdate.composables

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.features.todo.createupdate.CreateUpdateToDoAction
import com.instructure.pandautils.features.todo.createupdate.CreateUpdateToDoUiState
import com.instructure.pandautils.utils.ThemePrefs
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import java.util.Calendar

@ExperimentalFoundationApi
@Composable
internal fun CreateUpdateToDoScreenWrapper(
    title: String,
    uiState: CreateUpdateToDoUiState,
    actionHandler: (CreateUpdateToDoAction) -> Unit,
    navigationActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CanvasTheme {
        if (uiState.showCalendarSelector) {
            SelectCalendarScreen(
                uiState = uiState,
                actionHandler = actionHandler,
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
                navigationActionClick = navigationActionClick,
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
    navigationActionClick: () -> Unit,
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
            TopAppBarContent(
                title = title,
                uiState = uiState,
                actionHandler = actionHandler,
                navigationActionClick = navigationActionClick
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { padding ->
            CreateUpdateToDoContent(
                uiState = uiState,
                actionHandler = actionHandler,
                modifier = modifier
                    .padding(padding)
                    .fillMaxSize()
            )
        }
    )
}

@Composable
private fun TopAppBarContent(
    title: String,
    uiState: CreateUpdateToDoUiState,
    actionHandler: (CreateUpdateToDoAction) -> Unit,
    navigationActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(text = title, fontWeight = FontWeight(600))
        },
        elevation = 0.dp,
        actions = {
            if (uiState.saving) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.textLightest),
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
        backgroundColor = colorResource(id = R.color.backgroundLightestElevated),
        contentColor = colorResource(id = R.color.textDarkest),
        navigationIcon = {
            IconButton(onClick = navigationActionClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = stringResource(id = R.string.close)
                )
            }
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
            color = colorResource(id = R.color.textDarkest),
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
    val calendar = Calendar.getInstance()
    calendar.set(uiState.date.year, uiState.date.monthValue - 1, uiState.date.dayOfMonth)
    val datePickerDialog = DatePickerDialog(
        LocalContext.current,
        { _, year, month, dayOfMonth ->
            actionHandler(
                CreateUpdateToDoAction.UpdateDate(
                    LocalDate.of(
                        year,
                        month + 1,
                        dayOfMonth
                    )
                )
            )
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    calendar.set(Calendar.HOUR_OF_DAY, uiState.time.hour)
    calendar.set(Calendar.MINUTE, uiState.time.minute)
    val timePickerDialog = TimePickerDialog(
        LocalContext.current,
        { _, hourOfDay, minute ->
            actionHandler(
                CreateUpdateToDoAction.UpdateTime(
                    LocalTime.of(
                        hourOfDay,
                        minute
                    )
                )
            )
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

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
            val detailsFocusRequester = remember { FocusRequester() }
            val focusManager = LocalFocusManager.current

            LaunchedEffect(key1 = uiState.title, block = {
                awaitFrame()
                if (uiState.title.isEmpty()) {
                    titleFocusRequester.requestFocus()
                }
            })

            Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
            Row(
                verticalAlignment = CenterVertically,
                modifier = Modifier
                    .height(48.dp)
                    .clickable {
                        titleFocusRequester.requestFocus()
                    }
            ) {
                Text(
                    text = stringResource(id = R.string.createToDoTitleLabel),
                    modifier = Modifier.padding(start = 16.dp),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp,
                    fontWeight = FontWeight(600)
                )
                BasicTextField(
                    value = uiState.title,
                    onValueChange = {
                        actionHandler(CreateUpdateToDoAction.UpdateTitle(it))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .focusRequester(titleFocusRequester),
                    textStyle = TextStyle(
                        color = colorResource(id = R.color.textDarkest),
                        fontSize = 16.sp
                    ),
                    singleLine = true
                )
            }
            Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .height(48.dp)
                    .clickable {
                        focusManager.clearFocus()
                        datePickerDialog.show()
                    }
            ) {
                Text(
                    text = stringResource(R.string.createToDoDateLabel),
                    modifier = Modifier.padding(start = 16.dp),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp,
                    fontWeight = FontWeight(600)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = uiState.formattedDate,
                    modifier = Modifier.padding(end = 16.dp),
                    color = colorResource(id = R.color.textDark),
                    fontSize = 14.sp
                )
                Icon(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 16.dp),
                    tint = colorResource(id = R.color.textDark)
                )
            }
            Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
            Row(
                verticalAlignment = CenterVertically,
                modifier = Modifier
                    .height(48.dp)
                    .clickable {
                        focusManager.clearFocus()
                        timePickerDialog.show()
                    },
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = stringResource(id = R.string.createToDoTimeLabel),
                    modifier = Modifier.padding(start = 16.dp),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp,
                    fontWeight = FontWeight(600)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = uiState.formattedTime(LocalContext.current),
                    modifier = Modifier.padding(end = 16.dp),
                    color = colorResource(id = R.color.textDark),
                    fontSize = 14.sp
                )
                Icon(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 16.dp),
                    tint = colorResource(id = R.color.textDark)
                )
            }
            Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
            Row(
                verticalAlignment = CenterVertically,
                modifier = Modifier
                    .height(48.dp)
                    .clickable(enabled = !uiState.loadingCourses) {
                        focusManager.clearFocus()
                        actionHandler(CreateUpdateToDoAction.ShowSelectCalendarScreen)
                    },
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = stringResource(id = R.string.createToDoCalendarLabel),
                    modifier = Modifier.padding(start = 16.dp),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp,
                    fontWeight = FontWeight(600)
                )
                Spacer(modifier = Modifier.weight(1f))
                if (uiState.loadingCourses) {
                    CircularProgressIndicator(
                        color = Color(ThemePrefs.buttonColor),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                } else {
                    Text(
                        text = uiState.selectedCourse?.name ?: stringResource(id = R.string.noCalendarSelected),
                        modifier = Modifier.padding(end = 16.dp),
                        color = colorResource(id = R.color.textDark),
                        fontSize = 14.sp
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_right),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 16.dp),
                        tint = colorResource(id = R.color.textDark)
                    )
                }
            }
            Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        detailsFocusRequester.requestFocus()
                    }
            ) {
                Text(
                    text = stringResource(id = R.string.createToDoDetailsLabel),
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp,
                    fontWeight = FontWeight(600)
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
private fun CreateEditToDoPreview() {
    ContextKeeper.appContext = LocalContext.current
    AndroidThreeTen.init(LocalContext.current)
    CreateUpdateToDoScreen(
        title = "New To Do",
        uiState = CreateUpdateToDoUiState(
            title = "Title",
            date = LocalDate.now(),
            time = LocalTime.now(),
            selectedCourse = null,
            details = "Details",
            saving = false,
            errorSnack = null,
            loadingCourses = false,
            showCalendarSelector = false,
            courses = emptyList()
        ),
        actionHandler = {},
        navigationActionClick = {}
    )
}
