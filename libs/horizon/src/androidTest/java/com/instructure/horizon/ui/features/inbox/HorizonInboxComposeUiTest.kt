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
package com.instructure.horizon.ui.features.inbox

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import com.instructure.horizon.features.inbox.attachment.HorizonInboxAttachmentPickerUiState
import com.instructure.horizon.features.inbox.compose.HorizonInboxComposeScreen
import com.instructure.horizon.features.inbox.compose.HorizonInboxComposeUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HorizonInboxComposeUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val pickerState = HorizonInboxAttachmentPickerUiState()

    @Test
    fun testComposeScreenDisplaysCourseSelector() {
        val uiState = HorizonInboxComposeUiState(
            coursePickerOptions = listOf(
                Course(id = 1L, name = "Math 101"),
                Course(id = 2L, name = "Science 202")
            )
        )

        composeTestRule.setContent {
            HorizonInboxComposeScreen(uiState, pickerState, rememberNavController())
        }

        composeTestRule.onNodeWithContentDescription("Select Course")
            .assertIsDisplayed()
    }

    @Test
    fun testComposeScreenDisplaysRecipientField() {
        val uiState = HorizonInboxComposeUiState()

        composeTestRule.setContent {
            HorizonInboxComposeScreen(uiState, pickerState, rememberNavController())
        }

        composeTestRule.onNodeWithText("Recipient(s)")
            .assertIsDisplayed()
    }

    @Test
    fun testComposeScreenDisplaysSubjectField() {
        val uiState = HorizonInboxComposeUiState()

        composeTestRule.setContent {
            HorizonInboxComposeScreen(uiState, pickerState, rememberNavController())
        }

        composeTestRule.onNodeWithText("Title/Subject")
            .assertIsDisplayed()
    }

    @Test
    fun testComposeScreenDisplaysMessageField() {
        val uiState = HorizonInboxComposeUiState()

        composeTestRule.setContent {
            HorizonInboxComposeScreen(uiState, pickerState, rememberNavController())
        }

        composeTestRule.onNodeWithText("Message")
            .assertIsDisplayed()
    }

    @Test
    fun testSendButtonDisabledWhenFieldsEmpty() {
        val uiState = HorizonInboxComposeUiState()

        composeTestRule.setContent {
            HorizonInboxComposeScreen(uiState, pickerState, rememberNavController())
        }

        composeTestRule.onNodeWithText("Send")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testSelectedCourseDisplays() {
        val uiState = HorizonInboxComposeUiState(
            selectedCourse = Course(id = 1L, name = "Math 101")
        )

        composeTestRule.setContent {
            HorizonInboxComposeScreen(uiState, pickerState, rememberNavController())
        }

        composeTestRule.onNodeWithText("Math 101")
            .assertIsDisplayed()
    }

    @Test
    fun testSelectedRecipientsDisplay() {
        val uiState = HorizonInboxComposeUiState(
            selectedRecipients = listOf(
                Recipient(stringId = "1", name = "John Doe"),
                Recipient(stringId = "2", name = "Jane Smith")
            )
        )

        composeTestRule.setContent {
            HorizonInboxComposeScreen(uiState, pickerState, rememberNavController())
        }

        composeTestRule.onNodeWithText("John Doe")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Jane Smith")
            .assertIsDisplayed()
    }

    @Test
    fun testSubjectTextDisplays() {
        val uiState = HorizonInboxComposeUiState(
            subject = TextFieldValue("Test Subject")
        )

        composeTestRule.setContent {
            HorizonInboxComposeScreen(uiState, pickerState, rememberNavController())
        }

        composeTestRule.onNodeWithText("Test Subject")
            .assertIsDisplayed()
    }

    @Test
    fun testBodyTextDisplays() {
        val uiState = HorizonInboxComposeUiState(
            body = TextFieldValue("Test message body")
        )

        composeTestRule.setContent {
            HorizonInboxComposeScreen(uiState, pickerState, rememberNavController())
        }

        composeTestRule.onNodeWithText("Test message body")
            .assertIsDisplayed()
    }

    @Test
    fun testSendIndividuallyCheckboxDisplays() {
        val uiState = HorizonInboxComposeUiState()

        composeTestRule.setContent {
            HorizonInboxComposeScreen(uiState, pickerState, rememberNavController())
        }

        composeTestRule.onNodeWithText("Send an individual message to each recipient")
            .assertIsDisplayed()
    }

    @Test
    fun testErrorMessagesDisplay() {
        val uiState = HorizonInboxComposeUiState(
            courseErrorMessage = "Please select a course",
            recipientErrorMessage = "Please select at least one recipient",
            subjectErrorMessage = "Subject is required",
            bodyErrorMessage = "Message is required"
        )

        composeTestRule.setContent {
            HorizonInboxComposeScreen(uiState, pickerState, rememberNavController())
        }

        composeTestRule.onNodeWithText("Please select a course")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Please select at least one recipient")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Subject is required")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Message is required")
            .assertIsDisplayed()
    }

    @Test
    fun testAttachmentButtonDisplays() {
        val uiState = HorizonInboxComposeUiState()

        composeTestRule.setContent {
            HorizonInboxComposeScreen(uiState, pickerState, rememberNavController())
        }

        composeTestRule.onNodeWithText("Attach file")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testLoadingStateDisplaysProgressIndicator() {
        val uiState = HorizonInboxComposeUiState(
            isSendLoading = true
        )

        composeTestRule.setContent {
            HorizonInboxComposeScreen(uiState, pickerState, rememberNavController())
        }

        composeTestRule.onNodeWithTag("LoadingSpinner")
            .assertIsDisplayed()
    }
}
