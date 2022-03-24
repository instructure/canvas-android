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

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers.*
import com.instructure.canvasapi2.models.Attachment
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import org.hamcrest.Matchers.allOf

class SpeedGraderCommentsPage : BasePage() {

    private val commentEditText by OnViewWithId(R.id.commentEditText)
    private val sendCommentButton by WaitForViewWithId(R.id.sendCommentButton)

    companion object {
        const val ACTION_TAG = "SpeedGraderCommentsPage #ACTION# "
        const val ASSERTION_TAG = "SpeedGraderCommentsPage #ASSERT# "
    }

    fun assertDisplaysAuthorName(name: String) {
        Log.d(ASSERTION_TAG, "Assert author name is $name and visible.")
        onView(withText(name)).assertVisible()
    }

    fun assertDisplaysCommentText(comment: String) {
        Log.d(ASSERTION_TAG, "Assert comment text view has comment: $comment")
        waitForView(allOf(withId(R.id.commentTextView), withEffectiveVisibility(Visibility.VISIBLE)))
                .assertHasText(comment)
    }

    fun assertDisplaysCommentAttachment(attachment: Attachment) {
        Log.d(ASSERTION_TAG, "Assert ${attachment.displayName} is displayed as a comment.")
        onViewWithId(R.id.attachmentNameTextView).assertHasText(attachment.displayName!!)
    }

    fun assertDisplaysSubmission() {
        Log.d(ASSERTION_TAG, "Assert submission (attachment view) is displayed.")
        onViewWithId(R.id.commentSubmissionAttachmentView).assertDisplayed()
    }

    fun assertDisplaysSubmissionFile(attachment: Attachment) {
        val parentMatcher = withParent(withId(R.id.commentSubmissionAttachmentView))
        val match = onView(allOf(parentMatcher, withId(R.id.titleTextView)))
        Log.d(ASSERTION_TAG, "Assert that submission displays the following file as an attachment: ${attachment.displayName}")
        match.assertHasText(attachment.displayName!!)
    }

    fun assertCommentFieldHasText(text: String) {
        Log.d(ASSERTION_TAG, "Assert that comment edit field has text: $text")
        commentEditText.assertHasText(text)
    }

    fun typeComment(comment: String) {
        Log.d(ACTION_TAG,"Type $comment into comment edit text field.")
        onView(withId(R.id.commentEditText) + withAncestor(R.id.commentInputContainer)).typeText(comment)
    }

    fun clearComment() {
        Log.d(ACTION_TAG,"Clear edit text field.")
        onView(withId(R.id.commentEditText) + withAncestor(R.id.commentInputContainer)).clearText()
    }

    fun sendComment() {
        Log.d(ACTION_TAG,"Click on send comment button.")
        onView(withId(R.id.sendCommentButton) + withEffectiveVisibility(Visibility.VISIBLE))
            .click()
    }

    fun clickCommentField() {
        Log.d(ACTION_TAG,"Click on comment edit text field.")
        commentEditText.click()
    }

    fun assertDisplaysEmptyState() {
        Log.d(ASSERTION_TAG, "Assert empty state is displayed.")
        onViewWithText(R.string.no_submission_comments).assertDisplayed()
    }

    fun addComment(comment: String) {
        Log.d(ACTION_TAG, "Type $comment into the comment edit text.")
        commentEditText.typeText(comment)
        Log.d(ACTION_TAG, "Close soft keyboard.")
        Espresso.closeSoftKeyboard()
        Log.d(ACTION_TAG, "Click on send comment button.")
        callOnClick(withId(R.id.sendCommentButton))
    }

}
