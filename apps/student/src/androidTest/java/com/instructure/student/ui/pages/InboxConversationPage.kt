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

import android.os.SystemClock.sleep
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import com.instructure.canvas.espresso.explicitClick
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.*
import com.instructure.espresso.replaceText
import com.instructure.espresso.scrollTo
import com.instructure.student.R
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers

class InboxConversationPage : BasePage(R.id.inboxConversationPage) {

    fun replyToMessage(message: String) {
        waitForViewWithText(R.string.reply).click()
        waitForViewWithHint(R.string.message).replaceText(message)
        onViewWithContentDescription("Send").perform(explicitClick())
        // Wait for reply to propagate
        for(i in 1..10) {
            try {
                onView(withId(R.id.starred)).assertDisplayed()
                break
            }
            catch(t: Throwable) {
                sleep(1000)
            }
        }
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