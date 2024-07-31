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

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.Recipient
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.animations.ScreenSlideBackTransition
import com.instructure.pandautils.compose.animations.ScreenSlideTransition
import com.instructure.pandautils.compose.composables.Avatar
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.CanvasThemedTextField
import com.instructure.pandautils.features.inbox.compose.RecipientPickerActionHandler
import com.instructure.pandautils.features.inbox.compose.RecipientPickerScreenOption
import com.instructure.pandautils.features.inbox.compose.RecipientPickerUiState
import com.instructure.pandautils.utils.displayText

@Composable
fun RecipientPickerScreen(
    title: String,
    uiState: RecipientPickerUiState,
    actionHandler: (RecipientPickerActionHandler) -> Unit,
) {
    val animationLabel = "RecipientPickerScreenSlideTransition"
    AnimatedContent(
        label = animationLabel,
        targetState = uiState.screenOption,
        transitionSpec = {
            when(uiState.screenOption) {
                is  RecipientPickerScreenOption.Recipients -> {
                    ScreenSlideTransition
                }
                is RecipientPickerScreenOption.Roles -> {
                    ScreenSlideBackTransition
                }
            }
        }
    ){ screenOption ->
        when (screenOption) {
            is RecipientPickerScreenOption.Roles -> RecipientPickerRoleScreen(
                title,
                uiState,
                actionHandler
            )

            is RecipientPickerScreenOption.Recipients -> RecipientPickerPeopleScreen(
                title,
                uiState,
                actionHandler
            )
        }
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
            CanvasAppBar(
                title = title,
                navigationActionClick = { actionHandler(RecipientPickerActionHandler.DoneClicked) },
                actions = {
                    IconButton(
                        onClick = { actionHandler(RecipientPickerActionHandler.DoneClicked) },
                    ) {
                        Text(
                            text = stringResource(id = R.string.done),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.textDarkest),
                        )
                    }
                }
            )
         },
        content = { padding ->
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .padding(padding)
            ) {
                item {
                    SearchField(uiState.searchValue, actionHandler)
                }

                val showSearchResults = uiState.searchValue.text.isNotEmpty()
                if (showSearchResults) {
                    items(uiState.recipientsToShow) { recipient ->
                        RecipientRow(recipient = recipient, isSelected = uiState.selectedRecipients.contains(recipient), onSelect = {
                            actionHandler(RecipientPickerActionHandler.RecipientClicked(recipient))
                        })
                    }
                } else {
                    items(uiState.recipientsByRole.keys.toList()) { role ->
                        RoleRow(name = role.displayText, roleCount = uiState.recipientsByRole[role]?.size ?: 0, onSelect = {
                            actionHandler(RecipientPickerActionHandler.RoleClicked(role))
                        })
                    }
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
            CanvasAppBar(
                title = title,
                navigationActionClick = { actionHandler(RecipientPickerActionHandler.RecipientBackClicked) },
                navIconRes = R.drawable.ic_back_arrow,
                navIconContentDescription = stringResource(R.string.a11y_close_recipient_picker),
                actions = {
                    IconButton(
                        onClick = { actionHandler(RecipientPickerActionHandler.DoneClicked) },
                    ) {
                        Text(
                            text = stringResource(id = R.string.done),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorResource(id = R.color.textDarkest),
                        )
                    }
                }
            )
        },
        content = { padding ->
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .padding(padding)
            ) {
                item {
                    SearchField(uiState.searchValue, actionHandler)
                }

                items(uiState.recipientsToShow) { recipient ->
                    RecipientRow(recipient = recipient, isSelected = uiState.selectedRecipients.contains(recipient), onSelect = {
                        actionHandler(RecipientPickerActionHandler.RecipientClicked(recipient))
                    })
                }
            }

        }
    )
}

@Composable
private fun RoleRow(
    name: String,
    roleCount: Int,
    onSelect: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable {
                onSelect()
            }
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        Avatar(
            Recipient(name = name),
            Modifier
                .size(36.dp)
                .padding(2.dp)
        )

        Spacer(Modifier.width(8.dp))

        Column {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.textDarkest),
            )

            Text(
                text = roleCount.toString() + " " + if (roleCount == 1) "Person" else "People",
                fontSize = 14.sp,
                color = colorResource(id = R.color.textDark),
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun RecipientRow(
    recipient: Recipient,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onSelect() }
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        Avatar(
            recipient,
            Modifier
                .size(36.dp)
                .padding(2.dp)
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = recipient.name ?: "",
            fontSize = 16.sp,
            color = colorResource(id = R.color.textDarkest),
        )
        
        Spacer(Modifier.weight(1f))

        if (isSelected) {
            Icon(
                painter = painterResource(id = R.drawable.ic_checkmark),
                contentDescription = stringResource(R.string.a11y_selected),
                tint = colorResource(id = R.color.textDarkest),
            )
        }
    }
}

@Composable
private fun SearchField(
    value: TextFieldValue,
    actionHandler: (RecipientPickerActionHandler) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search_white_24dp),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .padding(2.dp)
            )

            Spacer(Modifier.width(8.dp))

            CanvasThemedTextField(
                value = value,
                onValueChange = { actionHandler(RecipientPickerActionHandler.SearchValueChanged(it)) },
                singleLine = true,
                placeholder = "Search",
                modifier = Modifier.fillMaxWidth()
            )
        }

        CanvasDivider()
    }
}

@Preview
@Composable
fun SearchFieldPreview() {
    SearchField(
        value = TextFieldValue(""),
        actionHandler = {}
    )
}