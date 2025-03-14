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
package com.instructure.teacher.ui.pages


import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.page.BasePage

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
    fun assertDisplaysNoSubmissionsView() {
        composeTestRule.onNodeWithText("No submissions").assertIsDisplayed()
    }

    /**
     * Assert has student submission
     *
     * @param canvasUser
     */
    fun assertHasStudentSubmission(canvasUser: CanvasUserApiModel) {
        composeTestRule.onNodeWithText(canvasUser.name, useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Assert filter label all submissions
     *
     */
    fun assertFilterLabelAllSubmissions() {
        composeTestRule.onNodeWithText("All submissions").assertIsDisplayed()
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
    }

    /**
     * Click submission
     *
     * @param student
     */
    fun clickSubmission(student: CanvasUserApiModel) {
        composeTestRule.onNodeWithText(student.name, useUnmergedTree = true)
            .performScrollTo()
            .performClick()
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
        composeTestRule.onNodeWithText("Submitted Late", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
    }

    /**
     * Click filter ungraded
     *
     */
    fun clickFilterUngraded() {
        composeTestRule.onNode(
            hasTestTag("filterItem")
                .and(hasAnyChild(hasText("Needs Grading"))),
            useUnmergedTree = true
        )
            .performScrollTo()
            .performClick()
    }

    /**
     * Assert filter label text
     *
     * @param text
     */
    fun assertFilterLabelText(text: String) {
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }


    /**
     * Assert has submission
     *
     * @param expectedCount
     */
    fun assertHasSubmission(expectedCount: Int = 1) {
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

    Clicks on the "OK" button in the filter dialog.
     */
    fun clickFilterDialogOk() {
        composeTestRule.onNodeWithText("Done")
            .performClick()
    }

    fun filterBySection(name: String) {
        composeTestRule.onNodeWithText(name, useUnmergedTree = true)
            .performScrollTo()
            .performClick()
    }

    fun assertGradesHidden(studentName: String) {
        composeTestRule.onNode(
            hasTestTag("hiddenIcon").and(
                hasParent(
                    hasTestTag("submissionListItem").and(
                        hasAnyChild(hasText(studentName))
                    )
                )
            ), useUnmergedTree = true
        )
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun clickFilterNotSubmitted() {
        composeTestRule.onNodeWithText("Not Submitted", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
    }

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
}
