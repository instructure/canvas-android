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
package com.instructure.pandautils.compose.features.inbox.compose

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.input.TextFieldValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import com.instructure.pandautils.compose.composables.MultipleValuesRowState
import com.instructure.pandautils.compose.composables.SelectContextUiState
import com.instructure.pandautils.features.inbox.compose.InboxComposeScreenOptions
import com.instructure.pandautils.features.inbox.compose.InboxComposeUiState
import com.instructure.pandautils.features.inbox.compose.RecipientPickerUiState
import com.instructure.pandautils.features.inbox.compose.composables.InboxComposeScreen
import com.instructure.pandautils.features.inbox.utils.AttachmentCardItem
import com.instructure.pandautils.features.inbox.utils.AttachmentStatus
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsDisabledFields
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsHiddenFields
import com.instructure.pandautils.utils.ScreenState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InboxComposeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val title = "New Message"

    @Test
    fun testComposeScreenAppbarWithInactiveSendButton() {
        setComposeScreen()

        val backButton =
            composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasContentDescription("Close")))
        backButton
            .assertIsDisplayed()
            .assertHasClickAction()

        val sendbutton =
            composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasContentDescription("Send message")))
        sendbutton
            .assertIsDisplayed()
            .assert(isNotEnabled())
            .assertHasClickAction()
    }

    @Test
    fun testComposeScreenAppbarWithActiveSendButton() {
        setComposeScreen(getUiState(
            selectedContext = Course(),
            selectedRecipients = listOf(Recipient(stringId = "r1", name = "r1")),
            isInlineSearchEnabled = false,
            sendIndividual = true,
            subject = "Subject",
            body = "Body",
        ))

        val backButton =
            composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasContentDescription("Close")))
        backButton
            .assertIsDisplayed()
            .assertHasClickAction()

        val sendbutton =
            composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasContentDescription("Send message")))
        sendbutton
            .assertIsDisplayed()
            .assert(isEnabled())
            .assertHasClickAction()
    }

    @Test
    fun testComposeScreenEmptyState() {
        setComposeScreen()

        composeTestRule.onNode(hasText("Course"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("To"))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasText("Send individual message to each recipient"))
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag("switch"))
            .assertIsOff()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("Subject"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("Message"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasContentDescription("Add attachment"))
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testInboxComposeSelectedContext() {
        setComposeScreen(getUiState(selectedContext = Course(name = "Course")))

        composeTestRule.onNode(hasText("Course"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("To"))
            .assertIsDisplayed()

        composeTestRule.onNode(hasContentDescription("Search among Recipients"), true)
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasContentDescription("Add"))
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testComposeScreenFilledState() {
        setComposeScreen(getUiState(
            selectedContext = Course(name = "Course 1"),
            selectedRecipients = listOf(Recipient(stringId = "r2", name = "r2")),
            isInlineSearchEnabled = true,
            sendIndividual = true,
            subject = "testSubject",
            body = "testBody",
        ))

        composeTestRule.onNode(hasText("Course"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("Course 1"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("To"))
            .assertIsDisplayed()

        composeTestRule.onNode(hasContentDescription("Search among Recipients"), true)
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasContentDescription("Add"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("r2"))
            .assertIsDisplayed()

        composeTestRule.onNode(hasContentDescription("Remove Recipient"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("Send individual message to each recipient"))
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag("switch"))
            .assertIsOn()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("Subject"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("testSubject"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("testBody"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasContentDescription("Add attachment"))
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testComposeScreenAttachment() {
        setComposeScreen(getUiState(attachments = listOf(Attachment(filename = "Attachment.jpg", size = 1500))))

        composeTestRule.onNode(hasText("Attachment.jpg"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("1.50 kB"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasContentDescription("Remove attachment"))
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testComposeScreenConfirmationDialog() {
        setComposeScreen(getUiState(showConfirmationDialog = true))

        composeTestRule.onNode(hasText("Exit without saving?"))
            .assertIsDisplayed()

        composeTestRule.onNode(hasText("Are you sure you would like to exit without saving?"))
            .assertIsDisplayed()

        composeTestRule.onNode(hasText("Exit"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("Cancel"))
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testDisabledFields() {
        setComposeScreen(
            InboxComposeUiState(
                disabledFields = InboxComposeOptionsDisabledFields(
                    isContextDisabled = true,
                    isRecipientsDisabled = true,
                    isSendIndividualDisabled = true,
                    isSubjectDisabled = true,
                    isBodyDisabled = true,
                    isAttachmentDisabled = true
                ),
                subject = TextFieldValue("testSubject"),
                body = TextFieldValue("testBody"),
                selectContextUiState = SelectContextUiState(selectedCanvasContext = Course(name = "Course 1")),
                inlineRecipientSelectorState = MultipleValuesRowState(enabled = false, isSearchEnabled = true, selectedValues = listOf(Recipient(stringId = "r2", name = "r2"))),
                recipientPickerUiState = RecipientPickerUiState(selectedRecipients = listOf(Recipient(stringId = "r2", name = "r2"))),
            )
        )

        composeTestRule.onNode(hasText("Course"))
            .assertIsDisplayed()
            .assert(isNotEnabled())

        composeTestRule.onNode(hasText("Course 1"))
            .assertIsDisplayed()
            .assert(isNotEnabled())

        composeTestRule.onNode(hasText("To"))
            .assertIsDisplayed()

        composeTestRule.onNode(hasText("Search"))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasContentDescription("Add"))
            .assertIsDisplayed()
            .assert(isNotEnabled())

        composeTestRule.onNode(hasText("r2"))
            .assertIsDisplayed()

        composeTestRule.onNode(hasContentDescription("Remove Recipient"))
            .assertIsDisplayed()
            .assert(isNotEnabled())

        composeTestRule.onNode(hasText("Send individual message to each recipient"))
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag("switch"))
            .assertIsDisplayed()
            .assert(isNotEnabled())

        composeTestRule.onNode(hasText("Subject"))
            .assertIsDisplayed()
            .assert(isNotEnabled())

        composeTestRule.onNode(hasText("testSubject"))
            .assertIsDisplayed()
            .assert(isNotEnabled())

        composeTestRule.onNode(hasText("testBody"))
            .assertIsDisplayed()
            .assert(isNotEnabled())

        composeTestRule.onNode(hasContentDescription("Add attachment"))
            .assertIsDisplayed()
            .assert(isNotEnabled())
    }

    @Test
    fun testHiddenFields() {
        setComposeScreen(
            InboxComposeUiState(
                hiddenFields = InboxComposeOptionsHiddenFields(
                    isContextHidden = true,
                    isRecipientsHidden = true,
                    isSendIndividualHidden = true,
                    isSubjectHidden = true,
                    isBodyHidden = true,
                    isAttachmentHidden = true,
                ),
                subject = TextFieldValue("testSubject"),
                body = TextFieldValue("testBody"),
                selectContextUiState = SelectContextUiState(selectedCanvasContext = Course(name = "Course 1")),
                inlineRecipientSelectorState = MultipleValuesRowState(isSearchEnabled = true, selectedValues = listOf(Recipient(stringId = "r2", name = "r2"))),
                recipientPickerUiState = RecipientPickerUiState(selectedRecipients = listOf(Recipient(stringId = "r2", name = "r2"))),
            )
        )

        composeTestRule.onNode(hasText("Course"))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasText("Course 1"))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasText("To"))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasText("Search"))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasContentDescription("Add"))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasText("r2"))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasContentDescription("Remove Recipient"))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasText("Send individual message to each recipient"))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasTestTag("switch"))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasText("Subject"))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasText("testSubject"))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasText("testBody"))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasContentDescription("Add attachment"))
            .assertIsNotDisplayed()
    }

    private fun setComposeScreen(uiState: InboxComposeUiState = getUiState()) {
        composeTestRule.setContent {
            InboxComposeScreen(
                title = title,
                uiState = uiState,
                actionHandler = {}
            )
        }
    }

    @Test
    fun testComposeScreenWithSignatureLoading() {
        setComposeScreen(getUiState(signatureLoading = true))

        composeTestRule.onNode(hasTestTag("SignatureLoading"))
            .assertIsDisplayed()
    }

    private fun getUiState(
        selectedContext: CanvasContext? = null,
        selectedRecipients: List<Recipient> = emptyList(),
        isInlineSearchEnabled: Boolean = true,
        inlineSearchContentDescription: String = "Search among Recipients",
        sendIndividual: Boolean = false,
        subject: String = "",
        body: String = "",
        attachments: List<Attachment> = emptyList(),
        showConfirmationDialog: Boolean = false,
        signatureLoading: Boolean = false
    ): InboxComposeUiState {
        return InboxComposeUiState(
            selectContextUiState = SelectContextUiState(selectedCanvasContext = selectedContext),
            recipientPickerUiState = RecipientPickerUiState(selectedRecipients = selectedRecipients),
            inlineRecipientSelectorState = MultipleValuesRowState(isSearchEnabled = isInlineSearchEnabled, selectedValues = selectedRecipients, searchFieldContentDescription = inlineSearchContentDescription),
            screenOption = InboxComposeScreenOptions.None,
            sendIndividual = sendIndividual,
            subject = TextFieldValue(subject),
            body = TextFieldValue(body),
            attachments = attachments.map { AttachmentCardItem(it, AttachmentStatus.UPLOADED, false) },
            screenState = ScreenState.Content,
            showConfirmationDialog = showConfirmationDialog,
            signatureLoading = signatureLoading
        )
    }
}