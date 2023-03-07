package com.instructure.teacher.ui.pages

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.waitForMatcherWithRefreshes
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.Conversation
import com.instructure.dataseeding.model.ConversationApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.WaitForToolbarTitle
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matchers

class InboxPage: BasePage() {

    private val toolbarTitle by WaitForToolbarTitle(R.string.tab_inbox)

    private val inboxRecyclerView by WaitForViewWithId(R.id.inboxRecyclerView)

    private val addMessageFAB by WaitForViewWithId(R.id.addMessage)

    //Only displayed when inbox is empty
    private val emptyPandaView by WaitForViewWithId(R.id.emptyInboxView)
    private val scopeFilterText by OnViewWithId(R.id.scopeFilterText)
    private val editToolbar by OnViewWithId(R.id.editToolbar)

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

    fun selectInboxScope(scope: InboxApi.Scope) {
        waitForView(withId(R.id.scopeFilterText))
        onView(withId(R.id.scopeFilter)).click()
        when (scope) {
            InboxApi.Scope.INBOX -> onViewWithText("Inbox").scrollTo().click()
            InboxApi.Scope.UNREAD -> onViewWithText("Unread").scrollTo().click()
            InboxApi.Scope.ARCHIVED -> onViewWithText("Archived").scrollTo().click()
            InboxApi.Scope.STARRED -> onViewWithText("Starred").scrollTo().click()
            InboxApi.Scope.SENT -> onViewWithText("Sent").scrollTo().click()
        }
    }

    fun assertThereIsAnUnreadMessage(unread: Boolean) {
        if(unread) onView(withId(R.id.unreadMark)).assertDisplayed()
        else onView(withId(R.id.unreadMark) + ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
    }

    fun assertConversationStarred(recipients: String) {
        onView(allOf(withId(R.id.star) + hasSibling(withId(R.id.userName) + withText(recipients))))
    }

    fun assertConversationDisplayed(subject: String) {
        val matcher = withText(subject)
        scrollRecyclerView(R.id.inboxRecyclerView, matcher)
        onView(matcher).assertDisplayed()
    }

    fun assertConversationNotDisplayed(subject: String) {
        val matcher = withText(subject)
        onView(matcher).check(ViewAssertions.doesNotExist())
    }

    fun assertUnreadMarkerVisibility(subject: String, visibility: ViewMatchers.Visibility) {
        val matcher = Matchers.allOf(
            withId(R.id.unreadMark),
            ViewMatchers.withEffectiveVisibility(visibility),
            hasSibling(Matchers.allOf(withId(R.id.avatar))),
            hasSibling(Matchers.allOf(withId(R.id.subjectView), withText(subject)))
        )
        if(visibility == ViewMatchers.Visibility.VISIBLE) {
            waitForMatcherWithRefreshes(matcher) // May need to refresh before the unread mark shows up
            scrollRecyclerView(R.id.inboxRecyclerView, matcher)
            onView(matcher).assertDisplayed()
        }
        else if(visibility == ViewMatchers.Visibility.GONE) {
            onView(matcher).check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))
        }
    }

    fun selectConversation(conversationSubject: String) {
        waitForView(withId(R.id.inboxRecyclerView))
        val matcher = withText(conversationSubject)
        scrollRecyclerView(R.id.inboxRecyclerView, matcher)
        onView(matcher).longClick()
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
        Espresso.openActionBarOverflowOrOptionsMenu(
            InstrumentationRegistry.getInstrumentation().getTargetContext()
        )
        onView(ViewMatchers.withText("Delete"))
            .perform(ViewActions.click());
    }

    fun confirmDelete() {
        waitForView(withText("DELETE") + withAncestor(R.id.buttonPanel)).click()
    }

    fun swipeConversationRight(conversationSubject: String) {
        waitForView(withId(R.id.inboxRecyclerView))
        val matcher = withText(conversationSubject)
        scrollRecyclerView(R.id.inboxRecyclerView, matcher)
        onView(matcher).swipeRight()
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
        scrollRecyclerView(R.id.inboxRecyclerView, matcher)
        onView(matcher).swipeLeft()
    }

    fun swipeConversationLeft(conversation: Conversation) {
        swipeConversationLeft(conversation.subject!!)
    }

    fun swipeConversationLeft(conversation: ConversationApiModel) {
        swipeConversationLeft(conversation.subject!!)
    }

    fun selectConversations(conversations: List<String>) {
        for(conversation in conversations) {
            selectConversation(conversation)
        }
    }

    fun assertSelectedConversationNumber(selectedConversationNumber: String) {
        onView(withText(selectedConversationNumber) + withAncestor(R.id.editToolbar))
    }
}
