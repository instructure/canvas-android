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
package com.instructure.parentapp.features.addstudent.pairingcode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.parentapp.R
import com.instructure.parentapp.features.addstudent.AddStudentAction
import com.instructure.parentapp.features.addstudent.AddStudentUiState

@Composable
fun PairingCodeScreen(
    uiState: AddStudentUiState,
    onCancelClick: () -> Unit
) {

    CanvasTheme {
        when {
            uiState.isLoading -> {
                Loading(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp))
            }

            else -> {
                PairingScreenContent(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    uiState = uiState,
                    onCancelClick = onCancelClick
                )
            }
        }
    }
}

@Composable
private fun PairingScreenContent(
    uiState: AddStudentUiState,
    modifier: Modifier = Modifier,
    onCancelClick: () -> Unit
) {
    var pairingCode by remember { mutableStateOf("") }
    CanvasTheme {
        Column(modifier = modifier) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .testTag("pairingCodeTextField"),
                value = pairingCode,
                onValueChange = {
                    pairingCode = it
                    if (uiState.isError) {
                        uiState.actionHandler(AddStudentAction.ResetError)
                    }
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = if (uiState.isError) {
                        colorResource(id = R.color.textDanger)
                    } else {
                        Color(uiState.color)
                    },
                    focusedLabelColor = Color(uiState.color),
                    cursorColor = Color(uiState.color),
                    textColor = colorResource(id = R.color.textDarkest),
                    unfocusedLabelColor = colorResource(id = R.color.textDark),
                    unfocusedIndicatorColor = colorResource(id = R.color.textDark)
                ),
                textStyle = MaterialTheme.typography.body1,
                label = {
                    Text(
                        text = stringResource(id = R.string.pairingCodeDialogLabel)
                    )
                })
            if (uiState.isError) {
                Text(
                    modifier = Modifier.testTag("errorText"),
                    text = stringResource(id = R.string.pairingCodeDialogError),
                    color = colorResource(id = R.color.textDanger)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onCancelClick() }) {
                    Text(
                        text = stringResource(id = R.string.pairingCodeDialogNegativeButton),
                        color = Color(uiState.color)
                    )
                }
                TextButton(
                    modifier = Modifier.testTag("okButton"),
                    onClick = { uiState.actionHandler(AddStudentAction.PairStudent(pairingCode)) },
                ) {
                    Text(
                        text = stringResource(id = R.string.pairingCodeDialogPositiveButton),
                        color = Color(uiState.color)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PairingCodeScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    PairingCodeScreen(
        uiState = AddStudentUiState(color = android.graphics.Color.BLUE) {},
        onCancelClick = {})
}

@Preview
@Composable
fun PairingScreenLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    PairingCodeScreen(
        uiState = AddStudentUiState(color = android.graphics.Color.BLUE, isLoading = true) {},
        onCancelClick = {})
}
