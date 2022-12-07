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
import com.instructure.espresso.*
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withId
import com.instructure.teacher.R
import org.hamcrest.Matchers

class InboxMessagePage: BasePage() {

    private val starImageButton by OnViewWithId(R.id.starred)
    private val subjectTextView by OnViewWithId(R.id.subjectView)
    private val authorNameTextView by OnViewWithId(R.id.authorName)
    private val messageRecyclerView by WaitForViewWithId(R.id.recyclerView)
    private val replyTextView by OnViewWithId(R.id.reply)


    override fun assertPageObjects(duration: Long) {
        starImageButton.assertDisplayed()
        subjectTextView.assertDisplayed()
        messageRecyclerView.assertDisplayed()
        authorNameTextView.assertDisplayed()
        replyTextView.assertDisplayed()
    }

    fun assertHasMessage() {
        messageRecyclerView.check(RecyclerViewItemCountAssertion(1))
    }

    fun clickReply() {
        replyTextView.click()
    }

    fun assertHasReply() {
        messageRecyclerView.check(RecyclerViewItemCountAssertion(2))
    }

    fun clickOnStarConversation() {
        onView(withId(R.id.starred)).click()
    }

    fun openOptionMenuFor(itemName: String) {
        Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
        Espresso.onView(ViewMatchers.withText(itemName))
            .perform(ViewActions.click());
    }

    fun deleteConversation() {
        openOptionMenuFor("Delete")
        Espresso.onView(Matchers.allOf(ViewMatchers.isAssignableFrom(AppCompatButton::class.java),
                containsTextCaseInsensitive("DELETE"))).click()
    }
}
