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
package com.instructure.parentapp.features.alerts.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.instructure.canvasapi2.models.AlertType
import com.instructure.canvasapi2.models.ThresholdWorkflowState
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.OverflowMenu
import com.instructure.pandautils.compose.composables.UserAvatar
import com.instructure.pandautils.utils.orDefault
import com.instructure.parentapp.R

private val percentageItems = listOf(
    AlertType.ASSIGNMENT_GRADE_HIGH,
    AlertType.ASSIGNMENT_GRADE_LOW,
    AlertType.COURSE_GRADE_HIGH,
    AlertType.COURSE_GRADE_LOW
)

@Composable
fun AlertSettingsScreen(
    uiState: AlertSettingsUiState,
    navigationActionClick: () -> Unit
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                CanvasAppBar(
                    title = stringResource(id = R.string.alertSettingsTitle),
                    navIconRes = R.drawable.ic_back_arrow,
                    navIconContentDescription = stringResource(id = R.string.back),
                    navigationActionClick = navigationActionClick,
                    backgroundColor = Color(uiState.userColor),
                    textColor = colorResource(id = R.color.textLightest),
                    actions = {
                        var showMenu by remember { mutableStateOf(false) }
                        var showConfirmationDialog by remember { mutableStateOf(false) }
                        if (showConfirmationDialog) {
                            UnpairStudentDialog(
                                uiState.student.id,
                                Color(uiState.userColor),
                                uiState.actionHandler
                            ) {
                                showConfirmationDialog = false
                            }
                        }
                        OverflowMenu(
                            modifier = Modifier
                                .background(color = colorResource(id = R.color.backgroundLightestElevated)),
                            showMenu = showMenu,
                            iconColor = colorResource(id = R.color.textLightest),
                            onDismissRequest = { showMenu = !showMenu }) {
                            DropdownMenuItem(
                                modifier = Modifier.testTag("deleteMenuItem"),
                                onClick = {
                                    showMenu = !showMenu
                                    if (!showMenu) {
                                        showConfirmationDialog = true
                                    }
                                }) {
                                Text(
                                    text = stringResource(id = R.string.delete),
                                    color = colorResource(id = R.color.textDarkest)
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->
            when {
                uiState.isLoading -> {
                    Loading(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .testTag("loading")
                    )
                }

                uiState.isError -> {
                    ErrorContent(
                        modifier = Modifier.fillMaxSize(),
                        errorMessage = stringResource(id = R.string.alertSettingsErrorMessage),
                        retryClick = {
                            uiState.actionHandler(AlertSettingsAction.ReloadAlertSettings)
                        })
                }

                else -> {
                    AlertSettingsContent(uiState, modifier = Modifier.padding(padding))
                }
            }
        }
    }
}

@Composable
fun AlertSettingsContent(uiState: AlertSettingsUiState, modifier: Modifier) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        StudentDetails(
            avatarUrl = uiState.avatarUrl,
            studentName = uiState.studentName,
            studentPronouns = uiState.studentPronouns,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        Text(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
            text = stringResource(id = R.string.alertSettingsThresholdsTitle),
            fontSize = 14.sp,
            color = colorResource(id = R.color.textDark)
        )
        listOf(
            AlertType.COURSE_GRADE_LOW,
            AlertType.COURSE_GRADE_HIGH,
            AlertType.ASSIGNMENT_MISSING,
            AlertType.ASSIGNMENT_GRADE_LOW,
            AlertType.ASSIGNMENT_GRADE_HIGH,
            AlertType.COURSE_ANNOUNCEMENT,
            AlertType.INSTITUTION_ANNOUNCEMENT
        ).forEach {
            ThresholdItem(
                alertType = it,
                threshold = uiState.thresholds[it]?.threshold,
                active = uiState.thresholds[it]?.workflowState == ThresholdWorkflowState.ACTIVE,
                color = Color(uiState.userColor),
                actionHandler = uiState.actionHandler,
                min = when (it) {
                    AlertType.COURSE_GRADE_HIGH -> {
                        uiState.thresholds[AlertType.COURSE_GRADE_LOW]?.threshold?.toIntOrNull()
                            ?: 0
                    }

                    AlertType.ASSIGNMENT_GRADE_HIGH -> {
                        uiState.thresholds[AlertType.ASSIGNMENT_GRADE_LOW]?.threshold?.toIntOrNull()
                            ?: 0
                    }

                    else -> 0
                },
                max = when (it) {
                    AlertType.COURSE_GRADE_LOW -> {
                        uiState.thresholds[AlertType.COURSE_GRADE_HIGH]?.threshold?.toIntOrNull()
                            ?: 100
                    }

                    AlertType.ASSIGNMENT_GRADE_LOW -> {
                        uiState.thresholds[AlertType.ASSIGNMENT_GRADE_HIGH]?.threshold?.toIntOrNull()
                            ?: 100
                    }

                    else -> 100
                }
            )
        }
    }
}

@Composable
private fun StudentDetails(
    avatarUrl: String,
    studentName: String,
    studentPronouns: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .testTag("studentListItem"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            imageUrl = avatarUrl,
            name = studentName,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = buildAnnotatedString {
                append(studentName)
                if (!studentPronouns.isNullOrEmpty()) {
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(" (${studentPronouns})")
                    }
                }
            },
            color = colorResource(id = com.instructure.pandautils.R.color.textDarkest),
            fontSize = 16.sp
        )
    }
}

@StringRes
fun getTitle(alertType: AlertType): Int {
    return when (alertType) {
        AlertType.ASSIGNMENT_MISSING -> R.string.alertSettingsAssignmentMissing
        AlertType.ASSIGNMENT_GRADE_HIGH -> R.string.alertSettingsAssignmentGradeHigh
        AlertType.ASSIGNMENT_GRADE_LOW -> R.string.alertSettingsAssignmentGradeLow
        AlertType.COURSE_GRADE_HIGH -> R.string.alertSettingsCourseGradeHigh
        AlertType.COURSE_GRADE_LOW -> R.string.alertSettingsCourseGradeLow
        AlertType.COURSE_ANNOUNCEMENT -> R.string.alertSettingsCourseAnnouncement
        AlertType.INSTITUTION_ANNOUNCEMENT -> R.string.alertSettingsGlobalAnnouncement
    }
}


@Composable
private fun ThresholdItem(
    alertType: AlertType,
    threshold: String?,
    active: Boolean,
    color: Color,
    min: Int = 0,
    max: Int = 100,
    actionHandler: (AlertSettingsAction) -> Unit
) {
    when (alertType) {
        in percentageItems -> PercentageItem(
            title = stringResource(id = getTitle(alertType)),
            threshold = threshold,
            alertType = alertType,
            color = color,
            actionHandler = actionHandler,
            min = min,
            max = max
        )

        else -> SwitchItem(
            title = stringResource(id = getTitle(alertType)),
            active = active,
            alertType = alertType,
            color = color,
            actionHandler = actionHandler
        )
    }
}

@Composable
private fun PercentageItem(
    title: String,
    threshold: String?,
    alertType: AlertType,
    color: Color,
    min: Int,
    max: Int,
    actionHandler: (AlertSettingsAction) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        ThresholdDialog(alertType, threshold, color, min, max, actionHandler) {
            showDialog = false
        }
    }
    Row(
        modifier = Modifier
            .clickable { showDialog = true }
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(56.dp)
            .testTag("${alertType.name}_thresholdItem"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.testTag("${alertType.name}_thresholdTitle"),
            text = title,
            fontSize = 16.sp,
            color = colorResource(id = R.color.textDarkest)
        )
        Text(
            text = threshold?.let { stringResource(id = R.string.alertSettingsPercentage, it) }
                ?: stringResource(id = R.string.alertSettingsThresholdNever),
            color = color,
            textAlign = TextAlign.End,
            modifier = Modifier
                .padding(8.dp)
                .testTag("${alertType.name}_thresholdValue")
        )
    }
}

@Composable
private fun SwitchItem(
    title: String,
    active: Boolean,
    alertType: AlertType,
    color: Color,
    actionHandler: (AlertSettingsAction) -> Unit
) {
    fun toggleAlert(state: Boolean) {
        if (state) {
            actionHandler(AlertSettingsAction.CreateThreshold(alertType, null))
        } else {
            actionHandler(AlertSettingsAction.DeleteThreshold(alertType))
        }
    }

    var switchState by remember { mutableStateOf(active) }
    Row(
        modifier = Modifier
            .clickable {
                switchState = !switchState
                toggleAlert(switchState)
            }
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(56.dp)
            .testTag("${alertType.name}_thresholdItem"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.testTag("${alertType.name}_thresholdTitle"),
            text = title,
            fontSize = 16.sp,
            color = colorResource(id = R.color.textDarkest)
        )
        Switch(
            modifier = Modifier.testTag("${alertType.name}_thresholdSwitch"),
            checked = switchState,
            onCheckedChange = {
                switchState = it
                toggleAlert(switchState)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = color,
                uncheckedTrackColor = colorResource(id = R.color.textDark)
            )
        )
    }
}

@Composable
private fun ThresholdDialog(
    alertType: AlertType,
    threshold: String?,
    color: Color,
    min: Int,
    max: Int,
    actionHandler: (AlertSettingsAction) -> Unit,
    onDismiss: () -> Unit
) {
    var percentage by remember { mutableStateOf(threshold.orEmpty()) }
    val enabled = percentage.toIntOrNull().orDefault() in (min + 1)..<max
    Dialog(onDismissRequest = { onDismiss() }) {
        Column(
            Modifier
                .background(color = colorResource(id = R.color.backgroundLightest))
                .padding(16.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("thresholdDialogTitle"),
                text = stringResource(id = getTitle(alertType)),
                fontSize = 18.sp,
                color = colorResource(id = R.color.textDarkest)
            )

            TextField(
                modifier = Modifier.testTag("thresholdDialogInput"),
                value = percentage,
                onValueChange = {
                    percentage = it
                },
                label = {
                    Text(text = stringResource(id = R.string.alertSettingsThresholdLabel))
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = color,
                    focusedLabelColor = color,
                    cursorColor = color,
                    textColor = colorResource(id = R.color.textDarkest),
                    unfocusedLabelColor = colorResource(id = R.color.textDark),
                    unfocusedIndicatorColor = colorResource(id = R.color.textDark)
                )
            )
            val errorText = when {
                (percentage.toIntOrNull() ?: 100) <= min ->
                    stringResource(id = R.string.alertSettingsMinThresholdError, min)

                (percentage.toIntOrNull() ?: 0) >= max ->
                    stringResource(id = R.string.alertSettingsMaxThresholdError, max)

                else -> null
            }
            if (errorText != null) {
                Text(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .testTag("thresholdDialogError"),
                    text = errorText,
                    color = colorResource(id = R.color.textDanger)
                )
            }

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    modifier = Modifier.testTag("thresholdDialogCancelButton"),
                    colors = ButtonDefaults.textButtonColors(contentColor = color),
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Text(text = stringResource(id = R.string.cancel))
                }
                TextButton(
                    modifier = Modifier.testTag("thresholdDialogNeverButton"),
                    colors = ButtonDefaults.textButtonColors(contentColor = color),
                    onClick = {
                        actionHandler(
                            AlertSettingsAction.DeleteThreshold(
                                alertType
                            )
                        )
                        onDismiss()
                    }) {
                    Text(text = stringResource(id = R.string.alertSettingsThresholdNever))
                }
                TextButton(
                    modifier = Modifier.testTag("thresholdDialogSaveButton"),
                    enabled = enabled,
                    colors = ButtonDefaults.textButtonColors(contentColor = color),
                    onClick = {
                        actionHandler(
                            AlertSettingsAction.CreateThreshold(
                                alertType,
                                percentage
                            )
                        )
                        onDismiss()
                    }
                ) {
                    Text(text = stringResource(id = R.string.save))
                }
            }
        }
    }
}

@Composable
private fun UnpairStudentDialog(
    studentId: Long,
    color: Color,
    actionHandler: (AlertSettingsAction) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        backgroundColor = colorResource(id = R.color.backgroundLightest),
        title = {
            Text(
                modifier = Modifier.testTag("deleteDialogTitle"),
                text = stringResource(id = R.string.unpairStudentTitle),
                color = colorResource(id = R.color.textDarkest)
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.unpairStudentMessage),
                color = colorResource(id = R.color.textDarkest)
            )
        },
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                modifier = Modifier.testTag("deleteConfirmButton"),
                colors = ButtonDefaults.textButtonColors(contentColor = color),
                onClick = {
                    actionHandler(AlertSettingsAction.UnpairStudent(studentId))
                    onDismiss()
                }) {
                Text(text = stringResource(id = R.string.delete))
            }
        },
        dismissButton = {
            TextButton(
                modifier = Modifier.testTag("deleteCancelButton"),
                onClick = { onDismiss() },
                colors = ButtonDefaults.textButtonColors(contentColor = color)
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
        })
}

@Preview
@Composable
fun AlertSettingsScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    AlertSettingsScreen(
        uiState = AlertSettingsUiState(
            student = User(),
            isLoading = false,
            userColor = android.graphics.Color.BLUE,
            avatarUrl = "",
            studentName = "Test Student",
            studentPronouns = "they/them"
        ) {}
    ) {}
}

@Preview
@Composable
fun PercentageItemPreview() {
    PercentageItem(
        "Test",
        "20",
        AlertType.ASSIGNMENT_GRADE_HIGH,
        Color.Blue,
        min = 21,
        max = 100
    ) {}
}

@Preview
@Composable
fun SwitchItemPreview() {
    SwitchItem("Test", true, AlertType.ASSIGNMENT_MISSING, Color.Blue) {}
}

@Preview
@Composable
fun ThresholdDialogPreview() {
    ThresholdDialog(AlertType.ASSIGNMENT_GRADE_HIGH, "20", Color.Blue, min = 21, max = 100, {}, {})
}

@Preview
@Composable
fun UnpairStudentDialogPreview() {
    UnpairStudentDialog(1, Color.Blue, {}, {})
}

@Preview
@Composable
fun AlertSettingsErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    AlertSettingsScreen(
        uiState = AlertSettingsUiState(
            student = User(),
            isLoading = false,
            isError = true,
            userColor = android.graphics.Color.BLUE,
            avatarUrl = "",
            studentName = "Test Student",
            studentPronouns = "they/them"
        ) {}
    ) {}
}
