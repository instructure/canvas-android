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

package com.instructure.parentapp.features.managestudents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ThemedColor


@Composable
internal fun StudentColorPickerDialog(
    initialColorIndex: Int,
    userColors: List<ThemedColor>,
    saving: Boolean,
    error: Boolean,
    onDismiss: () -> Unit,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var colorIndex by remember { mutableIntStateOf(initialColorIndex) }

    Dialog(
        onDismissRequest = {
            onDismiss()
        }
    ) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            color = colorResource(id = R.color.backgroundLightestElevated)
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.selectStudentColor),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(20.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    itemsIndexed(userColors) { index, color ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .let { modifier ->
                                    if (index == colorIndex) {
                                        modifier.border(3.dp, Color(color.backgroundColor()), CircleShape)
                                    } else {
                                        modifier
                                    }
                                }
                                .padding(8.dp)
                                .clip(shape = CircleShape)
                                .background(color = Color(color.backgroundColor()))
                                .clickable {
                                    colorIndex = index
                                }
                        )
                    }
                }
                if (error) {
                    Text(
                        text = stringResource(id = R.string.errorSavingColor),
                        color = colorResource(id = R.color.textDanger),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp)
                    )
                }
                Row(
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    if (saving) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color(ThemePrefs.textButtonColor),
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
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
                        TextButton(
                            onClick = {
                                if (colorIndex == initialColorIndex) {
                                    onDismiss()
                                } else {
                                    onColorSelected(colorIndex)
                                }
                            },
                        ) {
                            Text(
                                text = stringResource(id = R.string.ok),
                                color = Color(ThemePrefs.textButtonColor),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun StudentColorPickerDialogPreview() {
    ContextKeeper.appContext = LocalContext.current
    StudentColorPickerDialog(
        initialColorIndex = 0,
        userColors = ColorKeeper.getThemedUserColors().values.toList(),
        error = false,
        saving = false,
        onDismiss = {},
        onColorSelected = {}
    )
}
