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
package com.instructure.horizon.features.inbox.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.inbox.HorizonInboxItemType
import com.instructure.horizon.features.inbox.navigation.HorizonInboxRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonIconPosition
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.HorizonDivider
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.organisms.inputs.multiselectsearch.MultiSelectSearch
import com.instructure.horizon.horizonui.organisms.inputs.multiselectsearch.MultiSelectSearchInputSize
import com.instructure.horizon.horizonui.organisms.inputs.multiselectsearch.MultiSelectSearchState
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelect
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectState
import com.instructure.horizon.util.HorizonEdgeToEdgeSystemBars
import com.instructure.horizon.util.fullScreenInsets
import com.instructure.horizon.util.zeroScreenInsets
import com.instructure.pandautils.utils.localisedFormat
import java.util.Date

@Composable
fun HorizonInboxListScreen(
    state: HorizonInboxListUiState,
    navController: NavHostController
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.loadingState.snackbarMessage) {
        if (state.loadingState.snackbarMessage != null) {
            val result = snackbarHostState.showSnackbar(state.loadingState.snackbarMessage)
            if (result == SnackbarResult.Dismissed) {
                state.loadingState.onSnackbarDismiss()
            }
        }
    }

    HorizonEdgeToEdgeSystemBars(
        statusBarColor = HorizonColors.Surface.pagePrimary(),
        navigationBarColor = HorizonColors.Surface.cardPrimary(),
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.zeroScreenInsets,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = HorizonColors.Surface.pagePrimary(),
        ) { padding ->
            InboxStateWrapper(
                state,
                navController,
                Modifier.padding(padding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InboxStateWrapper(
    state: HorizonInboxListUiState,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = state.loadingState.isRefreshing,
        onRefresh = { state.loadingState.onRefresh() },
        state = pullToRefreshState,
        modifier = modifier.fillMaxSize(),
        indicator = {
            Indicator(
                modifier = Modifier
                    .align(Alignment.TopCenter),
                isRefreshing = state.loadingState.isRefreshing,
                containerColor = HorizonColors.Surface.pageSecondary(),
                color = HorizonColors.Surface.institution(),
                state = pullToRefreshState
            )
        },
        content = {
            LazyColumn(
                contentPadding = WindowInsets.fullScreenInsets.asPaddingValues()
            ) {
                inboxHeader(state, navController)

                if (state.loadingState.isLoading) {
                    loadingContent()
                } else {
                    inboxContent(state, navController)
                }
            }
        }
    )
}

private fun LazyListScope.loadingContent(modifier: Modifier = Modifier) {
    item {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillParentMaxSize()
                .clip(HorizonCornerRadius.level4Top)
                .background(HorizonColors.Surface.cardPrimary())
        ) {
            Spinner(
                color = HorizonColors.Surface.institution(),
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

private fun LazyListScope.inboxHeader(
    state: HorizonInboxListUiState,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    item {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    iconRes = R.drawable.arrow_back,
                    size = IconButtonSize.NORMAL,
                    color = IconButtonColor.Inverse,
                    elevation = HorizonElevation.level4,
                    onClick = { navController.popBackStack() },
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    label = stringResource(R.string.inboxCreateMessageLabel),
                    height = ButtonHeight.NORMAL,
                    width = ButtonWidth.RELATIVE,
                    color = ButtonColor.Institution,
                    iconPosition = ButtonIconPosition.Start(R.drawable.edit_square),
                    onClick = { navController.navigate(HorizonInboxRoute.InboxCompose.route) }
                )
            }

            HorizonSpace(SpaceSize.SPACE_16)

            val context = LocalContext.current
            var isScopeFilterFocused by remember { mutableStateOf(false) }
            var isScopeFilterOpen by remember { mutableStateOf(false) }
            val singleSelectState = SingleSelectState(
                size = SingleSelectInputSize.Medium,
                options = HorizonInboxScope.entries.map { context.getString(it.label) },
                isFocused = isScopeFilterFocused,
                onFocusChanged = { isFocused ->
                    isScopeFilterFocused = isFocused
                },
                selectedOption = context.getString(state.selectedScope.label),
                onOptionSelected = { option ->
                    state.updateScopeFilter(
                        HorizonInboxScope.entries.firstOrNull { context.getString(it.label) == option }
                            ?: HorizonInboxScope.All
                    )
                },
                isMenuOpen = isScopeFilterOpen,
                onMenuOpenChanged = { isOpen ->
                    isScopeFilterOpen = isOpen
                },
            )
            SingleSelect(singleSelectState)

            HorizonSpace(SpaceSize.SPACE_16)

            var isRecipientFilterFocused by remember { mutableStateOf(false) }
            var isRecipientFilterOpen by remember { mutableStateOf(false) }
            val multiSelectState = MultiSelectSearchState(
                size = MultiSelectSearchInputSize.Medium,
                options = state.allRecipients.map { it.name.orEmpty() },
                isFocused = isRecipientFilterFocused,
                onFocusChanged = { isFocused ->
                    isRecipientFilterFocused = isFocused
                },
                selectedOptions = state.selectedRecipients.map { it.name.orEmpty() },
                onOptionSelected = { option ->
                    val recipient = state.allRecipients.firstOrNull { it.name == option }
                    if (recipient != null) {
                        state.onRecipientSelected(recipient)
                    }
                },
                onOptionRemoved = { option ->
                    val recipient = state.allRecipients.firstOrNull { it.name == option }
                    if (recipient != null) {
                        state.onRecipientRemoved(recipient)
                    }
                },
                isMenuOpen = isRecipientFilterOpen,
                onMenuOpenChanged = { isOpen ->
                    isRecipientFilterOpen = isOpen
                },
                searchPlaceHolder = stringResource(R.string.inboxFilterByPersonLabel),
                searchQuery = state.recipientSearchQuery,
                onSearchQueryChanged = { query ->
                    state.updateRecipientSearchQuery(query)
                },
                isOptionListLoading = state.isOptionListLoading,
                minSearchQueryLengthForMenu = state.minQueryLength
            )
            MultiSelectSearch(state = multiSelectState)

            HorizonSpace(SpaceSize.SPACE_16)
        }
    }
}

private fun LazyListScope.inboxContent(
    state: HorizonInboxListUiState,
    navController: NavHostController,
) {
    if (state.items.isEmpty()) {
        item {
            Column(
                modifier = Modifier
                    .fillParentMaxSize()
                    .clip(HorizonCornerRadius.level4Top)
                    .background(HorizonColors.Surface.cardPrimary())
            ){
                Text(
                    text = stringResource(R.string.inboxNoMessagesLabel),
                    style = HorizonTypography.p1,
                    color = HorizonColors.Text.body(),
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
    items(state.items) { item ->
        InboxContentItem(
            item,
            item != state.items.lastOrNull(),
            {
                if (item.type != HorizonInboxItemType.AccountNotification) {
                    state.setItemAsRead(item.id)
                }
                navController.navigate(
                    HorizonInboxRoute.InboxDetails.route(item.id, item.type, item.courseId)
                )
            },
            Modifier
                .then(
                    when (item) {
                        state.items.firstOrNull() -> {
                            Modifier.clip(
                                HorizonCornerRadius.level4Top
                            )
                        }

                        else -> {
                            Modifier
                        }
                    }
                )
                .background(HorizonColors.Surface.cardPrimary())
        )
    }
}

@Composable
private fun InboxContentItem(
    item: HorizonInboxListItemState,
    showDivider: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.date?.localisedFormat("MMM dd, yyyy").orEmpty(),
                    style = HorizonTypography.p2,
                    color = HorizonColors.Text.timestamp(),
                )

                Spacer(modifier = Modifier.weight(1f))

                if (item.isUnread) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(HorizonColors.Surface.institution())
                    )
                }
            }

            HorizonSpace(SpaceSize.SPACE_8)

            val textStyle = if (item.isUnread) HorizonTypography.labelMediumBold else HorizonTypography.p2
            Text(
                text = item.title,
                style = textStyle,
                color = HorizonColors.Text.body(),
            )

            Text(
                text = item.description,
                style = textStyle,
                color = HorizonColors.Text.body(),
            )
        }

        if (showDivider) {
            HorizonDivider()
        }
    }
}

@Composable
@Preview
private fun HorizonInboxListPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = HorizonInboxListUiState(
        items = listOf(
            HorizonInboxListItemState(
                id = 1,
                title = "Message",
                description = "This is the first message.",
                date = Date(),
                type = HorizonInboxItemType.Inbox,
                isUnread = true
            ),
            HorizonInboxListItemState(
                id = 2,
                title = "Announcement",
                description = "This is the second message.",
                date = Date(),
                type = HorizonInboxItemType.AccountNotification,
                isUnread = false
            )
        ),
        selectedScope = HorizonInboxScope.All,
        allRecipients = emptyList(),
        selectedRecipients = emptyList(),
        updateScopeFilter = {},
        onRecipientSelected = {},
        onRecipientRemoved = {},
        updateRecipientSearchQuery = {}
    )
    HorizonInboxListScreen(state, rememberNavController())

}