/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

import androidx.compose.ui.semantics.SemanticsProperties.Text
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.instructure.canvasapi2.models.Conversation

class InboxDetailsPage(private val composeTestRule: ComposeTestRule) {

    fun assertTitle(title: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(title)
            .isDisplayed()
    }

    fun assertConversationSubject(subject: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(subject)
            .isDisplayed()
    }

    fun assertMessageDisplayed(messageBody: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(messageBody).performScrollTo()
    }

    fun assertMessageNotDisplayed(messageBody: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(messageBody).assertIsNotDisplayed()
    }

    fun assertAllMessagesDisplayed(conversation: Conversation) {
        conversation.messages.forEach { message ->
            assertMessageDisplayed(message.body ?: "")
        }
    }

    fun assertStarred(isStarred: Boolean) {
        composeTestRule.waitForIdle()
        if (isStarred) {
            composeTestRule.onNodeWithContentDescription("Unstar")
        } else {
            composeTestRule.onNodeWithContentDescription("Star")
        }
    }

    fun assertDeleteMessageAlertDialog() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Delete Message").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to delete your copy of this message? This action cannot be undone.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").assertIsDisplayed()
    }

    fun assertDeleteConversationAlertDialog() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Delete Conversation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to delete your copy of this conversation? This action cannot be undone.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").assertIsDisplayed()
    }

    fun pressBackButton() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Back").performClick()
    }

    fun pressReplyTextButtonForMessage(messageBody: String) {
        composeTestRule.waitForIdle()

        val replyButton = composeTestRule.onNodeWithText(messageBody)
            .onParent() // SelectionContainer
            .onParent() // Column
            .onChildren()
            .filterToOne(hasText("Reply"))

        composeTestRule.waitForIdle()
        replyButton.performScrollTo()
        replyButton.performClick()
    }

    fun pressReplyIconButtonForMessage(messageBody: String) {
        composeTestRule.waitForIdle()

        val replyButton = composeTestRule.onNodeWithText(messageBody ?: "")
            .onParent() // SelectionContainer
            .onParent() // Column
            .onChildren()
            .filterToOne(hasContentDescription("Reply"))

        replyButton.performScrollTo()
        composeTestRule.waitForIdle()
        replyButton.performScrollTo()
        replyButton.performClick()
    }

    fun pressStarButton(newIsStarred: Boolean) {
        if (newIsStarred) {
            composeTestRule.onNodeWithContentDescription("Star").performClick()
        } else {
            composeTestRule.onNodeWithContentDescription("Unstar").performClick()
        }
    }

    fun pressAlertButton(buttonLabel: String) {
        composeTestRule.onNodeWithText(buttonLabel).performClick()
    }

    fun pressOverflowMenuItemForMessage(messageBody: String, buttonLabel: String) {
        pressOverflowIconButtonForMessage(messageBody)

        composeTestRule.onNode(hasTestTag("messageMenuItem").and(hasText(buttonLabel)), true)
            .performClick()
    }

    fun pressOverflowMenuItemForConversation(buttonLabel: String) {
        pressOverflowIconButtonForConversation()

        composeTestRule.onNode(hasTestTag("messageMenuItem").and(hasText(buttonLabel)), true)
            .performClick()
    }

    private fun pressOverflowIconButtonForMessage(messageBody: String) {
        composeTestRule.waitForIdle()

        var messageId = 0
        composeTestRule
            .onAllNodes(hasTestTag("messageBodyText")).fetchSemanticsNodes().forEachIndexed() { index, node ->
                if (node.config.getOrNull(Text)?.first()?.text == messageBody) {
                    messageId = index
                }
            }
        val overflowButton = composeTestRule
            .onAllNodesWithContentDescription("More options")[messageId]

        overflowButton.performScrollTo()
        composeTestRule.waitForIdle()
        overflowButton.performClick()
    }

    private fun pressOverflowIconButtonForConversation() {
        composeTestRule.waitForIdle()

        val overflowButton = composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).and(
                hasContentDescription("More options")
            )
        )

        overflowButton.performClick()
    }
}