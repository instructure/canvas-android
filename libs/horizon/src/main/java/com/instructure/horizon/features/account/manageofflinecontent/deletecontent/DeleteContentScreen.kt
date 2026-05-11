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
package com.instructure.horizon.features.account.manageofflinecontent.deletecontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import com.instructure.horizon.features.account.navigation.AccountRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.organisms.scaffolds.HorizonDialogScaffold
import com.instructure.horizon.horizonui.organisms.scaffolds.HorizonScaffold

@Composable
fun DeleteContentScreen(
    navController: NavHostController,
    viewModel: DeleteContentViewModel = hiltViewModel(),
) {
    val isDeleting by viewModel.isDeleting.collectAsState()
    val isComplete by viewModel.isComplete.collectAsState()

    LaunchedEffect(isComplete){
        if (isComplete) {
            navController.popBackStack(AccountRoute.ManageOfflineContent.route, inclusive = true)
            navController.navigate(AccountRoute.ManageOfflineContent.route)
        }
    }

    if (isDeleting) {
        DeletingContentView()
    } else {
        ConfirmationDialogView(
            navController = navController,
            onConfirm = { viewModel.deleteContent() },
        )
    }
}

@Composable
private fun ConfirmationDialogView(
    navController: NavHostController,
    onConfirm: () -> Unit,
) {
    HorizonDialogScaffold(
        title = stringResource(R.string.offline_removeSyncedContentTitle),
        confirmLabel = stringResource(R.string.offline_removeSyncedContentConfirm),
        onConfirm = onConfirm,
        confirmColor = ButtonColor.Danger,
        dismissLabel = stringResource(R.string.offline_removeSyncedContentCancel),
        navigationBarColor = null,
        onDismiss = { navController.popBackStack() },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.offline_removeSyncedContentDescription),
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
            )
        }
    }
}

@Composable
private fun DeletingContentView() {
    HorizonScaffold(
        title = stringResource(R.string.offline_manageOfflineContentTitle),
        onBackPressed = {},
    ) { modifier ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxSize(),
        ) {
            Spinner()
            HorizonSpace(SpaceSize.SPACE_16)
            Text(
                text = stringResource(R.string.offline_deletingContent),
                style = HorizonTypography.h3,
                color = HorizonColors.Text.body(),
            )
        }
    }
}

@Preview
@Composable
private fun DeleteContentScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    ConfirmationDialogView(
        navController = rememberNavController(),
        onConfirm = {  },
    )
}

@Preview
@Composable
private fun DeletingContentViewPreview() {
    ContextKeeper.appContext = LocalContext.current
    DeletingContentView()
}
