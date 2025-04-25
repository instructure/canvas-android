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
package com.instructure.horizon.features.account.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.instructure.horizon.R
import com.instructure.horizon.features.account.AccountScaffold
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.organisms.controls.ControlsContentState
import com.instructure.horizon.horizonui.organisms.controls.SwitchItem
import com.instructure.horizon.horizonui.organisms.controls.SwitchItemState
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountNotificationsScreen(
    state: AccountNotificationsUiState,
    navController: NavController,
) {
    AccountScaffold(
        title = stringResource(R.string.accountNotificationsTitle),
        onBackPressed = { navController.popBackStack() }
    ) {
        LoadingStateWrapper(state.screenState) {
            AccountNotificationContent(state)
        }
    }
}

@Composable
private fun AccountNotificationContent(state: AccountNotificationsUiState) {
    LazyColumn(
        contentPadding = PaddingValues(
            vertical = 32.dp,
            horizontal = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        items(state.notificationItems) { notificationItem ->
            NotificationGroup(notificationItem, state.updateNotificationItem)
        }
    }
}

@Composable
private fun NotificationGroup(group: AccountNotificationGroup, onItemSelected: (AccountNotificationItem, Boolean) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = group.title,
            style = HorizonTypography.labelLargeBold,
            color = HorizonColors.Text.body(),
        )

        HorizonSpace(SpaceSize.SPACE_4)

        Text(
            text = group.description,
            style = HorizonTypography.p2,
            color = HorizonColors.Text.body(),
        )

        HorizonSpace(SpaceSize.SPACE_8)

        group.items.toList().forEach { item ->
            val emailSwitchState = SwitchItemState(
                controlsContentState = ControlsContentState(title = item.title),
                checked = item.checked,
                enabled = item.enabled,
                onCheckedChanged = {
                    onItemSelected(item, it)
               },
            )
            SwitchItem(emailSwitchState, Modifier.padding(vertical = 10.dp))

            HorizonSpace(SpaceSize.SPACE_4)
        }
    }
}