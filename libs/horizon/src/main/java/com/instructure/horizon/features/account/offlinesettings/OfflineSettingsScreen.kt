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

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.instructure.horizon.horizonui.organisms.scaffolds.HorizonDialogScaffold
import com.instructure.horizon.horizonui.organisms.scaffolds.HorizonScaffold

@Composable
fun OfflineSettingsScreen(
    uiState: OfflineSettingsUiState,
    navController: NavHostController,
) {
    if (uiState.showWifiOnlyConfirmation) {
        WifiOnlyDisableConfirmationDialog(
            onConfirm = uiState.onWifiOnlyConfirmationConfirmed,
            onDismiss = uiState.onWifiOnlyConfirmationDismissed,
        )
        return
    }
    HorizonScaffold(
        title = stringResource(R.string.offline_settingsTitle),
        onBackPressed = { navController.popBackStack() },
    ) { modifier ->
        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = modifier
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            val controlsEnabled = !uiState.isSyncActive
            Button(
                label = stringResource(R.string.offline_manageOfflineContentButton),
                width = ButtonWidth.FILL,
                onClick = uiState.onManageOfflineContentClick,
                modifier = Modifier.fillMaxWidth(),
            )
            Column {
                Text(
                    text = stringResource(R.string.offline_syncSettingsHeader),
                    style = HorizonTypography.labelLargeBold,
                    color = HorizonColors.Text.title(),
                )

                HorizonSpace(SpaceSize.SPACE_8)

                SwitchItem(
                    state = SwitchItemState(
                        controlsContentState = ControlsContentState(
                            title = stringResource(R.string.offline_autoSyncLabel),
                        ),
                        checked = uiState.autoSyncEnabled,
                        enabled = controlsEnabled,
                        onCheckedChanged = uiState.onAutoSyncToggled,
                    ),
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                Text(
                    text = stringResource(R.string.offline_autoSyncDescription),
                    style = HorizonTypography.p2,
                    color = HorizonColors.Text.body(),
                )
            }
            Column {
                SyncFrequencyDropdown(
                    selected = uiState.syncFrequency,
                    enabled = controlsEnabled && uiState.autoSyncEnabled,
                    onSelected = uiState.onSyncFrequencySelected,
                )
                HorizonSpace(SpaceSize.SPACE_4)
                Text(
                    text = stringResource(R.string.offline_syncFrequencyDescription),
                    style = HorizonTypography.p2,
                    color = HorizonColors.Text.body(),
                )
            }
            Column {
                SwitchItem(
                    state = SwitchItemState(
                        controlsContentState = ControlsContentState(
                            title = stringResource(R.string.offline_wifiOnlyLabel),
                        ),
                        checked = uiState.wifiOnlyEnabled,
                        enabled = controlsEnabled,
                        onCheckedChanged = uiState.onWifiOnlyToggled,
                    ),
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                Text(
                    text = stringResource(R.string.offline_wifiOnlyDescription),
                    style = HorizonTypography.p2,
                    color = HorizonColors.Text.body(),
                )
            }
        }
    }
}

@Composable
private fun WifiOnlyDisableConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    BackHandler(onBack = onDismiss)
    HorizonDialogScaffold(
        title = stringResource(R.string.offline_wifiOnlyDisableConfirmationTitle),
        confirmLabel = stringResource(R.string.offline_wifiOnlyDisableConfirmationConfirm),
        onConfirm = onConfirm,
        dismissLabel = stringResource(R.string.offline_wifiOnlyDisableConfirmationCancel),
        onDismiss = onDismiss,
        navigationBarColor = null,
        statusBarColor = HorizonColors.Surface.pageSecondary()
    ) { contentPadding ->
        Text(
            text = stringResource(R.string.offline_wifiOnlyDisableConfirmationDescription),
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body(),
            modifier = Modifier
                .padding(contentPadding)
                .padding(horizontal = 24.dp, vertical = 24.dp),
        )
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
    val optionLabels = remember(options) { options }.map { stringResource(it.labelRes) }
    val selectedLabel = stringResource(selected.labelRes)
    var isMenuOpen by rememberSaveable { mutableStateOf(false) }

    SingleSelect(
        state = SingleSelectState(
            label = stringResource(R.string.offline_syncFrequencyLabel),
            size = SingleSelectInputSize.Medium,
            options = optionLabels,
            selectedOption = selectedLabel,
            enabled = enabled,
            isMenuOpen = isMenuOpen,
            onMenuOpenChanged = { isMenuOpen = it },
            onOptionSelected = { label ->
                options.getOrNull(optionLabels.indexOf(label))?.let { onSelected(it) }
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

@Preview
@Composable
private fun OfflineSettingsScreenWifiOnlyConfirmationPreview() {
    OfflineSettingsScreen(
        uiState = OfflineSettingsUiState(showWifiOnlyConfirmation = true),
        navController = rememberNavController(),
    )
}