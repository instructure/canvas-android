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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasSwitch
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.features.dashboard.notifications.DashboardRouter
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.utils.ThemePrefs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun CustomizeDashboardScreen(router: DashboardRouter, onNavigateBack: () -> Unit) {
    val viewModel: CustomizeDashboardViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    CustomizeDashboardScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        router = router
    )
}

@Composable
fun CustomizeDashboardScreenContent(
    uiState: CustomizeDashboardUiState,
    router: DashboardRouter,
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
                        retryClick = {},
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
                        router = router,
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
    router: DashboardRouter,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            FeatureFlagToggle(
                router = router,
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
    modifier: Modifier = Modifier
) {
    val hasSettings = widgetItem.config?.getSettingDefinitions()?.isNotEmpty() == true

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
                            containerColor = colorResource(R.color.backgroundLightestElevated)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            IconButton(
                                onClick = onMoveUp,
                                enabled = !isFirst,
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("moveUpButton_${widgetItem.metadata.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = stringResource(id = R.string.move_up),
                                    tint = if (isFirst) colorResource(R.color.textDark) else Color(
                                        ThemePrefs.brandColor
                                    )
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
                                enabled = !isLast,
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("moveDownButton_${widgetItem.metadata.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = stringResource(id = R.string.move_down),
                                    tint = if (isLast) colorResource(R.color.textDark) else Color(
                                        ThemePrefs.brandColor
                                    )
                                )
                            }
                        }
                    }

                    Text(
                        text = widgetItem.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(R.color.textDarkest)
                    )
                }

                CanvasSwitch(
                    checked = widgetItem.metadata.isVisible,
                    onCheckedChange = { onToggleVisibility() },
                    modifier = Modifier.testTag("visibilitySwitch_${widgetItem.metadata.id}")
                )
            }
        }

        if (hasSettings) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 0.5.dp),
                thickness = 0.5.dp,
                color = colorResource(R.color.borderMedium)
            )
        }
    }
}

@Composable
private fun FeatureFlagToggle(router: DashboardRouter, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
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
            }
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
            router = router,
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
                text = stringResource(R.string.changing_dashboard_layout)
            )
        },
        text = {
            Text(
                text = stringResource(R.string.changing_dashboard_layout_message)
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(ThemePrefs.brandColor)
                )
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
                )
            ) {
                Text(
                    text = stringResource(R.string.cancel)
                )
            }
        }
    )
}

@Composable
private fun SurveyDialog(
    router: DashboardRouter,
    onSubmit: (String) -> Unit,
    onSkip: () -> Unit
) {
    var feedback by remember { mutableStateOf("") }

    fun restartApp() {
        GlobalScope.launch {
            try {
                router.restartApp()
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
                text = stringResource(R.string.switched_back)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.switched_back_message),
                )
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = {
                        Text(
                            text = stringResource(R.string.what_could_we_improve),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
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
                )
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
                )
            ) {
                Text(
                    text = stringResource(R.string.skip)
                )
            }
        }
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
                        config = null,
                        displayName = "Hello, [Riley]"
                    )
                ),
                loading = false,
                error = null,
                isDashboardRedesignEnabled = true,
                onToggleDashboardRedesign = {}
            ),
            onNavigateBack = {},
            router = object : DashboardRouter {
                override fun routeToGlobalAnnouncement(
                    subject: String,
                    message: String
                ) {
                    TODO("Not yet implemented")
                }

                override fun routeToSubmissionDetails(
                    canvasContext: CanvasContext,
                    assignmentId: Long,
                    attemptId: Long
                ) {
                    TODO("Not yet implemented")
                }

                override fun routeToMyFiles(
                    canvasContext: CanvasContext,
                    folderId: Long
                ) {
                    TODO("Not yet implemented")
                }

                override fun routeToSyncProgress() {
                    TODO("Not yet implemented")
                }

                override fun routeToCustomizeDashboard() {
                    TODO("Not yet implemented")
                }

                override fun restartApp() {
                    TODO("Not yet implemented")
                }

            }
        )
    }
}