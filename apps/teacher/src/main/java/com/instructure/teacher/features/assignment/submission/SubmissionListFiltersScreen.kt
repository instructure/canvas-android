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

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.compose.composables.BasicTextFieldWithHintDecoration
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.CheckboxText
import com.instructure.pandautils.compose.composables.FullScreenDialog
import com.instructure.pandautils.compose.composables.RadioButtonText
import com.instructure.pandautils.features.speedgrader.SubmissionListFilter
import com.instructure.pandautils.utils.orDefault
import com.instructure.teacher.R

data class SubmissionListFiltersUiState(
    val selectedFilters: Set<SubmissionListFilter>,
    val filterValueAbove: Double?,
    val filterValueBelow: Double?,
    val assignmentMaxPoints: Double?,
    val courseColor: Color,
    val assignmentName: String,
    val anonymousGrading: Boolean,
    val sections: List<CanvasContext>,
    val initialSelectedSections: List<Long>,
    val differentiationTags: List<DifferentiationTag>,
    val selectedDifferentiationTagIds: Set<String>,
    val includeStudentsWithoutTags: Boolean,
    val sortOrder: SubmissionSortOrder,
    val customGradeStatuses: List<CustomGradeStatus>,
    val selectedCustomStatusIds: Set<String>,
    val actionHandler: (SubmissionListAction) -> Unit
)

@Composable
fun SubmissionListFilters(
    uiState: SubmissionListFiltersUiState,
    modifier: Modifier = Modifier,
    dismiss: () -> Unit
) {

    FullScreenDialog(onDismissRequest = { dismiss() }) {
        SubmissionFilterScreenContent(
            uiState = uiState,
            modifier = modifier,
            dismiss = dismiss
        )
    }
}

@Composable
private fun SubmissionFilterScreenContent(
    uiState: SubmissionListFiltersUiState,
    modifier: Modifier = Modifier,
    dismiss: () -> Unit
) {
    var filters by rememberSaveable { mutableStateOf(uiState.selectedFilters) }
    var valueAbove by rememberSaveable {
        mutableStateOf(uiState.filterValueAbove?.let {
            NumberHelper.formatDecimal(it, 2, true)
        }.orEmpty())
    }
    var valueBelow by rememberSaveable {
        mutableStateOf(uiState.filterValueBelow?.let {
            NumberHelper.formatDecimal(it, 2, true)
        }.orEmpty())
    }
    var selectedSections by rememberSaveable { mutableStateOf(uiState.initialSelectedSections.toSet()) }
    var selectedTags by rememberSaveable { mutableStateOf(uiState.selectedDifferentiationTagIds) }
    var includeWithoutTags by rememberSaveable { mutableStateOf(uiState.includeStudentsWithoutTags) }
    var selectedSortOrder by rememberSaveable { mutableStateOf(uiState.sortOrder) }
    var selectedCustomStatuses by rememberSaveable { mutableStateOf(uiState.selectedCustomStatusIds) }
    Scaffold(
        backgroundColor = colorResource(id = R.color.backgroundLightest),
        topBar = {
            CanvasAppBar(
                title = stringResource(R.string.preferences),
                subtitle = uiState.assignmentName,
                navigationActionClick = {
                    dismiss()
                },
                navIconContentDescription = stringResource(R.string.close),
                navIconRes = R.drawable.ic_close,
                backgroundColor = uiState.courseColor,
                textColor = colorResource(id = R.color.textLightest),
                actions = {
                    TextButton(
                        modifier = Modifier.testTag("appBarDoneButton"),
                        onClick = {
                            uiState.actionHandler(
                                SubmissionListAction.SetFilters(
                                    selectedFilters = filters,
                                    filterValueAbove = valueAbove.toDoubleOrNull(),
                                    filterValueBelow = valueBelow.toDoubleOrNull(),
                                    selectedSections = selectedSections.toList(),
                                    selectedDifferentiationTagIds = selectedTags,
                                    includeStudentsWithoutTags = includeWithoutTags,
                                    sortOrder = selectedSortOrder,
                                    selectedCustomStatusIds = selectedCustomStatuses
                                )
                            )
                            dismiss()
                        }) {
                        Text(
                            text = stringResource(R.string.done),
                            color = colorResource(R.color.textLightest),
                            fontSize = 14.sp
                        )
                    }
                }
            )
        }) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            Header(text = stringResource(R.string.statuses))
            SubmissionListFilter.entries.filter {
                listOf(
                    SubmissionListFilter.ALL,
                    SubmissionListFilter.ABOVE_VALUE,
                    SubmissionListFilter.BELOW_VALUE
                ).contains(it).not()
            }.forEach { filter ->
                CheckboxText(
                    text = when (filter) {
                        SubmissionListFilter.LATE -> stringResource(R.string.late)
                        SubmissionListFilter.MISSING -> stringResource(R.string.missingTag)
                        SubmissionListFilter.NOT_GRADED -> stringResource(R.string.needsGrading)
                        SubmissionListFilter.GRADED -> stringResource(R.string.graded)
                        SubmissionListFilter.SUBMITTED -> stringResource(R.string.submitted)
                        else -> ""
                    },
                    testTag = "statusCheckBox",
                    selected = filters.contains(filter),
                    color = uiState.courseColor,
                    onCheckedChanged = {
                        filters = if (filters.contains(filter)) {
                            filters - filter
                        } else {
                            (filters - SubmissionListFilter.ALL) + filter
                        }.let { it.ifEmpty { setOf(SubmissionListFilter.ALL) } }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp)
                        .padding(horizontal = 4.dp)
                )
            }

            // Custom grade statuses
            uiState.customGradeStatuses.forEach { status ->
                CheckboxText(
                    text = status.name,
                    testTag = "customStatusCheckBox",
                    selected = selectedCustomStatuses.contains(status.id),
                    color = uiState.courseColor,
                    onCheckedChanged = {
                        selectedCustomStatuses = if (selectedCustomStatuses.contains(status.id)) {
                            selectedCustomStatuses - status.id
                        } else {
                            selectedCustomStatuses + status.id
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp)
                        .padding(horizontal = 4.dp)
                )
            }

            Header(text = stringResource(R.string.precise_filtering))
            Row(
                modifier = Modifier
                    .defaultMinSize(minHeight = 56.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("preciseFilterAboveRow"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.scored_more_than),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 21.sp,
                    color = colorResource(R.color.textDarkest)
                )
                Spacer(modifier = Modifier.weight(1f))
                BasicTextFieldWithHintDecoration(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .testTag("scoreMoreThanField"),
                    value = valueAbove,
                    onValueChange = { valueAbove = it },
                    hint = stringResource(R.string.write_score_here),
                    textColor = uiState.courseColor,
                    hintColor = colorResource(R.color.textDark),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    )
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.pointsPossible,
                        uiState.assignmentMaxPoints?.toInt().orDefault(),
                        uiState.assignmentMaxPoints.orDefault()
                    ),
                    fontSize = 16.sp,
                    color = colorResource(R.color.textDark)
                )
            }
            Row(
                modifier = Modifier
                    .defaultMinSize(minHeight = 56.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("preciseFilterBelowRow"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.scored_less_than),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 21.sp,
                    color = colorResource(R.color.textDarkest)
                )
                Spacer(modifier = Modifier.weight(1f))
                BasicTextFieldWithHintDecoration(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .testTag("scoreLessThanField"),
                    value = valueBelow,
                    onValueChange = { valueBelow = it },
                    hint = stringResource(R.string.write_score_here),
                    textColor = uiState.courseColor,
                    hintColor = colorResource(R.color.textDark),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    )
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.pointsPossible,
                        uiState.assignmentMaxPoints?.toInt().orDefault(),
                        uiState.assignmentMaxPoints.orDefault()
                    ),
                    fontSize = 16.sp,
                    color = colorResource(R.color.textDark)
                )
            }

            if (uiState.sections.isNotEmpty()) {
                Header(text = stringResource(R.string.filterBySection))
                uiState.sections.forEach { section ->
                    CheckboxText(
                        text = section.name.orEmpty(),
                        testTag = "sectionCheckBox",
                        selected = selectedSections.contains(section.id),
                        color = uiState.courseColor,
                        onCheckedChanged = {
                            selectedSections = if (selectedSections.contains(section.id)) {
                                selectedSections - section.id
                            } else {
                                selectedSections + section.id
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 56.dp)
                            .padding(horizontal = 4.dp)
                    )
                }
            }

            if (uiState.differentiationTags.isNotEmpty()) {
                Header(text = stringResource(R.string.differentiation_tags))
                CheckboxText(
                    text = stringResource(R.string.students_without_differentiation_tags),
                    testTag = "includeWithoutTagsCheckBox",
                    selected = includeWithoutTags,
                    color = uiState.courseColor,
                    onCheckedChanged = { includeWithoutTags = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp)
                        .padding(horizontal = 4.dp)
                )
                uiState.differentiationTags.forEach { tag ->
                    CheckboxText(
                        text = tag.name,
                        subtitle = tag.groupSetName,
                        testTag = "differentiationTagCheckBox",
                        selected = selectedTags.contains(tag.id),
                        color = uiState.courseColor,
                        onCheckedChanged = {
                            selectedTags = if (selectedTags.contains(tag.id)) {
                                selectedTags - tag.id
                            } else {
                                selectedTags + tag.id
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 56.dp)
                            .padding(horizontal = 4.dp)
                    )
                }
            }

            Header(text = stringResource(R.string.sort_by))
            SubmissionSortOrder.entries.filter {
                if (uiState.anonymousGrading) {
                    it != SubmissionSortOrder.STUDENT_NAME && it != SubmissionSortOrder.STUDENT_SORTABLE_NAME
                } else {
                    true
                }
            }.forEach { order ->
                RadioButtonText(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 56.dp)
                        .padding(horizontal = 4.dp)
                        .fillMaxWidth()
                        .testTag("${order.name}SortButton"),
                    text = when (order) {
                        SubmissionSortOrder.STUDENT_NAME -> stringResource(R.string.student_name)
                        SubmissionSortOrder.SUBMISSION_DATE -> stringResource(R.string.submission_date)
                        SubmissionSortOrder.STUDENT_SORTABLE_NAME -> stringResource(R.string.student_sortable_name)
                        SubmissionSortOrder.SUBMISSION_STATUS -> stringResource(R.string.submission_status)
                    },
                    selected = selectedSortOrder == order,
                    color = uiState.courseColor,
                    onClick = { selectedSortOrder = order }
                )
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

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SubmissionFilterScreenPreview() {
    SubmissionFilterScreenContent(
        uiState = SubmissionListFiltersUiState(
            selectedFilters = setOf(SubmissionListFilter.ALL),
            filterValueAbove = null,
            filterValueBelow = null,
            assignmentMaxPoints = 100.0,
            courseColor = Color.Black,
            assignmentName = "Assignment Name",
            anonymousGrading = false,
            sections = listOf(Section(id = 1, name = "Section 1"), Section(id = 2, name = "Section 2")),
            initialSelectedSections = listOf(1L),
            differentiationTags = emptyList(),
            selectedDifferentiationTagIds = emptySet(),
            includeStudentsWithoutTags = false,
            sortOrder = SubmissionSortOrder.STUDENT_SORTABLE_NAME,
            customGradeStatuses = emptyList(),
            selectedCustomStatusIds = emptySet(),
            actionHandler = {}
        ),
        dismiss = {}
    )
}