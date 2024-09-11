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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.UserAvatar
import com.instructure.parentapp.R

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
            modifier = Modifier.fillMaxWidth()
        )
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
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp, start = 16.dp)
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
