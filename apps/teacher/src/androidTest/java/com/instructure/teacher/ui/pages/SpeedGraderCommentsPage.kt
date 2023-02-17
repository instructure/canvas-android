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
import androidx.test.espresso.matcher.ViewMatchers.withParent
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvasapi2.models.Attachment
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import org.hamcrest.Matchers.allOf

class SpeedGraderCommentsPage : BasePage() {

    private val commentEditText by OnViewWithId(R.id.commentEditText)
    private val sendCommentButton by WaitForViewWithId(R.id.sendCommentButton)
    private val addAttachmentButton by OnViewWithId(R.id.addAttachment)

    fun assertDisplaysAuthorName(name: String) {
        onView(withText(name)).assertVisible()
    }

    fun assertDisplaysCommentText(comment: String) {
        waitForView(allOf(withId(R.id.commentTextView), withEffectiveVisibility(Visibility.VISIBLE)))
                .assertHasText(comment)
    }

    fun assertDisplaysCommentAttachment(attachment: Attachment) {
        onViewWithId(R.id.attachmentNameTextView).assertHasText(attachment.displayName!!)
    }

    fun assertDisplaysSubmission() {
        onViewWithId(R.id.commentSubmissionAttachmentView).assertDisplayed()
    }

    fun assertDisplaysSubmissionFile(attachment: Attachment) {
        val parentMatcher = withParent(withId(R.id.commentSubmissionAttachmentView))
        val match = onView(allOf(parentMatcher, withId(R.id.titleTextView)))
        match.assertHasText(attachment.displayName!!)
    }

    fun assertCommentFieldHasText(text: String) {
        commentEditText.assertHasText(text)
    }

    fun typeComment(comment: String) {
        onView(withId(R.id.commentEditText) + withAncestor(R.id.commentInputContainer)).typeText(comment)
    }

    fun clearComment() {
        onView(withId(R.id.commentEditText) + withAncestor(R.id.commentInputContainer)).clearText()
    }

    fun sendComment() {
        onView(withId(R.id.sendCommentButton) + withEffectiveVisibility(Visibility.VISIBLE))
            .click()
    }

    fun clickCommentField() {
        commentEditText.click()
    }

    fun assertDisplaysEmptyState() {
        onViewWithText(R.string.no_submission_comments).assertDisplayed()
    }

    fun addComment(comment: String) {
        commentEditText.typeText(comment)
        Espresso.closeSoftKeyboard()
        callOnClick(withId(R.id.sendCommentButton))
    }

    fun sendVideoComment() {
        clickOnAttachmentButton()
        onView(allOf(withId(R.id.videoText), withText(R.string.addVideoComment))).click()
        waitForView(withId(R.id.startRecordingButton)).click()
        Thread.sleep(3000) // Let the video recording go for a bit
        waitForView(withId(R.id.endRecordingButton)).click()
        waitForView(withId(R.id.sendButton)).click()
    }

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

    fun assertVideoCommentDisplayed() {
        val videoCommentMatcher = allOf(
            withId(R.id.commentHolder),
            hasDescendant(allOf(containsTextCaseInsensitive("video"), withId(R.id.attachmentNameTextView)))
        )

        waitForView(videoCommentMatcher).scrollTo().assertDisplayed()
    }

    fun assertAudioCommentDisplayed() {
        val audioCommentMatcher = allOf(
            withId(R.id.commentHolder),
            hasDescendant(allOf(containsTextCaseInsensitive("audio"), withId(R.id.attachmentNameTextView)))
        )

        waitForView(audioCommentMatcher).scrollTo().assertDisplayed()
    }

    fun clickOnAudioComment() {
        waitForView(allOf(withId(R.id.attachmentNameTextView), withText(R.string.mediaUploadAudio))).click()
    }

    fun clickOnVideoComment() {
        waitForView(allOf(withId(R.id.attachmentNameTextView), withText(R.string.mediaUploadVideo))).click()
    }

    fun assertMediaCommentPreviewDisplayed() {
        onView(allOf(withId(R.id.prepareMediaButton), withParent(R.id.mediaPreviewContainer))).assertDisplayed()
    }

}
