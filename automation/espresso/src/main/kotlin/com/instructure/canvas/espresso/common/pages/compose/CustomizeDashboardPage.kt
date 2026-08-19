/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.canvas.espresso.common.pages.compose

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isOff
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.composetest.assertTagOrder
import com.instructure.composetest.waitForTag
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.getStringFromResource
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ColorKeeper

class CustomizeDashboardPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertPageDisplayed() {
        composeTestRule.waitForTag(WIDGETS_LIST_TAG)
        composeTestRule.onNodeWithTag(WIDGETS_LIST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(DASHBOARD_REDESIGN_TOGGLE_TAG).assertIsDisplayed()
    }

    /**
     * Asserts the top-to-bottom order of the widget items in the list. Only the relative order of the
     * given widget ids is checked, see [assertTagOrder].
     */
    fun assertWidgetOrder(vararg widgetIds: String) {
        composeTestRule.assertTagOrder(WIDGET_ITEM_TAG_PREFIX, widgetIds.toList())
    }

    fun assertWidgetVisibilityOn(widgetId: String) {
        visibilitySwitch(widgetId).assertIsOn()
    }

    fun assertWidgetVisibilityOff(widgetId: String) {
        visibilitySwitch(widgetId).assertIsOff()
    }

    fun toggleWidgetVisibility(widgetId: String) {
        composeTestRule.waitForTag(visibilitySwitchTag(widgetId), useUnmergedTree = true)
        visibilitySwitch(widgetId).performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    fun moveWidgetUp(widgetId: String) {
        composeTestRule.waitForTag(moveUpButtonTag(widgetId), useUnmergedTree = true)
        moveUpButton(widgetId).performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    fun moveWidgetDown(widgetId: String) {
        composeTestRule.waitForTag(moveDownButtonTag(widgetId), useUnmergedTree = true)
        moveDownButton(widgetId).performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    fun assertMoveButtonsEnabled(widgetId: String) {
        moveUpButton(widgetId).assertIsEnabled()
        moveDownButton(widgetId).assertIsEnabled()
    }

    fun assertMoveButtonsDisabled(widgetId: String) {
        moveUpButton(widgetId).assertIsNotEnabled()
        moveDownButton(widgetId).assertIsNotEnabled()
    }

    fun assertMoveUpButtonEnabled(widgetId: String) {
        moveUpButton(widgetId).assertIsEnabled()
    }

    fun assertMoveUpButtonDisabled(widgetId: String) {
        moveUpButton(widgetId).assertIsNotEnabled()
    }

    fun assertMoveDownButtonDisabled(widgetId: String) {
        moveDownButton(widgetId).assertIsNotEnabled()
    }

    // The dashboard color picker is collapsed by default, so it has to be expanded before its color
    // options are in the semantics tree. Selecting a color collapses it again.
    fun expandDashboardColorPicker() {
        composeTestRule.waitForTag(DASHBOARD_COLOR_PICKER_TITLE_TAG)
        composeTestRule.onNodeWithTag(DASHBOARD_COLOR_PICKER_TITLE_TAG).performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Selects the given course color in the expanded dashboard color picker, and returns the color
     * that got selected: the contrast corrected variant of [courseColorRes] the picker works with,
     * see [ColorKeeper.createThemedColor].
     */
    fun selectDashboardColor(courseColorRes: Int): Int {
        val color = themedCourseColor(courseColorRes)
        composeTestRule.waitForTag(colorOptionTag(color))
        composeTestRule.onNodeWithTag(colorOptionTag(color)).performClick()
        composeTestRule.waitForIdle()
        return color
    }

    fun assertDashboardColorSelected(courseColorRes: Int) {
        composeTestRule.onNodeWithTag(colorOptionTag(themedCourseColor(courseColorRes)))
            .assertIsDisplayed()
            .assert(hasContentDescription(getStringFromResource(R.string.selected)))
    }

    fun assertNewDashboardToggleOn() {
        composeTestRule.waitForTag(DASHBOARD_REDESIGN_TOGGLE_TAG)
        composeTestRule.onNodeWithTag(DASHBOARD_REDESIGN_TOGGLE_TAG).assertIsOn()
    }

    // The switch follows the preference through a database write and a flow emission, which Compose
    // idleness does not cover, so wait for it to turn off before asserting.
    fun assertNewDashboardToggleOff(timeoutMillis: Long = 10000) {
        val matcher = hasTestTag(DASHBOARD_REDESIGN_TOGGLE_TAG).and(isOff())
        composeTestRule.waitUntil(timeoutMillis) {
            composeTestRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag(DASHBOARD_REDESIGN_TOGGLE_TAG).assertIsOff()
    }

    fun clickNewDashboardToggle() {
        composeTestRule.waitForTag(DASHBOARD_REDESIGN_TOGGLE_TAG)
        composeTestRule.onNodeWithTag(DASHBOARD_REDESIGN_TOGGLE_TAG).performClick()
        composeTestRule.waitForIdle()
    }

    fun assertChangingDashboardLayoutDialogDisplayed() {
        composeTestRule.waitForTag(CONFIRMATION_DIALOG_TAG)
        composeTestRule.onNodeWithTag(CONFIRMATION_DIALOG_TAG).assertIsDisplayed()
        assertDisplayedWithText(CONFIRMATION_DIALOG_TITLE_TAG, R.string.changing_dashboard_layout)
        assertDisplayedWithText(CONFIRMATION_DIALOG_MESSAGE_TAG, R.string.changing_dashboard_layout_message)
        assertDisplayedWithText(CONFIRMATION_DIALOG_CONFIRM_BUTTON_TAG, R.string.restart_now)
        assertDisplayedWithText(CONFIRMATION_DIALOG_DISMISS_BUTTON_TAG, R.string.cancel)
    }

    fun assertChangingDashboardLayoutDialogNotDisplayed() {
        composeTestRule.onNodeWithTag(CONFIRMATION_DIALOG_TAG).assertDoesNotExist()
    }

    fun clickChangingDashboardLayoutDialogCancelButton() {
        composeTestRule.onNodeWithTag(CONFIRMATION_DIALOG_DISMISS_BUTTON_TAG).performClick()
        composeTestRule.waitForIdle()
    }

    fun clickChangingDashboardLayoutDialogRestartButton() {
        composeTestRule.onNodeWithTag(CONFIRMATION_DIALOG_CONFIRM_BUTTON_TAG).performClick()
        composeTestRule.waitForIdle()
    }

    // There is deliberately no method for finishing the survey dialog: 'Submit', 'Skip' and dismissing
    // it all restart the app with ProcessPhoenix, which kills the app process and with it the
    // instrumentation, aborting the whole test run.
    fun assertSurveyDialogDisplayed() {
        composeTestRule.waitForTag(SURVEY_DIALOG_TAG)
        composeTestRule.onNodeWithTag(SURVEY_DIALOG_TAG).assertIsDisplayed()
        assertDisplayedWithText(SURVEY_DIALOG_TITLE_TAG, R.string.switched_back)
        assertDisplayedWithText(SURVEY_DIALOG_MESSAGE_TAG, R.string.switched_back_message)
        SurveyOption.entries.forEach { option ->
            assertDisplayedWithText(surveyOptionTag(option), option.textRes)
        }
        composeTestRule.onNodeWithTag(SURVEY_DIALOG_FEEDBACK_BUTTON_TAG).assertIsDisplayed()
        assertDisplayedWithText(SURVEY_DIALOG_SKIP_BUTTON_TAG, R.string.skip)
        assertDisplayedWithText(SURVEY_DIALOG_SUBMIT_BUTTON_TAG, R.string.submit)
    }

    fun assertSurveyDialogSubmitButtonDisabled() {
        composeTestRule.onNodeWithTag(SURVEY_DIALOG_SUBMIT_BUTTON_TAG).assertIsNotEnabled()
    }

    fun assertSurveyDialogSubmitButtonEnabled() {
        composeTestRule.onNodeWithTag(SURVEY_DIALOG_SUBMIT_BUTTON_TAG).assertIsEnabled()
    }

    fun selectSurveyOption(option: SurveyOption) {
        composeTestRule.onNodeWithTag(surveyOptionTag(option)).performClick()
        composeTestRule.waitForIdle()
    }

    fun clickCloseButton() {
        composeTestRule.waitForTag(NAVIGATION_BUTTON_TAG)
        composeTestRule.onAllNodesWithTag(NAVIGATION_BUTTON_TAG).onLast().performClick()
        composeTestRule.waitForIdle()
    }

    // The switch and the move buttons are descendants of a card that merges its descendants, so they
    // have to be queried on the unmerged tree.
    private fun visibilitySwitch(widgetId: String) =
        composeTestRule.onNodeWithTag(visibilitySwitchTag(widgetId), useUnmergedTree = true)

    private fun moveUpButton(widgetId: String) =
        composeTestRule.onNodeWithTag(moveUpButtonTag(widgetId), useUnmergedTree = true)

    private fun moveDownButton(widgetId: String) =
        composeTestRule.onNodeWithTag(moveDownButtonTag(widgetId), useUnmergedTree = true)

    private fun assertDisplayedWithText(tag: String, textRes: Int) {
        composeTestRule.onNodeWithTag(tag)
            .assertIsDisplayed()
            .assert(hasText(getStringFromResource(textRes)))
    }

    private fun themedCourseColor(courseColorRes: Int): Int {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return ColorKeeper.createThemedColor(context.getColor(courseColorRes)).light
    }

    private fun colorOptionTag(color: Int) = "$COLOR_OPTION_TAG_PREFIX$color"

    private fun surveyOptionTag(option: SurveyOption) = "$SURVEY_OPTION_TAG_PREFIX${option.id}"

    private fun visibilitySwitchTag(widgetId: String) = "$VISIBILITY_SWITCH_TAG_PREFIX$widgetId"

    private fun moveUpButtonTag(widgetId: String) = "$MOVE_UP_BUTTON_TAG_PREFIX$widgetId"

    private fun moveDownButtonTag(widgetId: String) = "$MOVE_DOWN_BUTTON_TAG_PREFIX$widgetId"

    /** The options of the survey dialog shown after the new dashboard is turned off. */
    enum class SurveyOption(val id: String, val textRes: Int) {
        HARD_TO_FIND(AnalyticsEventConstants.SURVEY_OPTION_HARD_TO_FIND, R.string.survey_option_hard_to_find),
        PREFER_OLD_LAYOUT(AnalyticsEventConstants.SURVEY_OPTION_PREFER_OLD_LAYOUT, R.string.survey_option_prefer_old_layout),
        SOMETHING_BROKEN(AnalyticsEventConstants.SURVEY_OPTION_SOMETHING_BROKEN, R.string.survey_option_something_broken)
    }

    companion object {
        private const val WIDGETS_LIST_TAG = "widgetsList"
        private const val DASHBOARD_REDESIGN_TOGGLE_TAG = "dashboardRedesignToggle"
        private const val WIDGET_ITEM_TAG_PREFIX = "widgetItem_"
        private const val VISIBILITY_SWITCH_TAG_PREFIX = "visibilitySwitch_"
        private const val MOVE_UP_BUTTON_TAG_PREFIX = "moveUpButton_"
        private const val MOVE_DOWN_BUTTON_TAG_PREFIX = "moveDownButton_"
        private const val NAVIGATION_BUTTON_TAG = "navigationButton"
        private const val CONFIRMATION_DIALOG_TAG = "confirmationDialog"
        private const val CONFIRMATION_DIALOG_TITLE_TAG = "confirmationDialogTitle"
        private const val CONFIRMATION_DIALOG_MESSAGE_TAG = "confirmationDialogMessage"
        private const val CONFIRMATION_DIALOG_CONFIRM_BUTTON_TAG = "confirmationDialogConfirmButton"
        private const val CONFIRMATION_DIALOG_DISMISS_BUTTON_TAG = "confirmationDialogDismissButton"
        private const val SURVEY_DIALOG_TAG = "surveyDialog"
        private const val SURVEY_DIALOG_TITLE_TAG = "surveyDialogTitle"
        private const val SURVEY_DIALOG_MESSAGE_TAG = "surveyDialogMessage"
        private const val SURVEY_DIALOG_FEEDBACK_BUTTON_TAG = "surveyDialogFeedbackButton"
        private const val SURVEY_DIALOG_SUBMIT_BUTTON_TAG = "surveyDialogSubmitButton"
        private const val SURVEY_DIALOG_SKIP_BUTTON_TAG = "surveyDialogSkipButton"
        private const val SURVEY_OPTION_TAG_PREFIX = "surveyOption_"
        private const val DASHBOARD_COLOR_PICKER_TITLE_TAG = "colorPickerTitle_backgroundColor"
        private const val COLOR_OPTION_TAG_PREFIX = "colorOption_"
    }
}