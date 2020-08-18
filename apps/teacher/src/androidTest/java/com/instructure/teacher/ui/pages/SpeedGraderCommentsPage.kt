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
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvasapi2.models.Attachment
import com.instructure.dataseeding.model.AttachmentApiModel
import com.instructure.espresso.*
import com.instructure.espresso.matchers.WaitForViewMatcher
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.callOnClick
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.onViewWithText
import com.instructure.teacher.R
import org.hamcrest.Matchers

class SpeedGraderCommentsPage : BasePage() {

    private val commentEditText by OnViewWithId(R.id.commentEditText)
    private val sendCommentButton by WaitForViewWithId(R.id.sendCommentButton)

    fun assertDisplaysAuthorName(name: String) {
        onView(withText(name)).assertVisible()
    }

    fun assertDisplaysCommentText(comment: String) {
        WaitForViewMatcher.waitForView(Matchers.allOf(ViewMatchers.withId(R.id.commentTextView), ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                .assertHasText(comment)
    }

    fun assertDisplaysCommentAttachment(attachment: Attachment) {
        onViewWithId(R.id.attachmentNameTextView).assertHasText(attachment.displayName!!)
    }

    fun assertDisplaysSubmission() {
        onViewWithId(R.id.commentSubmissionAttachmentView).assertDisplayed()
    }

    fun assertDisplaysSubmissionFile(attachment: Attachment) {
        val parentMatcher = ViewMatchers.withParent(ViewMatchers.withId(R.id.commentSubmissionAttachmentView))
        val match = Espresso.onView(Matchers.allOf(parentMatcher, ViewMatchers.withId(R.id.titleTextView)))
        match.assertHasText(attachment.displayName!!)
    }

    fun addComment(comment: String) {
        commentEditText.replaceText(comment)
        callOnClick(withId(R.id.sendCommentButton))
    }

    fun assertDisplaysEmptyState() {
        onViewWithText(R.string.no_submission_comments).assertDisplayed()
    }
}
