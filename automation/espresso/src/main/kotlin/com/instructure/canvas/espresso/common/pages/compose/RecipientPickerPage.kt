package com.instructure.canvas.espresso.common.pages.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

class RecipientPickerPage(private val composeTestRule: ComposeTestRule) {
    fun pressLabel(label: String) {
        composeTestRule.onNodeWithText(label, useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
    }

    fun pressDone() {
        composeTestRule.onNodeWithText("Done").performClick()
        composeTestRule.waitForIdle()
    }

    fun pressBack() {
        composeTestRule.onNodeWithTag("navigationButton").performClick()
        composeTestRule.waitForIdle()
    }

    fun assertRecipientDisplayed(recipientName: String) {
        composeTestRule.onNodeWithText(recipientName, useUnmergedTree = true).assertIsDisplayed()
    }

    fun assertRecipientNotDisplayed(recipientName: String) {
        composeTestRule.onNodeWithText(recipientName, useUnmergedTree = true).assertIsNotDisplayed()
    }
}