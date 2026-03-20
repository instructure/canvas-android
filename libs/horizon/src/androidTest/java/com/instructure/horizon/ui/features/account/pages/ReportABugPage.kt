/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.ui.features.account.pages

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput

class ReportABugPage(private val semanticsProvider: SemanticsNodeInteractionsProvider) {

    fun assertScreenDisplayed() {
        with(semanticsProvider) {
            onNodeWithText("Report a problem")
                .assertIsDisplayed()
        }
    }

    fun assertDescriptionMessageDisplayed() {
        with(semanticsProvider) {
            onNodeWithText("File a ticket for a personal response from our support team.")
                .assertIsDisplayed()
        }
    }

    fun assertCloseButtonDisplayed() {
        with(semanticsProvider) {
            onNodeWithContentDescription("Close")
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    fun assertTopicFieldDisplayed() {
        with(semanticsProvider) {
            onNodeWithText("Topic", useUnmergedTree = true)
                .assertIsDisplayed()
        }
    }

    fun assertSubjectFieldDisplayed() {
        with(semanticsProvider) {
            onNodeWithText("Subject")
                .assertIsDisplayed()
        }
    }

    fun assertDescriptionFieldDisplayed() {
        with(semanticsProvider) {
            onNodeWithText("Description")
                .assertIsDisplayed()
        }
    }

    fun assertCancelButtonDisplayed() {
        with(semanticsProvider) {
            onNodeWithText("Cancel")
                .performScrollTo()
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    fun assertSubmitButtonDisplayed() {
        with(semanticsProvider) {
            onNodeWithText("Submit ticket", useUnmergedTree = true)
                .performScrollTo()
                .assertIsDisplayed()
        }
    }

    fun assertSubjectRequiredIndicator() {
        with(semanticsProvider) {
            onNodeWithText("Subject", substring = true)
                .assertIsDisplayed()
        }
    }

    fun assertDescriptionRequiredIndicator() {
        with(semanticsProvider) {
            onNodeWithText("Description", substring = true)
                .assertIsDisplayed()
        }
    }

    fun selectTopic(topic: String) {
        with(semanticsProvider) {
            onNodeWithText("Topic")
                .performClick()
            onNodeWithText(topic)
                .performClick()
        }
    }

    fun enterSubject(subject: String) {
        with(semanticsProvider) {
            onNodeWithText("Subject")
                .performClick()
                .performTextInput(subject)
        }
    }

    fun enterDescription(description: String) {
        with(semanticsProvider) {
            onNodeWithText("Description")
                .performClick()
                .performTextInput(description)
        }
    }

    fun clickSubmit() {
        with(semanticsProvider) {
            onNodeWithText("Submit ticket")
                .performScrollTo()
                .performClick()
        }
    }

    fun clickCancel() {
        with(semanticsProvider) {
            onNodeWithText("Cancel")
                .performScrollTo()
                .performClick()
        }
    }

    fun clickClose() {
        with(semanticsProvider) {
            onNodeWithContentDescription("Close")
                .performClick()
        }
    }

    fun assertTopicError(message: String) {
        with(semanticsProvider) {
            onNodeWithText(message, useUnmergedTree = true)
                .assertIsDisplayed()
        }
    }

    fun assertSubjectError(message: String) {
        with(semanticsProvider) {
            onNodeWithText(message, useUnmergedTree = true)
                .assertIsDisplayed()
        }
    }

    fun assertDescriptionError(message: String) {
        with(semanticsProvider) {
            onNodeWithText(message, useUnmergedTree = true)
                .assertIsDisplayed()
        }
    }

    fun assertNotLoading() {
        with(semanticsProvider) {
            onNodeWithText("Submit ticket", useUnmergedTree = true)
                .performScrollTo()
                .assertIsEnabled()
        }
    }

    fun assertSelectedTopicDisplayed(topic: String) {
        with(semanticsProvider) {
            onNodeWithText(topic, useUnmergedTree = true)
                .assertIsDisplayed()
        }
    }
}
