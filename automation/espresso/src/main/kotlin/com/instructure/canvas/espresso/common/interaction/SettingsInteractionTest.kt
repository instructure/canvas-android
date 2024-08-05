/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */    package com.instructure.canvas.espresso.common.interaction

import com.instructure.canvas.espresso.CanvasComposeTest
import com.instructure.canvas.espresso.common.pages.compose.SettingsPage
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import org.junit.Test

abstract class SettingsInteractionTest : CanvasComposeTest() {

    private val settingsPage = SettingsPage(composeTestRule)

    @Test
    fun openAppThemeSelector() {
        val data = initData()
        goToSettings(data)

        composeTestRule.waitForIdle()

        settingsPage.assertSettingsItemDisplayed("App Theme")
        settingsPage.clickOnSettingsItem("App Theme")
        settingsPage.assertThemeSelectorOpened()
    }

    @Test
    fun openAboutScreen() {
        val data = initData()
        goToSettings(data)

        composeTestRule.waitForIdle()

        settingsPage.assertSettingsItemDisplayed("About")
        settingsPage.clickOnSettingsItem("About")
        settingsPage.assertAboutDialogOpened()
    }

    @Test
    fun openLegalScreen() {
        val data = initData()
        goToSettings(data)

        composeTestRule.waitForIdle()

        settingsPage.assertSettingsItemDisplayed("Legal")
        settingsPage.clickOnSettingsItem("Legal")
        settingsPage.assertLegalDialogOpened()
    }

    abstract fun initData(): MockCanvas

    abstract fun goToSettings(data: MockCanvas)

    override fun displaysPageObjects() = Unit
}