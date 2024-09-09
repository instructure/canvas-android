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
import com.instructure.pandautils.features.inbox.compose.AttachmentCardItem
import com.instructure.pandautils.features.inbox.compose.AttachmentStatus
import com.instructure.pandautils.features.inbox.compose.InboxComposeScreenOptions
import com.instructure.pandautils.features.inbox.compose.InboxComposeUiState
import com.instructure.pandautils.features.inbox.compose.RecipientPickerUiState
import com.instructure.pandautils.features.inbox.compose.ScreenState
import com.instructure.pandautils.features.inbox.compose.composables.InboxComposeScreen
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

        composeTestRule.onNode(hasText("Search"))
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

        composeTestRule.onNode(hasText("Search"))
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

        composeTestRule.onNode(hasContentDescription("Remove Attachment"))
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

    private fun setComposeScreen(uiState: InboxComposeUiState = getUiState()) {
        composeTestRule.setContent {
            InboxComposeScreen(
                title = title,
                uiState = uiState,
                actionHandler = {}
            )
        }
    }

    private fun getUiState(
        selectedContext: CanvasContext? = null,
        selectedRecipients: List<Recipient> = emptyList(),
        isInlineSearchEnabled: Boolean = true,
        sendIndividual: Boolean = false,
        subject: String = "",
        body: String = "",
        attachments: List<Attachment> = emptyList(),
        showConfirmationDialog: Boolean = false
    ): InboxComposeUiState {
        return InboxComposeUiState(
            selectContextUiState = SelectContextUiState(selectedCanvasContext = selectedContext),
            recipientPickerUiState = RecipientPickerUiState(selectedRecipients = selectedRecipients),
            inlineRecipientSelectorState = MultipleValuesRowState(isSearchEnabled = isInlineSearchEnabled, selectedValues = selectedRecipients),
            screenOption = InboxComposeScreenOptions.None,
            sendIndividual = sendIndividual,
            subject = TextFieldValue(subject),
            body = TextFieldValue(body),
            attachments = attachments.map { AttachmentCardItem(it, AttachmentStatus.UPLOADED) },
            screenState = ScreenState.Data,
            showConfirmationDialog = showConfirmationDialog
        )
    }
}