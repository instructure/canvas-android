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

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.DriverAtoms.webScrollIntoView
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertGone
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeUp
import com.instructure.espresso.typeText
import com.instructure.student.R
import com.instructure.student.ui.utils.TypeInRCETextEditor
import instructure.rceditor.RCETextEditor
import org.hamcrest.Matcher
import org.hamcrest.Matchers.containsString
import org.junit.Assert.assertTrue

class DiscussionDetailsPage : BasePage(R.id.discussionDetailsPage) {
    private val discussionTopicTitle by OnViewWithId(R.id.discussionTopicTitle)
    private val replyButton by OnViewWithId(R.id.replyToDiscussionTopic)

    fun assertTitleText(titleText: String) {
        discussionTopicTitle.assertHasText(titleText)
    }

    fun assertDescriptionText(descriptionText: String) {
        onWebView(withId(R.id.discussionTopicHeaderWebView))
                .withElement(findElement(Locator.ID,"content"))
                .check(webMatches(getText(), containsString(descriptionText)))
    }

    fun assertTopicInfoShowing(topicHeader: DiscussionTopicHeader) {
        assertTitleText(topicHeader.title!!)
        assertDescriptionText(topicHeader.message!!)
    }

    fun clickLinkInDescription(linkElementId : String) {
        onWebView(withId(R.id.discussionTopicHeaderWebView))
                .withElement(findElement(Locator.ID,linkElementId))
                .perform(webClick())
    }

    fun clickReply() {
        replyButton.click()
    }

    fun assertRepliesEnabled() {
        replyButton.assertDisplayed()
    }

    fun assertRepliesDisabled() {
        replyButton.assertGone()
    }

    fun assertRepliesDisplayed() {
        onView(withId(R.id.discussionTopicRepliesTitle)).scrollTo().assertDisplayed()
    }

    fun assertNoRepliesDisplayed() {
        onView(withId(R.id.discussionTopicRepliesTitle)).assertNotDisplayed()
    }

    fun sendReply(replyMessage: String) {
        clickReply()
        onView(withId(R.id.rce_webView)).perform(TypeInRCETextEditor(replyMessage))
        onView(withId(R.id.menu_send)).click()
    }

    fun assertReplyDisplayed(reply: DiscussionEntry) {
        onWebView(withId(R.id.discussionRepliesWebView))
                .withElement(findElement(Locator.ID, "message_content_${reply.id}"))
                .check(webMatches(getText(),containsString(reply.message)))
    }

    fun assertFavoritingEnabled(reply: DiscussionEntry) {
        try {
            onWebView(withId(R.id.discussionRepliesWebView))
                    .withElement(findElement(Locator.CLASS_NAME, "likes_icon_wrapper_${reply.id}"))
        }
        catch(t: Throwable) {
            assertTrue("Favoriting icon is disabled", false)
        }
    }

    fun assertFavoritingDisabled(reply: DiscussionEntry) {
        try {
            onWebView(withId(R.id.discussionRepliesWebView))
                    .withElement(findElement(Locator.CLASS_NAME, "likes_icon_wrapper_${reply.id}"))
            assertTrue("Favoriting icon is enabled", false)
        }
        catch(t: Throwable) {
        }
    }

    fun clickLikeOnEntry(reply: DiscussionEntry) {
        onWebView(withId(R.id.discussionRepliesWebView))
                .withElement(findElement(Locator.CLASS_NAME, "likes_icon_wrapper_${reply.id}"))
                .perform(webClick())
    }

    fun assertLikeCount(reply: DiscussionEntry, count: Int) {
        if(count > 0) {
            onWebView(withId(R.id.discussionRepliesWebView))
                    .withElement(findElement(Locator.CLASS_NAME, "likes_count_${reply.id}"))
                    .check(webMatches(getText(), containsString(count.toString())))
        }
        else {
            try {
                onWebView(withId(R.id.discussionRepliesWebView))
                        .withElement(findElement(Locator.CLASS_NAME, "likes_count_${reply.id}"))
                assertTrue("Didn't expect to see like count with 0 count", false)
            }
            catch(t: Throwable) { }

        }
    }

    fun replyToReply(reply: DiscussionEntry, replyMessage: String) {
        onWebView(withId(R.id.discussionRepliesWebView))
                .withElement(findElement(Locator.ID, "reply_button_wrapper_${reply.id}"))
                .perform(webClick())
        onView(withId(R.id.rce_webView)).perform(TypeInRCETextEditor(replyMessage))
        onView(withId(R.id.menu_send)).click()
    }


    fun swipeReplyInfoView(reply: DiscussionEntry) {
        onWebView(withId(R.id.discussionRepliesWebView))
                .withElement(findElement(Locator.ID, "message_content_${reply.id}"))
                .perform(webScrollIntoView())
    }

    fun swipeUpReplyButton() {
        replyButton.swipeUp()
    }
}

