package com.instructure.canvas.espresso.common.pages.compose

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

class RecipientPickerPage(private val composeTestRule: ComposeTestRule) {
    fun pressLabel(label: String) {
        composeTestRule.onNodeWithText(label, useUnmergedTree = true).performClick()
    }

    fun pressDone() {
        composeTestRule.onNodeWithText("Done").performClick()
    }
}