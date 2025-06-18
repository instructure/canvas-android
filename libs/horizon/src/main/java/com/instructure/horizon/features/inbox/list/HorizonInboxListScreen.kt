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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.inbox.navigation.HorizonInboxRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
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
import com.instructure.horizon.horizonui.organisms.inputs.multiselect.MultiSelect
import com.instructure.horizon.horizonui.organisms.inputs.multiselect.MultiSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.multiselect.MultiSelectState
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelect
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectState
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.utils.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizonInboxListScreen(
    state: HorizonInboxListUiState,
    mainNavController: NavHostController,
    navController: NavHostController
) {
    Scaffold { padding ->

        LoadingStateWrapper(state.loadingState) {
            if (state.items.isEmpty()) {
                Text("Empty")
            } else {
                InboxContent(
                    state,
                    mainNavController,
                    navController,
                    Modifier.padding(padding)
                )
            }
        }
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
                iconPosition = ButtonIconPosition.Start(R.drawable.edit),
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
    mainNavController: NavHostController,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            InboxHeader(state, mainNavController, navController)
        }
        items(state.items) { item ->
            InboxContentItem(item)
        }
    }
}

@Composable
private fun InboxContentItem(item: HorizonInboxListItemState) {
    Column {
        Text(
            text = item.date.format("MMM dd, yyyy"),
            style = HorizonTypography.p2,
            color = HorizonColors.Text.timestamp(),
        )

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