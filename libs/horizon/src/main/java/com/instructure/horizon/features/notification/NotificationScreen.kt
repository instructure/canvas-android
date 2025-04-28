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
package com.instructure.horizon.features.notification

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.horizon.utils.HorizonScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(state: NotificationUiState, mainNavController: NavController) {
    HorizonScaffold(
        title = stringResource(R.string.notificationsTitle),
        onBackPressed = { mainNavController.popBackStack() },
    ) {
        LoadingStateWrapper(state.screenState) {
            NotificationContent(state)
        }
    }
}

@Composable
private fun NotificationContent(state: NotificationUiState) {

}