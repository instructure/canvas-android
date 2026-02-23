/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.ui.pages.compose


import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.swipeDown
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.getStringFromResource
import com.instructure.teacher.R

/**
 * Represents a page for managing assignment submissions.
 *
 * This class extends the `BasePage` class and provides methods for interacting with various elements on the page.
 * It contains functions for asserting the presence of specific views, clicking on elements, and performing filter actions.
 *
 * @constructor Creates an instance of the `AssignmentSubmissionListPage` class.
 */
class AssignmentSubmissionListPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    /**
     * Assert displays no submissions view
     *
     */
    fun assertEmptyViewDisplayed() {
        composeTestRule.onNodeWithTag("EmptyContent").assertIsDisplayed()
        composeTestRule.onNodeWithText("No submissions").assertIsDisplayed()
    }

    /**
     * Assert that the student submission IS displayed.
     *
     * @param canvasUser The Canvas user whose submission is to be verified (based on the user's name).
     */
    fun assertHasStudentSubmission(canvasUser: CanvasUserApiModel) {
        composeTestRule.onNode(
            hasTestTag("submissionListItemStudentName") and hasText(canvasUser.name),
            useUnmergedTree = true
        )
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Assert that the student submission is NOT displayed.
     *
     * @param canvasUser The Canvas user whose submission is to be verified (based on the user's name).
     */
    fun assertStudentSubmissionNotDisplayed(canvasUser: CanvasUserApiModel) {
        composeTestRule.onNode(
            hasTestTag("submissionListItemStudentName") and hasText(canvasUser.name),
            useUnmergedTree = true
        )
            .assertDoesNotExist()
    }

    /**
     * Type the 'searchText' to the search input field.
     */
    fun searchSubmission(searchText: String) {
        composeTestRule.onNodeWithTag("searchField")
            .requestFocus()
            .performClick()
            .performTextInput(searchText)
        composeTestRule.onNodeWithTag("searchField").performImeAction()
        composeTestRule.waitForIdle()
    }

    /**
     * Clear the search input field.
     */
    fun clearSearch() {
        composeTestRule.onNodeWithTag("clearButton").performClick()
    }

    /**
     * Assert that the scoreText is displayed besides the proper student.
     *
     */
    fun assertStudentScoreText(studentName: String, scoreText: String) {

        composeTestRule.onNode(
            hasTestTag("scoreText") and hasText(scoreText) and (
                    hasParent(
                        hasTestTag("submissionListItem").and(
                            hasAnyDescendant(hasText(studentName) and hasTestTag("submissionListItemStudentName"))
                        )
                    )
                    ), useUnmergedTree = true
        ).assertIsDisplayed()
    }

    /**
     * Click on post policies
     *
     */
    fun clickOnPostPolicies() {
        composeTestRule.onNodeWithTag("postPolicyButton").performClick()
    }

    /**
     * Click filter button
     *
     */
    fun clickFilterButton() {
        composeTestRule.onNodeWithTag("filterButton").performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Click submission
     *
     * @param student
     */
    @OptIn(ExperimentalTestApi::class)
    fun clickSubmission(student: CanvasUserApiModel) {
        composeTestRule.waitUntilExactlyOneExists(hasText(student.name), timeoutMillis = 5000)
        composeTestRule.onNodeWithText(student.name, useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Click submission
     *
     * @param student
     */
    fun clickSubmission(student: User) {
        composeTestRule.onNode(
            hasTestTag("submissionListItem").and(hasAnyChild(hasText(student.name))),
            useUnmergedTree = true
        )
            .performScrollTo()
            .performClick()
    }

    /**
     * Click filter submitted late
     *
     */
    fun clickFilterSubmittedLate() {
        composeTestRule.onNode(
            hasTestTag("statusCheckBox").and(hasAnySibling(hasText("Late"))),
            useUnmergedTree = true
        )
            .performScrollTo()
            .performClick()
    }

    /**
     * Click filter ungraded
     *
     */
    fun clickFilterUngraded() {
        composeTestRule.onNode(
            hasTestTag("statusCheckBox") and hasAnySibling(hasText("Needs Grading")),
            useUnmergedTree = true
        ).performScrollTo()
            .performClick()
    }

    /**
     * Select a differentiation tag filter option.
     * @param differentiationTagText The text of the differentiation tag to select. Defaults to "Students without differentiation tags".
     */
    fun clickDifferentiationTagFilter(differentiationTagText: String = getStringFromResource(R.string.students_without_differentiation_tags)) {

        val differentiationTagTestTag = if (differentiationTagText == getStringFromResource(R.string.students_without_differentiation_tags))
            "includeWithoutTagsCheckBox"
        else "differentiationTagCheckBox"

        composeTestRule.onNode(
            hasTestTag(differentiationTagTestTag) and hasAnySibling(
                hasText(
                    differentiationTagText
                )
            ),
            useUnmergedTree = true
        ).performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Assert that the corresponding submission filter options are displayed.
     * @param filterName
     */
    fun assertSubmissionFilterOption(filterName: String) {
        composeTestRule.onNode(
            hasTestTag("statusCheckBox") and hasAnySibling(hasText(filterName)),
            useUnmergedTree = true
        ).performScrollTo().assertIsDisplayed()
    }

    /**
     * Assert that the corresponding custom status filter options are displayed.
     * @param filterName Custom status filter name.
     */
    fun assertCustomStatusFilterOption(filterName: String) {
        composeTestRule.onNode(
            hasTestTag("customStatusCheckBox") and hasAnySibling(hasText(filterName)),
            useUnmergedTree = true
        ).performScrollTo().assertIsDisplayed()
    }

    /**
     * Assert that the corresponding differentiation tag filter options are displayed.
     * @param differentiationTagText The text of the differentiation tag to verify. Defaults to "Students without differentiation tags".
     */
    fun assertDifferentiationTagFilterOption(differentiationTagText: String = getStringFromResource(R.string.students_without_differentiation_tags)) {
        val differentiationTagTestTag = if (differentiationTagText == getStringFromResource(R.string.students_without_differentiation_tags))
            "includeWithoutTagsCheckBox"
        else "differentiationTagCheckBox"
        composeTestRule.onNode(
            hasTestTag(differentiationTagTestTag) and hasAnySibling(hasText(differentiationTagText)),
            useUnmergedTree = true
        ).performScrollTo().assertIsDisplayed()
    }

    /**
     * Assert that the corresponding precise filter options are displayed.
     * @param filterName Precise filter name.
     */
    fun assertPreciseFilterOption(filterName: String) {
        composeTestRule.onNode(hasText(filterName), useUnmergedTree = true).performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Assert has submission
     *
     * @param expectedCount
     */
    fun assertHasSubmission(expectedCount: Int = 1) {
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodes(hasTestTag("submissionListItem"), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodes(hasTestTag("submissionListItem"), useUnmergedTree = true)
            .assertCountEquals(expectedCount)
    }

    /**
     * Assert has no submission
     *
     */
    fun assertHasNoSubmission() {
        composeTestRule.onAllNodes(hasTestTag("submissionListItem"), useUnmergedTree = true)
            .assertCountEquals(0)
    }

    /**
     * Assert submission status missing
     *
     */
    fun assertSubmissionStatusMissing() {
        composeTestRule.onNodeWithText("Missing", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Assert submission status submitted
     *
     */
    fun assertSubmissionStatusSubmitted() {
        composeTestRule.onNodeWithText("Submitted", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Assert submission status not submitted
     *
     */
    fun assertSubmissionStatusNotSubmitted() {
        composeTestRule.onNodeWithText("Not Submitted", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Assert submission status late
     *
     */
    fun assertSubmissionStatusLate() {
        composeTestRule.onNodeWithText("Late", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Click add message
     *
     */
    fun clickAddMessage() {
        composeTestRule.onNodeWithTag("addMessageButton")
            .performClick()
    }

    /**
    Clicks on the "Done" button in the filter dialog.
     */
    fun clickFilterDialogDone() {
        composeTestRule.onNode(hasTestTag("appBarDoneButton"), useUnmergedTree = true)
            .performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Filters the submissions by the specified section name.
     *
     * @param name The name of the section to filter by.
     */
    fun filterBySection(name: String) {
        composeTestRule.onNode(
            hasTestTag("sectionCheckBox") and hasAnySibling(hasText(name)),
            useUnmergedTree = true
        )
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Assert that the grades are hidden for the specified student.
     *
     * @param studentName The name of the student whose grades should be hidden.
     */
    fun assertGradesHidden(studentName: String) {
        composeTestRule.onNode(
            hasTestTag("hiddenIcon").and(
                hasParent(
                    hasTestTag("submissionListItem").and(
                        hasAnyDescendant(hasText(studentName) and hasTestTag("submissionListItemStudentName"))
                    )
                )
            ), useUnmergedTree = true
        )
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Click on the 'Submitted' filter option.
     *
     */
    fun clickFilterSubmitted() {
        composeTestRule.onNode(
            hasTestTag("statusCheckBox").and(hasAnySibling(hasText("Submitted"))),
            useUnmergedTree = true
        )
            .performScrollTo()
            .performClick()
    }

    /**
     * Click on the 'Not Submitted' filter option.
     *
     */
    fun clickFilterNotSubmitted() {
        // Note: In the actual filter screen, "Not Submitted" status doesn't exist
        // The statuses are: Late, Missing, Needs Grading, Graded, Submitted
        // This method may need to be updated based on the actual filter available
        composeTestRule.onNode(
            hasTestTag("statusCheckBox").and(hasAnySibling(hasText("Missing"))),
            useUnmergedTree = true
        )
            .performScrollTo()
            .performClick()
    }

    /**
     * Click on a student's avatar based on their name.
     *
     * @param name The name of the student whose avatar should be clicked.
     */
    fun clickOnStudentAvatar(name: String) {
        composeTestRule.onNode(
            hasTestTag("userAvatar").and(
                hasParent(
                    hasTestTag("submissionListItem").and(
                        hasAnyChild(
                            hasText(name)
                        )
                    )
                )
            ), useUnmergedTree = true
        )
            .performScrollTo()
            .performClick()
    }

    /**
     * Refresh the page.
     */
    fun refresh() {
        composeTestRule.onNodeWithTag("submissionList").performTouchInput {
            swipeDown()
        }
    }

    /**
     * Click on a sort order option.
     *
     * @param sortOrderName The name of the sort order option to click (e.g., "Submission Date", "Student Name").
     */
    fun clickSortOrder(sortOrderName: String) {
        composeTestRule.onNodeWithText(sortOrderName, useUnmergedTree = true)
            .performScrollTo()
            .performClick()
    }

    /**
     * Assert that submissions are displayed in the expected order.
     *
     * @param studentNames List of student names in the expected display order.
     */
    fun assertSubmissionsInOrder(studentNames: List<String>) {
        val submissionNodes = composeTestRule.onAllNodes(
            hasTestTag("submissionListItem"),
            useUnmergedTree = true
        )

        studentNames.forEachIndexed { index, name ->
            submissionNodes[index]
                .assert(hasAnyDescendant(hasText(name)))
        }
    }

    /**
     * Click on a custom grade status filter option.
     *
     * @param statusName The name of the custom status to filter by.
     */
    fun clickFilterCustomStatus(statusName: String) {
        composeTestRule.onNode(
            hasTestTag("customStatusCheckBox").and(hasAnySibling(hasText(statusName))),
            useUnmergedTree = true
        )
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Assert that a custom status tag is displayed on a submission.
     *
     * @param statusName The name of the custom status tag to verify.
     */
    fun assertCustomStatusTag(statusName: String) {
        composeTestRule.onNodeWithText(statusName, useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Assert that a differentiation tag is displayed on a submission.
     *
     * @param tagName The name of the differentiation tag to verify.
     */
    fun assertDifferentiationTag(tagName: String) {
        composeTestRule.onNodeWithText(tagName, useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Click on a differentiation tag filter option.
     *
     * @param tagName The name of the differentiation tag to filter by.
     */
    fun clickFilterDifferentiationTag(tagName: String) {
        // Click on the text element with the tag name that has a checkbox sibling
        // Use [0] to get the first match since there may be multiple text nodes in the tree
        composeTestRule.onAllNodes(
            hasText(tagName).and(
                hasAnySibling(hasTestTag("differentiationTagCheckBox"))
            ),
            useUnmergedTree = true
        )[0]
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Click on the "Include students without differentiation tags" checkbox.
     */
    fun clickIncludeStudentsWithoutTags() {
        composeTestRule.onNode(
            hasTestTag("includeWithoutTagsCheckBox"),
            useUnmergedTree = true
        )
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
    }

}
