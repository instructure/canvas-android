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
package com.instructure.teacher.ui.pages

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvasapi2.models.Attachment
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertVisible
import com.instructure.espresso.clearText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.callOnClick
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.typeText
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
class SpeedGraderCommentsPage : BasePage() {

    private val commentEditText by OnViewWithId(R.id.commentEditText)
    private val sendCommentButton by WaitForViewWithId(R.id.sendCommentButton)
    private val addAttachmentButton by OnViewWithId(R.id.addAttachment)

    /**
     * Asserts the display of the author name.
     *
     * @param name The name of the author.
     */
    fun assertDisplaysAuthorName(name: String) {
        onView(withText(name)).assertVisible()
    }

    /**
     * Asserts the display of the comment text.
     *
     * @param comment The comment text.
     */
    fun assertDisplaysCommentText(comment: String) {
        waitForView(allOf(withId(R.id.commentTextView), withEffectiveVisibility(Visibility.VISIBLE)))
            .assertHasText(comment)
    }

    /**
     * Asserts the display of the comment attachment.
     *
     * @param attachment The attachment representing the comment attachment.
     */
    fun assertDisplaysCommentAttachment(attachment: Attachment) {
        onViewWithId(R.id.attachmentNameTextView).assertHasText(attachment.displayName!!)
    }

    /**
     * Asserts the display of the submission.
     */
    fun assertDisplaysSubmission() {
        onViewWithId(R.id.commentSubmissionAttachmentView).assertDisplayed()
    }

    /**
     * Asserts the display of the submission file.
     *
     * @param attachment The attachment representing the submission file.
     */
    fun assertDisplaysSubmissionFile(attachment: Attachment) {
        val parentMatcher = withParent(withId(R.id.commentSubmissionAttachmentView))
        val match = onView(allOf(parentMatcher, withId(R.id.titleTextView)))
        match.assertHasText(attachment.displayName!!)
    }

    /**
     * Asserts that the comment field has the specified text.
     *
     * @param text The expected text in the comment field.
     */
    fun assertCommentFieldHasText(text: String) {
        commentEditText.assertHasText(text)
    }

    /**
     * Types the specified comment in the comment field.
     *
     * @param comment The comment to type.
     */
    fun typeComment(comment: String) {
        onView(withId(R.id.commentEditText) + withAncestor(R.id.commentInputContainer)).typeText(comment)
    }

    /**
     * Clears the comment field.
     */
    fun clearComment() {
        onView(withId(R.id.commentEditText) + withAncestor(R.id.commentInputContainer)).clearText()
    }

    /**
     * Sends the comment.
     */
    fun sendComment() {
        onView(withId(R.id.sendCommentButton) + withEffectiveVisibility(Visibility.VISIBLE))
            .click()
    }

    /**
     * Clicks on the comment field.
     */
    fun clickCommentField() {
        commentEditText.click()
    }

    /**
     * Asserts the display of the empty state.
     */
    fun assertDisplaysEmptyState() {
        onViewWithText(R.string.no_submission_comments).assertDisplayed()
    }

    /**
     * Adds a comment with the specified text.
     *
     * @param comment The comment text.
     */
    fun addComment(comment: String) {
        commentEditText.typeText(comment)
        Espresso.closeSoftKeyboard()
        callOnClick(withId(R.id.sendCommentButton))
    }

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
