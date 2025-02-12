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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.orDefault


@Composable
fun NumberOfOccurrencesDialog(
    initialValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val occurrences = remember { mutableIntStateOf(initialValue) }

    AlertDialog(
        title = {
            Text(
                text = stringResource(id = R.string.numberOfOccurrencesDialogTitle),
                color = colorResource(id = R.color.textDarkest)
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(id = R.string.numberOfOccurrencesDialogDescription),
                    color = colorResource(id = R.color.textDark)
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = occurrences.intValue.takeIf { it > 0 }?.toString().orEmpty(),
                    onValueChange = {
                        occurrences.intValue = it.toIntOrNull().orDefault().coerceAtMost(400)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = colorResource(id = R.color.borderMedium),
                        focusedContainerColor = colorResource(id = R.color.borderInfo),
                        cursorColor = colorResource(id = R.color.textDarkest),
                        focusedTextColor = colorResource(id = R.color.textDark)
                    )
                )
                Text(
                    text = stringResource(id = R.string.numberOfOccurrencesDialogSupportingText),
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.textDark),
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        },
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(occurrences.intValue)
                }
            ) {
                Text(
                    text = stringResource(id = R.string.done),
                    color = Color(ThemePrefs.textButtonColor)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
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
fun NumberOfOccurrencesDialogPreview() {
    ContextKeeper.appContext = LocalContext.current
    NumberOfOccurrencesDialog(
        initialValue = 25,
        onDismiss = {},
        onConfirm = {}
    )
}
