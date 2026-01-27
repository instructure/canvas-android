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
package com.instructure.pandautils.features.dashboard.widget.courses.customize

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CustomizeCourseScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context by lazy { InstrumentationRegistry.getInstrumentation().targetContext }

    @Before
    fun setup() {
        ContextKeeper.appContext = context
    }

    @Test
    fun screenDisplaysAllComponents() {
        val uiState = createSampleUiState()

        composeTestRule.setContent {
            CustomizeCourseScreenContent(
                uiState = uiState,
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithTag("customizeCourseScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("courseHeader").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nicknameCard").assertIsDisplayed()
        composeTestRule.onNodeWithTag("colorPickerCard").assertIsDisplayed()
        composeTestRule.onNodeWithTag("doneButton").assertIsDisplayed()
    }

    @Test
    fun appBarDisplaysCourseNameAndCode() {
        val uiState = createSampleUiState(
            courseName = "Introduction to Computer Science",
            courseCode = "CS101"
        )

        composeTestRule.setContent {
            CustomizeCourseScreenContent(
                uiState = uiState,
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("Introduction to Computer Science").assertIsDisplayed()
        composeTestRule.onNodeWithText("CS101").assertIsDisplayed()
    }

    @Test
    fun appBarDisplaysCloseButton() {
        val uiState = createSampleUiState()

        composeTestRule.setContent {
            CustomizeCourseScreenContent(
                uiState = uiState,
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithContentDescription(context.getString(R.string.close))
            .assertIsDisplayed()
    }

    @Test
    fun nicknameInputDisplaysCurrentValue() {
        val uiState = createSampleUiState(
            nickname = "My Favorite Course"
        )

        composeTestRule.setContent {
            CustomizeCourseScreenContent(
                uiState = uiState,
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("My Favorite Course").assertIsDisplayed()
    }

    @Test
    fun nicknameInputDisplaysPlaceholderWhenEmpty() {
        val uiState = createSampleUiState(nickname = "")

        composeTestRule.setContent {
            CustomizeCourseScreenContent(
                uiState = uiState,
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithTag("nicknameInput").assertIsDisplayed()
    }

    @Test
    fun nicknameInputTriggersCallback() {
        var capturedNickname = ""
        val uiState = createSampleUiState(
            nickname = "",
            onNicknameChanged = { capturedNickname = it }
        )

        composeTestRule.setContent {
            CustomizeCourseScreenContent(
                uiState = uiState,
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithTag("nicknameInput").performTextInput("Test Nickname")

        assertEquals("Test Nickname", capturedNickname)
    }

    @Test
    fun nicknameInputCanBeCleared() {
        var capturedNickname = "Initial"
        val uiState = createSampleUiState(
            nickname = "Initial",
            onNicknameChanged = { capturedNickname = it }
        )

        composeTestRule.setContent {
            CustomizeCourseScreenContent(
                uiState = uiState,
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithTag("nicknameInput").performTextClearance()

        assertEquals("", capturedNickname)
    }

    @Test
    fun nicknameInputCanBeUpdatedFromEmpty() {
        var capturedNickname = ""
        val uiState = createSampleUiState(
            nickname = "",
            onNicknameChanged = { capturedNickname = it }
        )

        composeTestRule.setContent {
            CustomizeCourseScreenContent(
                uiState = uiState,
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithTag("nicknameInput").performTextInput("New Name")

        assertEquals("New Name", capturedNickname)
    }

    @Test
    fun colorPickerDisplaysLabel() {
        val uiState = createSampleUiState()

        composeTestRule.setContent {
            CustomizeCourseScreenContent(
                uiState = uiState,
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.dashboardCourseColor))
            .assertIsDisplayed()
    }

    @Test
    fun doneButtonEnabledWhenNotLoading() {
        val uiState = createSampleUiState(isLoading = false)

        composeTestRule.setContent {
            CustomizeCourseScreenContent(
                uiState = uiState,
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithTag("doneButton").assertIsEnabled()
    }

    @Test
    fun doneButtonDisabledWhenLoading() {
        val uiState = createSampleUiState(isLoading = true)

        composeTestRule.setContent {
            CustomizeCourseScreenContent(
                uiState = uiState,
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithTag("doneButton").assertIsNotEnabled()
    }

    @Test
    fun handlesLongCourseName() {
        val longName = "This is a very long course name that might need to wrap or truncate in the UI"
        val uiState = createSampleUiState(courseName = longName)

        composeTestRule.setContent {
            CustomizeCourseScreenContent(
                uiState = uiState,
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText(longName).assertIsDisplayed()
    }

    @Test
    fun handlesLongNickname() {
        val longNickname = "This is a very long nickname that the user might enter"
        val uiState = createSampleUiState(nickname = longNickname)

        composeTestRule.setContent {
            CustomizeCourseScreenContent(
                uiState = uiState,
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText(longNickname).assertIsDisplayed()
    }

    @Test
    fun displaysMultipleColorOptions() {
        val colors = listOf(
            0xFF2573DF.toInt(),
            0xFFE71F63.toInt(),
            0xFF0B874B.toInt(),
            0xFFFC5E13.toInt(),
            0xFF8F3E97.toInt(),
            0xFF00AC18.toInt()
        )
        val uiState = createSampleUiState(availableColors = colors)

        composeTestRule.setContent {
            CustomizeCourseScreenContent(
                uiState = uiState,
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithTag("colorPickerCard").assertIsDisplayed()
    }

    private fun createSampleUiState(
        courseId: Long = 1L,
        courseName: String = "Test Course",
        courseCode: String = "TEST101",
        imageUrl: String? = null,
        nickname: String = "Test Nickname",
        selectedColor: Int = 0xFF2573DF.toInt(),
        availableColors: List<Int> = listOf(
            0xFF2573DF.toInt(),
            0xFFE71F63.toInt(),
            0xFF0B874B.toInt()
        ),
        isLoading: Boolean = false,
        showColorOverlay: Boolean = true,
        onNicknameChanged: (String) -> Unit = {},
        onColorSelected: (Int) -> Unit = {},
        onDone: () -> Unit = {},
        onNavigationHandled: () -> Unit = {},
        onErrorHandled: () -> Unit = {}
    ): CustomizeCourseUiState {
        return CustomizeCourseUiState(
            courseId = courseId,
            courseName = courseName,
            courseCode = courseCode,
            imageUrl = imageUrl,
            nickname = nickname,
            selectedColor = selectedColor,
            availableColors = availableColors,
            isLoading = isLoading,
            showColorOverlay = showColorOverlay,
            onNicknameChanged = onNicknameChanged,
            onColorSelected = onColorSelected,
            onDone = onDone,
            onNavigationHandled = onNavigationHandled,
            onErrorHandled = onErrorHandled
        )
    }
}
