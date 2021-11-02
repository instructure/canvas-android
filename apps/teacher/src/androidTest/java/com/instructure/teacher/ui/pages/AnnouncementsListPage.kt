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
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TypeInRCETextEditor

class AnnouncementsListPage : BasePage() {

    private val announcementListToolbar by OnViewWithId(R.id.discussionListToolbar)
    private val announcementsFAB by OnViewWithId(R.id.createNewDiscussion)
    private val announcementsRecyclerView by OnViewWithId(R.id.discussionRecyclerView)
    private val searchButton by OnViewWithId(R.id.search)
    private val searchInput by WaitForViewWithId(androidx.appcompat.R.id.search_src_text)
    private val createNewDiscussion by OnViewWithId(R.id.createNewDiscussion)

    fun clickDiscussion(discussion: DiscussionApiModel) {
        clickDiscussion(discussion.title)
    }

    fun clickDiscussion(discussionTitle: String) {
        waitForViewWithText(discussionTitle).click()
    }

    fun assertHasAnnouncement(discussion: DiscussionTopicHeader) {
        assertHasAnnouncement(discussion.title!!)
    }

    fun assertHasAnnouncement(discussion: DiscussionApiModel) {
        assertHasAnnouncement(discussion.title)
    }

    fun assertHasAnnouncement(announcementName: String) {
        onView(withText(announcementName)).assertDisplayed()
    }

    fun assertFAB() {
        announcementsFAB.assertDisplayed()
    }

    fun openSearch() {
        searchButton.click()
    }

    fun enterSearchQuery(query: String) {
        searchInput.perform(ViewActions.replaceText(query))
    }

    fun assertAnnouncementCount(count: Int) {
        announcementsRecyclerView.waitForCheck(RecyclerViewItemCountAssertion(count))
    }

    fun assertEmpty() {
        onView(withId(R.id.emptyPandaView)).assertDisplayed()
    }

    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout)).swipeDown()
    }

    fun createAnnouncement(announcementName: String, announcementDetails: String) {
        clickOnCreateNewAnnouncementButton()
        onView(withId(R.id.announcementNameEditText)).replaceText(announcementName)
        onView(withId(R.id.rce_webView)).perform(TypeInRCETextEditor(announcementDetails))
        onView(withId(R.id.menuSaveAnnouncement)).click()
    }

    fun clickOnCreateAnnouncementThenClose() {
        clickOnCreateNewAnnouncementButton()
        onViewWithContentDescription("Close").click()
    }

    fun clickOnCreateNewAnnouncementButton() {
        createNewDiscussion.click()
    }

    fun verifyExitWithoutSavingDialog() {
        onViewWithText(R.string.exitWithoutSavingMessage).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    fun assertOnNewAnnouncementPage() {
        Espresso.onView(ViewMatchers.withText(R.string.newAnnouncement)).assertDisplayed()
    }

    fun acceptExitWithoutSaveDialog() {
        onViewWithText(R.string.exitUnsaved).click()
    }
}
