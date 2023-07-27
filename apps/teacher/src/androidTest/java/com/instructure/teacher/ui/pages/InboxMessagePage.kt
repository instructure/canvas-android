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

import androidx.appcompat.widget.AppCompatButton
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.OnViewWithMatcher
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.allOf

/**
 * Represents the Inbox Message page.
 *
 * This page extends the BasePage class and provides functionality for interacting with an inbox message.
 * It contains various view elements such as star image button, author name text view, subject text view,
 * message recycler view, and reply text view.
 */
class InboxMessagePage: BasePage() {

    private val starImageButton by OnViewWithId(R.id.starred)
    private val authorNameTextView by OnViewWithId(R.id.authorName)
    private val subjectTextView by OnViewWithMatcher(allOf(withId(R.id.subjectView), withParent(R.id.header)))
    private val messageRecyclerView by WaitForViewWithId(R.id.recyclerView)
    private val replyTextView by OnViewWithId(R.id.reply)


    override fun assertPageObjects(duration: Long) {
        starImageButton.assertDisplayed()
        subjectTextView.assertDisplayed()
        messageRecyclerView.assertDisplayed()
        authorNameTextView.assertDisplayed()
        replyTextView.assertDisplayed()
    }

    /**
     * Asserts that the message page has at least one message.
     */
    fun assertHasMessage() {
        messageRecyclerView.check(RecyclerViewItemCountAssertion(1))
    }

    /**
     * Clicks the reply button to compose a reply message.
     */
    fun clickReply() {
        replyTextView.click()
    }

    /**
     * Asserts that the message page has at least one reply.
     */
    fun assertHasReply() {
        messageRecyclerView.check(RecyclerViewItemCountAssertion(2))
    }

    /**
     * Clicks on the star icon to mark the conversation as starred.
     */
    fun clickOnStarConversation() {
        onView(withId(R.id.starred)).click()
    }

    /**
     * Opens the option menu for the specified item.
     *
     * @param itemName The name of the item to open the option menu for.
     */
    fun openOptionMenuFor(itemName: String) {
        Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
        Espresso.onView(ViewMatchers.withText(itemName))
            .perform(ViewActions.click());
    }

    /**
     * Deletes the conversation.
     */
    fun deleteConversation() {
        openOptionMenuFor("Delete")
        Espresso.onView(CoreMatchers.allOf(ViewMatchers.isAssignableFrom(AppCompatButton::class.java),
            containsTextCaseInsensitive("DELETE"))).click()
    }
}
