/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.inbox.compose.composables

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.animations.ScreenSlideBackTransition
import com.instructure.pandautils.compose.animations.ScreenSlideTransition
import com.instructure.pandautils.compose.composables.SelectContextScreen
import com.instructure.pandautils.features.inbox.compose.ContextPickerActionHandler
import com.instructure.pandautils.features.inbox.compose.InboxComposeActionHandler
import com.instructure.pandautils.features.inbox.compose.InboxComposeScreenOptions
import com.instructure.pandautils.features.inbox.compose.InboxComposeUiState
import com.instructure.pandautils.features.inbox.compose.RecipientPickerActionHandler
import com.instructure.pandautils.features.inbox.compose.RecipientPickerScreenOption
import com.instructure.pandautils.utils.isGroup

@Composable
fun InboxComposeScreenWrapper(
    title: String,
    uiState: InboxComposeUiState,
    inboxComposeActionHandler: (InboxComposeActionHandler) -> Unit,
    contextPickerActionHandler: (ContextPickerActionHandler) -> Unit,
    recipientPickerActionHandler: (RecipientPickerActionHandler) -> Unit,
    ) {
    val animationLabel = "ScreenSlideTransition"

    BackHandler(uiState.enableCustomBackHandler) {
        when (uiState.screenOption) {
            is InboxComposeScreenOptions.None -> {
                inboxComposeActionHandler(InboxComposeActionHandler.CancelDismissDialog(true))
            }

            is InboxComposeScreenOptions.ContextPicker -> {
                contextPickerActionHandler(ContextPickerActionHandler.DoneClicked)
            }

            is InboxComposeScreenOptions.RecipientPicker -> {
                when (uiState.recipientPickerUiState.screenOption) {
                    RecipientPickerScreenOption.Recipients -> {
                        recipientPickerActionHandler(RecipientPickerActionHandler.RecipientBackClicked)
                    }
                    RecipientPickerScreenOption.Roles -> {
                        recipientPickerActionHandler(RecipientPickerActionHandler.DoneClicked)
                    }
                }
            }
        }
    }

    AnimatedContent(
        label = animationLabel,
        targetState = uiState.screenOption,
        transitionSpec = {
            when(uiState.screenOption) {
                is InboxComposeScreenOptions.None -> {
                    ScreenSlideBackTransition
                }
                is InboxComposeScreenOptions.ContextPicker -> {
                    ScreenSlideTransition
                }
                is InboxComposeScreenOptions.RecipientPicker -> {
                    ScreenSlideTransition
                }
            }
        }
    ) { screenOption ->
        when (screenOption) {
            InboxComposeScreenOptions.None -> {
                InboxComposeScreen(
                    title = title,
                    uiState = uiState
                ) { action ->
                    inboxComposeActionHandler(action)
                }
            }

            InboxComposeScreenOptions.ContextPicker -> {
                SelectContextScreen(
                    title = if (uiState.selectContextUiState.canvasContexts.none { it.isGroup })
                                stringResource(id = R.string.selectCourse)
                            else
                                stringResource(id = R.string.selectCourseOrGroup),
                    uiState = uiState.selectContextUiState,
                    onContextSelected = { contextPickerActionHandler(ContextPickerActionHandler.ContextClicked(it)) },
                    navigationActionClick = { contextPickerActionHandler(ContextPickerActionHandler.DoneClicked) },
                    navIconRes = R.drawable.ic_back_arrow
                )
            }

            InboxComposeScreenOptions.RecipientPicker -> {
                RecipientPickerScreen(
                    title = stringResource(id = R.string.selectRecipients),
                    uiState = uiState.recipientPickerUiState
                ) { action ->
                    recipientPickerActionHandler(action)
                }
            }
        }
    }
}