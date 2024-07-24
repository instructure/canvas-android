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
package com.instructure.pandautils.features.inbox.compose.recipientpicker

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.BasicUser
import com.instructure.canvasapi2.models.Recipient
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.Avatar
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.features.inbox.compose.RecipientPickerActionHandler
import com.instructure.pandautils.features.inbox.compose.RecipientPickerScreenOption
import com.instructure.pandautils.features.inbox.compose.RecipientPickerUiState

@Composable
fun RecipientPickerScreen(
    title: String,
    uiState: RecipientPickerUiState,
    actionHandler: (RecipientPickerActionHandler) -> Unit,
) {
    when (uiState.screenOption) {
        is RecipientPickerScreenOption.Roles -> RecipientPickerRoleScreen(title, uiState, actionHandler)
        is RecipientPickerScreenOption.Recipients -> RecipientPickerPeopleScreen(title, uiState, actionHandler)
    }
}

@Composable
private fun RecipientPickerRoleScreen(
    title: String,
    uiState: RecipientPickerUiState,
    actionHandler: (RecipientPickerActionHandler) -> Unit,
) {
    Scaffold (
        topBar = {
            CanvasThemedAppBar(title = title, navigationActionClick = { actionHandler(RecipientPickerActionHandler.DoneClicked) })
         },
        content = { padding ->
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .padding(padding)
            ) {
                items(uiState.roles) { roleRecipient ->
                    RoleRow(name = roleRecipient, onSelect = {
                        uiState.screenOption = RecipientPickerScreenOption.Recipients
                    })

                    Divider(color = colorResource(id = R.color.borderLight), thickness = 1.dp)
                }
            }

        }
    )
}

@Composable
private fun RecipientPickerPeopleScreen(
    title: String,
    uiState: RecipientPickerUiState,
    actionHandler: (RecipientPickerActionHandler) -> Unit,
) {
    Scaffold (
        topBar = {
            CanvasThemedAppBar(title = title, navigationActionClick = { actionHandler(RecipientPickerActionHandler.DoneClicked) })
        },
        content = { padding ->
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .padding(padding)
            ) {
                items(uiState.recipients) { recipient ->
                    RecipientRow(recipient = recipient, onSelect = {
                        uiState.screenOption = RecipientPickerScreenOption.Recipients
                    })

                    Divider(color = colorResource(id = R.color.borderLight), thickness = 1.dp)
                }
            }

        }
    )
}

@Composable
private fun RoleRow(
    name: String,
    onSelect: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Avatar(user = BasicUser(name = name))

        Spacer(Modifier.width(8.dp))

        Text(
            text = name,
            fontSize = 20.sp,
            color = colorResource(id = R.color.textDarkest),
            modifier = Modifier.padding(8.dp)
        )

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun RecipientRow(
    recipient: Recipient,
    onSelect: () -> Unit,
) {
    val basicUser = BasicUser(
        id = recipient.idAsLong,
        name = recipient.name,
        avatarUrl = recipient.avatarURL,
        pronouns = recipient.pronouns
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Avatar(user = basicUser)

        Spacer(Modifier.width(8.dp))

        Text(
            text = recipient.name ?: "",
            fontSize = 20.sp,
            color = colorResource(id = R.color.textDarkest),
            modifier = Modifier.padding(8.dp)
        )
        
        Spacer(Modifier.weight(1f))
    }
}