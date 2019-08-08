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
 *
 */
package com.instructure.student.ui.pages

import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.OnViewWithStringTextIgnoreCase
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.student.R
import com.instructure.student.ui.pages.renderPages.SubmissionCommentsRenderPage
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.containsString

open class SubmissionDetailsPage : BasePage(R.id.submissionDetails) {
    private val commentsButton by OnViewWithStringTextIgnoreCase("comments")

    private val submissionCommentsRenderPage = SubmissionCommentsRenderPage()

    fun openComments() {
        commentsButton.click()
    }

    /**
     * Assert that a comment is displayed
     * [description] contains some text that is in the comment
     * [user] is the author of the comment
     */
    fun assertCommentDisplayed(description: String, user: CanvasUserApiModel) {
        val commentMatcher = allOf(
                withId(R.id.commentHolder),
                hasDescendant(allOf(withText(user.shortName), withId(R.id.userNameTextView))),
                hasDescendant(allOf(withText(containsString(description)), anyOf(withId(R.id.titleTextView), withId(R.id.commentTextView))))
        )

        submissionCommentsRenderPage.scrollAndAssertDisplayed(commentMatcher)
    }

    fun addAndSendComment(comment: String) {
        submissionCommentsRenderPage.addAndSendComment(comment)
    }
}
