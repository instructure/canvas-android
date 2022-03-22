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
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import com.instructure.canvasapi2.models.Attachment
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf

class SpeedGraderCommentsPage : BasePage() {

    private val commentEditText by OnViewWithId(R.id.commentEditText)
    private val sendCommentButton by WaitForViewWithId(R.id.sendCommentButton)

    fun assertDisplaysAuthorName(name: String) {
        onView(withText(name)).assertVisible()
    }

    fun assertDisplaysCommentText(comment: String) {
        waitForView(Matchers.allOf(withId(R.id.commentTextView), withEffectiveVisibility(Visibility.VISIBLE)))
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

    fun addComment(comment: String) {
        commentEditText.typeText(comment)
        Espresso.closeSoftKeyboard()
        callOnClick(withId(R.id.sendCommentButton))
    }

    fun typeComment(comment: String) {
        onView(withId(R.id.commentEditText) + withAncestor(R.id.commentInputContainer)).typeText(comment)
    }

    fun focusOnCommentEditTextField() {
        commentEditText.click()
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
}
