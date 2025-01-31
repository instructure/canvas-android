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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs

@Composable
fun SingleChoiceAlertDialog(
    dialogTitle: String,
    items: List<String>,
    dismissButtonText: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    defaultSelection: Int = -1,
    confirmationButtonText: String? = null,
    onConfirmation: (Int) -> Unit = {},
    onItemSelected: (Int) -> Unit = {},
) {
    val selectedIndex = remember { mutableIntStateOf(defaultSelection) }

    Dialog(
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        Surface(
            modifier = modifier.testTag("singleChoiceAlertDialog"),
            shape = MaterialTheme.shapes.medium,
            color = colorResource(id = R.color.backgroundLightestElevated)
        ) {
            Column {
                Text(
                    text = dialogTitle,
                    color = colorResource(id = R.color.textDarkest),
                    modifier = Modifier
                        .padding(20.dp)
                        .semantics { heading() },
                    fontSize = 16.sp,
                )
                ChoiceList(
                    items = items,
                    selectedIndex = selectedIndex.value,
                    onItemSelected = {
                        selectedIndex.intValue = it
                        onItemSelected(it)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Row(
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
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
                    if (confirmationButtonText != null) {
                        val confirmEnabled = selectedIndex.intValue != -1
                        TextButton(
                            onClick = {
                                onConfirmation(selectedIndex.intValue)
                            },
                            enabled = confirmEnabled
                        ) {
                            Text(
                                text = confirmationButtonText,
                                color = Color(ThemePrefs.textButtonColor),
                                modifier = Modifier.alpha(if (confirmEnabled) 1f else .4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoiceList(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        content = {
            itemsIndexed(items) { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            },
                            indication = null,
                            onClick = {
                                onItemSelected(index)
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = item == items.getOrNull(selectedIndex),
                        onClick = {
                            onItemSelected(index)
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(color = ThemePrefs.brandColor),
                            unselectedColor = colorResource(id = R.color.textDarkest)
                        )
                    )
                    Text(
                        text = item,
                        color = colorResource(id = R.color.textDarkest),
                        fontSize = 14.sp,
                    )
                }
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
fun SingleChoiceAlertDialogPreview() {
    ContextKeeper.appContext = LocalContext.current
    SingleChoiceAlertDialog(
        dialogTitle = "Hello There!",
        items = listOf("Item 1", "Item 2", "Item 3"),
        defaultSelection = 1,
        dismissButtonText = "Dismiss",
        confirmationButtonText = "Confirm",
        onDismissRequest = {},
        onConfirmation = {},
        onItemSelected = {}
    )
}
