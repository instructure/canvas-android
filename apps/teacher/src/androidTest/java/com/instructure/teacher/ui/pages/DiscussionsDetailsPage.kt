/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.teacher.ui.pages

import com.instructure.espresso.ModuleItemInteractions
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withId
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeDown
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TypeInRCETextEditor

class DiscussionsDetailsPage(val moduleItemInteractions: ModuleItemInteractions) : BasePage() {

    /**
     * Asserts that the discussion has the specified [title].
     *
     * @param title The title of the discussion to be asserted.
     */
    fun assertDiscussionTitle(title: String) {
        onView(withId(R.id.discussionTopicTitle)).assertHasText(title)
    }

    /**
     * Asserts that the discussion is published.
     */
    fun assertDiscussionPublished() {
        checkPublishedTextView("Published")
    }

    /**
     * Asserts that the discussion is unpublished.
     */
    fun assertDiscussionUnpublished() {
        checkPublishedTextView("Unpublished")
    }

    /**
     * Asserts that there are no replies in the discussion.
     */
    fun assertNoReplies() {
        onView(withId(R.id.discussionTopicReplies)).assertNotDisplayed()
    }

    /**
     * Asserts that the discussion has at least one reply.
     */
    fun assertHasReply() {
        val repliesHeader = onView(withId(R.id.discussionTopicReplies))
        repliesHeader.scrollTo()
        repliesHeader.assertDisplayed()
    }

    /**
     * Opens the edit menu of the discussion.
     */
    fun openEdit() {
        onView(withId(R.id.menu_edit)).click()
    }

    /**
     * Refreshes the discussion page.
     */
    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout)).swipeDown()
    }

    /**
     * Adds a reply with the specified [content] to the discussion.
     *
     * @param content The content of the reply.
     */
    fun addReply(content: String) {
        onView(withId(R.id.replyToDiscussionTopic)).click()
        onView(withId(R.id.rce_webView)).perform(TypeInRCETextEditor(content))
        onView(withId(R.id.menu_send)).click()
    }

    private fun checkPublishedTextView(status: String) {
        onView(withId(R.id.publishStatusTextView)).assertHasText(status)
    }
}