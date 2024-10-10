package com.instructure.canvas.espresso.common.pages.compose

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick

class SelectContextPage(private val composeTestRule: ComposeTestRule) {
    fun selectContext(name: String) {
        composeTestRule.onNodeWithTag("title_$name", true).performClick()
    }
}