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