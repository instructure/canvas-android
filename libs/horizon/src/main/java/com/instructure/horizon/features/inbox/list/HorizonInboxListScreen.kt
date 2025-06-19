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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
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
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.organisms.inputs.multiselect.MultiSelect
import com.instructure.horizon.horizonui.organisms.inputs.multiselect.MultiSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.multiselect.MultiSelectState
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelect
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectState
import com.instructure.pandautils.utils.format

@Composable
fun HorizonInboxListScreen(
    state: HorizonInboxListUiState,
    mainNavController: NavHostController,
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = HorizonColors.Surface.pagePrimary(),
    ) { padding ->
        InboxStateWrapper(
            state,
            mainNavController,
            navController,
            Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InboxStateWrapper(
    state: HorizonInboxListUiState,
    mainNavController: NavHostController,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        InboxHeader(state, mainNavController, navController)
        HorizonSpace(SpaceSize.SPACE_16)

        PullToRefreshBox(
            isRefreshing = state.loadingState.isRefreshing,
            onRefresh = { state.loadingState.onRefresh() },
            state = pullToRefreshState,
            modifier = modifier.fillMaxSize(),
            indicator = {
                Indicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    isRefreshing = state.loadingState.isRefreshing,
                    containerColor = HorizonColors.Surface.pageSecondary(),
                    color = HorizonColors.Surface.institution(),
                    state = pullToRefreshState
                )
            },
            content = {
                if (state.loadingState.isLoading) {
                    LoadingContent()
                } else {
                    InboxContent(state)
                }
            }
        )
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .clip(HorizonCornerRadius.level4)
            .background(HorizonColors.Surface.cardPrimary())
    ) {
        Spinner(
            color = HorizonColors.Surface.institution()
        )
    }
}

@Composable
private fun InboxHeader(
    state: HorizonInboxListUiState,
    mainNavController: NavHostController,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
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
                onClick = { mainNavController.popBackStack() },
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                label = "Create Message",
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
        val multiSelectState = MultiSelectState(
            placeHolderText = "Filter by person",
            size = MultiSelectInputSize.Medium,
            options = state.allRecipients.map { it.name.orEmpty() },
            isFocused = isRecipientFilterFocused,
            onFocusChanged = { isFocused ->
                isRecipientFilterFocused = isFocused
            },
            selectedOptions = state.selectedRecipients.map { it.name.orEmpty() },
            onOptionSelected = { option ->
                val recipient = state.allRecipients.firstOrNull { it.name == option }
                if (recipient != null) {
                    state.updateSelectedRecipients(state.selectedRecipients + recipient)
                }
            },
            onOptionRemoved = { option ->
                val recipient = state.allRecipients.firstOrNull { it.name == option }
                if (recipient != null) {
                    state.updateSelectedRecipients(state.selectedRecipients - recipient)
                }
            },
            isMenuOpen = isRecipientFilterOpen,
            onMenuOpenChanged = { isOpen ->
                isRecipientFilterOpen = isOpen
            }
        )
        MultiSelect(state = multiSelectState)
    }
}

@Composable
private fun InboxContent(
    state: HorizonInboxListUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        if (state.items.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillParentMaxSize()
                        .clip(HorizonCornerRadius.level4)
                        .background(HorizonColors.Surface.cardPrimary())
                ){
                    Text(
                        text = "No messages yet.",
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
                Modifier
                    .then(
                        when (item) {
                            state.items.firstOrNull() -> {
                                Modifier.clip(
                                    HorizonCornerRadius.level4Top
                                )
                            }

                            state.items.lastOrNull() -> {
                                Modifier.clip(
                                    HorizonCornerRadius.level4Bottom
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
}

@Composable
private fun InboxContentItem(
    item: HorizonInboxListItemState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = item.date.format("MMM dd, yyyy"),
            style = HorizonTypography.p2,
            color = HorizonColors.Text.timestamp(),
        )

        HorizonSpace(SpaceSize.SPACE_8)

        Text(
            text = item.title,
            style = HorizonTypography.labelMediumBold,
            color = HorizonColors.Text.body(),
        )

        Text(
            text = item.description,
            style = HorizonTypography.labelMediumBold,
            color = HorizonColors.Text.body(),
        )
    }
}