/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.dataseeding.model.DiscussionApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.Searchable
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.onViewWithContentDescription
import com.instructure.espresso.pages.onViewWithText
import com.instructure.espresso.pages.plus
import com.instructure.espresso.pages.waitForViewWithText
import com.instructure.espresso.pages.withAncestor
import com.instructure.espresso.pages.withId
import com.instructure.espresso.pages.withText
import com.instructure.espresso.replaceText
import com.instructure.espresso.swipeDown
import com.instructure.espresso.waitForCheck
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TypeInRCETextEditor

/**
 * Announcements list page.
 *
 * @constructor Create empty Announcements list page.
 */
class AnnouncementsListPage(val searchable: Searchable) : BasePage() {

    private val announcementListToolbar by OnViewWithId(R.id.discussionListToolbar)
    private val announcementsFAB by OnViewWithId(R.id.createNewDiscussion)
    private val announcementsRecyclerView by OnViewWithId(R.id.discussionRecyclerView)
    private val createNewDiscussion by OnViewWithId(R.id.createNewDiscussion)

    /**
     * Click on the discussion given in parameter.
     *
     * @param announcement: The DiscussionApiModel parameter.
     */
    fun clickAnnouncement(announcement: DiscussionApiModel) {
        clickAnnouncement(announcement.title)
    }

    /**
     * Click on the discussion with the given title in parameter.
     *
     * @param announcementTitle: The discussion title parameter string.
     */
    fun clickAnnouncement(announcementTitle: String) {
        waitForViewWithText(announcementTitle).click()
    }

    /**
     * Assert that there is an announcement with the given discussion title.
     *
     * @param discussion: The DiscussionTopicHeader object parameter.
     */
    fun assertHasAnnouncement(discussion: DiscussionTopicHeader) {
        assertHasAnnouncement(discussion.title!!)
    }

    /**
     * Assert that there is an announcement with the given discussion title.
     *
     * @param discussion: The DiscussionApiModel object parameter.
     */
    fun assertHasAnnouncement(discussion: DiscussionApiModel) {
        assertHasAnnouncement(discussion.title)
    }

    /**
     * Assert that there is an announcement with the given name is displayed.
     *
     * @param announcementName: The announcement name string parameter.
     */
    fun assertHasAnnouncement(announcementName: String) {
        onView(withId(R.id.discussionTitle) + withText(announcementName)).assertDisplayed()
    }

    /**
     * Assert FAB is displayed.
     *
     */
    fun assertFAB() {
        announcementsFAB.assertDisplayed()
    }

    /**
     * Assert that the announcements recyclerview count is equals to the given one.
     *
     * @param count: The expected count integer parameter.
     */
    fun assertAnnouncementCount(count: Int) {
        announcementsRecyclerView.waitForCheck(RecyclerViewItemCountAssertion(count))
    }

    /**
     * Assert that the empty view is displayed.
     *
     */
    fun assertEmpty() {
        onView(withId(R.id.emptyPandaView)).assertDisplayed()
    }

    /**
     * Refresh the page (swipeRefreshLayout).
     *
     */
    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout)).swipeDown()
    }

    /**
     * Create a new announcement with the given name and details parameters.
     *
     * @param announcementName: Name of the new announcement.
     * @param announcementDetails: Details of the new announcement.
     */
    fun createAnnouncement(announcementName: String, announcementDetails: String) {
        clickOnCreateNewAnnouncementButton()
        onView(withId(R.id.announcementNameEditText)).replaceText(announcementName)
        onView(withId(R.id.rce_webView)).perform(TypeInRCETextEditor(announcementDetails))
        onView(withId(R.id.menuSaveAnnouncement)).click()
    }

    /**
     * Click on create announcement then close the creation view.
     *
     */
    fun clickOnCreateAnnouncementThenClose() {
        clickOnCreateNewAnnouncementButton()
        onViewWithContentDescription("Close").click()
    }

    /**
     * Click on create new announcement button.
     *
     */
    private fun clickOnCreateNewAnnouncementButton() {
        createNewDiscussion.click()
    }

    /**
     * Verify exit without saving dialog.
     *
     */
    fun verifyExitWithoutSavingDialog() {
        onViewWithText(R.string.exitWithoutSavingMessage).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    /**
     * Assert on new announcement page.
     *
     */
    fun assertOnNewAnnouncementPage() {
        Espresso.onView(ViewMatchers.withText(R.string.newAnnouncement)).assertDisplayed()
    }

    /**
     * Accept exit without save dialog.
     *
     */
    fun acceptExitWithoutSaveDialog() {
        onViewWithText(R.string.exitUnsaved).click()
    }

    /**
     * Assert search result count is equals to the expected.
     *
     * @param expectedCount: The expected search result count integer parameter.
     */
    fun assertSearchResultCount(expectedCount: Int) {
        Thread.sleep(1000)
        onView(withId(R.id.discussionRecyclerView) + withAncestor(R.id.swipeRefreshLayout)).check(
            ViewAssertions.matches(ViewMatchers.hasChildCount(expectedCount + 1)) //because of the FrameLayout, it does not actually a discussion
        )
    }
}
