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
package com.instructure.canvas.espresso.common.pages

import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.pandautils.R
import org.hamcrest.Matchers.allOf
import androidx.media3.ui.R as Media3R

/**
 * Page object for video player interactions.
 * Contains common methods for interacting with both in-app ExoPlayer and external device video players.
 */
class VideoPlayerPage : BasePage() {

    fun assertMediaCommentPreviewDisplayed() {
        onView(allOf(withId(R.id.prepareMediaButton), withParent(R.id.mediaPreviewContainer))).assertDisplayed()
    }

    fun clickPlayButton() {
        onView(withId(R.id.prepareMediaButton)).click()
    }

    fun assertPlayPauseButtonDisplayed() {
        onView(withId(Media3R.id.exo_play_pause)).assertDisplayed()
    }

    fun clickPlayPauseButton() {
        onView(withId(Media3R.id.exo_play_pause)).click()
    }

    /**
     * Waits until mediaProgressBar is no longer visible, confirming the video has finished
     * loading and started playing. Once loading completes, taps the screen center to reveal
     * the player controls.
     */
    fun waitForVideoToStart(device: UiDevice) {
        device.wait(Until.gone(By.res(device.currentPackageName, "mediaProgressBar")), 15000L)
        device.click(device.displayWidth / 2, device.displayHeight / 2)
    }

    /**
     * Waits until the ExoPlayer's player_view is visible (video opened directly from Files),
     * then taps the screen center to reveal the player controls.
     */
    fun waitForPlayerViewAndTapToShowControls(device: UiDevice) {
        device.wait(Until.findObject(By.res(device.currentPackageName, "player_view")), 15000L)
        device.click(device.displayWidth / 2, device.displayHeight / 2)
    }

}