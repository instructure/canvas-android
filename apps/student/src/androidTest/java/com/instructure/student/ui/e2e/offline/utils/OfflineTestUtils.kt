/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */
package com.instructure.student.ui.e2e.offline.utils

import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.matchers.WaitForViewMatcher.waitForView
import com.instructure.espresso.page.plus
import com.instructure.student.R
import org.hamcrest.CoreMatchers.allOf

object OfflineTestUtils {

    fun turnOffConnectionViaADB() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.executeShellCommand("svc wifi disable")
        device.executeShellCommand("svc data disable")
    }

    fun turnOnConnectionViaADB() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.executeShellCommand("svc wifi enable")
        device.executeShellCommand("svc data enable")
    }

    fun turnOffConnectionOnUI() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        device.swipe(0, device.displayHeight / 2, 0, 0, 10)

        Thread.sleep(1000)

        val settingsApp = device.findObject(UiSelector().text("Settings"))
        settingsApp.clickAndWaitForNewWindow()

        val scrollable = UiScrollable(UiSelector().scrollable(true))
        scrollable.scrollTextIntoView("Network & internet")

        val networkInternet = device.findObject(UiSelector().text("Network & internet"))
        networkInternet.clickAndWaitForNewWindow()

        val wifiSwitch =
            device.findObject(UiSelector().resourceId("com.android.settings:id/switch_widget"))
        if (wifiSwitch.isChecked) {
            wifiSwitch.click()
        }

        val airplaneMode = device.findObject(UiSelector().text("Airplane mode"))
        airplaneMode.click()

        device.pressHome()
    }

    fun assertOfflineIndicator() {
        waitForView(
            withId(R.id.offlineIndicator) + allOf(
                withChild(withId(R.id.divider)),
                withChild(withId(R.id.offlineIndicatorIcon)),
                withChild(withId(R.id.offlineIndicatorText) + withText(R.string.offline))
            )
        ).assertDisplayed()
    }
}