package com.instructure.teacher.ui.pages

import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.canvasapi2.models.Conversation
import com.instructure.dataseeding.model.ConversationApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.WaitForToolbarTitle
import org.hamcrest.CoreMatchers.allOf

class InboxPage: BasePage() {

    private val toolbarTitle by WaitForToolbarTitle(R.string.tab_inbox)

    private val inboxRecyclerView by WaitForViewWithId(R.id.inboxRecyclerView)

    private val addMessageFAB by WaitForViewWithId(R.id.addMessage)

    //Only displayed when inbox is empty
    private val emptyPandaView by WaitForViewWithId(R.id.emptyInboxView)
    private val scopeFilterText by OnViewWithId(R.id.scopeFilterText)

    override fun assertPageObjects(duration: Long) {
        toolbarTitle.assertDisplayed()
    }

    fun assertHasConversation() {
        assertConversationCountIsGreaterThan(0)
    }

    fun assertConversationCountIsGreaterThan(count: Int) {
        inboxRecyclerView.check(RecyclerViewItemCountGreaterThanAssertion(count))
    }

    fun clickConversation(conversation: ConversationApiModel) {
        clickConversation(conversation.subject)
    }

    fun clickConversation(conversation: Conversation) {
        clickConversation(conversation.subject!!)
    }

    fun clickConversation(conversationSubject: String) {
        waitForViewWithText(conversationSubject).click()
    }

    fun clickAddMessageFAB() {
        addMessageFAB.click()
    }

    fun assertInboxEmpty() {
        onView(withId(R.id.emptyInboxView)).assertDisplayed()
    }

    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout))
            .perform(withCustomConstraints(ViewActions.swipeDown(), isDisplayingAtLeast(50)))
    }

    fun filterInbox(filterFor: String) {
        onView(withId(R.id.scopeFilter)).click()
        waitForViewWithText(filterFor).click()
    }

    fun assertThereIsAnUnreadMessage(unread: Boolean) {
        if(unread) onView(withId(R.id.unreadMark)).assertDisplayed()
        else onView(withId(R.id.unreadMark) + ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
    }

    fun assertConversationStarred(recipients: String) {
        onView(allOf(withId(R.id.star) + hasSibling(withId(R.id.userName) + withText(recipients))))
    }


}
