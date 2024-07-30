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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.LabelValueVerticalItem
import com.instructure.pandautils.utils.AppTheme
import com.instructure.pandautils.utils.ThemePrefs

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    modifier: Modifier = Modifier,
    navigationActionClick: () -> Unit
) {
    CanvasTheme {
        Scaffold(topBar = {
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
    fun createAppThemeItem() {
        val initialAppTheme = AppTheme.fromIndex(ThemePrefs.appTheme)
        return LabelValueVerticalItem(
            modifier = Modifier
                .clickable {
                    uiState.onClick(SettingsItem.APP_THEME)
                }
                .padding(
                    horizontal = 16.dp,
                    vertical = 4.dp
                ),
            label = stringResource(R.string.appThemeSettingsTitle),
            value = stringResource(initialAppTheme.themeNameRes)
        )
    }

    @Composable
    fun createOfflineSyncItem() {
        return LabelValueVerticalItem(
            modifier = Modifier
                .clickable {
                    uiState.onClick(SettingsItem.OFFLINE_SYNCHRONIZATION)
                }
                .padding(
                    horizontal = 16.dp,
                    vertical = 4.dp
                ),
            label = stringResource(R.string.offlineSyncSettingsTitle),
            value = uiState.offlineState?.let { stringResource(it) }
        )
    }

    LazyColumn(modifier = modifier) {
        for ((sectionTitle, items) in uiState.items) {
            item {
                Text(
                    modifier = Modifier.padding(
                        top = 24.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    text = stringResource(sectionTitle),
                    color = colorResource(id = R.color.textDark)
                )
            }
            items(items) { settingsItem ->
                when (settingsItem) {
                    SettingsItem.APP_THEME -> {
                        createAppThemeItem()
                    }

                    SettingsItem.OFFLINE_SYNCHRONIZATION -> {
                        createOfflineSyncItem()
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
                                ),
                            label = stringResource(settingsItem.res)
                        )
                    }
                }
            }

            item {
                Divider()
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