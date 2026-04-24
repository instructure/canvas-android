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
package com.instructure.pandautils.features.privacysettings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.LabelSwitchRow

@Composable
fun PrivacySettingsScreen(
    uiState: PrivacySettingsUiState,
    navigationActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(R.color.backgroundLightest),
            topBar = {
                CanvasThemedAppBar(
                    title = stringResource(R.string.privacySettingsTitle),
                    navigationActionClick = navigationActionClick
                )
            }
        ) { padding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = stringResource(R.string.privacySettingsDescription),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    fontSize = 14.sp,
                    color = colorResource(R.color.textDarkest)
                )
                LabelSwitchRow(
                    label = stringResource(R.string.privacySettingsToggleLabel),
                    checked = uiState.consentEnabled,
                    enabled = !uiState.saving,
                    onCheckedChange = uiState.onToggleChanged,
                    modifier = Modifier.fillMaxWidth()
                )
                Divider()
            }
        }
    }
}

@Preview
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, backgroundColor = 0x0000000)
@Composable
fun PrivacySettingsScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    PrivacySettingsScreen(
        uiState = PrivacySettingsUiState(
            consentEnabled = true
        ),
        navigationActionClick = {}
    )
}
