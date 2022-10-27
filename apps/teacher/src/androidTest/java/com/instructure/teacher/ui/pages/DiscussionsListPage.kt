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

import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.dataseeding.model.DiscussionApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R

class DiscussionsListPage : BasePage() {

    private val discussionListToolbar by OnViewWithId(R.id.discussionListToolbar)
    private val discussionsFAB by OnViewWithId(R.id.createNewDiscussion)
    private val discussionsRecyclerView by OnViewWithId(R.id.discussionRecyclerView)
    private val searchButton by OnViewWithId(R.id.search)
    private val searchInput by WaitForViewWithId(androidx.appcompat.R.id.search_src_text)

    fun clickDiscussion(discussion: DiscussionApiModel) {
        waitForViewWithText(discussion.title).click()
    }

    fun clickDiscussion(discussionTitle: String) {
        waitForViewWithText(discussionTitle).click()
    }

    fun assertHasDiscussion(discussion: DiscussionApiModel) {
        waitForViewWithText(discussion.title).assertDisplayed()
    }

    fun assertHasDiscussion(discussionTitle: String) {
        waitForViewWithText(discussionTitle).assertDisplayed()
    }

    fun assertDiscussionDoesNotExist(discussionTitle: String) {
        onView(withText(discussionTitle)).check(doesNotExist())
    }

    fun assertNoDiscussion() {
        onView(withId(R.id.emptyPandaView)).assertDisplayed()
    }

    fun assertHasDiscussion(discussion: DiscussionTopicHeader) {
        waitForViewWithText(discussion.title!!).assertDisplayed()
    }

    fun openSearch() {
        searchButton.click()
    }

    fun enterSearchQuery(query: String) {
        searchInput.perform(ViewActions.replaceText(query))
    }

    fun assertDiscussionCount(count: Int) {
        discussionsRecyclerView.waitForCheck(RecyclerViewItemCountAssertion(count))
    }

    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout)).swipeDown()
    }

    fun createNewDiscussion() {
        onView(withId(R.id.createNewDiscussion)).click()
    }

    fun toggleCollapseExpandIcon() {
        onView(withId(R.id.collapseIcon)).click()
    }
}
