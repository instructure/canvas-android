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
package com.instructure.horizon.features.account.manageofflinecontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.organisms.scaffolds.HorizonScaffold

@Composable
fun RemoveSyncedContentConfirmationScreen(
    navController: NavHostController,
    onConfirm: () -> Unit = {},
) {
    HorizonScaffold(
        title = stringResource(R.string.offline_removeSyncedContentTitle),
        onBackPressed = { navController.popBackStack() },
    ) { modifier ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.offline_removeSyncedContentDescription),
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
            )
            HorizonSpace(SpaceSize.SPACE_16)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    label = stringResource(R.string.offline_removeSyncedContentCancel),
                    color = ButtonColor.Ghost,
                    width = ButtonWidth.FILL,
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                )
                Button(
                    label = stringResource(R.string.offline_removeSyncedContentConfirm),
                    color = ButtonColor.Danger,
                    width = ButtonWidth.FILL,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview
@Composable
private fun RemoveSyncedContentConfirmationScreenPreview() {
    RemoveSyncedContentConfirmationScreen(navController = rememberNavController())
}