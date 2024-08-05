/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.LabelValueVerticalItem

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    modifier: Modifier = Modifier,
    navigationActionClick: () -> Unit
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                CanvasThemedAppBar(
                    title = stringResource(id = R.string.screenTitleSettings),
                    navigationActionClick = {
                        navigationActionClick()
                    }
                )
            }) { padding ->
            SettingsContent(
                uiState = uiState,
                modifier = modifier.padding(padding)
            )
        }
    }
}

@Composable
fun SettingsContent(uiState: SettingsUiState, modifier: Modifier = Modifier) {

    @Composable
    fun AppThemeItem() {
        return LabelValueVerticalItem(
            modifier = Modifier
                .clickable {
                    uiState.onClick(SettingsItem.APP_THEME)
                }
                .padding(
                    horizontal = 16.dp,
                    vertical = 4.dp
                )
                .testTag("settingsItem"),
            label = stringResource(R.string.appThemeSettingsTitle),
            value = uiState.appTheme?.let { stringResource(it) }
        )
    }

    @Composable
    fun OfflineSyncItem() {
        return LabelValueVerticalItem(
            modifier = Modifier
                .clickable {
                    uiState.onClick(SettingsItem.OFFLINE_SYNCHRONIZATION)
                }
                .padding(
                    horizontal = 16.dp,
                    vertical = 4.dp
                )
                .testTag("settingsItem"),
            label = stringResource(R.string.offlineSyncSettingsTitle),
            value = uiState.offlineState?.let { stringResource(it) }
        )
    }

    LazyColumn(modifier = modifier) {
        uiState.items.onEachIndexed { index, entry ->
            val (sectionTitle, items) = entry
            item {
                Text(
                    modifier = Modifier.padding(
                        top = 24.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 8.dp
                    ),
                    text = stringResource(sectionTitle),
                    color = colorResource(id = R.color.textDark)
                )
            }
            items(items) { settingsItem ->
                when (settingsItem) {
                    SettingsItem.APP_THEME -> {
                        AppThemeItem()
                    }

                    SettingsItem.OFFLINE_SYNCHRONIZATION -> {
                        OfflineSyncItem()
                    }

                    else -> {
                        LabelValueVerticalItem(
                            modifier = Modifier
                                .clickable {
                                    uiState.onClick(settingsItem)
                                }
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 4.dp
                                )
                                .testTag("settingsItem"),
                            label = stringResource(settingsItem.res)
                        )
                    }
                }
            }

            if (index < uiState.items.size - 1) {
                item {
                    Divider(
                        color = colorResource(id = R.color.rce_divider),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun SettingsScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    SettingsScreen(SettingsUiState(
        mapOf(
            R.string.preferences to listOf(SettingsItem.APP_THEME),
            R.string.legal to listOf(SettingsItem.ABOUT, SettingsItem.LEGAL)
        )
    ) {}, navigationActionClick = {})
}