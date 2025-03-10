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
package com.instructure.teacher.features.assignment.submission

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.CheckboxText
import com.instructure.pandautils.compose.composables.FullScreenDialog
import com.instructure.pandautils.compose.composables.RadioButtonText
import com.instructure.teacher.R

@Composable
fun SubmissionListFilters(
    filter: SubmissionListFilter,
    filterValue: Double?,
    courseColor: Color,
    assignmentName: String,
    sections: List<CanvasContext>,
    selectedSections: List<Long>,
    actionHandler: (SubmissionListAction) -> Unit,
    dismiss: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(filter) }
    var selectedFilterValue by remember {
        mutableStateOf(filterValue?.let {
            NumberHelper.formatDecimal(
                it,
                2,
                true
            )
        }.orEmpty())
    }
    var selectedSections by remember { mutableStateOf(selectedSections) }
    var error by remember { mutableStateOf(false) }
    FullScreenDialog(onDismissRequest = { dismiss() }) {
        Scaffold(topBar = {
            CanvasAppBar(
                title = stringResource(R.string.preferences),
                subtitle = assignmentName,
                navigationActionClick = {
                    dismiss()
                },
                navIconContentDescription = stringResource(R.string.close),
                navIconRes = R.drawable.ic_close,
                backgroundColor = courseColor,
                textColor = colorResource(id = R.color.textLightest),
                actions = {
                    TextButton(
                        onClick = {
                            if (selectedFilter in listOf(
                                    SubmissionListFilter.BELOW_VALUE,
                                    SubmissionListFilter.ABOVE_VALUE
                                ) && selectedFilterValue.isEmpty()
                            ) {
                                error = true
                            } else {
                                actionHandler(
                                    SubmissionListAction.SetFilters(
                                        selectedFilter,
                                        selectedFilterValue.toDoubleOrNull(),
                                        selectedSections
                                    )
                                )
                                dismiss()
                            }
                        }) {
                        Text(
                            text = stringResource(R.string.done),
                            color = colorResource(R.color.textLightest),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        }) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Header(text = stringResource(R.string.submissionFilter))
                FilterItem(
                    text = stringResource(R.string.all_submissions),
                    selected = selectedFilter == SubmissionListFilter.ALL,
                    courseColor = courseColor,
                    onClick = {
                        selectedFilter = SubmissionListFilter.ALL
                    }
                )
                FilterItem(
                    text = stringResource(R.string.submitted_late),
                    selected = selectedFilter == SubmissionListFilter.LATE,
                    courseColor = courseColor,
                    onClick = {
                        selectedFilter = SubmissionListFilter.LATE
                    }
                )
                FilterItem(
                    text = stringResource(R.string.needsGrading),
                    selected = selectedFilter == SubmissionListFilter.NOT_GRADED,
                    courseColor = courseColor,
                    onClick = {
                        selectedFilter = SubmissionListFilter.NOT_GRADED
                    }
                )
                FilterItem(
                    text = stringResource(R.string.not_submitted),
                    selected = selectedFilter == SubmissionListFilter.MISSING,
                    courseColor = courseColor,
                    onClick = {
                        selectedFilter = SubmissionListFilter.MISSING
                    }
                )
                FilterItem(
                    text = stringResource(R.string.graded),
                    selected = selectedFilter == SubmissionListFilter.GRADED,
                    courseColor = courseColor,
                    onClick = {
                        selectedFilter = SubmissionListFilter.GRADED
                    }
                )
                FilterItem(
                    text = stringResource(R.string.scored_less_than),
                    selected = selectedFilter == SubmissionListFilter.BELOW_VALUE,
                    courseColor = courseColor,
                    onClick = {
                        selectedFilterValue = ""
                        selectedFilter = SubmissionListFilter.BELOW_VALUE
                    }
                )
                if (selectedFilter == SubmissionListFilter.BELOW_VALUE) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        value = selectedFilterValue,
                        onValueChange = {
                            error = false
                            selectedFilterValue = it
                        },
                        label = { Text(stringResource(R.string.score)) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = colorResource(R.color.textDarkest)
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        ),
                        isError = error
                    )
                }
                FilterItem(
                    text = stringResource(R.string.scored_more_than),
                    selected = selectedFilter == SubmissionListFilter.ABOVE_VALUE,
                    courseColor = courseColor,
                    onClick = {
                        selectedFilterValue = ""
                        selectedFilter = SubmissionListFilter.ABOVE_VALUE
                    }
                )
                if (selectedFilter == SubmissionListFilter.ABOVE_VALUE) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        value = selectedFilterValue,
                        onValueChange = {
                            error = false
                            selectedFilterValue = it
                        },
                        label = { Text(stringResource(R.string.score)) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = colorResource(R.color.textDarkest)
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        ),
                        isError = error
                    )
                }
                if (sections.isNotEmpty()) {
                    Header(text = stringResource(R.string.filterBySection))

                    sections.forEach { section ->
                        CheckboxText(
                            text = section.name.orEmpty(),
                            selected = selectedSections.contains(section.id),
                            color = courseColor,
                            onCheckedChanged = {
                                selectedSections = if (it) {
                                    selectedSections + section.id
                                } else {
                                    selectedSections - section.id
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(text: String) {
    CanvasDivider()
    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        text = text,
        fontSize = 14.sp,
        color = colorResource(id = R.color.textDark),
        fontWeight = FontWeight.SemiBold
    )
    CanvasDivider()
}

@Composable
private fun FilterItem(
    text: String,
    selected: Boolean,
    courseColor: Color,
    onClick: () -> Unit
) {
    RadioButtonText(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        selected = selected,
        color = courseColor,
        onClick = {
            onClick()
        }
    )
}