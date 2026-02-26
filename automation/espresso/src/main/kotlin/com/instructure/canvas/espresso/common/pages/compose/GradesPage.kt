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

package com.instructure.canvas.espresso.common.pages.compose

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import com.instructure.composetest.hasDrawable
import com.instructure.pandautils.R
import java.lang.Thread.sleep


class GradesPage(private val composeTestRule: ComposeTestRule) {

    fun clickGroupHeader(name: String) {
        composeTestRule.onNodeWithTag("gradesList")
            .performScrollToNode(hasText(name))
        composeTestRule.onNodeWithText(name)
            .performClick()
    }

    fun assertAssignmentIsDisplayed(name: String) {
        composeTestRule.onNodeWithTag("gradesList")
            .performScrollToNode(hasText(name))
        composeTestRule.onNodeWithText(name)
            .assertIsDisplayed()
    }

    fun assertAssignmentIsNotDisplayed(name: String) {
        composeTestRule.onNodeWithText(name)
            .assertIsNotDisplayed()
    }

    fun assertTotalGradeText(grade: String) {
        composeTestRule.onNode(hasTestTag("totalGradeScoreText") and hasText(grade), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    fun assertAssignmentGradeText(assignmentName: String, gradeText: String) {
        composeTestRule.onNode(hasText(assignmentName, substring = true) and hasText(gradeText, substring = true)).assertIsDisplayed()
    }

    fun assertGroupHeaderIsDisplayed(name: String) {
        composeTestRule.onNodeWithTag("gradesList")
            .performScrollToNode(hasText(name))
        composeTestRule.onNodeWithText(name)
            .assertIsDisplayed()
    }

    fun assertGroupHeaderIsNotDisplayed(name: String) {
        composeTestRule.onNodeWithText(name)
            .assertIsNotDisplayed()
    }

    fun clickFilterButton() {
        composeTestRule.onNodeWithContentDescription("Filter")
            .performClick()
    }

    fun assertFilterNotApplied() {
        composeTestRule.onNode(hasContentDescription("Filter") and hasDrawable(R.drawable.ic_filter))
    }

    fun assertFilterApplied() {
        composeTestRule.onNode(hasContentDescription("Filter") and hasDrawable(R.drawable.ic_filter_active))
    }

    fun clickFilterOption(option: String) {
        composeTestRule.onNodeWithText(option)
            .performClick()
    }

    fun clickSaveButton() {
        composeTestRule.onNodeWithText("Save")
            .performClick()
    }

    fun clickAssignment(name: String) {
        composeTestRule.onNodeWithTag("gradesList")
            .performScrollToNode(hasText(name))
        composeTestRule.onNodeWithText(name)
            .performClick()
    }

    fun assertEmptyStateIsDisplayed() {
        composeTestRule.onNodeWithText("No Assignments")
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun scrollDownScreen() {
        composeTestRule.onNodeWithTag("gradesList")
            .performTouchInput { swipeUp() }
    }

    fun scrollUpScreen() {
        composeTestRule.onNodeWithTag("gradesList")
            .performTouchInput { swipeDown() }
    }

    fun assertCardText(text: String) {
        composeTestRule.onNodeWithTag("gradesCardText", true)
            .assertTextEquals(text)
            .assertIsDisplayed()
    }

    fun assertBasedOnGradedAssignmentsLabel() {
        composeTestRule.onNodeWithTag("basedOnGradedAssignmentsLabel", useUnmergedTree = true).assertIsDisplayed()
    }

    fun refresh() {
        composeTestRule.onNodeWithTag("gradesList").performTouchInput { swipeDown() }
        composeTestRule.waitForIdle()
    }

    fun assertGradesPreferencesFilterScreenLabels() {
        composeTestRule.onNode(hasTestTag("GradePreferencesToolbar") and hasText("Grade Preferences"), useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Grading Period", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Sort By", useUnmergedTree = true).assertIsDisplayed()
    }

    fun assertToolbarTitles(subtitle: String) {
        composeTestRule.onNodeWithText("Grades", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle, useUnmergedTree = true).assertIsDisplayed()
    }

    fun clickBasedOnGradedAssignments() {
        composeTestRule.onNodeWithText("Based on graded assignments")
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun assertBasedOnGradedAssignmentsToggleState(isOn: Boolean) {
        if (isOn) {
            composeTestRule.onNodeWithTag("basedOnGradedAssignmentsSwitch", useUnmergedTree = true)
                .assertIsDisplayed().assertIsOn()
        }
        else {
            composeTestRule.onNodeWithTag("basedOnGradedAssignmentsSwitch", useUnmergedTree = true)
                .assertIsDisplayed().assertIsOff()
        }
    }

    fun clickShowWhatIfScore() {
        composeTestRule.onNodeWithTag("showWhatIfScoreLabel", useUnmergedTree = true)
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun assertShowWhatIfScoreToggleState(isOn: Boolean) {
        if (isOn) {
            composeTestRule.onNodeWithTag("showWhatIfScoreSwitch", useUnmergedTree = true)
                .assertIsDisplayed().assertIsOn()
        }
        else {
            composeTestRule.onNodeWithTag("showWhatIfScoreSwitch", useUnmergedTree = true)
                .assertIsDisplayed().assertIsOff()
        }
    }

    fun assertShowWhatIfScoreIsDisplayed() {
        composeTestRule.onNodeWithTag("showWhatIfScoreLabel", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    fun clickEditWhatIfScore(assignmentName: String) {
        composeTestRule.onNodeWithTag("gradesList")
            .performScrollToNode(hasText(assignmentName))
        composeTestRule.onNode(
            hasTestTag("editWhatIfScore") and hasAnyAncestor(
                hasTestTag("assignmentItem") and hasAnyDescendant(hasText(assignmentName))
            ),
            useUnmergedTree = true
        ).performClick()
        composeTestRule.waitForIdle()
    }

    fun enterWhatIfScore(score: String) {
        composeTestRule.onNodeWithTag("whatIfScoreInput")
            .performClick()
        composeTestRule.onNodeWithTag("whatIfScoreInput")
            .performTextInput(score)
        composeTestRule.waitForIdle()
    }

    fun clickDoneInWhatIfDialog() {
        composeTestRule.onNodeWithTag("doneButton")
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun clickCancelInWhatIfDialog() {
        composeTestRule.onNodeWithTag("cancelButton")
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun clickClearWhatIfScore() {
        composeTestRule.onNodeWithTag("clearWhatIfScoreButton")
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun assertWhatIfGradeText(assignmentName: String, gradeText: String) {
        composeTestRule.onNodeWithTag("gradesList")
            .performScrollToNode(hasText(assignmentName))
        composeTestRule.onNodeWithTag("whatIfGradeText", useUnmergedTree = true)
            .assertTextEquals(gradeText)
    }

    fun assertAssignmentDueDate(assignmentName: String, dueDate: String) {
        composeTestRule.onNode(hasTestTag("assignmentDueDate") and hasText(dueDate, substring = true
        ) and hasParent(hasAnyChild(hasText(assignmentName))), useUnmergedTree = true)
        .assertIsDisplayed()
    }

    fun assertAssignmentStatus(assignmentName: String, stateText: String) {
        composeTestRule.onNode(hasTestTag("submissionStateLabel") and hasText(stateText
        ) and hasParent(hasAnyChild(hasText(assignmentName))), useUnmergedTree = true)
        .assertIsDisplayed()
    }

    fun clickAssignmentGroupExpandCollapseButton(assignmentGroupName: String) {
        composeTestRule.onNode(
            hasTestTag("assignmentGroupExpandCollapseIcon") and hasAnySibling(hasText(assignmentGroupName)), useUnmergedTree = true
        ).performClick()
        composeTestRule.waitForIdle()
        sleep(1000)
    }

    fun assertAllAssignmentItemCount(expectedCount: Int) {
        composeTestRule.onAllNodesWithTag("assignmentItem")
            .assertCountEquals(expectedCount)
    }
}
