/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.assignments.list.filter

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.MultiChoicePicker
import com.instructure.pandautils.compose.composables.SingleChoicePicker

@Composable
fun AssignmentListFilterScreen(
    state: AssignmentListFilterState,
    onFilterChange: (AssignmentListFilterState) -> Unit,
    onBackPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            CanvasAppBar(
                title = stringResource(R.string.gradePreferences),
                backgroundColor = Color(state.contextColor),
                textColor = colorResource(R.color.backgroundLightest),
                navigationActionClick = { onBackPressed() },
                actions = {
                    TextButton({ onBackPressed() }) {
                        Text(stringResource(R.string.done), color = colorResource(R.color.backgroundLightest))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            state.filterGroups.forEach { group ->
                AssignmentListFilterGroup(group, Color(state.contextColor)) {
                    onFilterChange(state.copy(filterGroups = state.filterGroups.map { g ->
                        if (g.title == it.title) {
                            it
                        } else {
                            g
                        }
                    }))
                }
            }
        }
    }
}

@Composable
private fun AssignmentListFilterGroup(
    group: AssignmentListFilterGroup,
    contextColor: Color,
    onFilterChange: (AssignmentListFilterGroup) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        when (group.groupType) {
            AssignmentListFilterGroupType.SingleChoice -> {
                Log.d("AssignmentListFilterScreen", "SingleChoicePicker: ${group.options}")
                SingleChoicePicker(
                    title = group.title,
                    items = group.options,
                    contextColor = contextColor,
                    selectedItem = group.selectedOptions.first(),
                    onItemSelected = { onFilterChange(group.copy(selectedOptions = listOf(it))) },
                )
            }
            AssignmentListFilterGroupType.MultiChoice -> {
                MultiChoicePicker(
                    title = group.title,
                    items = group.options,
                    contextColor = contextColor,
                    selectedItems = group.selectedOptions,
                    onItemChange = { item, isSelected ->
                        val newSelectedOptions = if (isSelected) {
                            group.selectedOptions + item
                        } else {
                            group.selectedOptions - item
                        }
                        onFilterChange(group.copy(selectedOptions = newSelectedOptions))
                    },
                )
            }
        }
    }
}