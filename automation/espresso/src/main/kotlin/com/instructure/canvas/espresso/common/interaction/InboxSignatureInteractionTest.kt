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
package com.instructure.canvas.espresso.common.interaction

import com.instructure.canvas.espresso.CanvasComposeTest
import com.instructure.canvas.espresso.common.pages.compose.InboxSignatureSettingsPage
import com.instructure.canvas.espresso.common.pages.compose.SettingsPage
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import org.junit.Test

abstract class InboxSignatureInteractionTest : CanvasComposeTest() {

    val settingsPage = SettingsPage(composeTestRule)
    private val inboxSignatureSettingsPage = InboxSignatureSettingsPage(composeTestRule)

    @Test
    fun screenOpensWithSignature() {
        val data = initData()
        data.inboxSignature = "Hello, PR reviewer!"
        goToInboxSignatureSettings(data)

        inboxSignatureSettingsPage.assertSignatureText(data.inboxSignature)
        inboxSignatureSettingsPage.assertSignatureEnabledState(true)
    }

    @Test
    fun changeSignature() {
        val data = initData()
        data.inboxSignature = ""
        data.signatureEnabled = false
        goToInboxSignatureSettings(data)

        inboxSignatureSettingsPage.assertSignatureEnabledState(false)

        val newSignature = "Hello, code reviewer!"
        inboxSignatureSettingsPage.toggleSignatureEnabledState()
        inboxSignatureSettingsPage.changeSignatureText(newSignature)
        inboxSignatureSettingsPage.saveChanges()

        settingsPage.assertSettingsItemDisplayed("Inbox Signature", "Enabled")
        settingsPage.clickOnSettingsItem("Inbox Signature")

        inboxSignatureSettingsPage.assertSignatureText(newSignature)
        inboxSignatureSettingsPage.assertSignatureEnabledState(true)
    }

    override fun displaysPageObjects() = Unit

    abstract fun initData(): MockCanvas

    abstract fun goToInboxSignatureSettings(data: MockCanvas)
}