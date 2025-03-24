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
package com.instructure.canvas.espresso.common.pages.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvasapi2.models.Assignment
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.QuizApiModel
import com.instructure.espresso.R
import com.instructure.espresso.page.plus
import com.instructure.pandautils.utils.toFormattedString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AssignmentListPage(private val composeTestRule: ComposeTestRule) {
    val searchBar = SearchableToolbar(composeTestRule)

    fun clickAssignment(assignment: AssignmentApiModel) {
        composeTestRule.onNodeWithText(assignment.name)
            .performScrollTo()
            .performClick()
    }

    fun clickAssignment(assignment: Assignment) {
        composeTestRule.onNodeWithText(assignment.name!!)
            .performScrollTo()
            .performClick()
    }

    fun clickQuiz(quiz: QuizApiModel) {
        composeTestRule.onNodeWithText(quiz.title)
            .performScrollTo()
            .performClick()
    }

    fun assertDisplaysNoAssignmentsView() {
        composeTestRule.onNodeWithText("No Assignments")
            .assertIsDisplayed()
    }

    fun assertHasAssignment(assignment: AssignmentApiModel) {
        assertHasAssignmentCommon(assignment.name, assignment.dueAt, null)
    }

    fun assertHasAssignment(assignment: AssignmentApiModel, expectedGrade: String? = null) {
        assertHasAssignmentCommon(assignment.name, assignment.dueAt, expectedGrade)
    }

    fun assertHasAssignment(assignment: Assignment, expectedGrade: String? = null) {
        assertHasAssignmentCommon(assignment.name!!, assignment.dueAt, expectedGrade)
    }

    fun assertHasAssignment(assignment: AssignmentApiModel, needsGradingCount: Int? = null) {
        assertHasAssignmentCommon(assignment.name!!, assignment.dueAt, "$needsGradingCount Needs Grading")
    }

    fun assertAssignmentNotDisplayed(assignmentName: String) {
        onView(withText(assignmentName) + withId(R.id.title) + hasSibling(withId(R.id.description))).check(
            doesNotExist()
        )
    }

    fun assertAssignmentGroupDisplayed(groupName: String) {
        composeTestRule.onNode(
            hasText(groupName).and(hasParent(hasAnyDescendant(hasContentDescription("Collapse content, $groupName").or(
                hasContentDescription("Expand content, $groupName")
            ))))
        )
        .performScrollTo()
        .assertIsDisplayed()
    }

    fun expandCollapseAssignmentGroup(groupName: String) {
        composeTestRule.onNode(
            hasText(groupName).and(hasParent(hasAnyDescendant(hasContentDescription("Collapse content, $groupName").or(
                hasContentDescription("Expand content, $groupName")
            ))))
        )
            .performScrollTo()
            .performClick()
    }

    private fun assertHasAssignmentCommon(assignmentName: String, assignmentDueAt: String?, expectedLabel: String? = null) {

        // Check that either the assignment due date is present, or "No Due Date" is displayed
        if(assignmentDueAt != null) {
            composeTestRule.onNode(
                hasText(assignmentName).and(
                    hasParent(hasAnyDescendant(hasText(assignmentDueAt.toDate()!!.toFormattedString())))
                )
            )
            .assertIsDisplayed()
        }
        else {
            composeTestRule.onNode(
                hasText(assignmentName).and(
                    hasParent(hasAnyDescendant(hasText("No Due Date")))
                )
            )
            .assertIsDisplayed()
        }

        // Check that grade is present, if that is specified
        if(expectedLabel != null) {
            composeTestRule.onNode(
                hasText(assignmentName).and(
                    hasParent(hasAnyDescendant(hasText(expectedLabel, substring = true)))
                )
            )
            .assertIsDisplayed()
        }

    }

    fun assertQuizNotDisplayed(quiz: QuizApiModel) {
        composeTestRule.onNodeWithText(quiz.title)
            .assertIsNotDisplayed()
    }

    fun refreshAssignmentList() {
        composeTestRule.onNodeWithTag("assignmentList").performTouchInput { swipeDown() }
    }

    fun assertGradingPeriodLabel(gradingPeriodName: String? = null) {
        composeTestRule.onNode(
            hasText("Grading Period:").and(hasParent(hasAnyDescendant(hasText(gradingPeriodName ?: "All"))))
        )
            .assertIsDisplayed()
    }

    private fun clickFilterMenu() {
        composeTestRule.onNodeWithContentDescription("Filter Assignments").performClick()
    }

    fun filterAssignments(groupName: String, option: FilterOption) {
        clickFilterMenu()
        selectFilterOption(groupName, option)
        composeTestRule.onNodeWithText("Done").performClick()
    }

    fun groupByAssignments(option: GroupByOption) {
        clickFilterMenu()
        selectGroupByOption("Grouped By", option)
        composeTestRule.onNodeWithText("Done").performClick()
    }

    private fun selectFilterOption(groupName: String, option: FilterOption) {
        val filterText = when(option) {
            is FilterOption.All -> "All Assignments"
            is FilterOption.Graded -> "Graded"
            is FilterOption.ToBeGraded -> "To Be Graded"
            is FilterOption.NotYetSubmitted -> "Not Yet Submitted"
            is FilterOption.Other -> "Other"
            is FilterOption.Ungraded -> "Ungraded"
            is FilterOption.NeedsGrading -> "Needs Grading"
            is FilterOption.NoSubmission -> "Not Submitted"
            is FilterOption.Published -> "Published"
            is FilterOption.Unpublished -> "Unpublished"
            is FilterOption.GradingPeriod -> option.name ?: "All Grading Period"
        }
        composeTestRule.onNode(
            hasText(filterText).and(hasParent(hasAnyDescendant(hasText(groupName))))
        )
        .performClick()
    }

    private fun selectGroupByOption(groupName: String, option: GroupByOption) {
        val groupByText = when(option) {
            is GroupByOption.AssignmentGroup -> "Assignment Group"
            is GroupByOption.AssignmentType -> "Assignment Type"
            is GroupByOption.DueDate -> "Due Date"
        }
        composeTestRule.onNode(
            hasText(groupByText).and(hasParent(hasAnyDescendant(hasText(groupName))))
        )
            .performClick()
    }

    private fun String?.toDate(): Date? {
        this ?: return null

        return try {
            var s = this.replace("Z", "+00:00")
            s = s.substring(0, 22) + s.substring(23)
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).parse(s)
        } catch (e: Exception) {
            null
        }
    }

    sealed class FilterOption {
        data object All : FilterOption()

        data object Graded : FilterOption()
        data object ToBeGraded : FilterOption()
        data object NotYetSubmitted : FilterOption()
        data object Other : FilterOption()
        data object Ungraded : FilterOption()

        data object NeedsGrading : FilterOption()
        data object NoSubmission : FilterOption()
        data object Published : FilterOption()
        data object Unpublished : FilterOption()

        data class GradingPeriod(val name: String?) : FilterOption()
    }

    sealed class GroupByOption {
        data object AssignmentGroup : GroupByOption()
        data object AssignmentType : GroupByOption()
        data object DueDate : GroupByOption()
    }
}