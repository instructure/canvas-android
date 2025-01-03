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

import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.waitForMatcherWithRefreshes
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.espresso.DoesNotExistAssertion
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.Searchable
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withDescendant
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeDown
import com.instructure.espresso.waitForCheck
import com.instructure.student.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.containsString

open class DiscussionListPage(val searchable: Searchable) : BasePage(R.id.discussionListPage) {

    private val createNewDiscussion by OnViewWithId(R.id.createNewDiscussion)
    private val announcementsRecyclerView by OnViewWithId(R.id.discussionRecyclerView)

    fun waitForDiscussionTopicToDisplay(topicTitle: String) {
        val matcher = allOf(withText(topicTitle), withId(R.id.discussionTitle))
        waitForView(matcher).assertDisplayed()
    }

    fun assertTopicDisplayed(topicTitle: String) {
        val matcher = allOf(withText(topicTitle), withId(R.id.discussionTitle))
        scrollRecyclerView(R.id.discussionRecyclerView, matcher)
        onView(matcher).assertDisplayed()
    }

    fun assertTopicNotDisplayed(topicTitle: String?) {
        onView(allOf(withText(topicTitle))).check(DoesNotExistAssertion(5))
    }

    fun assertEmpty() {
        onView(
            allOf(
                withId(R.id.emptyView),
                withParent(withId(R.id.discussionListPage))
            )
        ).assertDisplayed()
    }

    fun selectTopic(topicTitle: String) {
        val matcher = allOf(withText(topicTitle), withId(R.id.discussionTitle))
        scrollRecyclerView(R.id.discussionRecyclerView, matcher)
        onView(matcher).click()
    }

    fun assertReplyCount(topicTitle: String, count: Int) {
        val matcher = allOf(
                withId(R.id.readUnreadCounts),
                withText(anyOf(containsString("$count Reply"), containsString("$count Replies"))), // Could be "Reply" or "Replies"
                hasSibling(allOf(
                        withId(R.id.discussionTitle),
                        withText(topicTitle)
                )))

        onView(matcher).scrollTo().assertDisplayed()
    }

    fun assertUnreadReplyCount(topicTitle: String, count: Int) {
        val matcher = allOf(withId(R.id.readUnreadCounts), withText(containsString("$count Unread")),
            hasSibling(allOf(withId(R.id.discussionTitle), withText(topicTitle))))

        onView(matcher).scrollTo().assertDisplayed()
    }

    fun assertUnreadCount(topicTitle: String, count: Int) {
        val matcher = allOf(
                withId(R.id.readUnreadCounts),
                withText(containsString("$count Unr")), // "Unread"
                hasSibling(allOf(
                        withId(R.id.discussionTitle),
                        withText(topicTitle)
                )))

        waitForMatcherWithRefreshes(matcher)
        scrollRecyclerView(R.id.discussionRecyclerView, matcher)
        onView(matcher).assertDisplayed() // probably redundant

    }

    fun assertAnnouncementCreated(inputTitle: String) {
            var expectedTitle = inputTitle
            if (inputTitle.isNullOrEmpty()) {
                expectedTitle = InstrumentationRegistry.getInstrumentation().targetContext.resources.getString(R.string.utils_noTitle)
            }
            waitForDiscussionTopicToDisplay(expectedTitle)
    }

    fun launchCreateDiscussionThenClose() {
        createNewDiscussion.click()
        onView(withText(R.string.newDiscussion)).assertDisplayed()
        onView(allOf(withContentDescription(R.string.abc_action_bar_up_description), hasSibling(withText(R.string.newDiscussion)))).click()
    }

    fun launchCreateAnnouncementThenClose() {
        createNewDiscussion.click()
        onView(withText(R.string.newAnnouncement)).assertDisplayed()
        onView(allOf(withContentDescription(R.string.abc_action_bar_up_description), hasSibling(withText(R.string.newAnnouncement)))).click()
    }

    fun verifyExitWithoutSavingDialog() {
        onView(withText(R.string.exitWithoutSavingMessage)).check(matches(isDisplayed()))
    }

    fun pullToUpdate() {
        // I don't think that we need to worry about scrolling to the top first,
        // but we may at some point.
        onView(withId(R.id.discussionRecyclerView)).swipeDown()
    }

    fun assertDiscussionCreationDisabled() {
        onView(withId(R.id.createNewDiscussion)).assertNotDisplayed()
    }

    fun assertOnNewAnnouncementPage() {
        onView(withText(R.string.newAnnouncement)).assertDisplayed()
    }

    fun acceptExitWithoutSaveDialog() {
        onViewWithText(R.string.exitUnsaved).click()
    }

    fun assertHasAnnouncement(discussion: DiscussionTopicHeader) {
        assertHasAnnouncement(discussion.title!!)
    }

    fun assertAnnouncementCount(count: Int) {
        announcementsRecyclerView.waitForCheck(RecyclerViewItemCountAssertion(count))
    }

    fun assertHasAnnouncement(announcementName: String) {
        onView(withText(announcementName)).assertDisplayed()
    }

    fun assertAnnouncementLocked(announcementName: String) {
        val ancestorMatcher = allOf(withId(R.id.discussionLayout), withDescendant(withId(R.id.discussionTitle) + withText(announcementName)))
        onView(allOf(withId(R.id.nestedIcon), withContentDescription(R.string.locked), withAncestor(ancestorMatcher))).assertDisplayed()
    }

    fun assertDueDate(topicTitle: String, expectedDateString: String) {
        val matcher = allOf(withId(R.id.dueDate), withText(containsString(expectedDateString)), hasSibling(allOf(withId(R.id.discussionTitle), withText(topicTitle))))
        onView(matcher).scrollTo().assertDisplayed()
    }
}