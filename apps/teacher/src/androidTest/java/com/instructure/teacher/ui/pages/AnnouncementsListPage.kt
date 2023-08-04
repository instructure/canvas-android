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
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
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
import com.instructure.espresso.page.onViewWithContentDescription
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.replaceText
import com.instructure.espresso.swipeDown
import com.instructure.espresso.waitForCheck
import com.instructure.teacher.R
import com.instructure.teacher.ui.interfaces.SearchablePage
import com.instructure.teacher.ui.utils.TypeInRCETextEditor

/**
 * Announcements list page
 *
 * @constructor Create empty Announcements list page
 */
class AnnouncementsListPage : BasePage(), SearchablePage {

    private val announcementListToolbar by OnViewWithId(R.id.discussionListToolbar)
    private val announcementsFAB by OnViewWithId(R.id.createNewDiscussion)
    private val announcementsRecyclerView by OnViewWithId(R.id.discussionRecyclerView)
    private val searchButton by OnViewWithId(R.id.search)
    private val searchInput by WaitForViewWithId(androidx.appcompat.R.id.search_src_text)
    private val createNewDiscussion by OnViewWithId(R.id.createNewDiscussion)

    /**
     * Click on the discussion given in parameter.
     *
     * @param discussion: The DiscussionApiModel parameter.
     */
    fun clickDiscussion(discussion: DiscussionApiModel) {
        clickDiscussion(discussion.title)
    }

    /**
     * Click on the discussion with the given title in parameter.
     *
     * @param discussionTitle: The discussion title parameter string.
     */
    fun clickDiscussion(discussionTitle: String) {
        waitForViewWithText(discussionTitle).click()
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
        onView(withText(announcementName)).assertDisplayed()
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
     * Click search button.
     *
     */
    override fun clickOnSearchButton() {
        searchButton.click()
    }

    /**
     * Type the given search text into the search input field.
     *
     * @param searchText: The search text query parameter.
     */
    override fun typeToSearchBar(searchText: String) {
        searchInput.perform(ViewActions.replaceText(searchText))
    }

    /**
     * Click reset search text.
     *
     */
    override fun clickOnClearSearchButton() {
        waitForView(withId(R.id.search_close_btn)).click()
    }

    /**
     * Click on back button on the search bar.
     * NOT IMPLEMENTED YET
     */
    override fun pressSearchBackButton() = Unit

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
