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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
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
        onRestartApp = { router.restartApp() },
        onTrackSurvey = viewModel::trackDashboardSurvey
    )
}

@Composable
fun CustomizeDashboardScreenContent(
    uiState: CustomizeDashboardUiState,
    onRestartApp: () -> Unit,
    onNavigateBack: () -> Unit,
    onTrackSurvey: (String, String) -> Unit = { _, _ -> }
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
                        onTrackSurvey = onTrackSurvey,
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
    onTrackSurvey: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.testTag("widgetsList"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            FeatureFlagToggle(
                onRestartApp = onRestartApp,
                isEnabled = uiState.isDashboardRedesignEnabled,
                onToggle = uiState.onToggleDashboardRedesign,
                onTrackSurvey = onTrackSurvey
            )
        }
        item {
            WidgetSettingsContent(
                widgetId = WidgetMetadata.WIDGET_ID_GLOBAL,
                settings = uiState.globalSettings,
                onUpdateSetting = uiState.onUpdateSetting,
                modifier = Modifier
                    .fillMaxWidth()
            )
            CanvasDivider()
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
                        .padding(horizontal = 16.dp)
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
                        }
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
                        titleModifier = Modifier.padding(vertical = 8.dp)
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
        "backgroundColor" -> stringResource(R.string.widgetSettings_widgetsColor)
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
    onRestartApp: () -> Unit,
    onTrackSurvey: (String, String) -> Unit
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
            onSubmit = { selectedOption, feedback ->
                onTrackSurvey(selectedOption, feedback)
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

private data class SurveyOptionData(
    val id: String,
    val textRes: Int
)

private val SURVEY_OPTIONS = listOf(
    SurveyOptionData(AnalyticsEventConstants.SURVEY_OPTION_HARD_TO_FIND, R.string.survey_option_hard_to_find),
    SurveyOptionData(AnalyticsEventConstants.SURVEY_OPTION_PREFER_OLD_LAYOUT, R.string.survey_option_prefer_old_layout),
    SurveyOptionData(AnalyticsEventConstants.SURVEY_OPTION_SOMETHING_BROKEN, R.string.survey_option_something_broken)
)

@Composable
private fun SurveyDialog(
    onRestartApp: () -> Unit,
    onSubmit: (String, String) -> Unit,
    onSkip: () -> Unit
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var feedback by remember { mutableStateOf("") }
    val maxCharacters = 255

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
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.switched_back_message),
                    modifier = Modifier.testTag("surveyDialogMessage")
                )

                Spacer(modifier = Modifier.height(16.dp))

                CanvasDivider()

                SURVEY_OPTIONS.forEach { option ->
                    SurveyOption(
                        id = option.id,
                        text = stringResource(option.textRes),
                        selected = selectedOption == option.id,
                        onClick = { selectedOption = option.id }
                    )
                }

                CanvasDivider()

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = feedback,
                    onValueChange = {
                        if (it.length <= maxCharacters) {
                            feedback = it
                        }
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.what_could_we_improve),
                            color = colorResource(R.color.textDark)
                        )
                    },
                    supportingText = {
                        Text(
                            text = "${feedback.length}/$maxCharacters",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            color = colorResource(R.color.textDark)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("surveyDialogFeedbackField"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colorResource(R.color.textDarkest),
                        focusedBorderColor = colorResource(R.color.borderMedium),
                        unfocusedTextColor = colorResource(R.color.textDarkest),
                        unfocusedBorderColor = colorResource(R.color.borderMedium)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit(selectedOption.orEmpty(), feedback)
                    restartApp()
                },
                enabled = selectedOption != null,
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

@Composable
private fun SurveyOption(
    id: String,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .testTag("surveyOption_$id"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(ThemePrefs.brandColor),
                unselectedColor = colorResource(R.color.textDark)
            )
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp),
            fontWeight = FontWeight.SemiBold,
            color = colorResource(R.color.textDarkest)
        )
    }
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