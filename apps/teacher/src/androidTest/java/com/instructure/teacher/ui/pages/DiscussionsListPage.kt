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
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.dataseeding.model.DiscussionApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withDescendant
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.swipeDown
import com.instructure.espresso.waitForCheck
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

    fun clickDiscussionOverFlowMenu(discussionTitle: String) {
        waitForView(withId(R.id.discussionOverflow) + ViewMatchers.hasSibling(
            withId(R.id.discussionTitle) + withText(
                discussionTitle
            )
        )
        ).click()
    }

    fun selectOverFlowMenu(menuText: String) {
        waitForView(withText(menuText) + withParent(R.id.coursePages)).click()
        onView(withText(R.string.ok) + withAncestor(R.id.buttonPanel)).click()
    }

    fun deleteDiscussionFromOverflowMenu(discussionTitle: String) {
        clickDiscussionOverFlowMenu(discussionTitle)
        selectOverFlowMenu("Delete")
        onView(withId(android.R.id.button1) + withText(R.string.delete)).click()
    }

    fun assertGroupDisplayed(groupName: String) {
        waitForView(withId(R.id.groupName) + withText(groupName)).assertDisplayed()
    }

    fun assertDiscussionInGroup(groupName: String, discussionTitle: String) {
        val groupChildMatcher = withId(R.id.groupName) + withText(groupName)
        waitForView(withId(R.id.discussionTitle) + withText(discussionTitle) +
                withAncestor(withId(R.id.discussionRecyclerView) + withDescendant(groupChildMatcher))).assertDisplayed()
    }
}
