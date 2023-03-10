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

import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvas.espresso.refresh
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.waitForMatcherWithRefreshes
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.dataseeding.model.ConversationApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.student.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not

class InboxPage : BasePage(R.id.inboxPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val createMessageButton by OnViewWithId(R.id.addMessage)
    private val scopeButton by OnViewWithId(R.id.scopeFilter)
    private val filterButton by OnViewWithId(R.id.courseFilter)
    private val inboxRecyclerView by WaitForViewWithId(R.id.inboxRecyclerView)
    private val editToolbar by OnViewWithId(R.id.editToolbar, autoAssert = false)

    fun assertConversationDisplayed(conversation: ConversationApiModel) {
        assertConversationDisplayed(conversation.subject)
    }

    fun assertConversationDisplayed(subject: String) {
        val matcher = withText(subject)
        onView(matcher).scrollTo().assertDisplayed()
    }

    fun assertConversationNotDisplayed(conversation: ConversationApiModel) {
        assertConversationNotDisplayed(conversation.subject)
    }

    fun assertConversationNotDisplayed(subject: String) {
        val matcher = withText(subject)
        onView(matcher).check(doesNotExist())
    }

    fun assertMessageBodyDisplayed(messageBody: String) {
        val matcher = allOf(withId(R.id.message), withText(messageBody))
        waitForMatcherWithRefreshes(matcher) // May need to refresh before the new message body shows up
        scrollRecyclerView(R.id.inboxRecyclerView, matcher)
        onView(matcher).assertDisplayed()
    }

    fun openConversation(subject: String) {
        val matcher = withText(subject)
        scrollRecyclerView(R.id.inboxRecyclerView, matcher)
        onView(matcher).click()
    }

    fun openConversation(conversation: ConversationApiModel) {
        openConversation(conversation.subject)
    }

    fun openConversation(conversation: Conversation) {
        waitForView(withId(R.id.inboxRecyclerView))
        val matcher = withText(conversation.subject)
        scrollRecyclerView(R.id.inboxRecyclerView, matcher)
        onView(matcher).click()
    }

    fun filterInbox(filterFor: String) {
        refresh()
        waitForView(withId(R.id.scopeFilterText))
        scopeButton.click()
        waitForViewWithText(filterFor).click()
    }

    fun selectInboxFilter(course: Course) {
        filterButton.click()
        waitForViewWithText(course.name).click()
    }

    fun pressNewMessageButton() {
        createMessageButton.click()
    }

    fun goToDashboard() {
        onView(withId(R.id.bottomNavigationHome)).click()
    }

    fun assertConversationStarred(subject: String) {
        val matcher = allOf(
            withId(R.id.star),
            withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
            hasSibling(withId(R.id.userName)),
            hasSibling(withId(R.id.date)),
            hasSibling(allOf(withId(R.id.subjectView), withText(subject))))
        waitForMatcherWithRefreshes(matcher) // May need to refresh before the star shows up
        onView(matcher).scrollTo().assertDisplayed()

    }

    fun assertUnreadMarkerVisibility(conversation: Conversation, visibility: ViewMatchers.Visibility) {
        val matcher = allOf(
                withId(R.id.unreadMark),
                withEffectiveVisibility(visibility),
                hasSibling(allOf(withId(R.id.message), withText(conversation.lastMessage))))

        if(visibility == ViewMatchers.Visibility.VISIBLE) {
            waitForMatcherWithRefreshes(matcher) // May need to refresh before the unread mark shows up
            scrollRecyclerView(R.id.inboxRecyclerView, matcher)
            onView(matcher).assertDisplayed()
        }
        else if(visibility == ViewMatchers.Visibility.GONE) {
            onView(matcher).check(matches(not(isDisplayed())))
        }
    }

    fun assertUnreadMarkerVisibility(subject: String, visibility: ViewMatchers.Visibility) {
        val matcher = allOf(
            withId(R.id.unreadMark),
            withEffectiveVisibility(visibility),
            hasSibling(allOf(withId(R.id.avatar))),
            hasSibling(allOf(withId(R.id.subjectView), withText(subject)))
        )
        if(visibility == ViewMatchers.Visibility.VISIBLE) {
            waitForMatcherWithRefreshes(matcher) // May need to refresh before the unread mark shows up
            scrollRecyclerView(R.id.inboxRecyclerView, matcher)
            onView(matcher).assertDisplayed()
        }
        else if(visibility == ViewMatchers.Visibility.GONE) {
            onView(matcher).check(matches(not(isDisplayed())))
        }
    }

        fun assertInboxEmpty() {
        onView(withId(R.id.emptyInboxView)).assertDisplayed()
    }

    fun assertHasConversation() {
        assertConversationCountIsGreaterThan(0)
    }

    fun assertConversationCountIsGreaterThan(count: Int) {
        inboxRecyclerView.check(RecyclerViewItemCountGreaterThanAssertion(count))
    }

    fun selectConversation(conversationSubject: String) {
        waitForView(withId(R.id.inboxRecyclerView))
        val matcher = withText(conversationSubject)
        onView(matcher).scrollTo().longClick()
    }

    fun selectConversation(conversation: Conversation) {
        selectConversation(conversation.subject!!)
    }

    fun selectConversation(conversation: ConversationApiModel) {
        selectConversation(conversation.subject!!)
    }

    fun assertEditToolbarDisplayed() {
        editToolbar.assertDisplayed()
    }

    fun clickArchive() {
        waitForViewWithId(R.id.inboxArchiveSelected).click()
    }

    fun clickUnArchive() {
        waitForViewWithId(R.id.inboxUnarchiveSelected).click()
    }

    fun clickStar() {
        waitForViewWithId(R.id.inboxStarSelected).click()
    }

    fun clickUnstar() {
        waitForViewWithId(R.id.inboxUnstarSelected).click()
    }

    fun clickMarkAsRead() {
        waitForViewWithId(R.id.inboxMarkAsReadSelected).click()
    }

    fun clickMarkAsUnread() {
        waitForViewWithId(R.id.inboxMarkAsUnreadSelected).click()
    }

    fun clickDelete() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
        onView(ViewMatchers.withText("Delete"))
            .perform(ViewActions.click());
    }

    fun confirmDelete() {
        waitForView(withText("DELETE") + withAncestor(R.id.buttonPanel)).click()
    }
    fun swipeConversationRight(conversationSubject: String) {
        waitForView(withId(R.id.inboxRecyclerView))
        val matcher = withText(conversationSubject)
        onView(matcher).scrollTo().swipeRight()
    }

    fun swipeConversationRight(conversation: ConversationApiModel) {
        swipeConversationRight(conversation.subject!!)
    }

    fun swipeConversationRight(conversation: Conversation) {
        swipeConversationRight(conversation.subject!!)
    }

    fun swipeConversationLeft(conversationSubject: String) {
        waitForView(withId(R.id.inboxRecyclerView))
        val matcher = withText(conversationSubject)
        onView(matcher).scrollTo()
        onView(matcher).swipeLeft()
    }

    fun swipeConversationLeft(conversation: Conversation) {
        swipeConversationLeft(conversation.subject!!)
    }

    fun swipeConversationLeft(conversation: ConversationApiModel) {
        swipeConversationLeft(conversation.subject!!)
    }

    fun selectConversations(conversations: List<String>) {
        refresh()
        for(conversation in conversations) {
            selectConversation(conversation)
        }
    }

    fun assertSelectedConversationNumber(selectedConversationNumber: String) {
        onView(withText(selectedConversationNumber) + withAncestor(R.id.editToolbar))
    }

}