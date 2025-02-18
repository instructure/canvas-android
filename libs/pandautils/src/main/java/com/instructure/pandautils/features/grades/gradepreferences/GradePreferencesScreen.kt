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

package com.instructure.pandautils.features.grades.gradepreferences

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.ListHeaderItem


@Composable
fun GradePreferencesScreen(
    uiState: GradePreferencesUiState,
    onPreferenceChangeSaved: (GradingPeriod?, SortBy) -> Unit,
    navigationActionClick: () -> Unit
) {
    var selectedPeriod by rememberSaveable { mutableStateOf(uiState.selectedGradingPeriod) }
    var selectedSortBy by rememberSaveable { mutableStateOf(uiState.sortBy) }

    Scaffold(
        backgroundColor = colorResource(id = R.color.backgroundLightest),
        topBar = {
            CanvasThemedAppBar(
                title = stringResource(id = R.string.gradePreferencesScreenTitle),
                subtitle = uiState.courseName,
                navigationActionClick = navigationActionClick,
                navIconRes = R.drawable.ic_close,
                navIconContentDescription = stringResource(id = R.string.close),
                backgroundColor = Color(color = uiState.canvasContextColor),
                contentColor = colorResource(id = R.color.textLightest)
            ) {
                val saveEnabled = selectedPeriod != uiState.selectedGradingPeriod || selectedSortBy != uiState.sortBy
                TextButton(
                    onClick = {
                        onPreferenceChangeSaved(selectedPeriod, selectedSortBy)
                    },
                    enabled = saveEnabled
                ) {
                    Text(
                        text = stringResource(id = R.string.save),
                        color = colorResource(id = R.color.textLightest),
                        fontSize = 14.sp,
                        modifier = Modifier.alpha(if (saveEnabled) 1f else .4f)
                    )
                }
            }
        }
    ) {
        GradePreferencesContent(
            uiState = uiState,
            selectedSortBy = selectedSortBy,
            selectedGradingPeriod = selectedPeriod,
            onGradingPeriodChanged = { gradingPeriod ->
                selectedPeriod = gradingPeriod
            },
            onSortByChanged = { sortBy ->
                selectedSortBy = sortBy
            },
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        )
    }
}

@Composable
private fun GradePreferencesContent(
    uiState: GradePreferencesUiState,
    selectedSortBy: SortBy,
    selectedGradingPeriod: GradingPeriod?,
    onGradingPeriodChanged: (GradingPeriod?) -> Unit,
    onSortByChanged: (SortBy) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        item {
            ListHeaderItem(text = stringResource(id = R.string.gradePreferencesHeaderGradingPeriod))
        }
        item {
            GradePreferencesItem(
                color = Color(color = uiState.canvasContextColor),
                itemTitle = stringResource(id = R.string.allGradingPeriods),
                id = 0,
                selected = selectedGradingPeriod == null,
                onItemSelected = {
                    onGradingPeriodChanged(null)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        items(
            uiState.gradingPeriods
        ) { gradingPeriod ->
            val selected = gradingPeriod == selectedGradingPeriod
            GradePreferencesItem(
                color = Color(color = uiState.canvasContextColor),
                itemTitle = gradingPeriod.title.orEmpty(),
                id = gradingPeriod.id,
                selected = selected,
                onItemSelected = { id ->
                    onGradingPeriodChanged(
                        uiState.gradingPeriods.find {
                            it.id == id
                        }
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        item {
            ListHeaderItem(text = stringResource(id = R.string.gradePreferencesHeaderSortBy))
        }
        items(
            SortBy.entries
        ) { sortBy ->
            val selected = sortBy == selectedSortBy
            GradePreferencesItem(
                color = Color(color = uiState.canvasContextColor),
                itemTitle = stringResource(id = sortBy.titleRes),
                id = sortBy.ordinal.toLong(),
                selected = selected,
                onItemSelected = { id ->
                    onSortByChanged(
                        SortBy.entries[id.toInt()]
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun GradePreferencesItem(
    color: Color,
    itemTitle: String,
    id: Long,
    selected: Boolean,
    onItemSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 54.dp)
            .selectable(selected = selected) {
                onItemSelected(id)
            }
            .padding(start = 8.dp, end = 16.dp)
            .semantics {
                role = Role.RadioButton
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = {
                onItemSelected(id)
            },
            colors = RadioButtonDefaults.colors(
                selectedColor = color,
                unselectedColor = color
            ),
            modifier = Modifier.semantics {
                invisibleToUser()
            }
        )
        Text(
            text = itemTitle,
            color = colorResource(id = R.color.textDarkest),
            fontSize = 16.sp
        )
    }
}

@Preview
@Composable
private fun GradePreferencesPreview() {
    ContextKeeper.appContext = LocalContext.current
    GradePreferencesScreen(
        uiState = GradePreferencesUiState(
            show = true,
            courseName = "Cosmology & Space longer spaceholder spaceholder spaceholder spaceholder spaceholder",
            gradingPeriods = listOf(
                GradingPeriod(
                    id = 1,
                    title = "Grading Period 1"
                ),
                GradingPeriod(
                    id = 2,
                    title = "Grading Period 2"
                )
            ),
            selectedGradingPeriod = GradingPeriod(
                id = 1,
                title = "Grading Period 1"
            )
        ),
        onPreferenceChangeSaved = { _, _ -> },
        navigationActionClick = {}
    )
}