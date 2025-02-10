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

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.displayText
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.CanvasThemedTextField
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.UserAvatar
import com.instructure.pandautils.features.inbox.compose.RecipientPickerActionHandler
import com.instructure.pandautils.features.inbox.compose.RecipientPickerScreenOption
import com.instructure.pandautils.features.inbox.compose.RecipientPickerUiState
import com.instructure.pandautils.features.inbox.compose.ScreenState
import java.util.EnumMap

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecipientPickerScreen(
    title: String,
    uiState: RecipientPickerUiState,
    actionHandler: (RecipientPickerActionHandler) -> Unit,
) {
    val animationLabel = "RecipientPickerScreenSlideTransition"
    AnimatedContent(
        label = animationLabel,
        targetState = uiState.screenOption
    ){ screenOption ->
        val pullToRefreshState = rememberPullRefreshState(refreshing = false, onRefresh = {
            actionHandler(RecipientPickerActionHandler.RefreshCalled)
        })

        CanvasTheme {
            Scaffold(
                backgroundColor = colorResource(id = com.instructure.pandares.R.color.backgroundLightest),
                topBar = { TopBar(title, uiState, actionHandler) },
                content = { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pullRefresh(pullToRefreshState)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                        ) {
                            if (uiState.screenState != ScreenState.Loading && uiState.screenState != ScreenState.Error) {
                                val searchContext = if (uiState.selectedRole == null) stringResource(
                                    R.string.inboxAllRecipients
                                ) else uiState.selectedRole.displayText
                                SearchField(
                                    value = uiState.searchValue,
                                    placeholder = stringResource(
                                        R.string.inboxSearchIn,
                                        searchContext
                                    ),
                                    actionHandler = actionHandler
                                )
                            }

                            when (uiState.screenState) {
                                is ScreenState.Data -> {
                                    when (screenOption) {
                                        is RecipientPickerScreenOption.Roles -> RecipientPickerRoleScreen(
                                            uiState,
                                            actionHandler
                                        )

                                        is RecipientPickerScreenOption.Recipients -> RecipientPickerPeopleScreen(
                                            uiState,
                                            actionHandler
                                        )
                                    }
                                }

                                else -> {
                                    StateScreen(uiState)
                                }
                            }
                        }

                        PullRefreshIndicator(
                            refreshing = uiState.screenState == ScreenState.Loading,
                            state = pullToRefreshState,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .testTag("pullRefreshIndicator"),
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun RecipientPickerRoleScreen(
    uiState: RecipientPickerUiState,
    actionHandler: (RecipientPickerActionHandler) -> Unit,
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
    ) {
        val showSearchResults = uiState.searchValue.text.isNotEmpty()
        if (showSearchResults) {
            items(uiState.recipientsToShow) { recipient ->
                RecipientRow(
                    recipient = recipient,
                    isSelected = uiState.selectedRecipients.contains(recipient),
                    onSelect = {
                        actionHandler(
                            RecipientPickerActionHandler.RecipientClicked(
                                recipient
                            )
                        )
                    })
            }
        } else {
            uiState.allRecipientsToShow?.let {
                item {
                    RoleRow(
                        name = it.name ?: "",
                        roleCount = it.userCount,
                        isSelected = uiState.selectedRecipients.contains(it),
                    ) {
                        actionHandler(
                            RecipientPickerActionHandler.RecipientClicked(
                                it
                            )
                        )
                    }
                }
            }

            items(uiState.recipientsByRole.keys.toList()) { role ->
                RoleRow(
                    name = role.displayText,
                    roleCount = uiState.recipientsByRole[role]?.size ?: 0,
                    isSelected = false,
                    onSelect = {
                        actionHandler(RecipientPickerActionHandler.RoleClicked(role))
                    })
            }
        }

    }
}

@Composable
private fun RecipientPickerPeopleScreen(
    uiState: RecipientPickerUiState,
    actionHandler: (RecipientPickerActionHandler) -> Unit,
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
    ) {
        uiState.allRecipientsToShow?.let {
            item {
                RoleRow(
                    name = it.name ?: "",
                    roleCount = it.userCount,
                    isSelected = uiState.selectedRecipients.contains(it),
                ) {
                    actionHandler(
                        RecipientPickerActionHandler.RecipientClicked(
                            it
                        )
                    )
                }
            }
        }
        items(uiState.recipientsToShow) { recipient ->
            RecipientRow(
                recipient = recipient,
                isSelected = uiState.selectedRecipients.contains(recipient),
                onSelect = {
                    actionHandler(
                        RecipientPickerActionHandler.RecipientClicked(
                            recipient
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun StateScreen(
    uiState: RecipientPickerUiState,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        when (uiState.screenState) {
            is ScreenState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Loading()
                }
            }

            is ScreenState.Error -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    ErrorContent(errorMessage = stringResource(id = R.string.failedToLoadRecipients))
                }
            }

            is ScreenState.Empty -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    EmptyContent(
                        emptyMessage = stringResource(id = R.string.noRecipients),
                        imageRes = R.drawable.ic_panda_nothing_to_see
                    )
                }
            }

            else -> {}
        }
    }

}

@Composable
private fun RoleRow(
    name: String,
    roleCount: Int,
    isSelected: Boolean,
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
        UserAvatar(
            null,
            name,
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
                text = pluralStringResource(id = R.plurals.people, roleCount, roleCount),
                fontSize = 14.sp,
                color = colorResource(id = R.color.textDark),
            )
        }

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
        UserAvatar(
            recipient.avatarURL,
            recipient.name ?: "",
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
    placeholder: String?,
    actionHandler: (RecipientPickerActionHandler) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Icon(
                tint = colorResource(id = R.color.textDark),
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
                placeholder = placeholder,
                modifier = Modifier.fillMaxWidth()
            )
        }

        CanvasDivider()
    }
}

@Composable
private fun TopBar(
    title: String,
    uiState: RecipientPickerUiState,
    actionHandler: (RecipientPickerActionHandler) -> Unit
) {
    val navigationAction = when (uiState.screenOption) {
        is RecipientPickerScreenOption.Roles -> RecipientPickerActionHandler.DoneClicked
        is RecipientPickerScreenOption.Recipients -> RecipientPickerActionHandler.RecipientBackClicked
    }

    val navIconContextDescription = when (uiState.screenOption) {
        is RecipientPickerScreenOption.Roles -> stringResource(R.string.a11y_closeRecipientPicker)
        is RecipientPickerScreenOption.Recipients -> stringResource(R.string.a11y_backToRoles)
    }

    CanvasAppBar(
        title = title,
        navigationActionClick = { actionHandler(navigationAction) },
        navIconRes = R.drawable.ic_back_arrow,
        navIconContentDescription = navIconContextDescription,
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
}

@Preview
@Composable
fun RecipientPickerRolesScreenPreview() {
    ContextKeeper.appContext = LocalContext.current

    val roleRecipients: EnumMap<EnrollmentType, List<Recipient>> = EnumMap(EnrollmentType::class.java)
    roleRecipients[EnrollmentType.STUDENTENROLLMENT] = listOf(
        Recipient(name = "John Doe 1"),
        Recipient(name = "John Smith 1"),
    )

    roleRecipients[EnrollmentType.TEACHERENROLLMENT] = listOf(
        Recipient(name = "John Doe 2"),
        Recipient(name = "John Smith 2"),
    )


    RecipientPickerScreen(
        title = "Select Recipients",
        uiState = RecipientPickerUiState(
            screenOption = RecipientPickerScreenOption.Roles,
            screenState = ScreenState.Data,
            searchValue = TextFieldValue(""),
            selectedRecipients = emptyList(),
            recipientsByRole = roleRecipients,
            recipientsToShow = emptyList()
        ),
        actionHandler = {}
    )
}

@Preview
@Composable
fun RecipientPickerRecipientsScreenPreview() {
    ContextKeeper.appContext = LocalContext.current

    val roleRecipients: EnumMap<EnrollmentType, List<Recipient>> = EnumMap(EnrollmentType::class.java)
    roleRecipients[EnrollmentType.STUDENTENROLLMENT] = listOf(
        Recipient(name = "John Doe 1"),
        Recipient(name = "John Smith 1"),
    )

    roleRecipients[EnrollmentType.TEACHERENROLLMENT] = listOf(
        Recipient(name = "John Doe 2"),
        Recipient(name = "John Smith 2"),
    )


    RecipientPickerScreen(
        title = "Select Recipients",
        uiState = RecipientPickerUiState(
            screenOption = RecipientPickerScreenOption.Recipients,
            screenState = ScreenState.Data,
            searchValue = TextFieldValue(""),
            selectedRole = EnrollmentType.TEACHERENROLLMENT,
            selectedRecipients = listOf(roleRecipients[EnrollmentType.TEACHERENROLLMENT]!!.first()),
            recipientsByRole = roleRecipients,
            recipientsToShow = roleRecipients[EnrollmentType.TEACHERENROLLMENT]!!,
        ),
        actionHandler = {}
    )
}

@Preview
@Composable
fun RecipientPickerSearchScreenPreview() {
    ContextKeeper.appContext = LocalContext.current

    val roleRecipients: EnumMap<EnrollmentType, List<Recipient>> = EnumMap(EnrollmentType::class.java)
    roleRecipients[EnrollmentType.STUDENTENROLLMENT] = listOf(
        Recipient(name = "John Doe 1"),
        Recipient(name = "John Smith 1"),
    )

    roleRecipients[EnrollmentType.TEACHERENROLLMENT] = listOf(
        Recipient(name = "John Doe 2"),
        Recipient(name = "John Smith 2"),
    )


    RecipientPickerScreen(
        title = "Select Recipients",
        uiState = RecipientPickerUiState(
            screenOption = RecipientPickerScreenOption.Roles,
            screenState = ScreenState.Data,
            searchValue = TextFieldValue("John"),
            selectedRecipients = listOf(roleRecipients[EnrollmentType.TEACHERENROLLMENT]!!.first()),
            recipientsByRole = roleRecipients,
            recipientsToShow = listOf(
                roleRecipients[EnrollmentType.TEACHERENROLLMENT]!!.first(),
                roleRecipients[EnrollmentType.STUDENTENROLLMENT]!!.first()
            )
        ),
        actionHandler = {}
    )
}

@Preview
@Composable
fun RecipientPickerLoadingScreenPreview() {
    ContextKeeper.appContext = LocalContext.current

    RecipientPickerScreen(
        title = "Select Recipients",
        uiState = RecipientPickerUiState(
            screenOption = RecipientPickerScreenOption.Roles,
            screenState = ScreenState.Loading,
            searchValue = TextFieldValue(""),
            selectedRecipients = emptyList(),
            recipientsByRole = EnumMap(EnrollmentType::class.java),
            recipientsToShow = emptyList()
        ),
        actionHandler = {}
    )
}

@Preview
@Composable
fun RecipientPickerErrorScreenPreview() {
    ContextKeeper.appContext = LocalContext.current

    RecipientPickerScreen(
        title = "Select Recipients",
        uiState = RecipientPickerUiState(
            screenOption = RecipientPickerScreenOption.Roles,
            screenState = ScreenState.Error,
            searchValue = TextFieldValue(""),
            selectedRecipients = emptyList(),
            recipientsByRole = EnumMap(EnrollmentType::class.java),
            recipientsToShow = emptyList()
        ),
        actionHandler = {}
    )
}

@Preview
@Composable
fun RecipientPickerEmptyScreenPreview() {
    ContextKeeper.appContext = LocalContext.current

    RecipientPickerScreen(
        title = "Select Recipients",
        uiState = RecipientPickerUiState(
            screenOption = RecipientPickerScreenOption.Roles,
            screenState = ScreenState.Empty,
            searchValue = TextFieldValue(""),
            selectedRecipients = emptyList(),
            recipientsByRole = EnumMap(EnrollmentType::class.java),
            recipientsToShow = emptyList()
        ),
        actionHandler = {}
    )
}

@Preview
@Composable
fun SearchFieldPreview() {
    SearchField(
        value = TextFieldValue(""),
        placeholder = "Search",
        actionHandler = {}
    )
}

@Preview
@Composable
fun RoleRowPreview() {
    RoleRow(
        name = "Teacher",
        roleCount = 5,
        isSelected = false,
        onSelect = {}
    )
}

@Preview
@Composable
fun RecipientRowPreview() {
    RecipientRow(
        recipient = Recipient(
            name = "John Doe",
            avatarURL = null
        ),
        isSelected = false,
        onSelect = {}
    )
}