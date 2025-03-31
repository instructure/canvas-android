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
package com.instructure.pandautils.compose.features.assignments.list

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.compose.composables.GroupedListViewState
import com.instructure.pandautils.features.assignments.list.AssignmentGroupItemState
import com.instructure.pandautils.features.assignments.list.AssignmentGroupState
import com.instructure.pandautils.features.assignments.list.AssignmentListMenuOverFlowItem
import com.instructure.pandautils.features.assignments.list.AssignmentListScreenOption
import com.instructure.pandautils.features.assignments.list.AssignmentListUiState
import com.instructure.pandautils.features.assignments.list.composables.AssignmentListScreen
import com.instructure.pandautils.features.assignments.list.filter.AssignmentFilter
import com.instructure.pandautils.features.assignments.list.filter.AssignmentGroupByOption
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterData
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterOptions
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterType
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListSelectedFilters
import com.instructure.pandautils.utils.ScreenState
import com.jakewharton.threetenabp.AndroidThreeTen
import org.junit.Rule
import org.junit.Test

class AssignmentListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertListToolbarWithOverflow() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            val state = AssignmentListUiState(
                state = ScreenState.Content,
                subtitle = "Course Name",
                overFlowItems = listOf(AssignmentListMenuOverFlowItem("Item 1", {})),
            )
            AssignmentListScreen(
                title = "Assignment List",
                state = state,
                contextColor = Color.Red,
                screenActionHandler = {},
                listActionHandler = {}
            )
        }

        val toolbar = composeTestRule.onNodeWithTag("toolbar")
        toolbar.assertExists()
        composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasText("Assignment List"))).assertIsDisplayed()
        composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasText("Course Name"))).assertIsDisplayed()
        val backButton = composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasContentDescription("Back")))
        backButton
            .assertIsDisplayed()
            .assertHasClickAction()
        val filterButton = composeTestRule.onNode(hasContentDescription("Filter Assignments"))
        filterButton
            .assertIsDisplayed()
            .assertHasClickAction()
        val overflowButton = composeTestRule.onNode(hasTestTag("overflowMenu"))
        overflowButton
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertListToolbarWithoutOverflow() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            val state = AssignmentListUiState(
                state = ScreenState.Content,
                subtitle = "Course Name",
            )
            AssignmentListScreen(
                title = "Assignment List",
                state = state,
                contextColor = Color.Red,
                screenActionHandler = {},
                listActionHandler = {}
            )
        }

        val toolbar = composeTestRule.onNodeWithTag("toolbar")
        toolbar.assertExists()
        composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasText("Assignment List"))).assertIsDisplayed()
        composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasText("Course Name"))).assertIsDisplayed()
        val backButton = composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasContentDescription("Back")))
        backButton
            .assertIsDisplayed()
            .assertHasClickAction()
        val filterButton = composeTestRule.onNode(hasContentDescription("Filter Assignments"))
        filterButton
            .assertIsDisplayed()
            .assertHasClickAction()
        val overflowButton = composeTestRule.onNode(hasTestTag("overflowMenu"))
        overflowButton
            .assertIsNotDisplayed()
    }

    @Test
    fun assertFilterToolbar() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            val state = AssignmentListUiState(
                course = Course(name = "Course Name"),
                state = ScreenState.Content,
                subtitle = "Course",
                screenOption = AssignmentListScreenOption.Filter,
                overFlowItems = listOf(AssignmentListMenuOverFlowItem("Item 1", {})),
                filterOptions = AssignmentListFilterOptions(
                    assignmentFilters = AssignmentListFilterData(
                        listOf(AssignmentFilter.All),
                        AssignmentListFilterType.SingleChoice
                    ),
                    assignmentStatusFilters = null,
                    groupByOptions = listOf(AssignmentGroupByOption.AssignmentGroup),
                    gradingPeriodOptions = null
                ),
                selectedFilterData = AssignmentListSelectedFilters(
                    selectedAssignmentFilters = listOf(AssignmentFilter.All),
                    selectedAssignmentStatusFilter = null,
                    selectedGroupByOption = AssignmentGroupByOption.AssignmentGroup,
                    selectedGradingPeriodFilter = null
                )
            )
            AssignmentListScreen(
                title = "Grade Preferences",
                state = state,
                contextColor = Color.Red,
                screenActionHandler = {},
                listActionHandler = {}
            )
        }

        val toolbar = composeTestRule.onNodeWithTag("toolbar")
        toolbar.assertExists()
        composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasText("Grade Preferences"))).assertIsDisplayed()
        composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasText("Course Name"))).assertIsDisplayed()
        val backButton = composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasContentDescription("Close")))
        backButton
            .assertIsDisplayed()
            .assertHasClickAction()
        val filterButton = composeTestRule.onNode(hasText("Done"))
        filterButton
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertListContent() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            val assignmentGroups = listOf(
                AssignmentGroup(
                    id = 1,
                    name = "Group 1",
                    assignments = listOf(
                        Assignment(name = "Assignment 1"),
                    )
                ),
                AssignmentGroup(
                    id = 2,
                    name = "Group 2",
                    assignments = listOf(
                        Assignment(name = "Assignment 2"),
                    )
                )
            )
            val state = AssignmentListUiState(
                state = ScreenState.Content,
                subtitle = "Course",
                allAssignments = assignmentGroups.flatMap { it.assignments },
                assignmentGroups = assignmentGroups,
                listState = GroupedListViewState(
                    groups = listOf(
                        AssignmentGroupState(
                            id = assignmentGroups[0].id,
                            title = assignmentGroups[0].name ?: "",
                            items = assignmentGroups[0].assignments.map { AssignmentGroupItemState(course = Course(), assignment = it) }
                        ),
                        AssignmentGroupState(
                            id = assignmentGroups[1].id,
                            title = assignmentGroups[1].name ?: "",
                            items = assignmentGroups[1].assignments.map { AssignmentGroupItemState(course = Course(), assignment = it) }
                        ),
                    )
                ),
                screenOption = AssignmentListScreenOption.List,
                overFlowItems = listOf(AssignmentListMenuOverFlowItem("Item 1", {})),
            )
            AssignmentListScreen(
                title = "Grade Preferences",
                state = state,
                contextColor = Color.Red,
                screenActionHandler = {},
                listActionHandler = {}
            )
        }

        val group1 = composeTestRule.onNode(hasText("Group 1"))
        group1
            .assertIsDisplayed()
            .assertHasClickAction()
        val group2 = composeTestRule.onNode(hasText("Group 2"))
        group2
            .assertIsDisplayed()
            .assertHasClickAction()

        val assignment1 = composeTestRule.onNode(hasText("Assignment 1"))
        assignment1
            .assertIsDisplayed()
            .assertHasClickAction()
        val assignment2 = composeTestRule.onNode(hasText("Assignment 2"))
        assignment2
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertListEmptyContent() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            val state = AssignmentListUiState(
                state = ScreenState.Empty,
                subtitle = "Course",
                screenOption = AssignmentListScreenOption.List,
                overFlowItems = listOf(AssignmentListMenuOverFlowItem("Item 1", {})),
            )
            AssignmentListScreen(
                title = "Grade Preferences",
                state = state,
                contextColor = Color.Red,
                screenActionHandler = {},
                listActionHandler = {}
            )
        }

        composeTestRule.onNode(hasText("No Assignments"))
            .assertIsDisplayed()
    }

    @Test
    fun assertListError() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            val state = AssignmentListUiState(
                state = ScreenState.Error,
                subtitle = "Course",
                screenOption = AssignmentListScreenOption.List,
                overFlowItems = listOf(AssignmentListMenuOverFlowItem("Item 1", {})),
            )
            AssignmentListScreen(
                title = "Grade Preferences",
                state = state,
                contextColor = Color.Red,
                screenActionHandler = {},
                listActionHandler = {}
            )
        }

        composeTestRule.onNode(hasText("An error occurred while loading assignments"))
            .assertIsDisplayed()

        composeTestRule.onNode(hasText("Retry"))
            .assertIsDisplayed()
            .assertHasClickAction()
    }
}