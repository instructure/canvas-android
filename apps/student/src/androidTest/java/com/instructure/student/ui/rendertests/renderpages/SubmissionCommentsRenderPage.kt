/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.rendertests.renderpages

import android.os.SystemClock.sleep
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.DirectlyPopulateEditText
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertGone
import com.instructure.espresso.assertVisible
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.scrollTo
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.CommentItemState
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.containsString

class SubmissionCommentsRenderPage: BasePage(R.id.submissionCommentsPage) {

    val recyclerView by OnViewWithId(R.id.recyclerView)
    val commentInput by OnViewWithId(R.id.commentInput)
    val addAttachmentButton by OnViewWithId(R.id.addFileButton)

    fun verifyDisplaysEmptyState() {
        onViewWithText(R.string.emptySubmissionCommentsSubtext).assertDisplayed()
    }

    fun verifyCommentPresent(commentState: CommentItemState) {
        // Make sure that the message is on the screen
        val message = getMessage(commentState)
        val messageMatcher = allOf(withText(containsString(message)), anyOf(withId(R.id.commentTextView), withId(R.id.subtitleTextView)))
        scrollAndAssertDisplayed(messageMatcher)

        // Make sure that the comment date is on the screen
        val dateString = getDateString(commentState)
        val dateMatcher = allOf(withText(containsString(dateString)), withId(R.id.commentDateTextView))
        scrollAndAssertDisplayed(dateMatcher)
    }

    fun verifyPendingCommentPresent(commentState: CommentItemState, failed: Boolean) {
        // Make sure that the message is on the screen
        val message = getMessage(commentState)
        val messageMatcher = allOf(withText(containsString(message)), anyOf(withId(R.id.commentTextView), withId(R.id.subtitleTextView)))
        onView(messageMatcher).scrollTo().assertDisplayed()

        if (failed) {
            // Make sure the fail message is on the screen and the sending layout is gone
            scrollAndAssertDisplayed(withText(R.string.submissionCommentErrorMessage))
            onViewWithId(R.id.sendingLayout).assertGone()
        } else {
            // Make sure the sending layout is on the screen and the error layout is gone
            scrollAndAssertDisplayed(withId(R.id.sendingLayout))
            onViewWithId(R.id.errorLayout).assertGone()
        }
    }

    fun verifyDisplaysPronoun(commentState: CommentItemState) {
        // Make sure that the author name and pronoun are on the screen
        val nameWithPronoun = getAuthorNameAndPronoun(commentState)
        val nameMatcher = allOf(withText(nameWithPronoun.toString()), withId(R.id.userNameTextView))
        scrollAndAssertDisplayed(nameMatcher)
    }

    fun verifyDisplaysRetryOptions() {
        onViewWithId(R.id.errorLayout).click()
        onViewWithText(R.string.retry).assertVisible()
        onViewWithText(R.string.delete).assertVisible()
    }

    private fun getMessage(commentState: CommentItemState) : String? {
        return when(commentState) {
            is CommentItemState.CommentItem -> commentState.message
            is CommentItemState.SubmissionItem -> commentState.submission.body
            is CommentItemState.PendingCommentItem -> commentState.pendingComment.message
            else -> throw IllegalStateException("Unknown comment item state type ${commentState::class.java.simpleName}")
        }
    }

    private fun getDateString(commentState: CommentItemState) : String? {
        return when(commentState) {
            is CommentItemState.CommentItem -> commentState.dateText
            is CommentItemState.SubmissionItem -> commentState.dateText
            is CommentItemState.PendingCommentItem -> commentState.sortDate.toLocaleString() // Probably wrong.  Fix this when we actually test pending comments.
            else -> throw IllegalStateException("Unknown comment item state type ${commentState::class.java.simpleName}")
        }
    }

    private fun getAuthorNameAndPronoun(commentState: CommentItemState) : CharSequence {
        val (name, pronoun) = when(commentState) {
            is CommentItemState.CommentItem -> commentState.authorName to commentState.authorPronouns
            is CommentItemState.PendingCommentItem -> commentState.authorName to commentState.authorPronouns
            is CommentItemState.SubmissionItem -> commentState.authorName to commentState.authorPronouns
            else -> throw IllegalStateException("Unknown comment item state type ${commentState::class.java.simpleName}")
        }
        return Pronouns.span(name, pronoun)
    }

    fun scrollAndAssertDisplayed(matcher: Matcher<View>) {
        scrollRecyclerView(R.id.recyclerView, matcher)
        onView(matcher).assertDisplayed()
    }

    fun getCommentLeftOffset(commentState: CommentItemState): Int {
        val message = getMessage(commentState)
        val messageMatcher = allOf(withText(containsString(message)), anyOf(withId(R.id.commentTextView), withId(R.id.subtitleTextView)))
        scrollAndAssertDisplayed(messageMatcher)
        val resultArray = arrayOf(0)
        onView(messageMatcher).perform(GetViewLeftOffset(resultArray))
        return resultArray[0]
    }

    fun clickInCommentBox() {
        commentInput.click()
    }

    fun addAndSendComment(comment: String) {
        // When we're in landscape mode (or any short-screen mode, I suppose), clicking on
        // commentInput will bring up a system dialog for text entry (??), and the test is hosed
        // at that point.  So we'll bypass espresso and populate the text directly.
        commentInput.perform(DirectlyPopulateEditText(comment))
        onView(withId(R.id.sendCommentButton)).click()
    }

    fun addAndSendVideoComment() {
        clickOnAddAttachmentButton()
        onView(withId(R.id.videoComment)).click()
        onView(allOf(withId(R.id.startRecordingButton), isDisplayed())).click()
        sleep(3000)
        onView(allOf(withId(R.id.endRecordingButton), isDisplayed())).click()
        onView(allOf(withId(R.id.sendButton), isDisplayed())).click()
    }

    fun addAndSendAudioComment() {
        clickOnAddAttachmentButton()
        onView(withId(R.id.audioComment)).click()
        onView(allOf(withId(R.id.recordAudioButton), isDisplayed())).click()
        sleep(3000)
        onView(allOf(withId(R.id.stopButton), isDisplayed())).click()
        onView(allOf(withId(R.id.sendAudioButton), isDisplayed())).click()
    }

    fun clickOnAddAttachmentButton() {
        addAttachmentButton.click()
    }

}

// Custom action to get the left offset of a view and deposit it in the
// first element of the output array.
class GetViewLeftOffset(val output: Array<Int>) : ViewAction {
    override fun getDescription(): String {
        return "Grab left offset of view"
    }

    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(View::class.java)
    }

    override fun perform(uiController: UiController?, view: View?) {
        output[0] = view!!.left
    }
}

