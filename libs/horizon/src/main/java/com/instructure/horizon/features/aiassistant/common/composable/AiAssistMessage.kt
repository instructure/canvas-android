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
package com.instructure.horizon.features.aiassistant.common.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.instructure.canvasapi2.models.journey.JourneyAssistChatMessage
import com.instructure.canvasapi2.models.journey.JourneyAssistRole

@Composable
fun AiAssistMessage(
    message: JourneyAssistChatMessage,
    onSendPrompt: (String) -> Unit
) {
    if (message.role == JourneyAssistRole.ASSISTANT) {
        AiAssistResponseTextBlock(
            text = message.displayText,
            footerState = AiAssistResponseTextBlockFooterState(
                isFooterEnabled = true,
                sources = message.citations.map {
                    AiAssistResponseTextBlockSource(
                        label = it.title,
                        url = ""
                    )
                }
            ),
            chips = message.chipOptions.map {
                AiAssistResponseTextBlockChipState(
                    label = it.chip,
                    onClick = {
                        onSendPrompt(it.prompt)
                    }
                )
            }

        )
    } else if (message.role == JourneyAssistRole.USER) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ){
            AiAssistUserTextBlock(
                text = message.displayText,
            )
        }
    }
}