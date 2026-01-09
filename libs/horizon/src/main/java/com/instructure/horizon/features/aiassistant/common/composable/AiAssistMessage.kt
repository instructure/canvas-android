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

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.semantics
import com.instructure.canvasapi2.models.journey.JourneyAssistRole
import com.instructure.horizon.features.aiassistant.common.model.AiAssistMessage
import com.instructure.horizon.features.aiassistant.common.model.toDeepLink

@Composable
fun AiAssistMessage(
    message: AiAssistMessage,
    onSendPrompt: (String) -> Unit,
    onSourceSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null
) {
    val focusModifier = focusRequester?.let {
        Modifier
            .semantics(mergeDescendants = true) {}
            .focusRequester(it)
            .focusable()
    } ?: Modifier

    if (!message.errorMessage.isNullOrBlank()) {
        AiAssistResponseTextBlock(
            text = message.errorMessage,
            modifier = modifier.then(focusModifier)
        )
    } else if (message.role == JourneyAssistRole.Assistant) {
        AiAssistResponseTextBlock(
            text = message.text,
            footerState = AiAssistResponseTextBlockFooterState(
                isFooterEnabled = true,
                sources = message.citations.map {
                    AiAssistResponseTextBlockSource(
                        label = it.title,
                        url = it.toDeepLink()
                    )
                },
                onSourceSelected = { onSourceSelected(it.url) }
            ),
            chips = message.chipOptions.map {
                AiAssistResponseTextBlockChipState(
                    label = it.chip,
                    onClick = {
                        onSendPrompt(it.prompt)
                    }
                )
            },
            modifier = modifier.then(focusModifier)
        )
    } else if (message.role == JourneyAssistRole.User) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(focusModifier),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ){
            AiAssistUserTextBlock(
                text = message.text,
                modifier = modifier
            )
        }
    }
}