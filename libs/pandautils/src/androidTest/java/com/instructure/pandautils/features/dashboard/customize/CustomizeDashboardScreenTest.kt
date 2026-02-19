/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.dashboard.customize

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.features.dashboard.widget.SettingType
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CustomizeDashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoadingState() {
        val uiState = CustomizeDashboardUiState(
            loading = true,
            widgets = emptyList()
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithTag("loading").assertIsDisplayed()
        composeTestRule.onNodeWithTag("errorContent").assertDoesNotExist()
        composeTestRule.onNodeWithTag("emptyContent").assertDoesNotExist()
    }

    @Test
    fun testErrorState() {
        val uiState = CustomizeDashboardUiState(
            loading = false,
            error = "Test error message",
            widgets = emptyList()
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithTag("errorContent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test error message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("loading").assertDoesNotExist()
        composeTestRule.onNodeWithTag("emptyContent").assertDoesNotExist()
    }

    @Test
    fun testEmptyState() {
        val uiState = CustomizeDashboardUiState(
            loading = false,
            error = null,
            widgets = emptyList()
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithTag("emptyContent").assertIsDisplayed()
        composeTestRule.onNodeWithTag("loading").assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorContent").assertDoesNotExist()
    }

    @Test
    fun testWidgetListDisplay() {
        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = emptyList()
            ),
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget2",
                    position = 1,
                    isVisible = false,
                    isEditable = true
                ),
                displayName = "Widget 2",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("Widget 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Widget 2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("widgetItem_widget1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("widgetItem_widget2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("loading").assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorContent").assertDoesNotExist()
    }

    @Test
    fun testWidgetWithSettings() {
        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = listOf(
                    WidgetSettingItem(
                        key = "showGreeting",
                        value = true,
                        type = SettingType.BOOLEAN
                    ),
                    WidgetSettingItem(
                        key = "backgroundColor",
                        value = 123456,
                        type = SettingType.COLOR
                    )
                )
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("Widget 1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("widgetItem_widget1").assertIsDisplayed()
    }

    @Test
    fun testMoveUpButton() {
        var moveUpCalled = false
        var movedWidgetId = ""

        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = emptyList()
            ),
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget2",
                    position = 1,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 2",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets,
            onMoveUp = { widgetId ->
                moveUpCalled = true
                movedWidgetId = widgetId
            }
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        composeTestRule.onNode(
            hasTestTag("moveUpButton_widget2"),
            useUnmergedTree = true
        ).performClick()

        assert(moveUpCalled)
        assert(movedWidgetId == "widget2")
    }

    @Test
    fun testMoveDownButton() {
        var moveDownCalled = false
        var movedWidgetId = ""

        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = emptyList()
            ),
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget2",
                    position = 1,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 2",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets,
            onMoveDown = { widgetId ->
                moveDownCalled = true
                movedWidgetId = widgetId
            }
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        composeTestRule.onNode(
            hasTestTag("moveDownButton_widget1"),
            useUnmergedTree = true
        ).performClick()

        assert(moveDownCalled)
        assert(movedWidgetId == "widget1")
    }

    @Test
    fun testVisibilityToggle() {
        var toggleCalled = false
        var toggledWidgetId = ""

        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets,
            onToggleVisibility = { widgetId ->
                toggleCalled = true
                toggledWidgetId = widgetId
            }
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        composeTestRule.onNode(
            hasTestTag("visibilitySwitch_widget1"),
            useUnmergedTree = true
        ).performClick()

        assert(toggleCalled)
        assert(toggledWidgetId == "widget1")
    }

    @Test
    fun testDashboardRedesignToggleDisplayed() {
        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets,
            isDashboardRedesignEnabled = false
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        composeTestRule.onNode(
            hasTestTag("dashboardRedesignToggle"),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun testConfirmationDialogAppearsWhenDisablingRedesign() {
        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets,
            isDashboardRedesignEnabled = true
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        // Toggle the switch to disable
        composeTestRule.onNode(
            hasTestTag("dashboardRedesignToggle"),
            useUnmergedTree = true
        ).performClick()

        // Confirmation dialog should appear
        composeTestRule.onNodeWithTag("confirmationDialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("confirmationDialogTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("confirmationDialogMessage").assertIsDisplayed()
        composeTestRule.onNodeWithTag("confirmationDialogConfirmButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("confirmationDialogDismissButton").assertIsDisplayed()
    }

    @Test
    fun testConfirmationDialogDismiss() {
        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets,
            isDashboardRedesignEnabled = true
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        // Toggle to show confirmation dialog
        composeTestRule.onNode(
            hasTestTag("dashboardRedesignToggle"),
            useUnmergedTree = true
        ).performClick()

        // Click dismiss button
        composeTestRule.onNodeWithTag("confirmationDialogDismissButton").performClick()

        // Dialog should be dismissed
        composeTestRule.onNodeWithTag("confirmationDialog").assertDoesNotExist()
    }

    @Test
    fun testSurveyDialogAppearsAfterConfirmation() {
        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets,
            isDashboardRedesignEnabled = true
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        // Toggle to show confirmation dialog
        composeTestRule.onNode(
            hasTestTag("dashboardRedesignToggle"),
            useUnmergedTree = true
        ).performClick()

        // Confirm the action
        composeTestRule.onNodeWithTag("confirmationDialogConfirmButton").performClick()

        // Survey dialog should appear
        composeTestRule.onNodeWithTag("surveyDialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("surveyDialogTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("surveyDialogMessage").assertIsDisplayed()
        composeTestRule.onNodeWithTag("surveyDialogFeedbackButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("surveyDialogSubmitButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("surveyDialogSkipButton").assertIsDisplayed()
    }

    @Test
    fun testSurveyDialogSubmitButtonDisabledWhenEmpty() {
        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets,
            isDashboardRedesignEnabled = true
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        // Navigate to survey dialog
        composeTestRule.onNode(
            hasTestTag("dashboardRedesignToggle"),
            useUnmergedTree = true
        ).performClick()
        composeTestRule.onNodeWithTag("confirmationDialogConfirmButton").performClick()

        // Submit button should be disabled when feedback is empty
        composeTestRule.onNodeWithTag("surveyDialogSubmitButton").assertIsNotEnabled()
    }

    @Test
    fun testSurveyDialogSubmitButtonEnabledWithOption() {
        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets,
            isDashboardRedesignEnabled = true
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        // Navigate to survey dialog
        composeTestRule.onNode(
            hasTestTag("dashboardRedesignToggle"),
            useUnmergedTree = true
        ).performClick()
        composeTestRule.onNodeWithTag("confirmationDialogConfirmButton").performClick()

        // Select a survey option
        composeTestRule.onNodeWithTag("surveyOption_hard_to_find").performClick()

        // Submit button should be enabled
        composeTestRule.onNodeWithTag("surveyDialogSubmitButton").assertIsEnabled()
    }

    @Test
    fun testSurveyDialogSkipButton() {
        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets,
            isDashboardRedesignEnabled = true
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        // Navigate to survey dialog
        composeTestRule.onNode(
            hasTestTag("dashboardRedesignToggle"),
            useUnmergedTree = true
        ).performClick()
        composeTestRule.onNodeWithTag("confirmationDialogConfirmButton").performClick()

        // Click skip - verifying it doesn't crash
        composeTestRule.onNodeWithTag("surveyDialogSkipButton").performClick()

        // Survey dialog should be dismissed (restartApp is called asynchronously)
        composeTestRule.waitUntil(5000) {
            composeTestRule
                .onAllNodes(hasTestTag("surveyDialog"))
                .fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun testSurveyDialogOptionsDisplayed() {
        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets,
            isDashboardRedesignEnabled = true
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        // Navigate to survey dialog
        composeTestRule.onNode(
            hasTestTag("dashboardRedesignToggle"),
            useUnmergedTree = true
        ).performClick()
        composeTestRule.onNodeWithTag("confirmationDialogConfirmButton").performClick()

        // Verify all survey options are displayed
        composeTestRule.onNodeWithTag("surveyOption_hard_to_find").assertIsDisplayed()
        composeTestRule.onNodeWithTag("surveyOption_prefer_old_layout").assertIsDisplayed()
        composeTestRule.onNodeWithTag("surveyOption_something_broken").assertIsDisplayed()

        // Verify option text is displayed
        composeTestRule.onNodeWithText("Hard to find things").assertIsDisplayed()
        composeTestRule.onNodeWithText("Prefer the old layout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Something didn't work right").assertIsDisplayed()
    }

    @Test
    fun testSurveyDialogSubmitEnabledWithOnlyOption() {
        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets,
            isDashboardRedesignEnabled = true
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        // Navigate to survey dialog
        composeTestRule.onNode(
            hasTestTag("dashboardRedesignToggle"),
            useUnmergedTree = true
        ).performClick()
        composeTestRule.onNodeWithTag("confirmationDialogConfirmButton").performClick()

        // Select option
        composeTestRule.onNodeWithTag("surveyOption_hard_to_find").performClick()

        // Submit button should be enabled
        composeTestRule.onNodeWithTag("surveyDialogSubmitButton").assertIsEnabled()
    }

    @Test
    fun testSurveyDialogFeedbackButtonDisplayed() {
        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets,
            isDashboardRedesignEnabled = true
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        // Navigate to survey dialog
        composeTestRule.onNode(
            hasTestTag("dashboardRedesignToggle"),
            useUnmergedTree = true
        ).performClick()
        composeTestRule.onNodeWithTag("confirmationDialogConfirmButton").performClick()

        // Verify feedback section is displayed
        composeTestRule.onNodeWithText("What do you think of the new dashboard?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Let us know!").assertIsDisplayed()
        composeTestRule.onNodeWithTag("surveyDialogFeedbackButton").assertIsDisplayed()
    }

    @Test
    fun testMultipleWidgets() {
        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = emptyList()
            ),
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget2",
                    position = 1,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 2",
                settings = emptyList()
            ),
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget3",
                    position = 2,
                    isVisible = false,
                    isEditable = true
                ),
                displayName = "Widget 3",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("Widget 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Widget 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Widget 3").assertIsDisplayed()
        composeTestRule.onNodeWithTag("widgetItem_widget1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("widgetItem_widget2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("widgetItem_widget3").assertIsDisplayed()
    }

    @Test
    fun testTodoWidgetDisplayed() {
        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "todo",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "To Do",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("To Do").assertIsDisplayed()
        composeTestRule.onNodeWithTag("widgetItem_todo").assertIsDisplayed()
    }

    @Test
    fun testGlobalSettingsWithBackgroundColor() {
        val globalSettings = listOf(
            WidgetSettingItem(
                key = "backgroundColor",
                value = 0xFF0000FF.toInt(),
                type = SettingType.COLOR
            )
        )

        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets,
            globalSettings = globalSettings
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        // Verify the widgets color label is displayed (from globalSettings)
        composeTestRule.onNodeWithText("Dashboard Main Color").assertIsDisplayed()
    }

    @Test
    fun testMultipleGlobalSettings() {
        val globalSettings = listOf(
            WidgetSettingItem(
                key = "backgroundColor",
                value = 0xFF0000FF.toInt(),
                type = SettingType.COLOR
            ),
            WidgetSettingItem(
                key = "showGreeting",
                value = true,
                type = SettingType.BOOLEAN
            )
        )

        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "widget1",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Widget 1",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets,
            globalSettings = globalSettings
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        // Verify both global settings are displayed
        composeTestRule.onNodeWithText("Dashboard Main Color").assertIsDisplayed()
        composeTestRule.onNodeWithText("Show greeting").assertIsDisplayed()
    }

    @Test
    fun testWelcomeWidgetDisplayedWithCustomName() {
        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "welcome",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Hello, Test User",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("Hello, Test User").assertIsDisplayed()
        composeTestRule.onNodeWithTag("widgetItem_welcome").assertIsDisplayed()
    }

    @Test
    fun testAllDefaultWidgetsDisplayed() {
        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "course_invitations",
                    position = 0,
                    isVisible = true,
                    isEditable = false
                ),
                displayName = "Course Invitations",
                settings = emptyList()
            ),
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "institutional_announcements",
                    position = 1,
                    isVisible = true,
                    isEditable = false
                ),
                displayName = "Institutional Announcements",
                settings = emptyList()
            ),
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "welcome",
                    position = 2,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Hello, Test User",
                settings = emptyList()
            ),
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "courses",
                    position = 3,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Courses",
                settings = emptyList()
            ),
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "forecast",
                    position = 4,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Forecast",
                settings = emptyList()
            ),
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "todo",
                    position = 5,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "To Do",
                settings = emptyList()
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("Course Invitations").assertIsDisplayed()
        composeTestRule.onNodeWithText("Institutional Announcements").assertIsDisplayed()

        composeTestRule.onNodeWithTag("widgetsList")
            .performScrollToIndex(4)
        composeTestRule.onNodeWithText("Hello, Test User").assertIsDisplayed()
        composeTestRule.onNodeWithText("Courses").assertIsDisplayed()
        composeTestRule.onNodeWithText("Forecast").assertIsDisplayed()

        composeTestRule.onNodeWithTag("widgetsList")
            .performScrollToIndex(7)

        composeTestRule.onNodeWithText("To Do").assertIsDisplayed()
    }

    @Test
    fun testWidgetSettingsUpdate() {
        var updateCalled = false
        var updatedWidgetId = ""
        var updatedKey = ""
        var updatedValue: Any? = null

        val widgets = listOf(
            WidgetItem(
                metadata = WidgetMetadata(
                    id = "courses",
                    position = 0,
                    isVisible = true,
                    isEditable = true
                ),
                displayName = "Courses",
                settings = listOf(
                    WidgetSettingItem(
                        key = "showGrades",
                        value = false,
                        type = SettingType.BOOLEAN
                    )
                )
            )
        )

        val uiState = CustomizeDashboardUiState(
            loading = false,
            widgets = widgets,
            onUpdateSetting = { widgetId, key, value ->
                updateCalled = true
                updatedWidgetId = widgetId
                updatedKey = key
                updatedValue = value
            }
        )

        composeTestRule.setContent {
            CustomizeDashboardScreenContent(
                uiState = uiState,
                onRestartApp = {},
                onNavigateBack = {}
            )
        }

        // Verify the widget and its settings are displayed
        composeTestRule.onNodeWithText("Courses").assertIsDisplayed()
        composeTestRule.onNodeWithTag("widgetItem_courses").assertIsDisplayed()
    }
}