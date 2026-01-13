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
import androidx.compose.ui.platform.LocalContext
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
import com.instructure.pandautils.utils.LocaleUtils
import com.instructure.pandautils.utils.getActivityOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    state: AccountUiState,
    navController: NavController,
) {

    val renameFlow = remember { navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<String?>(AccountViewModel.CHANGE_USER_NAME, null) }
    val rename = renameFlow?.collectAsState()?.value

    LaunchedEffect(rename) {
        if (rename != null) {
            state.updateUserName(rename)
        }
    }

    val activity = LocalContext.current.getActivityOrNull()
    LaunchedEffect(state.restartApp) {
        if (state.restartApp && activity != null) {
            LocaleUtils.restartApp(activity)
        }
    }

    LoadingStateWrapper(state.screenState) {
        AccountContentScreen(state, navController, state.performLogout, state.switchExperience)
    }
}

@Composable
private fun AccountContentScreen(state: AccountUiState, navController: NavController, onLogout: () -> Unit, switchExperience: () -> Unit) {
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

                HorizonSpace(SpaceSize.SPACE_40)
            }
        }

        state.accountGroups.forEach { accountGroup ->
            if (accountGroup.title != null && accountGroup.items.isNotEmpty()) {
                item {
                    Text(
                        text = accountGroup.title,
                        style = HorizonTypography.h3,
                        color = HorizonColors.Text.title(),
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            val items = accountGroup.items.filter { it.visible }
            itemsIndexed(items) { index, accountItem  ->
                if (accountItem.visible) {
                    val clipModifier = when {
                        items.lastIndex == 0 -> {
                            Modifier.clip(HorizonCornerRadius.level3)
                        }

                        index == 0 -> {
                            Modifier.clip(HorizonCornerRadius.level3Top)
                        }

                        index == items.lastIndex -> {
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
                            switchExperience,
                            clipModifier
                        )

                        if (index != items.lastIndex) {
                            HorizonDivider()
                        }
                    }
                }
            }

            if (accountGroup != items.lastOrNull() && items.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun AccountItem(item: AccountItemState, navController: NavController, onLogout: () -> Unit, switchExperience: () -> Unit, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(HorizonColors.Surface.cardPrimary())
            .clickable {
                when (item.type) {
                    is AccountItemType.Open -> navController.navigate(item.type.route.route)

                    is AccountItemType.OpenWithoutIndicator -> navController.navigate(item.type.route.route)

                    is AccountItemType.OpenExternal -> uriHandler.openUri(item.type.url)

                    is AccountItemType.LogOut -> {
                        onLogout()
                    }

                    AccountItemType.SwitchExperience -> {
                        switchExperience()
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
                modifier = Modifier.weight(1f)
            )

            item.type.icon?.let { icon ->
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = HorizonColors.Icon.medium(),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(24.dp)
                )
            }
        }
    }
}