/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.HorizonDivider
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    state: AccountUiState,
    navController: NavController,
    mainNavController: NavController,
) {

    val renameFlow = remember { mainNavController.currentBackStackEntry?.savedStateHandle?.getStateFlow<String?>(AccountViewModel.CHANGE_USER_NAME, null) }
    val rename = renameFlow?.collectAsState()?.value

    LaunchedEffect(rename) {
        if (rename != null) {
            state.updateUserName(rename)
        }
    }

    LoadingStateWrapper(state.screenState) {
        AccountContentScreen(state, navController, state.performLogout)
    }
}

@Composable
private fun AccountContentScreen(state: AccountUiState, navController: NavController, onLogout: () -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(24.dp)
    ) {
        item {
            Column {
                Text(
                    text = state.userName,
                    style = HorizonTypography.h1,
                    color = HorizonColors.Text.title()
                )

                HorizonSpace(SpaceSize.SPACE_4)

                Text(
                    text = state.accountName,
                    style = HorizonTypography.h3,
                    color = HorizonColors.Surface.institution()
                )

                HorizonSpace(SpaceSize.SPACE_40)
            }
        }

        state.accountGroups.forEach { accountGroup ->
            if (accountGroup.title != null) {
                item {
                    Text(
                        text = accountGroup.title,
                        style = HorizonTypography.h3,
                        color = HorizonColors.Text.title(),
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            itemsIndexed(accountGroup.items) { index, accountItem  ->
                if (accountItem.visible) {
                    val clipModifier = when {
                        accountGroup.items.lastIndex == 0 -> {
                            Modifier.clip(HorizonCornerRadius.level3)
                        }

                        index == 0 -> {
                            Modifier.clip(HorizonCornerRadius.level3Top)
                        }

                        index == accountGroup.items.lastIndex -> {
                            Modifier.clip(HorizonCornerRadius.level3Bottom)
                        }

                        else -> {
                            Modifier
                        }
                    }
                    Column {
                        AccountItem(
                            accountItem,
                            navController,
                            onLogout,
                            clipModifier
                        )

                        if (index != accountGroup.items.lastIndex) {
                            HorizonDivider()
                        }
                    }
                }
            }

            if (accountGroup != state.accountGroups.last()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun AccountItem(item: AccountItemState, navController: NavController, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(HorizonColors.Surface.cardPrimary())
            .clickable {
                when (item.type) {
                    is AccountItemType.Open -> navController.navigate(item.type.route.route)
                    is AccountItemType.OpenInNew -> {
                        uriHandler.openUri(item.type.url)
                    }

                    is AccountItemType.LogOut -> {
                        onLogout()
                    }
                }
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.title,
                style = HorizonTypography.labelLargeBold,
                color = HorizonColors.Text.body(),
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                painter = painterResource(id = item.type.icon),
                contentDescription = null,
                tint = HorizonColors.Icon.medium(),
                modifier = Modifier
                    .size(24.dp)
            )
        }
    }
}