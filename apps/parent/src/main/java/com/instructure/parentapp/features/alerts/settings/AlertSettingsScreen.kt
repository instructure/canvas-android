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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.AlertType
import com.instructure.canvasapi2.models.ThresholdWorkflowState
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.UserAvatar
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
            topBar = {
                CanvasAppBar(
                    title = stringResource(id = R.string.alertSettingsTitle),
                    navigationActionClick = navigationActionClick,
                    backgroundColor = Color(uiState.userColor),
                    textColor = colorResource(id = R.color.textLightest)
                )
            }
        ) { padding ->
            AlertSettingsContent(uiState, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
fun AlertSettingsContent(uiState: AlertSettingsUiState, modifier: Modifier) {
    Column(modifier = modifier) {
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
            style = TextStyle(fontSize = 14.sp, color = colorResource(id = R.color.textDark))
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
        AlertType.INSTITUTION_ANNOUNCEMENT -> R.string.alertSettingsInstitutionAnnouncement
    }
}


@Composable
private fun ThresholdItem(
    alertType: AlertType,
    threshold: String?,
    active: Boolean,
    color: Color,
) {
    when (alertType) {
        in percentageItems -> PercentageItem(
            title = stringResource(id = getTitle(alertType)),
            threshold = threshold,
            alertType = alertType,
            color = color,
        )

        else -> SwitchItem(
            title = stringResource(id = getTitle(alertType)),
            active = active,
            alertType = alertType,
            color = color,
        )
    }
}

@Composable
private fun PercentageItem(
    title: String,
    threshold: String?,
    alertType: AlertType,
    color: Color,
) {
    Row(
        modifier = Modifier
            .clickable { }
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(fontSize = 16.sp, color = colorResource(id = R.color.textDarkest))
        )
        Text(
            text = threshold?.let { "${it}%" }
                ?: stringResource(id = R.string.alertSettingsThresholdNever),
            style = TextStyle(color = color, textAlign = TextAlign.End),
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
private fun SwitchItem(
    title: String,
    active: Boolean,
    alertType: AlertType,
    color: Color,
    modifier: Modifier = Modifier
) {
    var switchState by remember { mutableStateOf(active) }
    Row(
        modifier = modifier
            .clickable { switchState = !switchState }
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(fontSize = 16.sp, color = colorResource(id = R.color.textDarkest))
        )
        Switch(
            checked = switchState,
            onCheckedChange = { switchState = it },
            colors = SwitchDefaults.colors(checkedThumbColor = color)
        )
    }
}

@Preview
@Composable
fun AlertSettingsScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    AlertSettingsScreen(
        uiState = AlertSettingsUiState(
            student = User(),
            userColor = android.graphics.Color.BLUE,
            avatarUrl = "",
            studentName = "Test Student",
            studentPronouns = "they/them"
        )
    ) {}
}

@Preview
@Composable
fun PercentageItemPreview() {
    PercentageItem("Test", "20", AlertType.ASSIGNMENT_GRADE_HIGH, Color.Blue)
}

@Preview
@Composable
fun SwitchItemPreview() {
    SwitchItem("Test", true, AlertType.ASSIGNMENT_MISSING, Color.Blue)
}
