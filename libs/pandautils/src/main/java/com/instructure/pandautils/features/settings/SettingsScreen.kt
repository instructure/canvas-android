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

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.LabelValueVerticalItem
import com.instructure.pandautils.utils.AppTheme
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemePrefs

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
private fun SettingsContent(uiState: SettingsUiState, modifier: Modifier = Modifier) {
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
                        AppThemeItem(uiState)
                    }

                    SettingsItem.OFFLINE_SYNCHRONIZATION -> {
                        OfflineSyncItem(uiState)
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
                        color = colorResource(id = R.color.backgroundMedium),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppThemeItem(uiState: SettingsUiState) {
    val context = LocalContext.current
    Column {
        Text(
            modifier = Modifier
                .testTag("label")
                .padding(horizontal = 16.dp, vertical = 8.dp),
            text = stringResource(id = R.string.appThemeSettingsTitle),
            style = TextStyle(fontSize = 16.sp, color = colorResource(id = R.color.textDarkest))
        )
    }
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AppThemeButton(icon = R.drawable.panda_light_mode, title = R.string.appThemeLight) {
            val appTheme = AppTheme.LIGHT
            AppCompatDelegate.setDefaultNightMode(appTheme.nightModeType)
            ThemePrefs.appTheme = appTheme.ordinal

            val nightModeFlags: Int =
                context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            ColorKeeper.darkTheme = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
            ThemePrefs.isThemeApplied = false
        }
        AppThemeButton(icon = R.drawable.panda_dark_mode, title = R.string.appThemeDark) {
            val appTheme = AppTheme.DARK
            AppCompatDelegate.setDefaultNightMode(appTheme.nightModeType)
            ThemePrefs.appTheme = appTheme.ordinal

            val nightModeFlags: Int =
                context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            ColorKeeper.darkTheme = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
            ThemePrefs.isThemeApplied = false
        }
        AppThemeButton(icon = R.drawable.panda_light_mode, title = R.string.appThemeAuto) {
            val appTheme = AppTheme.SYSTEM
            AppCompatDelegate.setDefaultNightMode(appTheme.nightModeType)
            ThemePrefs.appTheme = appTheme.ordinal

            val nightModeFlags: Int =
                context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            ColorKeeper.darkTheme = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
            ThemePrefs.isThemeApplied = false
        }
    }
}

@Composable
private fun AppThemeButton(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier.height(120.dp)) {
        IconButton(onClick = { onClick() }) {
            Image(
                modifier = Modifier
                    .height(88.dp)
                    .aspectRatio(1f),
                painter = painterResource(id = icon),
                contentDescription = stringResource(id = title)
            )
        }
        Text(
            modifier = Modifier
                .padding(top = 8.dp)
                .width(80.dp),
            text = stringResource(title),
            style = TextStyle(
                fontSize = 12.sp,
                color = colorResource(id = R.color.textDarkest),
                textAlign = TextAlign.Center
            )
        )
    }

}

@Composable
private fun OfflineSyncItem(uiState: SettingsUiState) {
    LabelValueVerticalItem(
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