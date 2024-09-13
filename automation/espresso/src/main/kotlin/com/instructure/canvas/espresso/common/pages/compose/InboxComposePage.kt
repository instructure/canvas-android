package com.instructure.canvas.espresso.common.pages.compose

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement

class InboxComposePage(private val composeTestRule: ComposeTestRule) {
    fun assertTitle(title: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(title)
            .isDisplayed()
    }

    fun assertIfSendButtonState(isEnabled: Boolean) {
        composeTestRule.waitForIdle()
        if (isEnabled) {
            composeTestRule.onNodeWithContentDescription("Send message")
                .assert(isEnabled())
        } else {
            composeTestRule.onNodeWithContentDescription("Send message")
                .assert(isNotEnabled())
        }
    }

    fun assertContextSelected(contextName: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(contextName).assertIsDisplayed()
    }

    fun assertRecipientSelected(recipientName: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(recipientName).assertIsDisplayed()
    }

    fun assertRecipientNotSelected(recipientName: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(recipientName).assertIsNotDisplayed()
    }

    fun assertRecipientSearchDisplayed() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Search").assertIsDisplayed()
    }

    fun assertIndividualSwitchState(isEnabled: Boolean) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("switch").assertIsDisplayed()

        if (isEnabled) {
            composeTestRule.onNodeWithTag("switch").assertIsOn()
        } else {
            composeTestRule.onNodeWithTag("switch").assertIsOff()
        }
    }

    fun assertSubjectText(subject: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("labelTextFieldRowTextField").assertTextEquals(subject)
    }

    fun assertBodyText(body: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("textFieldWithHeaderTextField").assertTextEquals(body)
    }

    fun assertAlertDialog() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Exit without saving?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you would like to exit without saving?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Exit").assertIsDisplayed()
    }

    fun typeSubject(subject: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("labelTextFieldRowTextField").performClick()
        composeTestRule.onNodeWithTag("labelTextFieldRowTextField").performTextReplacement(subject)
    }

    fun typeBody(body: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("textFieldWithHeaderTextField").performClick()
        composeTestRule.onNodeWithTag("textFieldWithHeaderTextField").performTextReplacement(body)
    }

    fun pressBackButton() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Close").performClick()
    }

    fun pressSendButton() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Send message").performClick()
    }

    fun pressCourseSelector() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Course").performClick()
    }

    fun pressAddRecipient() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Add").performClick()
    }

    fun pressRemoveRecipient(index: Int) {
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodes(hasContentDescription("Remove Recipient"))[index].performClick()
    }

    fun pressIndividualSendSwitch() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("switch").performClick()
    }
}