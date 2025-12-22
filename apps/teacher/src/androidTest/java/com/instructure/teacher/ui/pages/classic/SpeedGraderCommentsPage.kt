/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.ui.pages.classic

import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R
import org.hamcrest.Matchers.allOf

/**
 * Represents the SpeedGrader comments page.
 *
 * This page provides functionality for interacting with the elements on the SpeedGrader comments page. It contains methods
 * for asserting the display of author name, comment text, comment attachment, submission, comment field text, and empty state.
 * It also provides methods for typing a comment, clearing a comment, sending a comment, clicking on the comment field,
 * adding a comment, sending a video comment, sending an audio comment, and asserting the display of video and audio comments.
 * This page extends the BasePage class.
 */
class SpeedGraderCommentsPage : BasePage() { // TODO: We’re still using these tests elsewhere, but that test is stubbed as well, so once we un-stub it, we can regroup these and delete this class.

    private val commentEditText by OnViewWithId(R.id.commentEditText)
    private val sendCommentButton by WaitForViewWithId(R.id.sendCommentButton)
    private val addAttachmentButton by OnViewWithId(R.id.addAttachment)

    /**
     * Sends a video comment.
     */
    fun sendVideoComment() {
        clickOnAttachmentButton()
        onView(allOf(withId(R.id.videoText), withText(R.string.addVideoComment))).click()
        waitForView(withId(R.id.startRecordingButton)).click()
        Thread.sleep(3000) // Let the video recording go for a bit
        waitForView(withId(R.id.endRecordingButton)).click()
        waitForView(withId(R.id.sendButton)).click()
    }

    /**
     * Sends an audio comment.
     */
    fun sendAudioComment() {
        clickOnAttachmentButton()
        onView(allOf(withId(R.id.audioText), withText(R.string.addAudioComment))).click()
        waitForView(withId(R.id.recordAudioButton)).click()
        Thread.sleep(3000) // Let the audio recording go for a bit
        waitForView(withId(R.id.stopButton)).click()
        waitForView(withId(R.id.sendAudioButton)).click()
    }

    private fun clickOnAttachmentButton() {
        addAttachmentButton.click()
    }

    /**
     * Asserts the display of a video comment.
     */
    fun assertVideoCommentDisplayed() {
        val videoCommentMatcher = allOf(
            withId(R.id.commentHolder),
            hasDescendant(allOf(containsTextCaseInsensitive("video"), withId(R.id.attachmentNameTextView)))
        )

        waitForView(videoCommentMatcher).scrollTo().assertDisplayed()
    }

    /**
     * Asserts the display of an audio comment.
     */
    fun assertAudioCommentDisplayed() {
        val audioCommentMatcher = allOf(
            withId(R.id.commentHolder),
            hasDescendant(allOf(containsTextCaseInsensitive("audio"), withId(R.id.attachmentNameTextView)))
        )

        waitForView(audioCommentMatcher).scrollTo().assertDisplayed()
    }

    /**
     * Clicks on an audio comment.
     */
    fun clickOnAudioComment() {
        waitForView(allOf(withId(R.id.attachmentNameTextView), withText(R.string.mediaUploadAudio))).click()
    }

    /**
     * Clicks on a video comment.
     */
    fun clickOnVideoComment() {
        waitForView(allOf(withId(R.id.attachmentNameTextView), withText(R.string.mediaUploadVideo))).click()
    }

    /**
     * Asserts the display of the media comment preview.
     */
    fun assertMediaCommentPreviewDisplayed() {
        onView(allOf(withId(R.id.prepareMediaButton), withParent(R.id.mediaPreviewContainer))).assertDisplayed()
    }

}
