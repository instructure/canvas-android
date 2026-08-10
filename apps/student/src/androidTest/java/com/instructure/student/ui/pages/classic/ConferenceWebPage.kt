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
 *
 */
package com.instructure.student.ui.pages.classic

import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.util.regex.Pattern

private const val WEB_TIMEOUT = 13_000L
private const val SHORT_TIMEOUT = 8_000L
private const val CHOOSER_TIMEOUT = 3_000L
private const val CHROME_EDIT = "android.widget.EditText"
private const val STUDENT_PACKAGE_FRAGMENT = "candroid"

/**
 * Drives the conferences web page that the "Open Externally" button opens in a Chrome Custom Tab.
 *
 * A Custom Tab runs in a separate process, so Espresso (and espresso-web) cannot reach it. All
 * interactions here go through UI Automator, matching Chrome's rendered web content by text/class
 * only (web content has no Android resource-ids).
 */
class ConferenceWebPage(private val device: UiDevice) {

    private var firstRunHandled = false

    fun waitForPageLoaded() {
        selectChromeFromChooserIfPresent()
        if (!firstRunHandled) {
            dismissChromeFirstRunIfPresent()
            firstRunHandled = true
        }
        val anchor = device.wait(Until.findObject(By.textContains("Conference")), WEB_TIMEOUT)
        checkNotNull(anchor) { "The conferences web page did not load in the Custom Tab within ${WEB_TIMEOUT}ms." }
        dismissTestInstallationBannerIfPresent()
        device.waitForIdle()
    }

    fun clickAddConference() {
        findClickable("New Conference", WEB_TIMEOUT).click()
        device.waitForIdle()
    }

    fun assertCreateModalDisplayed() {
        requirePresent("New Video Conference", SHORT_TIMEOUT, "the 'New Video Conference' modal title")
        requirePresent("BigBlueButton", SHORT_TIMEOUT, "the 'BigBlueButton' conference type")
        requireObject(By.clazz(CHROME_EDIT), SHORT_TIMEOUT, "the conference name input field")
        requirePresent("Create", SHORT_TIMEOUT, "the 'Create' button")
        requirePresent("Cancel", SHORT_TIMEOUT, "the 'Cancel' button")
    }

    fun setConferenceName(name: String) {
        val nameField = device.findObjects(By.clazz(CHROME_EDIT)).minByOrNull { it.visibleBounds.top }
        checkNotNull(nameField) { "Could not find the conference name input field in the modal." }
        nameField.text = name
        device.waitForIdle()
    }

    fun clickCreate() {
        findClickable("Create", SHORT_TIMEOUT).click()
        device.waitForIdle()
    }

    fun assertConferenceInWebList(conferenceName: String) {
        requireObject(By.textContains(conferenceName), WEB_TIMEOUT, "conference '$conferenceName' in the web list")
        requireObject(By.textContains("New Conferences"), SHORT_TIMEOUT, "the 'New Conferences' section header")
    }

    fun clickStart() {
        findClickable("Start", WEB_TIMEOUT).click()
        Thread.sleep(1000)
    }

    fun assertJoinPageDisplayed() {
        selectChromeFromChooserIfPresent()
        requirePresent("BigBlueButton", WEB_TIMEOUT, "the BigBlueButton page in the Custom Tab")
    }

    fun clickEnd() {
        findClickable("End", WEB_TIMEOUT).click()
        device.waitForIdle()
    }

    fun confirmEndDialog() {
        val ok = device.wait(Until.findObject(By.text(Pattern.compile("OK", Pattern.CASE_INSENSITIVE))), SHORT_TIMEOUT)
            ?: device.wait(Until.findObject(By.textContains("OK")), SHORT_TIMEOUT)
        checkNotNull(ok) { "Could not find the 'OK' button on the end-conference confirmation dialog." }
        ok.click()
        device.waitForIdle()
    }

    fun closeCustomTab() {
        // Returning from the multi-page Custom Tab (conferences list -> BBB page) may take a few
        // back presses; stop as soon as the Student app is in the foreground again.
        repeat(3) {
            device.pressBack()
            device.waitForIdle()
            if (isBackInApp()) return
        }
        Thread.sleep(3000) // give the Custom Tab a chance to close and return to the app
    }

    private fun isBackInApp(): Boolean {
        return device.currentPackageName?.contains(STUDENT_PACKAGE_FRAGMENT) == true
    }

    private fun selectChromeFromChooserIfPresent() {
        // asChooserExcludingInstructure() can surface an "Open with" chooser before the page loads.
        // Best-effort: if a Chrome entry is shown in a chooser, select it. May need tuning per CI image.
        val chromeEntry = device.wait(Until.findObject(By.text(Pattern.compile("Chrome", Pattern.CASE_INSENSITIVE))), CHOOSER_TIMEOUT)
        if (chromeEntry != null && device.currentPackageName?.contains("chrome") != true) {
            chromeEntry.click()
            device.findObject(By.text(Pattern.compile("Just once", Pattern.CASE_INSENSITIVE)))?.click()
            device.waitForIdle()
        }
    }

    private fun dismissChromeFirstRunIfPresent() {
        // A freshly-installed Chrome (e.g. on the CI emulator) shows a first-run / welcome flow that
        // blocks the page. Click through whichever known buttons appear, for a handful of screens.
        val firstRunLabels = listOf(
            "Continue as",            // sign in with the pre-populated account (accepts the inline Terms of Service)
            "Accept & continue",      // standalone Terms of Service step, if shown
            "No thanks",              // "Turn on sync?" prompt
            "Not now",                // misc prompts
            "Don't allow",            // notifications permission dialog
            "Got it"
        )
        repeat(5) {
            val button = waitForFirstPresent(firstRunLabels) ?: return
            button.click()
            device.waitForIdle()
        }
    }

    private fun waitForFirstPresent(labels: List<String>, attempts: Int = 10, intervalMs: Long = 500): UiObject2? {
        repeat(attempts) {
            labels.firstNotNullOfOrNull { device.findObject(By.textContains(it)) }?.let { return it }
            Thread.sleep(intervalMs)
        }
        return null
    }

    private fun dismissTestInstallationBannerIfPresent() {
        val close = device.findObject(By.descContains("Close warning"))
            ?: device.findObject(By.textContains("Close warning"))
            ?: return
        close.click()
        device.waitForIdle()
    }

    private fun requireObject(selector: BySelector, timeout: Long, what: String): UiObject2 {
        val obj = device.wait(Until.findObject(selector), timeout)
        return checkNotNull(obj) { "Could not find $what in the Custom Tab within ${timeout}ms." }
    }

    private fun findClickable(label: String, timeout: Long): UiObject2 {
        // Canvas web action buttons carry their label in content-desc (from aria-label) with an empty
        // text node, while other elements use text. Prefer exact matches, then fall back to substrings.
        val obj = device.wait(Until.findObject(By.desc(label)), timeout)
            ?: device.findObject(By.text(label))
            ?: device.findObject(By.descContains(label))
            ?: device.findObject(By.textContains(label))
        return checkNotNull(obj) { "Could not find clickable '$label' (by content-desc or text) in the Custom Tab within ${timeout}ms." }
    }

    private fun requirePresent(label: String, timeout: Long, what: String) {
        val obj = device.wait(Until.findObject(By.textContains(label)), timeout)
            ?: device.findObject(By.descContains(label))
        checkNotNull(obj) { "Could not find $what in the Custom Tab within ${timeout}ms." }
    }
}