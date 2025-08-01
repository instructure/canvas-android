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

import com.instructure.horizon.horizonui.platform.LoadingState

data class AccountNotificationsUiState(
    val screenState: LoadingState = LoadingState(isPullToRefreshEnabled = false),
    val notificationItems: List<AccountNotificationGroup> = emptyList(),
)

data class AccountNotificationGroup(
    val title: String,
    val description: String,
    val items: List<AccountNotificationTypeState>,
)

data class AccountNotificationTypeState(
    val label: String,
    val enabled: Boolean,
    val checked: Boolean,
    val onClick: (Boolean) -> Unit
)