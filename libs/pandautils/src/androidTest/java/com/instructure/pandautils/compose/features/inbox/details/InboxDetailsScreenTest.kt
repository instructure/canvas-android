package com.instructure.pandautils.compose.features.inbox.details

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Recipient
import com.instructure.pandautils.features.inbox.details.InboxDetailsUiState
import com.instructure.pandautils.features.inbox.details.composables.InboxDetailsScreen
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InboxDetailsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val title = "New Message"

    private fun setDetailsScreen(uiState: InboxDetailsUiState = getUiState()) {
        composeTestRule.setContent {
            InboxDetailsScreen(
                title = title,
                uiState = uiState,
                messageActionHandler = {},
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
    ): InboxDetailsUiState {
        return InboxDetailsUiState(
        )
    }
}