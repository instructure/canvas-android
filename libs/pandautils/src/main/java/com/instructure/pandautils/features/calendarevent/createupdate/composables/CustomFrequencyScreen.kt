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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.Dropdown
import com.instructure.pandautils.compose.getDatePickerDialog
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventAction
import com.instructure.pandautils.features.calendarevent.createupdate.CustomFrequencyUiState
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.orDefault
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import java.util.Locale


@Composable
internal fun CustomFrequencyScreen(
    uiState: CustomFrequencyUiState,
    actionHandler: (CreateUpdateEventAction) -> Unit,
    navigationActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        backgroundColor = colorResource(id = R.color.backgroundLightest),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CanvasAppBar(
                title = stringResource(id = R.string.eventCustomFrequencyScreenTitle),
                navigationActionClick = navigationActionClick,
                navIconRes = R.drawable.ic_back_arrow,
                navIconContentDescription = stringResource(id = R.string.back),
                actions = {
                    val saveEnabled = uiState.quantity > 0 && (uiState.selectedDate != null || uiState.selectedOccurrences > 0)
                    TextButton(
                        onClick = {
                            actionHandler(CreateUpdateEventAction.SaveCustomFrequency)
                            actionHandler(CreateUpdateEventAction.HideCustomFrequencyScreen)
                            actionHandler(CreateUpdateEventAction.HideFrequencyDialog)
                        },
                        enabled = saveEnabled
                    ) {
                        Text(
                            text = stringResource(id = R.string.save),
                            color = Color(color = ThemePrefs.textButtonColor),
                            fontSize = 14.sp,
                            modifier = Modifier.alpha(if (saveEnabled) 1f else .4f)
                        )
                    }
                }
            )
        },
        content = { padding ->
            CustomFrequencyContent(
                uiState = uiState,
                actionHandler = actionHandler,
                modifier = modifier
                    .padding(padding)
                    .fillMaxSize()
            )
        }
    )
}

@Composable
private fun CustomFrequencyContent(
    uiState: CustomFrequencyUiState,
    actionHandler: (CreateUpdateEventAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDatePickerDialog by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(showDatePickerDialog) {
        if (showDatePickerDialog) {
            getDatePickerDialog(
                context = context,
                date = uiState.selectedDate ?: LocalDate.now().plusYears(1),
                onDateSelected = {
                    actionHandler(CreateUpdateEventAction.UpdateCustomFrequencyEndDate(it))
                    showDatePickerDialog = false
                },
                onCancel = {
                    showDatePickerDialog = false
                },
                onDismiss = {
                    showDatePickerDialog = false
                }

            ).show()
        }
    }

    var showNumberOfOccurrencesDialog by rememberSaveable { mutableStateOf(false) }
    if (showNumberOfOccurrencesDialog) {
        NumberOfOccurrencesDialog(
            initialValue = uiState.selectedOccurrences,
            onDismiss = {
                showNumberOfOccurrencesDialog = false
            },
            onConfirm = {
                actionHandler(CreateUpdateEventAction.UpdateCustomFrequencyOccurrences(it))
                showNumberOfOccurrencesDialog = false
            }
        )
    }

    Surface(
        modifier = modifier,
        color = colorResource(id = R.color.backgroundLightest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                OutlinedTextField(
                    value = uiState.quantity.takeIf { it > 0 }?.toString().orEmpty(),
                    onValueChange = {
                        actionHandler(CreateUpdateEventAction.UpdateCustomFrequencyQuantity(it.toIntOrNull().orDefault()))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = colorResource(id = R.color.borderMedium),
                        focusedBorderColor = colorResource(id = R.color.borderInfo),
                        cursorColor = colorResource(id = R.color.textDarkest),
                        textColor = colorResource(id = R.color.textDark)
                    )
                )
                Dropdown(
                    selectedIndex = uiState.selectedTimeUnitIndex,
                    options = uiState.timeUnits,
                    onOptionSelected = {
                        actionHandler(CreateUpdateEventAction.UpdateCustomFrequencySelectedTimeUnitIndex(it))
                    },
                    modifier = Modifier
                        .weight(2f)
                        .padding(end = 16.dp)
                )
            }
            AnimatedVisibility(visible = uiState.daySelectorVisible) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    WeekDaySelector(
                        uiState = uiState,
                        actionHandler = actionHandler,
                        modifier = modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxWidth()
                    )
                }
            }
            AnimatedVisibility(visible = uiState.repeatsOnVisible) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.eventCustomFrequencyScreenRepeatsOn),
                            color = colorResource(id = R.color.textDarkest),
                            fontSize = 16.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        )
                        Dropdown(
                            selectedIndex = uiState.selectedRepeatsOnIndex,
                            options = uiState.repeatsOn,
                            onOptionSelected = {
                                actionHandler(CreateUpdateEventAction.UpdateCustomFrequencySelectedRepeatsOnIndex(it))
                            },
                            modifier = Modifier
                                .weight(2f)
                                .padding(end = 16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.eventCustomFrequencyScreenEnds),
                fontSize = 14.sp,
                color = colorResource(id = R.color.textDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colorResource(id = R.color.backgroundLight))
                    .padding(16.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .height(48.dp)
                    .clickable {
                        showDatePickerDialog = true
                    }
            ) {
                RadioButton(
                    selected = uiState.selectedDate != null,
                    onClick = {
                        showDatePickerDialog = true
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(color = ThemePrefs.brandColor),
                        unselectedColor = colorResource(id = R.color.textDarkest)
                    ),
                    modifier = Modifier.padding(start = 2.dp)
                )
                Text(
                    text = stringResource(id = R.string.eventCustomFrequencyScreenOn),
                    modifier = Modifier.padding(start = 8.dp),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = uiState.formattedEndDate,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .weight(1f),
                    color = colorResource(id = R.color.textDark),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End
                )
                Icon(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 16.dp),
                    tint = colorResource(id = R.color.textDark)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .height(48.dp)
                    .clickable {
                        showNumberOfOccurrencesDialog = true
                    }
            ) {
                RadioButton(
                    selected = uiState.selectedOccurrences > 0,
                    onClick = {
                        showNumberOfOccurrencesDialog = true
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(color = ThemePrefs.brandColor),
                        unselectedColor = colorResource(id = R.color.textDarkest)
                    ),
                    modifier = Modifier.padding(start = 2.dp)
                )
                Text(
                    text = stringResource(id = R.string.eventCustomFrequencyScreenAfter),
                    modifier = Modifier.padding(start = 8.dp),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = uiState.selectedOccurrences.takeIf { it > 0 }?.toString().orEmpty(),
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .weight(1f),
                    color = colorResource(id = R.color.textDark),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End
                )
                Icon(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 16.dp),
                    tint = colorResource(id = R.color.textDark)
                )
            }
        }
    }
}

@Composable
private fun WeekDaySelector(
    uiState: CustomFrequencyUiState,
    actionHandler: (CreateUpdateEventAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        uiState.days.forEach {
            val selected = uiState.selectedDays.contains(it)
            Text(
                text = it.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                fontSize = 12.sp,
                color = if (selected) {
                    Color(ThemePrefs.buttonTextColor)
                } else {
                    colorResource(id = R.color.textDarkest)
                },
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(size = 40.dp))
                    .background(
                        color = if (selected) {
                            Color(ThemePrefs.buttonColor)
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(40.dp)
                    )
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .clickable {
                        actionHandler(
                            CreateUpdateEventAction.UpdateCustomFrequencySelectedDays(
                                if (selected) {
                                    uiState.selectedDays - it
                                } else {
                                    uiState.selectedDays + it
                                }
                            )
                        )
                    }
            )
        }
    }
}

@Preview
@Composable
fun CustomFrequencyScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    AndroidThreeTen.init(LocalContext.current)
    CanvasTheme {
        CustomFrequencyScreen(
            uiState = CustomFrequencyUiState(),
            actionHandler = {},
            navigationActionClick = {}
        )
    }
}
