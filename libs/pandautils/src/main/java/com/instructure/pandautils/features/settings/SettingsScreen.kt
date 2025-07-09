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
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.LabelValueSwitch
import com.instructure.pandautils.compose.composables.LabelValueVerticalItem
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.utils.AppTheme
import com.instructure.pandautils.utils.LocaleUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getActivityOrNull

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    modifier: Modifier = Modifier,
    navigationActionClick: () -> Unit
) {
    val activity = LocalContext.current.getActivityOrNull()
    LaunchedEffect(uiState.restartApp) {
        if (uiState.restartApp && activity != null) {
            LocaleUtils.restartApp(activity)
        }
    }

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
            if (uiState.loading) {
                Loading(modifier = Modifier.fillMaxSize())
            } else {
                SettingsContent(
                    uiState = uiState,
                    modifier = modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun SettingsContent(uiState: SettingsUiState, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        scrollState.scrollTo(uiState.scrollValue)
    }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .scrollable(state = scrollState, orientation = Orientation.Vertical)
            .testTag("settingsList")
    ) {
        uiState.items.onEachIndexed { index, entry ->
            val (sectionTitle, items) = entry
            item {
                Text(
                    modifier = Modifier
                        .padding(
                            top = 24.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 8.dp
                        )
                        .semantics { heading() },
                    text = stringResource(sectionTitle),
                    color = colorResource(id = R.color.textDark)
                )
            }
            items(items) { settingsItem ->
                when (settingsItem.item) {
                    SettingsItem.APP_THEME -> {
                        AppThemeItem(uiState.appTheme) { appTheme, x, y ->
                            uiState.actionHandler(
                                SettingsAction.SetAppTheme(
                                    appTheme,
                                    x,
                                    y,
                                    scrollState.value
                                )
                            )
                        }
                    }

                    SettingsItem.HOMEROOM_VIEW -> {
                        HomeroomViewItem(uiState.homeroomView) {
                            uiState.actionHandler(SettingsAction.SetHomeroomView(it))
                        }
                    }

                    else -> {
                        LabelValueVerticalItem(
                            modifier = Modifier
                                .semantics {
                                    role = Role.Button
                                }
                                .clickable {
                                    uiState.actionHandler(SettingsAction.ItemClicked(settingsItem.item))
                                }
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 4.dp
                                )
                                .testTag("settingsItem"),
                            label = stringResource(settingsItem.item.res),
                            value = settingsItem.subtitleRes?.let { stringResource(it) }
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
private fun AppThemeItem(appTheme: Int, appThemeSelected: (AppTheme, Int, Int) -> Unit) {
    Column(modifier = Modifier.testTag("settingsItem")) {
        Text(
            modifier = Modifier
                .testTag("label")
                .padding(horizontal = 16.dp, vertical = 8.dp),
            text = stringResource(id = R.string.appThemeSettingsTitle),
            fontSize = 16.sp,
            color = colorResource(id = R.color.textDarkest)
        )
    }
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AppThemeButton(
            icon = R.drawable.ic_panda_light,
            title = R.string.appThemeLight,
            testTag = "lightThemeButton",
            selected = appTheme == AppTheme.LIGHT.ordinal
        ) {
            appThemeSelected(
                AppTheme.LIGHT,
                it.x.toInt(),
                it.y.toInt()
            )
        }
        AppThemeButton(
            icon = R.drawable.ic_panda_dark,
            title = R.string.appThemeDark,
            testTag = "darkThemeButton",
            selected = appTheme == AppTheme.DARK.ordinal
        ) {
            appThemeSelected(
                AppTheme.DARK,
                it.x.toInt(),
                it.y.toInt()
            )
        }
        AppThemeButton(
            icon = R.drawable.ic_panda_system,
            title = R.string.appThemeAuto,
            testTag = "systemThemeButton",
            selected = appTheme == AppTheme.SYSTEM.ordinal
        ) {
            appThemeSelected(
                AppTheme.SYSTEM,
                it.x.toInt(),
                it.y.toInt()
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AppThemeButton(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    selected: Boolean,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: (Offset) -> Unit
) {
    var position by remember { mutableStateOf(Offset.Zero) }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            modifier = Modifier
                .testTag(testTag)
                .onGloballyPositioned {
                    position = Offset(
                        x = it.positionInRoot().x + it.size.width / 2,
                        y = it.positionInRoot().y + it.size.height / 2
                    )
                }
                .border(
                    2.dp,
                    if (selected) Color(ThemePrefs.brandColor) else Color.Transparent,
                    CircleShape
                ), onClick = { onClick(position) }) {
            Image(
                modifier = Modifier
                    .padding(4.dp)
                    .size(88.dp)
                    .aspectRatio(1f),
                painter = painterResource(id = icon),
                contentDescription = stringResource(id = title)
            )
        }
        Text(
            modifier = Modifier
                .padding(top = 8.dp)
                .semantics { this.invisibleToUser() },
            text = stringResource(title),
            fontSize = 12.sp,
            color = colorResource(id = R.color.textDarkest),
            textAlign = TextAlign.Center
        )
    }

}

@Composable
private fun HomeroomViewItem(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    LabelValueSwitch(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        label = stringResource(id = R.string.settingsHomeroomView),
        value = stringResource(
            id = R.string.settingsElementaryViewSubtitle
        ),
        isChecked = checked,
        onCheckedChange = onCheckedChange
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SettingsScreenDarkPreview() {
    ContextKeeper.appContext = LocalContext.current
    SettingsScreen(SettingsUiState(
        appTheme = AppTheme.SYSTEM.ordinal,
        homeroomView = true,
        actionHandler = {},
        items = mapOf(
            R.string.preferences to listOf(SettingsItemUiState(SettingsItem.APP_THEME), SettingsItemUiState(SettingsItem.HOMEROOM_VIEW)),
            R.string.legal to listOf(SettingsItemUiState(SettingsItem.ABOUT), SettingsItemUiState(SettingsItem.LEGAL))
        ),
    ), navigationActionClick = {})
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
fun SettingsScreenLightPreview() {
    ContextKeeper.appContext = LocalContext.current
    SettingsScreen(SettingsUiState(
        appTheme = AppTheme.SYSTEM.ordinal,
        homeroomView = false,
        actionHandler = {},
        items = mapOf(
            R.string.preferences to listOf(
                SettingsItemUiState(SettingsItem.APP_THEME),
                SettingsItemUiState(SettingsItem.HOMEROOM_VIEW),
                SettingsItemUiState(SettingsItem.PROFILE_SETTINGS)
            ),
            R.string.legal to listOf(SettingsItemUiState(SettingsItem.ABOUT), SettingsItemUiState(SettingsItem.LEGAL))
        ),
    ), navigationActionClick = {})
}