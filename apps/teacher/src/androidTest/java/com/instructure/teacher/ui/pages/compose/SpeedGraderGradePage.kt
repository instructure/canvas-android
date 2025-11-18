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

import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso
import com.instructure.composetest.hasTestTagThatContains
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.OnViewWithText
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.WaitForViewWithText
import com.instructure.espresso.page.BasePage
import com.instructure.teacher.R

/**
 * Represents the SpeedGrader grade page.
 *
 * This page provides functionality for interacting with the elements on the SpeedGrader grade page. It contains methods
 * for opening the grade dialog, entering a new grade, asserting the grade dialog, asserting the presence of a grade,
 * asserting the visibility of the rubric, slider, and checkbox, asserting the values of the slider's max and min values,
 * asserting the presence of an overgrade warning, clicking the excuse student button, asserting the student's excused
 * status, and asserting the enabled or disabled state of the excuse and no grade buttons. This page extends the BasePage
 * class.
 */
class SpeedGraderGradePage(private val composeTestRule: ComposeTestRule) : BasePage() { // TODO: YET this is a 'hybrid' page because it's highly used in tests, we'll eliminate the non-compose parts step by step.

    private val gradeContainer by OnViewWithId(R.id.gradeContainer)
    private val gradeTextContainer by OnViewWithId(R.id.gradeTextContainer)
    private val gradingField by OnViewWithText(R.string.grade)

    private val addGradeIcon by WaitForViewWithId(R.id.addGradeIcon)
    private val gradeValueText by WaitForViewWithId(R.id.gradeValueText)

    private val slider by OnViewWithId(R.id.speedGraderSlider)

    //dialog views
    private val gradeEditText by WaitForViewWithId(R.id.gradeEditText)
    private val customizeGradeTitle by WaitForViewWithText(R.string.customize_grade)

    /**
     * Assert that the 'Grade' label is displayed on the SpeedGrader page's 'Grade & Rubric' tab.
     */
    @OptIn(ExperimentalTestApi::class)
    fun assertSpeedGraderLabelDisplayed() {
        composeTestRule.waitUntilExactlyOneExists(hasTestTag("speedGraderCurrentGradeGradeLabel"), timeoutMillis = 5000)
    }

    /**
     * Asserts that the slider is hidden (aka. not displayed).
     */
    @OptIn(ExperimentalTestApi::class)
    fun assertSliderHidden() {
        composeTestRule.waitUntilDoesNotExist(hasTestTag("speedGraderSlider"), timeoutMillis = 5000)
    }

    /**
     * Asserts that the slider is visible.
     */
    fun assertSliderVisible() {
        composeTestRule.onNodeWithTag("speedGraderSlider", useUnmergedTree = true).assertIsDisplayed()
    }

    /**
     * Asserts that 'Rubrics' label is displayed.
     */
    fun assertRubricsLabelDisplayed() {
        composeTestRule.onNodeWithTag("speedGraderRubricsLabel", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
    }

    /**
     * Asserts that there isn't any rubric criterion displayed.
     */
    fun assertNoRubricCriterionDisplayed() {
        composeTestRule.onAllNodes(hasTestTagThatContains("rubricCriterionDescription"), useUnmergedTree = true).assertCountEquals(0)
    }

    /**
     * Asserts that the current entered score is displayed in the Compose UI.
     *
     * @param currentScore The expected current score to be displayed.
     */
    fun assertCurrentEnteredScore(currentScore: String) {
        composeTestRule.onNode(hasTestTag("speedGraderCurrentGradeTextField") and hasText(currentScore), useUnmergedTree = true).assertIsDisplayed()
    }

    /**
     * Asserts that the current entered score is displayed in the Compose UI.
     *
     * @param currentScore The expected current score to be displayed.
     */
    fun assertCurrentEnteredPassFailScore(currentScore: String) {
        composeTestRule.onNode(hasTestTag("speedGraderCurrentPassFailPointsGradeText") and hasText(currentScore), useUnmergedTree = true).assertIsDisplayed()
    }

    /**
     * Asserts that the current entered percent is displayed in the Compose UI.
     *
     * @param currentPercent The expected current percent to be displayed.
     */
    fun assertCurrentEnteredPercentage(currentPercent: String) {
        composeTestRule.onNode(hasTestTag("speedGraderCurrentGradeTextField") and hasText(currentPercent), useUnmergedTree = true).assertIsDisplayed()
    }

    /**
     * Asserts that the possible score is displayed in the Compose UI.
     *
     * @param possibleScore The expected possible score to be displayed.
     */
    fun assertPointsPossible(possibleScore: String) {
        composeTestRule.onNode(hasTestTag("speedGraderPointsPossibleText") and hasText(possibleScore, substring = true), useUnmergedTree = true).assertIsDisplayed()
    }

    /**
     * Asserts that the possible points for percentage grading type is displayed in the Compose UI.
     *
     * @param possiblePoints The expected possible points to be displayed.
     */
    fun assertPossiblePointsForPercentageGradingType(possiblePoints: String) {
        composeTestRule.onNode(hasTestTag("speedGraderPercentagePossibleGradeLabel") and hasText(possiblePoints, substring = true), useUnmergedTree = true).assertIsDisplayed()
    }

    /**
     * Clicks the expand panel button in the Compose UI.
     */
    fun clickExpandPanelButton(composeTestRule: ComposeTestRule) {
        composeTestRule
            .onNodeWithTag("expandPanelButton", useUnmergedTree = true)
            .performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Enters a new grade in the Compose grade input field.
     *
     * @param grade The grade value to input.
     */
    fun enterNewGrade(grade: String) {
        composeTestRule
            .onNodeWithTag("speedGraderCurrentGradeTextField")
            .performTextReplacement(grade)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("speedGraderCurrentGradeGradeLabel").performClick() // To loose focus from the text field to apply changes.
        composeTestRule.waitForIdle()
        Espresso.closeSoftKeyboard()
    }

    /**
     * Asserts that the final grade is displayed in the Compose UI.
     *
     * @param gradeValue The expected grade value to be displayed.
     */
    fun assertFinalGradeIsDisplayed(gradeValue: String) {
        composeTestRule.onNodeWithTag("finalGradeLabel").performScrollTo().assertIsDisplayed()
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("finalGradeValue")
                .fetchSemanticsNodes().any { it.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text)?.any { text -> text.text.contains(gradeValue) } == true }
        }
    }

    /**
     * Asserts that the final grade points value is displayed in the Compose UI.
     *
     * @param finalGradePointsValue The expected final grade points value to be displayed.
     */
    @OptIn(ExperimentalTestApi::class)
    fun assertFinalGradePointsValueDisplayed(finalGradePointsValue: String) {
        composeTestRule.onNodeWithTag("finalGradePointsLabel").performScrollTo().assertIsDisplayed()
        // Wait up to 5 seconds for the expected substring to appear because sometimes it late a bit.
        composeTestRule.waitUntilExactlyOneExists(hasTestTag("finalGradePointsValue") and hasText(finalGradePointsValue), timeoutMillis = 5000)
        composeTestRule
            .onNodeWithTag("finalGradePointsValue")
            .assertTextContains(finalGradePointsValue, substring = true)
            .assertIsDisplayed()
    }

    /**
     * Asserts that the late penalty label is displayed in the Compose UI.
     *
     * @param latePenaltyPointsValue The expected late penalty points value to be displayed.
     */
    fun assertLatePenaltyValueDisplayed(latePenaltyPointsValue: String) {
        composeTestRule.onNodeWithTag("speedGraderLatePenaltyLabel").performScrollTo().assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("speedGraderLatePenaltyValue")
            .assertTextContains(latePenaltyPointsValue, substring = true)
            .assertIsDisplayed()
    }

    /**
     * Asserts that the excuse button is enabled.
     */
    fun assertExcuseButtonEnabled() {
        composeTestRule.onNodeWithTag("speedGraderExcuseButton", useUnmergedTree = true).assertIsDisplayed().assertIsEnabled()
    }

    /**
     * Asserts that the excuse button is disabled.
     */
    fun assertExcuseButtonDisabled() {
        composeTestRule.onNodeWithTag("speedGraderExcuseButton", useUnmergedTree = true).assertIsNotEnabled()
    }

    /**
     * Asserts that the no grade button is enabled.
     */
    fun assertNoGradeButtonEnabled() {
        composeTestRule.onNodeWithTag("speedGraderNoGradeButton", useUnmergedTree = true).assertIsDisplayed().assertIsEnabled()
    }

    /**
     * Asserts that the 'No Grade' button does not exist in the Compose UI.
     */
    fun assertNoGradeButtonDoesNotExist() {
        composeTestRule.onAllNodes(hasTestTagThatContains("speedGraderNoGradeButton"), useUnmergedTree = true).assertCountEquals(0)
    }

    /**
     * Asserts that the no grade button is disabled.
     */
    fun assertNoGradeButtonDisabled() {
        composeTestRule.onNodeWithTag("speedGraderNoGradeButton", useUnmergedTree = true).assertIsNotEnabled()
    }

    /**
     * Clicks the no grade button.
     */
    fun clickNoGradeButton() {
        composeTestRule.onNodeWithTag("speedGraderNoGradeButton", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Clicks the excuse student button.
     */
    fun clickExcuseStudentButton() {
        composeTestRule.onNodeWithTag("speedGraderExcuseButton", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Asserts that the status dropdown is displayed with the specified status text selected.
     *
     * @param statusText The expected status text to be displayed in the dropdown as selected value.
     */
    fun assertSelectedStatusText(statusText: String) {
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("speedGraderStatusDropdown")
                .fetchSemanticsNodes().any { it.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text)?.any { text -> text.text.contains(statusText) } == true }
        }
    }

    /**
     * Asserts that the current status is displayed for the specified student.
     *
     * @param expectedStatus The expected status to be displayed.
     * @param studentName The name of the student associated with the status.
     */
    fun assertCurrentStatus(expectedStatus: String, studentName: String) {
        composeTestRule.onNode(hasText(expectedStatus) and hasTestTag("submissionStatusLabel") and hasAnyAncestor(hasAnyDescendant(hasText(studentName))), useUnmergedTree = true).assertIsDisplayed()
    }

    /**
     * Selects a status from the status dropdown in the Compose UI.
     *
     * @param statusText The status text to be selected from the dropdown.
     */
    fun selectStatus(statusText: String) {
        composeTestRule.onNodeWithTag("speedGraderStatusDropdown", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasText(statusText), useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Asserts that the 'Days Late' label is displayed and the specified days late value is shown.
     *
     * @param daysLate The expected days late value to be displayed.
     */
    fun assertDaysLate(daysLate: String) {
        composeTestRule.onNodeWithTag("speedGraderDaysLateLabel", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("speedGraderDaysLateValue", useUnmergedTree = true).assertTextContains(daysLate).assertIsDisplayed()
    }

    /**
     * Opens the grade dialog.
     */
    fun openGradeDialog(gradingType: String) {
        when (gradingType) {
            "points" -> composeTestRule.onNode(hasTestTag("speedGraderPointsGradeTextField")).performClick()
            "percent" -> composeTestRule.onNodeWithTag("speedGraderPointsGradeTextField", useUnmergedTree = true).performClick()
            "letter" -> composeTestRule.onNodeWithTag("speedGraderPointsGradeTextField", useUnmergedTree = true).performClick()
            "pass_fail" -> composeTestRule.onNodeWithTag("speedGraderPointsGradeTextField", useUnmergedTree = true).performClick()
            "gpa_scale" -> composeTestRule.onNodeWithTag("speedGraderPointsGradeTextField", useUnmergedTree = true).performClick()
            else -> throw IllegalArgumentException("Unsupported grading type: $gradingType")
        }

        composeTestRule.waitForIdle()
    }

    /**
     * Assert that the 'Complete' and 'Incomplete' radio buttons are displayed in the Compose UI.
     */
    fun assertCompleteIncompleteButtonsDisplayed() {
        composeTestRule.onNodeWithTag("speedGraderCompleteRadioButton", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("speedGraderIncompleteRadioButton", useUnmergedTree = true).assertIsDisplayed()
    }

    /**
     * Clicks the 'Complete' radio button in the Compose UI.
     */
    fun selectCompleteButton() {
        composeTestRule.onNodeWithTag("speedGraderCompleteRadioButton", useUnmergedTree = true).performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Asserts that the 'Complete' radio button is selected in the Compose UI.
     */
    fun assertCompleteButtonSelected() {
        composeTestRule.onNodeWithTag("speedGraderCompleteRadioButton", useUnmergedTree = true).assertIsDisplayed().assertIsSelected()
    }

    /**
     * Asserts that the 'Complete' radio button is NOT selected in the Compose UI.
     */
    fun assertCompleteButtonNotSelected() {
        composeTestRule.onNodeWithTag("speedGraderCompleteRadioButton", useUnmergedTree = true).assertIsDisplayed().assertIsNotSelected()
    }

    /**
     * Asserts that the 'Incomplete' radio button is selected in the Compose UI.
     */
    fun assertIncompleteButtonSelected() {
        composeTestRule.onNodeWithTag("speedGraderIncompleteRadioButton", useUnmergedTree = true).assertIsDisplayed().assertIsSelected()
    }

    /**
     * Asserts that the 'Incomplete' radio button is NOT selected in the Compose UI.
     */
    fun assertIncompleteButtonNotSelected() {
        composeTestRule.onNodeWithTag("speedGraderIncompleteRadioButton", useUnmergedTree = true).assertIsDisplayed().assertIsNotSelected()
    }

    /**
     * Clicks the 'Incomplete' radio button in the Compose UI.
     */
    fun selectIncompleteButton() {
        composeTestRule.onNodeWithTag("speedGraderIncompleteRadioButton", useUnmergedTree = true).performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

}
