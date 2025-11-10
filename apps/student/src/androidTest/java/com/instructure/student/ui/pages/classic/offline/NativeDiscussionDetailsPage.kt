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
package com.instructure.student.ui.pages.classic.offline

import android.os.SystemClock.sleep
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.isElementDisplayed
import com.instructure.canvas.espresso.waitForMatcherWithSleeps
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.canvas.espresso.withElementRepeat
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.espresso.ModuleItemInteractions
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertGone
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForViewWithId
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.student.R
import com.instructure.student.ui.pages.classic.WebViewTextCheck
import com.instructure.student.ui.utils.TypeInRCETextEditor
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.junit.Assert.assertTrue


class NativeDiscussionDetailsPage(val moduleItemInteractions: ModuleItemInteractions) : BasePage(R.id.discussionDetailsPage) {
    private val discussionTopicTitle by OnViewWithId(R.id.discussionTopicTitle)
    private val replyButton by OnViewWithId(R.id.replyToDiscussionTopic)

    fun assertTitleText(titleText: String) {
        discussionTopicTitle.assertHasText(titleText)
    }

    fun assertDescriptionText(descriptionText: String) {
        onWebView(withId(R.id.contentWebView) + withAncestor(R.id.discussionTopicHeaderWebViewWrapper))
            .withElement(findElement(Locator.ID, "content"))
            .check(webMatches(getText(), containsString(descriptionText)))
    }

    fun assertTopicInfoShowing(topicHeader: DiscussionTopicHeader) {
        assertTitleText(topicHeader.title!!)
        assertDescriptionText(topicHeader.message!!)
    }

    fun clickLinkInDescription(linkElementId: String) {
        onWebView(withId(R.id.contentWebView) + withAncestor(R.id.discussionTopicHeaderWebViewWrapper))
            .withElement(findElement(Locator.ID, linkElementId))
            .perform(webClick())
    }

    fun refresh() {
        scrollToTop()
        onView(allOf(withId(R.id.swipeRefreshLayout), isDisplayingAtLeast(10)))
            .perform(withCustomConstraints(swipeDown(), isDisplayingAtLeast(10)))
    }

    fun scrollToRepliesWebview() {
        waitForMatcherWithSleeps(withId(R.id.contentWebView) + withAncestor(R.id.discussionRepliesWebViewWrapper), timeout = 20000)
        onView(withId(R.id.contentWebView) + withAncestor(R.id.discussionRepliesWebViewWrapper)).scrollTo()
    }

    fun clickReply() {
        replyButton.click()
    }

    fun assertReplyButtonNotDisplayed() {
        replyButton.check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
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
        waitForViewWithId(R.id.rce_webView).perform(TypeInRCETextEditor(replyMessage))
        clickOnSendReplyButton()

        sleep(3000) // wait out the toast message
    }

    private fun clickOnSendReplyButton() {
        onView(withId(R.id.menu_send)).click()
    }

    fun assertReplyDisplayed(reply: DiscussionEntry, refreshesAllowed: Int = 0) {

        // Allow up to refreshesAllowed attempt/refresh cycles
        for (i in 0 until refreshesAllowed) {
            try {
                onWebView(withId(R.id.contentWebView) + withAncestor(R.id.discussionRepliesWebViewWrapper))
                    .withElement(findElement(Locator.ID, "message_content_${reply.id}"))
                    .check(webMatches(getText(), containsString(reply.message)))
                return
            } catch (t: Throwable) {
                refresh()
            }
        }

        // If we have not yet seen the desired reply, try one more time, allowing up to
        // 3 retries for the page to render and the reply to appear.  (Each attempt
        // waits ~10 secs before failing.)
        // (It can take a *long* time for the reply to get rendered to the webview on
        // tablets (in FTL, anyway).)
        onWebView(withId(R.id.contentWebView) + withAncestor(R.id.discussionRepliesWebViewWrapper))
            .withElementRepeat(findElement(Locator.ID, "message_content_${reply.id}"), 3)
            .check(webMatches(getText(), containsString(reply.message)))
    }

    fun assertReplyDisplayed(reply: DiscussionEntry) {
        onWebView(withId(R.id.contentWebView) + withAncestor(R.id.discussionRepliesWebViewWrapper))
            .withElement(findElement(Locator.TAG_NAME, "html"))
            .check(webMatches(getText(), containsString(reply.message)))
    }

    fun assertIfThereIsAReply() {
        onView(withId(R.id.discussionTopicRepliesTitle)).assertDisplayed()
        onView(withId(R.id.contentWebView) + withAncestor(R.id.discussionRepliesWebViewWrapper)).assertDisplayed()
    }

    fun assertFavoritingEnabled(reply: DiscussionEntry) {
        try {
            onWebView(withId(R.id.contentWebView) + withAncestor(R.id.discussionRepliesWebViewWrapper))
                .withElement(findElement(Locator.CLASS_NAME, "likes_icon_wrapper_${reply.id}"))
        } catch (t: Throwable) {
            assertTrue("Favoriting icon is disabled", false)
        }
    }

    fun assertFavoritingDisabled(reply: DiscussionEntry) {
        try {
            onWebView(withId(R.id.contentWebView) + withAncestor(R.id.discussionRepliesWebViewWrapper))
                .withElement(findElement(Locator.CLASS_NAME, "likes_icon_wrapper_${reply.id}"))
            // We shouldn't reach this point if the favoriting icon is disabled -- we should throw
            assertTrue("Favoriting icon is enabled", false)
        } catch (_: Throwable) {}
    }

    fun clickLikeOnEntry(reply: DiscussionEntry) {
        onWebView(withId(R.id.contentWebView) + withAncestor(R.id.discussionRepliesWebViewWrapper))
            .withElement(findElement(Locator.CLASS_NAME, "likes_icon_wrapper_${reply.id}"))
            .perform(webClick())
    }

    fun assertLikeCount(reply: DiscussionEntry, count: Int, refreshesAllowed: Int = 0) {
        if (count > 0) {

            for (i in 0 until refreshesAllowed) {
                try {
                    onWebView(withId(R.id.contentWebView) + withAncestor(R.id.discussionRepliesWebViewWrapper))
                        .withElement(findElement(Locator.CLASS_NAME, "likes_count_${reply.id}"))
                        .check(webMatches(getText(), containsString(count.toString())))
                    return
                } catch (t: Throwable) {
                    refresh()
                }
            }

            // If we haven't verified our info by now, let's make one last call to either
            // (1) succeed or (2) throw a sensible error.
            onWebView(withId(R.id.contentWebView) + withAncestor(R.id.discussionRepliesWebViewWrapper))
                .withElement(findElement(Locator.CLASS_NAME, "likes_count_${reply.id}"))
                .check(webMatches(getText(), containsString(count.toString())))
        } else {
            try {
                onWebView(withId(R.id.contentWebView) + withAncestor(R.id.discussionRepliesWebViewWrapper))
                    .withElement(findElement(Locator.CLASS_NAME, "likes_count_${reply.id}"))
                assertTrue("Didn't expect to see like count with 0 count", false)
            } catch (_: Throwable) {}
        }
    }

    fun assertReplyAttachment(reply: DiscussionEntry) {
        try {
            onWebView(withId(R.id.contentWebView) + withAncestor(R.id.discussionRepliesWebViewWrapper))
                .withElement(findElement(Locator.CLASS_NAME, "attachments_${reply.id}"))
        } catch (t: Throwable) {
            assertTrue("Discussion entry did not have an attachment", false)
        }
    }

    fun previewAndCheckReplyAttachment(reply: DiscussionEntry, vararg checks: WebViewTextCheck) {

        // Sometimes clicking the attachment logo fails to do anything.
        // We'll give it 3 chances.
        var triesRemaining = 3;
        while (!isElementDisplayed(R.id.canvasWebViewWrapper) && triesRemaining > 0) {
            if (triesRemaining < 3) {
                refresh() // Maybe web content was incorrectly rendered?  Try again
                sleep(1500) // Allow webview some time to render
            }
            onWebView(withId(R.id.contentWebView) + withAncestor(R.id.discussionRepliesWebViewWrapper))
                .withElementRepeat(findElement(Locator.CLASS_NAME, "attachments_${reply.id}"), 20)
                .perform(webClick())
            triesRemaining -= 1
        }

        assertTrue("FAILED to bring up reply attachment", isElementDisplayed(R.id.canvasWebViewWrapper));

        for (check in checks) {
            onWebView(withId(R.id.contentWebView) + withAncestor(R.id.canvasWebViewWrapper))
                .withElement(findElement(check.locatorType, check.locatorValue))
                .check(webMatches(getText(), containsString(check.textValue)))
        }
        Espresso.pressBack()

    }

    fun replyToReply(reply: DiscussionEntry, replyMessage: String) {

        // It appears that sometimes the click to reply doesn't work.
        // Let's give it 3 chances.
        var triesRemaining = 3
        while (!isElementDisplayed(R.id.rce_webView) && triesRemaining > 0) {
            if (triesRemaining < 3) {
                refresh() // maybe the html was rendered badly and needs refreshing?
                sleep(2000) // A little time for the webview to render
            }
            onWebView(withId(R.id.contentWebView) + withAncestor(R.id.discussionRepliesWebViewWrapper))
                .withElementRepeat(findElement(Locator.ID, "reply_${reply.id}"), 20)
                .perform(webClick())
            triesRemaining -= 1
        }

        assertTrue("FAILED to bring up rce / reply editor", isElementDisplayed(R.id.rce_webView));

        onView(withId(R.id.rce_webView)).perform(TypeInRCETextEditor(replyMessage))
        onView(withId(R.id.menu_send)).click()

        sleep(3000) // wait out the toast message
    }

    fun assertMainAttachmentDisplayed() {
        onView(withId(R.id.attachmentIcon)).assertDisplayed()
    }

    /**
     * Assumes that the attachment is html
     */
    fun previewAndCheckMainAttachment(vararg checks: WebViewTextCheck) {
        onView(withId(R.id.attachmentIcon)).click()
        for (check in checks) {
            onWebView(withId(R.id.contentWebView) + withAncestor(R.id.canvasWebViewWrapper))
                .withElement(findElement(check.locatorType, check.locatorValue))
                .check(webMatches(getText(), containsString(check.textValue)))
        }
        Espresso.pressBack()
    }

    /**
     * It can take a *long* time (much longer than the advertised 2.5 seconds) for
     * the unread indicator to disappear when running tests on FTL.  Rather than
     * use an ever-increasing timeout, I figured I would just programmatically
     * wait until the unread indicator disappeared, for up to 10 seconds.
     *
     * The downside is that we are relying on webview elements to not change.
     * If that is something that happens often (or maybe even once), we will
     * need to go back to using a timeout.
     */
    fun waitForUnreadIndicatorToDisappear(reply: DiscussionEntry) {
        repeat(10) {
            if (!isUnreadIndicatorVisible(reply)) return
            sleep(1000)
        }

        assertTrue("Timed out waiting for unread indicator to disappear", false)
    }

    fun assertPointsPossibleDisplayed(points: String) {
        onView(withId(R.id.pointsTextView)).check(matches(containsTextCaseInsensitive(points)))
    }

    fun assertPointsPossibleNotDisplayed() {
        onView(withId(R.id.pointsTextView)).assertNotDisplayed()
    }

    fun clickOnInnerReply() {
        onWebView(withId(R.id.contentWebView) + withAncestor(R.id.discussionRepliesWebViewWrapper))
            .withElement(findElement(Locator.XPATH, "//div[@class='reply_wrapper' and contains(@id, 'reply')]"))
            .perform(webClick())
    }

    fun clickOnAddBookmarkMenu() {
        onView(withText("Add Bookmark")).click()
    }

    private fun isUnreadIndicatorVisible(reply: DiscussionEntry): Boolean {
        return try {
            onWebView(withId(R.id.contentWebView) + withAncestor(R.id.discussionRepliesWebViewWrapper))
                .withElement(findElement(Locator.ID, "unread_indicator_${reply.id}"))
                .withElement(findElement(Locator.CLASS_NAME, "unread"))
            true
        } catch (t: Throwable) {
            false
        }
    }

    private fun scrollToTop() {
        onView(allOf(withId(R.id.swipeRefreshLayout), isDisplayingAtLeast(10)))
            .perform(withCustomConstraints(swipeDown(), isDisplayingAtLeast(10)))
    }
}
