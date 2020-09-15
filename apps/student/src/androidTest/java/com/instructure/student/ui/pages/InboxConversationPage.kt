/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.ui.pages

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withHint
import com.instructure.canvas.espresso.explicitClick
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithContentDescription
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.waitForViewWithHint
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.replaceText
import com.instructure.student.R
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf

class InboxConversationPage : BasePage(R.id.inboxConversationPage) {

    fun replyToMessage(message: String) {
        waitForViewWithText(R.string.reply).click()
        waitForViewWithHint(R.string.message).replaceText(message)
        onViewWithContentDescription("Send").perform(explicitClick())
        // Wait for reply to propagate, and for us to return to the email thread page
        waitForView(withId(R.id.starred)).assertDisplayed()
    }

    fun replyAllToMessage(replyMessage: String, expectedChipCount: Int) {
        onView(withId(R.id.messageOptions)).click()
        onView(withText("Reply All")).click()
        onView(withId(R.id.chipGroup)).check(matches(hasChildCount(expectedChipCount)))
        onView(withHint(R.string.message)).replaceText(replyMessage)
        onView(withContentDescription("Send")).perform(explicitClick())
        onView(allOf(withId(R.id.messageBody), withText(replyMessage))).assertDisplayed()
    }

    fun assertMessageDisplayed(message: String) {
        val itemMatcher = CoreMatchers.allOf(
                ViewMatchers.hasSibling(withId(R.id.attachmentContainer)),
                ViewMatchers.hasSibling(withId(R.id.headerDivider)),
                withId(R.id.messageBody),
                withText(message)
        )
        waitForView(itemMatcher).assertDisplayed()
    }

    fun assertAttachmentDisplayed(displayName: String) {
        scrollRecyclerView(R.id.listView,withText(displayName))
        onViewWithText(displayName).check(matches(isDisplayingAtLeast(5)))
    }

    fun refresh() {
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.swipeRefreshLayout), ViewMatchers.isDisplayingAtLeast(10)))
                .perform(withCustomConstraints(ViewActions.swipeDown(), ViewMatchers.isDisplayingAtLeast(10)))
    }

}