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
package com.instructure.horizon.features.account.offlinesettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.organisms.controls.ControlsContentState
import com.instructure.horizon.horizonui.organisms.controls.SwitchItem
import com.instructure.horizon.horizonui.organisms.controls.SwitchItemState
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelect
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectState
import com.instructure.horizon.horizonui.organisms.scaffolds.HorizonScaffold

@Composable
fun OfflineSettingsScreen(
    uiState: OfflineSettingsUiState,
    navController: NavHostController,
) {
    HorizonScaffold(
        title = stringResource(R.string.offline_settingsTitle),
        onBackPressed = { navController.popBackStack() },
    ) { modifier ->
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Button(
                label = stringResource(R.string.offline_manageOfflineContentButton),
                width = ButtonWidth.FILL,
                onClick = uiState.onManageOfflineContentClick,
                modifier = Modifier.fillMaxWidth(),
            )
            HorizonSpace(SpaceSize.SPACE_8)
            Text(
                text = stringResource(R.string.offline_syncSettingsHeader),
                style = HorizonTypography.h4,
                color = HorizonColors.Text.title(),
            )
            SwitchItem(
                state = SwitchItemState(
                    controlsContentState = ControlsContentState(
                        title = stringResource(R.string.offline_autoSyncLabel),
                        description = stringResource(R.string.offline_autoSyncDescription),
                    ),
                    checked = uiState.autoSyncEnabled,
                    onCheckedChanged = uiState.onAutoSyncToggled,
                )
            )
            SyncFrequencyDropdown(
                selected = uiState.syncFrequency,
                enabled = uiState.autoSyncEnabled,
                onSelected = uiState.onSyncFrequencySelected,
            )
            SwitchItem(
                state = SwitchItemState(
                    controlsContentState = ControlsContentState(
                        title = stringResource(R.string.offline_wifiOnlyLabel),
                        description = stringResource(R.string.offline_wifiOnlyDescription),
                    ),
                    checked = uiState.wifiOnlyEnabled,
                    onCheckedChanged = uiState.onWifiOnlyToggled,
                )
            )
        }
    }
}

@Composable
private fun SyncFrequencyDropdown(
    selected: SyncFrequency,
    enabled: Boolean,
    onSelected: (SyncFrequency) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = SyncFrequency.entries
    val optionLabels = options.map { it.labelKey }
    var isMenuOpen by remember { mutableStateOf(false) }

    SingleSelect(
        state = SingleSelectState(
            label = stringResource(R.string.offline_syncFrequencyLabel),
            size = SingleSelectInputSize.Medium,
            options = optionLabels,
            selectedOption = selected.labelKey,
            enabled = enabled,
            isMenuOpen = isMenuOpen,
            onMenuOpenChanged = { isMenuOpen = it },
            onOptionSelected = { label ->
                options.find { it.labelKey == label }?.let { onSelected(it) }
            },
        ),
        modifier = modifier,
    )
}

@Preview
@Composable
private fun OfflineSettingsScreenPreview() {
    OfflineSettingsScreen(
        uiState = OfflineSettingsUiState(),
        navController = rememberNavController(),
    )
}