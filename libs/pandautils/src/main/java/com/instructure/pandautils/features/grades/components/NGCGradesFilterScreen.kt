/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.pandautils.features.grades.components

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.indicator.IconButton
import com.instructure.instui.compose.indicator.IconButtonColor
import com.instructure.instui.compose.input.Checkbox
import com.instructure.instui.compose.input.RadioButton
import com.instructure.instui.compose.input.TextButton
import com.instructure.instui.compose.list.Separator
import com.instructure.instui.compose.navigation.TopBar
import com.instructure.instui.compose.text.Text as InstUIText
import com.instructure.instui.token.component.InstUIHeading
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.R
import com.instructure.pandautils.features.assignments.list.filter.AssignmentFilter
import com.instructure.pandautils.features.assignments.list.filter.AssignmentGroupByOption
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterData
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterType
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListSelectedFilters
import com.instructure.pandautils.features.assignments.list.filter.AssignmentStatusFilterOption

@Composable
fun NGCGradesFilterScreen(
    courseName: String,
    contextColor: Color,
    assignmentFilterOptions: AssignmentListFilterData,
    assignmentStatusFilterOptions: List<AssignmentStatusFilterOption>?,
    assignmentGroupByOptions: List<AssignmentGroupByOption>,
    gradingPeriodOptions: List<GradingPeriod?>?,
    selectedOptions: AssignmentListSelectedFilters,
    onFilterChange: (AssignmentListSelectedFilters) -> Unit,
    onBackPressed: () -> Unit,
) {
    var selected by remember { mutableStateOf(selectedOptions) }
    val onColor = InstUISemanticColors.Text.onColor()

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.gradePreferences),
                subtitle = courseName,
                containerColor = contextColor,
                contentColor = onColor,
                navigationIcon = {
                    IconButton(
                        iconRes = R.drawable.ic_close,
                        contentDescription = stringResource(R.string.close),
                        color = IconButtonColor.Inverse,
                        onClick = onBackPressed,
                    )
                },
                actions = {
                    TextButton(
                        text = stringResource(R.string.done),
                        contentColor = onColor,
                        onClick = {
                            onFilterChange(selected)
                            onBackPressed()
                        },
                    )
                },
            )
        },
        containerColor = InstUISemanticColors.Background.page(),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            FilterSection(R.string.assignmentFilter) {
                if (assignmentFilterOptions.assignmentFilterType == AssignmentListFilterType.SingleChoice) {
                    assignmentFilterOptions.assignmentFilterOptions.forEach { option ->
                        FilterRadioRow(
                            label = option.label(),
                            selected = option == selected.selectedAssignmentFilters.firstOrNull(),
                            accent = contextColor,
                            onClick = {
                                selected = selected.copy(selectedAssignmentFilters = listOf(option))
                            },
                        )
                    }
                } else {
                    assignmentFilterOptions.assignmentFilterOptions.forEach { option ->
                        val checked = option in selected.selectedAssignmentFilters
                        FilterCheckboxRow(
                            label = option.label(),
                            checked = checked,
                            accent = contextColor,
                            onToggle = { isChecked ->
                                val newList = if (isChecked) {
                                    selected.selectedAssignmentFilters + option
                                } else {
                                    selected.selectedAssignmentFilters - option
                                }
                                selected = selected.copy(selectedAssignmentFilters = newList)
                            },
                        )
                    }
                }
            }

            assignmentStatusFilterOptions?.let { statusOptions ->
                Separator()
                FilterSection(R.string.statusFilter) {
                    statusOptions.forEach { option ->
                        FilterRadioRow(
                            label = option.label(),
                            selected = option == (selected.selectedAssignmentStatusFilter ?: statusOptions.first()),
                            accent = contextColor,
                            onClick = {
                                selected = selected.copy(selectedAssignmentStatusFilter = option)
                            },
                        )
                    }
                }
            }

            Separator()
            FilterSection(R.string.groupedBy) {
                assignmentGroupByOptions.forEach { option ->
                    FilterRadioRow(
                        label = option.label(),
                        selected = option == selected.selectedGroupByOption,
                        accent = contextColor,
                        onClick = {
                            selected = selected.copy(selectedGroupByOption = option)
                        },
                    )
                }
            }

            if (gradingPeriodOptions != null && gradingPeriodOptions.size > 1) {
                Separator()
                FilterSection(R.string.gradingPeriodHeading) {
                    gradingPeriodOptions.forEach { period ->
                        FilterRadioRow(
                            label = period?.title ?: stringResource(R.string.allGradingPeriods),
                            selected = period == selected.selectedGradingPeriodFilter,
                            accent = contextColor,
                            onClick = {
                                selected = selected.copy(selectedGradingPeriodFilter = period)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    @StringRes title: Int,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.background(InstUISemanticColors.Background.container()),
    ) {
        InstUIText(
            text = stringResource(title),
            style = InstUIHeading.label,
            color = InstUISemanticColors.Text.muted(),
            modifier = Modifier.padding(
                start = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                end = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                top = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                bottom = InstUILayoutSizes.Spacing.SpaceSm.spaceSm,
            ),
        )
        content()
    }
}

@Composable
private fun FilterRadioRow(label: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    RadioButton(
        selected = selected,
        onClick = onClick,
        label = label,
        accentColor = accent,
    )
}

@Composable
private fun FilterCheckboxRow(label: String, checked: Boolean, accent: Color, onToggle: (Boolean) -> Unit) {
    Checkbox(
        checked = checked,
        onCheckedChange = onToggle,
        label = label,
        accentColor = accent,
    )
}

@Composable
private fun AssignmentFilter.label(): String = stringResource(
    when (this) {
        AssignmentFilter.All -> R.string.all
        AssignmentFilter.NotYetSubmitted -> R.string.notYetSubmitted
        AssignmentFilter.ToBeGraded -> R.string.toBeGraded
        AssignmentFilter.Graded -> R.string.graded
        AssignmentFilter.Other -> R.string.other
        AssignmentFilter.NeedsGrading -> R.string.needsGrading
        AssignmentFilter.NotSubmitted -> R.string.notSubmitted
    }
)

@Composable
private fun AssignmentStatusFilterOption.label(): String = stringResource(
    when (this) {
        AssignmentStatusFilterOption.All -> R.string.allAssignments
        AssignmentStatusFilterOption.Published -> R.string.published
        AssignmentStatusFilterOption.Unpublished -> R.string.unpublished
    }
)

@Composable
private fun AssignmentGroupByOption.label(): String = stringResource(
    when (this) {
        AssignmentGroupByOption.DueDate -> R.string.dueDate
        AssignmentGroupByOption.AssignmentGroup -> R.string.assignmentGroup
        AssignmentGroupByOption.AssignmentType -> R.string.assignmentType
    }
)

@Preview(name = "NGCGradesFilterScreen — Light", showBackground = true)
@Preview(
    name = "NGCGradesFilterScreen — Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun NGCGradesFilterScreenPreview() {
    InstUITheme(courseColor = Color(0xFF00828E)) {
        NGCGradesFilterScreen(
            courseName = "Biology 101",
            contextColor = Color(0xFF00828E),
            assignmentFilterOptions = AssignmentListFilterData(
                assignmentFilterType = AssignmentListFilterType.MultiChoice,
                assignmentFilterOptions = listOf(
                    AssignmentFilter.NotYetSubmitted,
                    AssignmentFilter.ToBeGraded,
                    AssignmentFilter.Graded,
                    AssignmentFilter.Other,
                ),
            ),
            assignmentStatusFilterOptions = null,
            assignmentGroupByOptions = listOf(
                AssignmentGroupByOption.DueDate,
                AssignmentGroupByOption.AssignmentGroup,
            ),
            gradingPeriodOptions = listOf(
                GradingPeriod(id = 1L, title = "Grading Period 1"),
                GradingPeriod(id = 2L, title = "Grading Period 2"),
            ),
            selectedOptions = AssignmentListSelectedFilters(
                selectedAssignmentFilters = listOf(
                    AssignmentFilter.NotYetSubmitted,
                    AssignmentFilter.ToBeGraded,
                ),
                selectedGroupByOption = AssignmentGroupByOption.AssignmentGroup,
            ),
            onFilterChange = {},
            onBackPressed = {},
        )
    }
}
