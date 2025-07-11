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
package com.instructure.horizon.features.account

import androidx.annotation.DrawableRes
import com.instructure.horizon.R
import com.instructure.horizon.features.account.navigation.AccountRoute
import com.instructure.horizon.horizonui.platform.LoadingState

data class AccountUiState(
    val screenState: LoadingState = LoadingState(),
    val userName: String = "",
    val accountGroups: List<AccountGroupState> = emptyList(),
    val updateUserName: (String) -> Unit = {},
    val performLogout: () -> Unit = {},
    val switchExperience: () -> Unit = {},
    val restartApp: Boolean = false,
)

data class AccountGroupState(
    val title: String?,
    val items: List<AccountItemState>,
)

data class AccountItemState(
    val title: String,
    val type: AccountItemType,
    val visible: Boolean = true
)

sealed class AccountItemType(@DrawableRes val icon: Int) {
    data class Open(val route: AccountRoute) : AccountItemType(R.drawable.arrow_forward)
    data class OpenInNew(val url: String) : AccountItemType(R.drawable.open_in_new)
    data object LogOut : AccountItemType(R.drawable.logout)
    data object SwitchExperience : AccountItemType(R.drawable.swap_horiz)
}