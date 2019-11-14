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

import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.dataseeding.model.ConversationApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithId
import com.instructure.student.R

class InboxPage : BasePage(R.id.inboxPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val createMessageButton by OnViewWithId(R.id.addMessage)
    private val filterButton by OnViewWithId(R.id.filterButton)

    fun assertConversationDisplayed(conversation: ConversationApiModel) {
        assertConversationDisplayed(conversation.subject)
    }

    fun assertConversationDisplayed(subject: String) {
        val matcher = withText(subject)
        scrollRecyclerView(R.id.inboxRecyclerView, matcher)
        onView(matcher).assertDisplayed()
    }

    fun selectConversation(conversation: ConversationApiModel) {
        val matcher = withText(conversation.subject)
        scrollRecyclerView(R.id.inboxRecyclerView, matcher)
        onView(matcher).click()
    }

    fun selectInboxFilter(scope: InboxApi.Scope) {
        filterButton.click()
        when (scope) {
            InboxApi.Scope.ALL -> onViewWithId(R.id.inbox_all).click()
            InboxApi.Scope.UNREAD -> onViewWithId(R.id.inbox_unread).click()
            InboxApi.Scope.ARCHIVED -> onViewWithId(R.id.inbox_starred).click()
            InboxApi.Scope.STARRED -> onViewWithId(R.id.inbox_sent).click()
            InboxApi.Scope.SENT -> onViewWithId(R.id.inbox_archived).click()
        }
    }

    fun pressNewMessageButton() {
        createMessageButton.click()
    }

    fun goToDashboard() {
        onView(withId(R.id.bottomNavigationCourses)).click()
    }
}