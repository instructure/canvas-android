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

import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.parentapp.R
import com.instructure.parentapp.features.addstudent.AddStudentUiState

@Composable
fun PairingCodeScreen(
    uiState: AddStudentUiState,
    onCancelClick: () -> Unit
) {
    var pairingCode by remember { mutableStateOf("") }

    CanvasTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                value = pairingCode,
                onValueChange = { pairingCode = it },
                label = { Text(text = stringResource(id = R.string.pairingCodeDialogLabel)) })

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onCancelClick() }) {
                    Text(text = stringResource(id = R.string.pairingCodeDialogNegativeButton))
                }
                TextButton(
                    onClick = { uiState.onStartPairing(pairingCode) },
                ) {
                    Text(text = stringResource(id = R.string.pairingCodeDialogPositiveButton))
                }
            }
        }
    }
}

@Preview
@Composable
fun PairingCodeScreenPreview() {
    PairingCodeScreen(
        uiState = AddStudentUiState(color = Color.BLUE) {},
        onCancelClick = {})
}
