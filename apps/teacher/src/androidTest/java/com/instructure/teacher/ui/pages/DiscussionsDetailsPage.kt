package com.instructure.teacher.ui.pages

import com.instructure.espresso.*
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withId
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TypeInRCETextEditor

class DiscussionsDetailsPage : BasePage() {

    fun assertDiscussionTitle(title: String) {
        onView(withId(R.id.discussionTopicTitle)).assertHasText(title)
    }

    fun assertDiscussionPublished() {
        checkPublishedTextView("Published")
    }

    fun assertDiscussionUnpublished() {
        checkPublishedTextView("Unpublished")
    }

    fun assertNoReplies() {
        onView(withId(R.id.discussionTopicReplies)).assertNotDisplayed()
    }

    fun assertHasReply() {
        val repliesHeader = onView(withId(R.id.discussionTopicReplies))
        repliesHeader.scrollTo()
        repliesHeader.assertDisplayed()
    }

    fun openEdit() {
        onView(withId(R.id.menu_edit)).click()
    }

    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout)).swipeDown()
    }

    fun addReply(content: String) {
        onView(withId(R.id.replyToDiscussionTopic)).click()
        onView(withId(R.id.rce_webView)).perform(TypeInRCETextEditor(content))
        onView(withId(R.id.menu_send)).click()
    }

    private fun checkPublishedTextView(status: String) {
        onView(withId(R.id.publishStatusTextView)).assertHasText(status)

    }
}