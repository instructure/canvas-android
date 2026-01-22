/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.customize

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.CanvasSwitch
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.ColorPicker
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.features.dashboard.notifications.DashboardRouter
import com.instructure.pandautils.features.dashboard.widget.SettingType
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.courses.CoursesConfig
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ThemedColor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun CustomizeDashboardScreen(router: DashboardRouter, onNavigateBack: () -> Unit) {
    val viewModel: CustomizeDashboardViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    CustomizeDashboardScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRestartApp = { router.restartApp() }
    )
}

@Composable
fun CustomizeDashboardScreenContent(
    uiState: CustomizeDashboardUiState,
    onRestartApp: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.background(colorResource(R.color.backgroundLight)),
        topBar = {
            CanvasThemedAppBar(
                title = stringResource(id = R.string.customize_dashboard),
                navIconRes = R.drawable.ic_close_lined,
                navigationActionClick = {
                    onNavigateBack()
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .background(colorResource(R.color.backgroundLight))
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                uiState.error != null -> {
                    ErrorContent(
                        errorMessage = uiState.error,
                        retryClick = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("errorContent")
                    )
                }

                uiState.loading -> {
                    Loading(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("loading")
                    )
                }

                uiState.widgets.isEmpty() -> {
                    EmptyContent(
                        emptyMessage = stringResource(id = R.string.no_widgets),
                        imageRes = R.drawable.ic_panda_nothing_to_see,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("emptyContent")
                    )
                }

                else -> {
                    WidgetList(
                        uiState = uiState,
                        onRestartApp = onRestartApp,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun WidgetList(
    uiState: CustomizeDashboardUiState,
    onRestartApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            FeatureFlagToggle(
                onRestartApp = onRestartApp,
                isEnabled = uiState.isDashboardRedesignEnabled,
                onToggle = uiState.onToggleDashboardRedesign
            )
        }
        item {
            Text(
                stringResource(R.string.dashboard_widgets),
                color = colorResource(R.color.textDarkest),
                fontSize = 14.sp,
                lineHeight = 19.sp
            )
        }
        items(
            items = uiState.widgets,
            key = { it.metadata.id }
        ) { widgetItem ->
            val index = uiState.widgets.indexOf(widgetItem)
            WidgetListItem(
                widgetItem = widgetItem,
                isFirst = index == 0,
                isLast = index == uiState.widgets.size - 1,
                onMoveUp = { uiState.onMoveUp(widgetItem.metadata.id) },
                onMoveDown = { uiState.onMoveDown(widgetItem.metadata.id) },
                onToggleVisibility = { uiState.onToggleVisibility(widgetItem.metadata.id) },
                onUpdateSetting = uiState.onUpdateSetting,
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun WidgetListItem(
    widgetItem: WidgetItem,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleVisibility: () -> Unit,
    onUpdateSetting: (widgetId: String, key: String, value: Any) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasSettings = widgetItem.settings.isNotEmpty()

    Column(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("widgetItem_${widgetItem.metadata.id}"),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.backgroundLightest)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp, top = 12.dp, end = 16.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, end = 16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (widgetItem.metadata.isVisible) {
                                colorResource(R.color.backgroundLightestElevated)
                            } else {
                                colorResource(R.color.backgroundLight)
                            }
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (widgetItem.metadata.isVisible) 4.dp else 0.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            IconButton(
                                onClick = onMoveUp,
                                enabled = !isFirst && widgetItem.metadata.isVisible,
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = Color(ThemePrefs.brandColor),
                                    disabledContentColor = colorResource(R.color.disabledColor)
                                ),
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("moveUpButton_${widgetItem.metadata.id}")
                            ) {
                                Icon(
                                    modifier = Modifier.rotate(180f),
                                    painter = painterResource(R.drawable.ic_chevron_down_small),
                                    contentDescription = stringResource(id = R.string.move_up)
                                )
                            }

                            VerticalDivider(
                                modifier = Modifier
                                    .height(24.dp)
                                    .padding(vertical = 4.dp),
                                thickness = 0.5.dp,
                                color = colorResource(R.color.borderMedium)
                            )

                            IconButton(
                                onClick = onMoveDown,
                                enabled = !isLast && widgetItem.metadata.isVisible,
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = Color(ThemePrefs.brandColor),
                                    disabledContentColor = colorResource(R.color.disabledColor)
                                ),
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("moveDownButton_${widgetItem.metadata.id}")
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_chevron_down_small),
                                    contentDescription = stringResource(id = R.string.move_down),
                                )
                            }
                        }
                    }

                    Text(
                        text = widgetItem.displayName,
                        fontSize = 16.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(R.color.textDarkest)
                    )
                }

                CanvasSwitch(
                    checked = widgetItem.metadata.isVisible,
                    onCheckedChange = { onToggleVisibility() },
                    modifier = Modifier.testTag("visibilitySwitch_${widgetItem.metadata.id}")
                )
            }
            if (hasSettings) {
                CanvasDivider(
                    modifier = Modifier
                        .fillMaxWidth(),
                )

                WidgetSettingsContent(
                    widgetId = widgetItem.metadata.id,
                    settings = widgetItem.settings,
                    onUpdateSetting = onUpdateSetting,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }

    }
}

@Composable
private fun WidgetSettingsContent(
    widgetId: String,
    settings: List<WidgetSettingItem>,
    onUpdateSetting: (widgetId: String, key: String, value: Any) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        settings.forEach { setting ->
            when (setting.type) {
                SettingType.BOOLEAN -> {
                    BooleanSettingRow(
                        label = getSettingLabel(setting.key),
                        checked = setting.value as? Boolean ?: false,
                        onCheckedChange = { newValue ->
                            onUpdateSetting(widgetId, setting.key, newValue)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                SettingType.COLOR -> {
                    val colorValue = when (val value = setting.value) {
                        is Int -> value
                        is Double -> value.toInt()
                        is String -> value.toIntOrNull() ?: ThemePrefs.brandColor
                        else -> ThemePrefs.brandColor
                    }
                    ColorPicker(
                        label = getSettingLabel(setting.key),
                        selectedColor = colorValue,
                        colors = getAvailableColors(),
                        onColorSelected = { newValue ->
                            onUpdateSetting(widgetId, setting.key, newValue)
                        },
                        titleModifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun getSettingLabel(key: String): String {
    return when (key) {
        "showGreeting" -> stringResource(R.string.widget_setting_show_greeting)
        "backgroundColor" -> stringResource(R.string.background_color)
        CoursesConfig.KEY_SHOW_GRADES -> stringResource(R.string.widget_setting_show_grades)
        CoursesConfig.KEY_SHOW_COLOR_OVERLAY -> stringResource(R.string.widget_setting_show_color_overlay)
        else -> key
    }
}

@Composable
private fun getAvailableColors(): List<ThemedColor> {
    val context = LocalContext.current
    val lightColors = ColorKeeper.courseColors.map { context.getColor(it) }
    val themedColors = lightColors.map { ColorKeeper.createThemedColor(it) }.toMutableList().apply {
        add(
            ThemedColor(
                context.getColor(R.color.backgroundLightest),
                context.getColor(R.color.backgroundLightest)
            )
        )
        add(
            ThemedColor(
                context.getColor(R.color.backgroundDarkest),
                context.getColor(R.color.backgroundDarkest)
            )
        )
    }
    return themedColors
}

@Composable
private fun BooleanSettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 21.sp,
            color = colorResource(R.color.textDarkest)
        )

        CanvasSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun FeatureFlagToggle(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onRestartApp: () -> Unit
) {
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showSurveyDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(51.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            stringResource(R.string.new_mobile_dashboard),
            color = colorResource(R.color.textDarkest),
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 21.sp
        )

        CanvasSwitch(
            checked = isEnabled,
            onCheckedChange = { newValue ->
                if (isEnabled && !newValue) {
                    showConfirmationDialog = true
                } else {
                    onToggle(newValue)
                }
            },
            modifier = Modifier.testTag("dashboardRedesignToggle")
        )
    }

    if (showConfirmationDialog) {
        ConfirmationDialog(
            onConfirm = {
                onToggle(false)
                showConfirmationDialog = false
                showSurveyDialog = true
            },
            onDismiss = {
                showConfirmationDialog = false
            }
        )
    }

    if (showSurveyDialog) {
        SurveyDialog(
            onRestartApp = onRestartApp,
            onSubmit = { feedback ->
                // TODO: Send feedback to backend
                showSurveyDialog = false
            },
            onSkip = {
                showSurveyDialog = false
            }
        )
    }
}

@Composable
private fun ConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        containerColor = colorResource(R.color.backgroundLightest),
        textContentColor = colorResource(R.color.textDarkest),
        titleContentColor = colorResource(R.color.textDarkest),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.changing_dashboard_layout),
                modifier = Modifier.testTag("confirmationDialogTitle")
            )
        },
        text = {
            Text(
                text = stringResource(R.string.changing_dashboard_layout_message),
                modifier = Modifier.testTag("confirmationDialogMessage")
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(ThemePrefs.brandColor)
                ),
                modifier = Modifier.testTag("confirmationDialogConfirmButton")
            ) {
                Text(
                    text = stringResource(R.string.restart_now)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(ThemePrefs.brandColor)
                ),
                modifier = Modifier.testTag("confirmationDialogDismissButton")
            ) {
                Text(
                    text = stringResource(R.string.cancel)
                )
            }
        },
        modifier = Modifier.testTag("confirmationDialog")
    )
}

@Composable
private fun SurveyDialog(
    onRestartApp: () -> Unit,
    onSubmit: (String) -> Unit,
    onSkip: () -> Unit
) {
    var feedback by remember { mutableStateOf("") }

    fun restartApp() {
        GlobalScope.launch {
            try {
                onRestartApp()
            } catch (e: Exception) {
                // No-op
            }
        }
    }

    AlertDialog(
        containerColor = colorResource(R.color.backgroundLightest),
        textContentColor = colorResource(R.color.textDarkest),
        titleContentColor = colorResource(R.color.textDarkest),
        onDismissRequest = {
            onSkip()
            restartApp()
        },
        title = {
            Text(
                text = stringResource(R.string.switched_back),
                modifier = Modifier.testTag("surveyDialogTitle")
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.switched_back_message),
                    modifier = Modifier.testTag("surveyDialogMessage")
                )
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = {
                        Text(
                            text = stringResource(R.string.what_could_we_improve),
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("surveyDialogFeedbackField"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colorResource(R.color.textDarkest),
                        focusedLabelColor = colorResource(R.color.textDark),
                        focusedBorderColor = colorResource(R.color.borderMedium),
                        focusedPlaceholderColor = colorResource(R.color.textDark),
                        unfocusedPlaceholderColor = colorResource(R.color.textDark)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit(feedback)
                    restartApp()
                },
                enabled = feedback.isNotBlank(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(ThemePrefs.brandColor),
                    disabledContentColor = Color(ThemePrefs.brandColor).copy(alpha = 0.6f),
                ),
                modifier = Modifier.testTag("surveyDialogSubmitButton")
            ) {
                Text(
                    text = stringResource(R.string.submit)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onSkip()
                    restartApp()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(ThemePrefs.brandColor),
                ),
                modifier = Modifier.testTag("surveyDialogSkipButton")
            ) {
                Text(
                    text = stringResource(R.string.skip)
                )
            }
        },
        modifier = Modifier.testTag("surveyDialog")
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CustomizeDashboardScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    CanvasTheme {
        CustomizeDashboardScreenContent(
            uiState = CustomizeDashboardUiState(
                widgets = listOf(
                    WidgetItem(
                        metadata = WidgetMetadata(
                            id = WidgetMetadata.WIDGET_ID_WELCOME,
                            position = 0,
                            isVisible = true
                        ),
                        displayName = "Hello, [Riley]",
                        settings = listOf(
                            WidgetSettingItem(
                                key = "showGreeting",
                                value = false,
                                type = SettingType.BOOLEAN
                            ),
                            WidgetSettingItem(
                                key = "backgroundColor",
                                value = 0x2573DF,
                                type = SettingType.COLOR
                            )
                        )
                    ),
                    WidgetItem(
                        metadata = WidgetMetadata(
                            id = WidgetMetadata.WIDGET_ID_WELCOME,
                            position = 0,
                            isVisible = false
                        ),
                        displayName = "Hello, [Riley]",
                        settings = listOf(
                            WidgetSettingItem(
                                key = "showGreeting",
                                value = false,
                                type = SettingType.BOOLEAN
                            ),
                            WidgetSettingItem(
                                key = "backgroundColor",
                                value = 0x2573DF,
                                type = SettingType.COLOR
                            )
                        )
                    )
                ),
                loading = false,
                error = null,
                isDashboardRedesignEnabled = true,
                onToggleDashboardRedesign = {}
            ),
            onNavigateBack = {},
            onRestartApp = {}
        )
    }
}