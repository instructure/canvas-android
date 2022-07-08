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

import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvas.espresso.DirectlyPopulateEditText
import com.instructure.canvas.espresso.explicitClick
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.waitForMatcherWithRefreshes
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.student.R
import com.instructure.student.ui.utils.TypeInRCETextEditor
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

class DiscussionListPage : BasePage(R.id.discussionListPage) {

    private val createNewDiscussion by OnViewWithId(R.id.createNewDiscussion)
    private val announcementsRecyclerView by OnViewWithId(R.id.discussionRecyclerView)

    fun waitForDiscussionTopicToDisplay(topicTitle: String) {
        val matcher = allOf(withText(topicTitle), withId(R.id.discussionTitle))
        waitForView(matcher)

    }

    fun assertTopicDisplayed(topicTitle: String) {
        val matcher = allOf(withText(topicTitle), withId(R.id.discussionTitle))
        scrollRecyclerView(R.id.discussionRecyclerView, matcher)
        onView(matcher).assertDisplayed()
    }

    fun assertTopicNotDisplayed(topicTitle: String?) {
        onView(allOf(withText(topicTitle))).check(ViewAssertions.doesNotExist())
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
                withText(containsString("$count Repl")), // Could be "Reply" or "Replies"
                hasSibling(allOf(
                        withId(R.id.discussionTitle),
                        withText(topicTitle)
                )))

        scrollRecyclerView(R.id.discussionRecyclerView, matcher)
        onView(matcher).assertDisplayed() // probably redundant
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

    fun createDiscussionTopic(name: String, description: String) {
        createNewDiscussion.click()
        // Directly populate the EditView, otherwise it might pop up a system dialog when
        // short-screen/landscape conditions are present.
        onView(withId(R.id.editDiscussionName)).perform(DirectlyPopulateEditText(name))
        onView(withId(R.id.rce_webView)).perform(TypeInRCETextEditor(description))
        onView(withId(R.id.menuSaveDiscussion)).perform(explicitClick()) // Can be mis-interpreted as a long press
        waitForDiscussionTopicToDisplay(name)
    }

    fun createAnnouncement(name: String, description: String) {
        createNewDiscussion.click()
        onView(withId(R.id.announcementNameEditText)).perform(DirectlyPopulateEditText(name))
        onView(withId(R.id.rce_webView)).perform(TypeInRCETextEditor(description))
        onView(withId(R.id.menuSaveAnnouncement)).perform(explicitClick())
    }

    fun assertAnnouncementCreated(inputTitle: String) {
            var expectedTitle = inputTitle
            if (inputTitle.isNullOrEmpty()) {
                expectedTitle = InstrumentationRegistry.getInstrumentation().targetContext.resources.getString(R.string.utils_noTitle)
            }
            waitForDiscussionTopicToDisplay(expectedTitle)
    }

    fun launchCreateAnnouncementThenClose() {
        createNewDiscussion.click()
        onView(withContentDescription("Close")).click()
    }

    fun clickOnSearchButton() {
        onView(withId(R.id.search)).click()
    }

    fun typeToSearchBar(textToType: String) {
        waitForViewWithId(R.id.search_src_text).replaceText(textToType)
    }

    fun clickOnClearSearchButton() {
        onView(withId(R.id.search_close_btn)).click()
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

}