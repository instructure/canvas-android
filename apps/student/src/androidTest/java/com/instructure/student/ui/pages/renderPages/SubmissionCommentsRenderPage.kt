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
package com.instructure.student.ui.pages.renderPages

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.closeSoftKeyboard
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.typeText
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.CommentItemState
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.containsString

class SubmissionCommentsRenderPage: BasePage(R.id.submissionCommentsPage) {

    val recyclerView by OnViewWithId(R.id.recyclerView)
    val commentInput by OnViewWithId(R.id.commentInput)
    val addFileButton by OnViewWithId(R.id.addFileButton)

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

    fun scrollAndAssertDisplayed(matcher: Matcher<View>) {
        onView(allOf(withId(R.id.recyclerView), isDisplayed()))
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(matcher)))
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
        clickInCommentBox()
        commentInput.typeText(comment)
        commentInput.closeSoftKeyboard()
        onView(withId(R.id.sendCommentButton)).click()
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