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

package com.instructure.pandautils.compose.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs

@Composable
fun SimpleAlertDialog(
    dialogTitle: String,
    dialogText: String,
    dismissButtonText: String,
    confirmationButtonText: String,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        title = {
            Text(
                text = dialogTitle,
                color = colorResource(id = R.color.textDarkest)
            )
        },
        text = {
            Text(
                text = dialogText,
                color = colorResource(id = R.color.textDarkest)
            )
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(
                    text = confirmationButtonText,
                    color = Color(ThemePrefs.textButtonColor)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(
                    text = dismissButtonText,
                    color = Color(ThemePrefs.textButtonColor)
                )
            }
        },
        containerColor = colorResource(R.color.backgroundLightestElevated),
        modifier = modifier
    )
}

@Preview
@Composable
fun SimpleAlertDialogPreview() {
    ContextKeeper.appContext = LocalContext.current
    SimpleAlertDialog(
        dialogTitle = "Hello There!",
        dialogText = "General Kenobi!",
        dismissButtonText = "Dismiss",
        confirmationButtonText = "Confirm",
        onDismissRequest = {},
        onConfirmation = {}
    )
}
