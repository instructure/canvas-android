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
package com.instructure.teacher.ui.pages.classic

import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.dataseeding.model.DiscussionApiModel
import com.instructure.espresso.DoesNotExistAssertion
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.Searchable
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

/**
 * Represents the Discussions List page.
 */
class DiscussionsListPage(val searchable: Searchable) : BasePage() {

    private val discussionListToolbar by OnViewWithId(R.id.discussionListToolbar)
    private val discussionsFAB by OnViewWithId(R.id.createNewDiscussion)
    private val discussionsRecyclerView by OnViewWithId(R.id.discussionRecyclerView)

    /**
     * Clicks on the specified [discussion] in the discussions list.
     *
     * @param discussion The discussion to be clicked.
     */
    fun clickDiscussion(discussion: DiscussionApiModel) {
        waitForViewWithText(discussion.title).click()
    }

    /**
     * Clicks on the discussion with the specified [discussionTitle] in the discussions list.
     *
     * @param discussionTitle The title of the discussion to be clicked.
     */
    fun clickDiscussion(discussionTitle: String) {
        waitForViewWithText(discussionTitle).click()
    }

    /**
     * Asserts that the discussions list contains the specified [discussion].
     *
     * @param discussion The discussion to be asserted.
     */
    fun assertHasDiscussion(discussion: DiscussionApiModel) {
        waitForViewWithText(discussion.title).assertDisplayed()
    }

    /**
     * Asserts that the discussions list contains a discussion with the specified [discussionTitle].
     *
     * @param discussionTitle The title of the discussion to be asserted.
     */
    fun assertHasDiscussion(discussionTitle: String) {
        waitForViewWithText(discussionTitle).assertDisplayed()
    }

    /**
     * Asserts that the discussion with the specified [discussionTitle] does not exist in the discussions list.
     *
     * @param discussionTitle The title of the discussion to be asserted.
     */
    fun assertDiscussionDoesNotExist(discussionTitle: String) {
        onView(withText(discussionTitle)).check(doesNotExist())
    }

    /**
     * Asserts that no discussions are present in the discussions list.
     */
    fun assertNoDiscussion() {
        onView(withId(R.id.emptyPandaView)).assertDisplayed()
    }

    /**
     * Asserts that a discussion with the specified [discussion] is present in the discussions list.
     *
     * @param discussion The discussion to be asserted.
     */
    fun assertHasDiscussion(discussion: DiscussionTopicHeader) {
        waitForViewWithText(discussion.title!!).assertDisplayed()
    }

    /**
     * Asserts the number of discussions in the discussions list.
     *
     * @param count The expected number of discussions.
     */
    fun assertDiscussionCount(count: Int) {
        discussionsRecyclerView.waitForCheck(RecyclerViewItemCountAssertion(count + 1)) //Because of the header.
    }

    /**
     * Refreshes the discussions list.
     */
    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout)).swipeDown()
    }

    /**
     * Creates a new discussion.
     */
    fun createNewDiscussion() {
        onView(withId(R.id.createNewDiscussion)).click()
    }

    /**
     * Toggles the collapse/expand icon in the discussions list.
     */
    fun toggleCollapseExpandIcon() {
        onView(withId(R.id.collapseIcon)).click()
    }

    /**
     * Clicks on the overflow menu for the discussion with the specified [discussionTitle].
     *
     * @param discussionTitle The title of the discussion.
     */
    fun clickDiscussionOverFlowMenu(discussionTitle: String) {
        waitForView(withId(R.id.discussionOverflow) + ViewMatchers.hasSibling(
            withId(R.id.discussionTitle) + withText(
                discussionTitle
            )
        )
        ).click()
    }

    /**
     * Selects the specified [menuText] from the overflow menu.
     *
     * @param menuText The text of the menu item to be selected.
     */
    fun selectOverFlowMenu(menuText: String) {
        waitForView(withText(menuText) + withParent(R.id.coursePages)).click()
        onView(withText(android.R.string.ok) + withAncestor(R.id.buttonPanel)).click()
    }

    /**
     * Deletes the discussion with the specified [discussionTitle] from the overflow menu.
     *
     * @param discussionTitle The title of the discussion to be deleted.
     */
    fun deleteDiscussionFromOverflowMenu(discussionTitle: String) {
        clickDiscussionOverFlowMenu(discussionTitle)
        selectOverFlowMenu("Delete")
        onView(withId(android.R.id.button1) + withText(R.string.delete)).click()
    }

    /**
     * Asserts that a group with the specified [groupName] is displayed in the discussions list.
     *
     * @param groupName The name of the group to be asserted.
     */
    fun assertGroupDisplayed(groupName: String) {
        waitForView(withId(R.id.groupName) + withText(groupName)).assertDisplayed()
    }

    /**
     * Asserts that a discussion with the specified [discussionTitle] is present in the specified [groupName] group.
     *
     * @param groupName The name of the group containing the discussion.
     * @param discussionTitle The title of the discussion to be asserted.
     */
    fun assertDiscussionInGroup(groupName: String, discussionTitle: String) {
        val groupChildMatcher = withId(R.id.groupName) + withText(groupName)
        waitForView(withId(R.id.discussionTitle) + withText(discussionTitle) +
                withAncestor(withId(R.id.discussionRecyclerView) + withDescendant(groupChildMatcher))).assertDisplayed()
    }

    /**
     * Asserts that a discussion with the specified [discussionTitle] is NOT present in the specified [groupName] group.
     *
     * @param groupName The name of the group NOT containing the discussion.
     * @param discussionTitle The title of the discussion to be asserted.
     */
    fun assertDiscussionNotInGroup(groupName: String, discussionTitle: String) {
        val groupChildMatcher = withId(R.id.groupName) + withText(groupName)
        onView(withId(R.id.discussionTitle) + withText(discussionTitle) +
                withAncestor(withId(R.id.discussionRecyclerView) + withDescendant(groupChildMatcher))).check(DoesNotExistAssertion(5))
    }
}
