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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.ical.values.RRule
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.getDatePickerDialog
import com.instructure.pandautils.utils.ThemePrefs
import org.threeten.bp.LocalDate


@Composable
internal fun CustomFrequencyDialog(
    defaultRRule: RRule?,
    defaultDate: LocalDate,
    onConfirm: (RRule) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedDate = remember { defaultDate }
    val datePickerDialog = remember {
        getDatePickerDialog(
            context = context,
            date = selectedDate,
            onDateSelected = {
                selectedDate = it
            }
        )
    }

    Dialog(
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            color = colorResource(id = R.color.backgroundLightestElevated)
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.eventFrequencyCustomDialogTitle),
                    color = colorResource(id = R.color.textDarkest),
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.subtitle1
                )



                Row(
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {

                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.done),
                            color = Color(ThemePrefs.textButtonColor)
                        )
                    }
                    TextButton(
                        onClick = {
                            onDismissRequest()
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.cancel),
                            color = Color(ThemePrefs.textButtonColor)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CustomFrequencyDialogPreview() {
    ContextKeeper.appContext = LocalContext.current
    CustomFrequencyDialog(
        defaultRRule = RRule(),
        defaultDate = LocalDate.of(2024, 1, 5),
        onConfirm = {},
        onDismissRequest = {}
    )
}
