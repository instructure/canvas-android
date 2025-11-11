/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.todolist.filter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.utils.ThemePrefs

@Composable
fun ToDoFilterScreen(
    onFiltersChanged: (areDateFiltersChanged: Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = hiltViewModel<ToDoFilterViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.shouldCloseAndApplyFilters) {
        if (uiState.shouldCloseAndApplyFilters) {
            onFiltersChanged(uiState.areDateFiltersChanged)
            uiState.onFiltersApplied()
        }
    }

    Scaffold(
        backgroundColor = colorResource(R.color.backgroundLightest),
        topBar = {
            CanvasThemedAppBar(
                title = stringResource(id = R.string.todoFilterPreferences),
                navIconRes = R.drawable.ic_close,
                navIconContentDescription = stringResource(id = R.string.close),
                navigationActionClick = onDismiss,
                actions = {
                    TextButton(
                        onClick = {
                            uiState.onDone()
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.done),
                            color = Color(ThemePrefs.primaryTextColor),
                            fontSize = 14.sp
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        ToDoFilterContent(
            uiState = uiState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

@Composable
fun ToDoFilterContent(
    uiState: ToDoFilterUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            SectionHeader(title = stringResource(id = R.string.todoFilterVisibleItems))
        }

        uiState.checkboxItems.forEachIndexed { index, item ->
            item {
                CheckboxItem(
                    title = stringResource(id = item.titleRes),
                    checked = item.checked,
                    onCheckedChange = item.onToggle,
                    showDivider = index == uiState.checkboxItems.lastIndex
                )
            }
        }

        item {
            SectionHeader(title = stringResource(id = R.string.todoFilterShowTasksFrom))
        }

        item {
            DateRangeOptions(
                options = uiState.pastDateOptions,
                selectedOption = uiState.selectedPastOption,
                onOptionSelected = uiState.onPastDaysChanged,
                showDivider = true
            )
        }

        item {
            SectionHeader(title = stringResource(id = R.string.todoFilterShowTasksUntil))
        }

        item {
            DateRangeOptions(
                options = uiState.futureDateOptions,
                selectedOption = uiState.selectedFutureOption,
                onOptionSelected = uiState.onFutureDaysChanged,
                showDivider = false
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(id = R.color.textDark),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        CanvasDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp)
        )
    }
}

@Composable
private fun CheckboxItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(ThemePrefs.brandColor),
                    uncheckedColor = Color(ThemePrefs.brandColor)
                )
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                color = colorResource(id = R.color.textDarkest)
            )
        }

        if (showDivider) {
            CanvasDivider(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DateRangeOptions(
    options: List<DateRangeOption>,
    selectedOption: DateRangeSelection,
    onOptionSelected: (DateRangeSelection) -> Unit,
    showDivider: Boolean
) {
    Column {
        options.forEachIndexed { index, option ->
            DateRangeOptionItem(
                option = option,
                isSelected = selectedOption == option.selection,
                onSelected = { onOptionSelected(option.selection) }
            )
        }

        if (showDivider) {
            CanvasDivider(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DateRangeOptionItem(
    option: DateRangeOption,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelected)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelected,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(ThemePrefs.brandColor),
                unselectedColor = Color(ThemePrefs.brandColor)
            ),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.labelText,
                fontSize = 16.sp,
                color = colorResource(id = R.color.textDarkest)
            )

            Text(
                text = option.dateText,
                fontSize = 14.sp,
                color = colorResource(id = R.color.textDark)
            )
        }

        Spacer(modifier = Modifier.width(24.dp))
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun ToDoFilterScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    CanvasTheme {
        ToDoFilterContent(
            uiState = ToDoFilterUiState(
                checkboxItems = listOf(
                    FilterCheckboxItem(
                        titleRes = R.string.todoFilterShowPersonalToDos,
                        checked = false,
                        onToggle = {}
                    ),
                    FilterCheckboxItem(
                        titleRes = R.string.todoFilterShowCalendarEvents,
                        checked = true,
                        onToggle = {}
                    ),
                    FilterCheckboxItem(
                        titleRes = R.string.todoFilterShowCompleted,
                        checked = false,
                        onToggle = {}
                    ),
                    FilterCheckboxItem(
                        titleRes = R.string.todoFilterFavoriteCoursesOnly,
                        checked = true,
                        onToggle = {}
                    )
                ),
                pastDateOptions = listOf(
                    DateRangeOption(
                        selection = DateRangeSelection.FOUR_WEEKS,
                        labelText = "4 Weeks Ago",
                        dateText = "From 7 Oct"
                    ),
                    DateRangeOption(
                        selection = DateRangeSelection.THREE_WEEKS,
                        labelText = "3 Weeks Ago",
                        dateText = "From 14 Oct"
                    ),
                    DateRangeOption(
                        selection = DateRangeSelection.TWO_WEEKS,
                        labelText = "2 Weeks Ago",
                        dateText = "From 21 Oct"
                    ),
                    DateRangeOption(
                        selection = DateRangeSelection.ONE_WEEK,
                        labelText = "Last Week",
                        dateText = "From 28 Oct"
                    ),
                    DateRangeOption(
                        selection = DateRangeSelection.THIS_WEEK,
                        labelText = "This Week",
                        dateText = "From 4 Nov"
                    ),
                    DateRangeOption(
                        selection = DateRangeSelection.TODAY,
                        labelText = "Today",
                        dateText = "From 4 Nov"
                    )
                ),
                selectedPastOption = DateRangeSelection.ONE_WEEK,
                futureDateOptions = listOf(
                    DateRangeOption(
                        selection = DateRangeSelection.TODAY,
                        labelText = "Today",
                        dateText = "Until 4 Nov"
                    ),
                    DateRangeOption(
                        selection = DateRangeSelection.THIS_WEEK,
                        labelText = "This Week",
                        dateText = "Until 10 Nov"
                    ),
                    DateRangeOption(
                        selection = DateRangeSelection.ONE_WEEK,
                        labelText = "Next Week",
                        dateText = "Until 17 Nov"
                    ),
                    DateRangeOption(
                        selection = DateRangeSelection.TWO_WEEKS,
                        labelText = "In 2 Weeks",
                        dateText = "Until 24 Nov"
                    ),
                    DateRangeOption(
                        selection = DateRangeSelection.THREE_WEEKS,
                        labelText = "In 3 Weeks",
                        dateText = "Until 1 Dec"
                    ),
                    DateRangeOption(
                        selection = DateRangeSelection.FOUR_WEEKS,
                        labelText = "In 4 Weeks",
                        dateText = "Until 8 Dec"
                    )
                ),
                selectedFutureOption = DateRangeSelection.ONE_WEEK
            )
        )
    }
}