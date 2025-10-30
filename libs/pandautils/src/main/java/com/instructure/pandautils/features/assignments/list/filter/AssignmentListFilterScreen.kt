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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.MultiChoicePicker
import com.instructure.pandautils.compose.composables.SingleChoicePicker
import com.instructure.pandautils.utils.color

@Composable
fun AssignmentListFilterScreen(
    courseName: String,
    contextColor: Color,
    assignmentFilterOptions: AssignmentListFilterData,
    assignmentStatusFilterOptions: List<AssignmentStatusFilterOption>?,
    assignmentGroupByOptions: List<AssignmentGroupByOption>,
    gradingPeriodOptions: List<GradingPeriod?>?,
    selectedOptions: AssignmentListSelectedFilters,
    onFilterChange: (AssignmentListSelectedFilters) -> Unit,
    onBackPressed: () -> Unit
) {
    var selectedFilters by remember { mutableStateOf(selectedOptions) }
    Scaffold(
        backgroundColor = colorResource(id = com.instructure.pandares.R.color.backgroundLightest),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CanvasThemedAppBar(
                title = stringResource(R.string.gradePreferences),
                subtitle = courseName,
                backgroundColor = contextColor,
                contentColor = colorResource(R.color.backgroundLightest),
                navIconRes = R.drawable.ic_close,
                navIconContentDescription = stringResource(R.string.close),
                navigationActionClick = { onBackPressed() },
                actions = {
                    TextButton({ onFilterChange(selectedFilters); onBackPressed() }) {
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
                .verticalScroll(rememberScrollState())
        ) {
            if (assignmentFilterOptions.assignmentFilterType == AssignmentListFilterType.SingleChoice) {
                SingleChoicePicker(
                    title = stringResource(R.string.assignmentFilter),
                    items = assignmentFilterOptions.assignmentFilterOptions,
                    stringValueOfItem = {
                        when (it) {
                            AssignmentFilter.All -> stringResource(R.string.allAssignments)
                            AssignmentFilter.NotYetSubmitted -> stringResource(R.string.notYetSubmitted)
                            AssignmentFilter.ToBeGraded -> stringResource(R.string.toBeGraded)
                            AssignmentFilter.Graded -> stringResource(R.string.graded)
                            AssignmentFilter.Other -> stringResource(R.string.other)
                            AssignmentFilter.NeedsGrading -> stringResource(R.string.needsGrading)
                            AssignmentFilter.NotSubmitted -> stringResource(R.string.notSubmitted)
                        }
                    },
                    contextColor = contextColor,
                    selectedItem = selectedFilters.selectedAssignmentFilters.first(),
                    onItemSelected = { selectedFilters = selectedFilters.copy(selectedAssignmentFilters = listOf(it)) }
                )
            } else {
                MultiChoicePicker(
                    title = stringResource(R.string.assignmentFilter),
                    items = assignmentFilterOptions.assignmentFilterOptions,
                    stringValueOfItem = {
                        when (it) {
                            AssignmentFilter.All -> stringResource(R.string.all)
                            AssignmentFilter.NotYetSubmitted -> stringResource(R.string.notYetSubmitted)
                            AssignmentFilter.ToBeGraded -> stringResource(R.string.toBeGraded)
                            AssignmentFilter.Graded -> stringResource(R.string.graded)
                            AssignmentFilter.Other -> stringResource(R.string.other)
                            AssignmentFilter.NeedsGrading -> stringResource(R.string.needsGrading)
                            AssignmentFilter.NotSubmitted -> stringResource(R.string.notSubmitted)
                        }
                    },
                    contextColor = contextColor,
                    selectedItems = selectedFilters.selectedAssignmentFilters,
                    onItemChange = { item, isSelected ->
                        val newSelectedIndexes = if (isSelected) {
                            selectedFilters.selectedAssignmentFilters + item
                        } else {
                            selectedFilters.selectedAssignmentFilters - item
                        }
                        selectedFilters = selectedFilters.copy(selectedAssignmentFilters = newSelectedIndexes)
                    }
                )
            }

            assignmentStatusFilterOptions?.let {
                SingleChoicePicker(
                    title = stringResource(R.string.statusFilter),
                    items = assignmentStatusFilterOptions,
                    stringValueOfItem = {
                        when (it) {
                            AssignmentStatusFilterOption.All -> stringResource(R.string.allAssignments)
                            AssignmentStatusFilterOption.Published -> stringResource(R.string.published)
                            AssignmentStatusFilterOption.Unpublished -> stringResource(R.string.unpublished)
                        }
                    },
                    contextColor = contextColor,
                    selectedItem = selectedFilters.selectedAssignmentStatusFilter ?: assignmentStatusFilterOptions.first(),
                    onItemSelected = { selectedFilters = selectedFilters.copy(selectedAssignmentStatusFilter = it) }
                )
            }

            SingleChoicePicker(
                title = stringResource(R.string.groupedBy),
                items = assignmentGroupByOptions,
                stringValueOfItem = {
                    when (it) {
                        AssignmentGroupByOption.DueDate -> stringResource(R.string.dueDate)
                        AssignmentGroupByOption.AssignmentGroup -> stringResource(R.string.assignmentGroup)
                        AssignmentGroupByOption.AssignmentType -> stringResource(R.string.assignmentType)
                    }
                },
                contextColor = contextColor,
                selectedItem = selectedFilters.selectedGroupByOption ?: assignmentGroupByOptions.first(),
                onItemSelected = { selectedFilters = selectedFilters.copy(selectedGroupByOption = it) }
            )

            if (gradingPeriodOptions != null && gradingPeriodOptions.size > 1) {
                SingleChoicePicker(
                    title = stringResource(R.string.gradingPeriodHeading),
                    items = gradingPeriodOptions,
                    stringValueOfItem = {
                        if (it == null)  {
                             stringResource(R.string.allGradingPeriods)
                        } else {
                            it.title.orEmpty()
                        }
                    },
                    contextColor = contextColor,
                    selectedItem = selectedFilters.selectedGradingPeriodFilter ?: gradingPeriodOptions.first(),
                    onItemSelected = { selectedFilters = selectedFilters.copy(selectedGradingPeriodFilter = it) }
                )
            }
        }
    }
}

@Composable
@Preview
fun AssignmentFilterPreview() {
    ContextKeeper.appContext = LocalContext.current
    val course = Course(name = "Course 1", courseColor = Color.Magenta.toString())

    AssignmentListFilterScreen(
        courseName = course.name,
        contextColor = Color(course.color),
        assignmentFilterOptions = AssignmentListFilterData(
            assignmentFilterType = AssignmentListFilterType.MultiChoice,
            assignmentFilterOptions = listOf(
                AssignmentFilter.NotYetSubmitted,
                AssignmentFilter.ToBeGraded,
                AssignmentFilter.Graded,
                AssignmentFilter.Other,
            )
        ),
        assignmentStatusFilterOptions = listOf(
            AssignmentStatusFilterOption.All,
            AssignmentStatusFilterOption.Published,
            AssignmentStatusFilterOption.Unpublished
        ),
        assignmentGroupByOptions = listOf(
            AssignmentGroupByOption.DueDate,
            AssignmentGroupByOption.AssignmentGroup,
            AssignmentGroupByOption.AssignmentType
        ),
        gradingPeriodOptions = null,
        selectedOptions = AssignmentListSelectedFilters(
            selectedAssignmentFilters = listOf(AssignmentFilter.All),
            selectedAssignmentStatusFilter = AssignmentStatusFilterOption.All,
            selectedGroupByOption = AssignmentGroupByOption.DueDate,
            selectedGradingPeriodFilter = null
        ),
        onFilterChange = {},
        onBackPressed = {}
    )
}