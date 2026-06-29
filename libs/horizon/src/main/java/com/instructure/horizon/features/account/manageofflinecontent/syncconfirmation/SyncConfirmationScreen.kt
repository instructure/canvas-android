/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.account.manageofflinecontent.syncconfirmation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.account.manageofflinecontent.ManageOfflineContentViewModel
import com.instructure.horizon.features.account.navigation.AccountRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.organisms.scaffolds.HorizonDialogScaffold
import kotlinx.coroutines.launch

@Composable
fun SyncConfirmationScreen(
    navController: NavHostController,
) {
    val parentEntry = remember { navController.getBackStackEntry(AccountRoute.ManageOfflineContent.route) }
    val viewModel: ManageOfflineContentViewModel = hiltViewModel(parentEntry)
    val scope = rememberCoroutineScope()

    HorizonDialogScaffold(
        title = stringResource(R.string.offline_startSyncConfirmationTitle),
        confirmLabel = stringResource(R.string.offline_startSyncConfirmationConfirm),
        onConfirm = {
            scope.launch {
                val started = viewModel.onSyncConfirmed()
                if (started) {
                    navController.navigate(AccountRoute.SyncingContent.route) {
                        popUpTo(AccountRoute.ManageOfflineContent.route)
                        launchSingleTop = true
                    }
                } else {
                    // Either nothing selected, or storage check rejected the plan.
                    // Pop back so the Manage screen can surface the warning.
                    navController.popBackStack()
                }
            }
        },
        dismissLabel = stringResource(R.string.offline_startSyncConfirmationCancel),
        onDismiss = { navController.popBackStack() },
        navigationBarColor = null,
        statusBarColor = HorizonColors.Surface.pageSecondary()
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.offline_startSyncConfirmationMessage),
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
            )
        }
    }
}

@Preview
@Composable
private fun SyncConfirmationScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    SyncConfirmationScreen(navController = rememberNavController())
}
