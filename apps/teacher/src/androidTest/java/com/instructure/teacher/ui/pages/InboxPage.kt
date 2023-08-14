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
import com.instructure.canvasapi2.models.Conversation
import com.instructure.dataseeding.model.ConversationApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.RecyclerViewItemCountGreaterThanAssertion
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertVisibility
import com.instructure.espresso.click
import com.instructure.espresso.longClick
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.waitForViewWithId
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeLeft
import com.instructure.espresso.swipeRight
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.WaitForToolbarTitle
import org.hamcrest.Matchers

/**
 * Represents the Inbox Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the inbox.
 * It contains various view elements such as toolbar, inbox recycler view, add message FAB,
 * empty inbox view, scope filter text, and edit toolbar.
 */
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

    /**
     * Asserts that the inbox has at least one conversation.
     */
    fun assertHasConversation() {
        assertConversationCountIsGreaterThan(0)
    }

    /**
     * Asserts that the count of conversations is greater than the specified count.
     *
     * @param count The count to compare against.
     */
    fun assertConversationCountIsGreaterThan(count: Int) {
        inboxRecyclerView.check(RecyclerViewItemCountGreaterThanAssertion(count))
    }

    /**
     * Asserts that the count of conversations matches the specified count.
     *
     * @param count The expected count of conversations.
     */
    fun assertConversationCount(count: Int) {
        inboxRecyclerView.check(RecyclerViewItemCountAssertion(count))
    }

    /**
     * Clicks on the conversation with the specified subject.
     *
     * @param conversationSubject The subject of the conversation to click.
     */
    fun clickConversation(conversationSubject: String) {
        waitForViewWithText(conversationSubject).click()
    }

    /**
     * Clicks on the conversation with the specified subject.
     *
     * @param conversation The subject of the conversation to click.
     */
    fun clickConversation(conversation: ConversationApiModel) {
        clickConversation(conversation.subject)
    }

    /**
     * Clicks on the conversation with the specified subject.
     *
     * @param conversation The subject of the conversation to click.
     */
    fun clickConversation(conversation: Conversation) {
        clickConversation(conversation.subject!!)
    }

    /**
     * Clicks on the add message FAB.
     */
    fun clickAddMessageFAB() {
        addMessageFAB.click()
    }

    /**
     * Asserts that the inbox is empty.
     */
    fun assertInboxEmpty() {
        onView(withId(R.id.emptyInboxView)).assertDisplayed()
    }

    /**
     * Refreshes the inbox view.
     */
    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout))
            .perform(withCustomConstraints(ViewActions.swipeDown(), isDisplayingAtLeast(50)))
    }

    /**
     * Filters the messages by the specified filter.
     *
     * @param filterFor The filter to apply.
     */
    fun filterMessageScope(filterFor: String) {
        waitForView(withId(R.id.scopeFilterText))
        onView(withId(R.id.scopeFilter)).click()
        waitForViewWithText(filterFor).click()
    }

    /**
     * Filters the messages by the specified course scope.
     *
     * @param courseName The name of the course to filter for.
     */
    fun filterCourseScope(courseName: String) {
        waitForView(withId(R.id.courseFilter)).click()
        waitForViewWithText(courseName).click()
    }

    /**
     * Clears the course filter.
     */
    fun clearCourseFilter() {
        waitForView(withId(R.id.courseFilter)).click()
        onView(withId(R.id.clear) + withText(R.string.inboxClearFilter)).click()
    }

    /**
     * Asserts whether there is an unread message based on the specified flag.
     *
     * @param unread Flag indicating whether there is an unread message.
     */
    fun assertThereIsAnUnreadMessage(unread: Boolean) {
        if(unread) onView(withId(R.id.unreadMark)).assertDisplayed()
        else onView(withId(R.id.unreadMark) + ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
    }

    /**
     * Asserts that the conversation with the specified subject is starred.
     *
     * @param subject The subject of the conversation.
     */
    fun assertConversationStarred(subject: String) {
        val matcher = Matchers.allOf(
            withId(R.id.star),
            ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
            hasSibling(withId(R.id.userName)),
            hasSibling(withId(R.id.date)),
            hasSibling(Matchers.allOf(withId(R.id.subjectView), withText(subject)))
        )
        waitForMatcherWithRefreshes(matcher) // May need to refresh before the star shows up
        onView(matcher).assertDisplayed()
    }

    /**
     * Asserts that the conversation with the specified subject is not starred.
     *
     * @param subject The subject of the conversation.
     */
    fun assertConversationNotStarred(subject: String) {
        val matcher = Matchers.allOf(
            withId(R.id.star),
            ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
            hasSibling(withId(R.id.userName)),
            hasSibling(withId(R.id.date)),
            hasSibling(Matchers.allOf(withId(R.id.subjectView), withText(subject)))
        )
        waitForMatcherWithRefreshes(matcher) // May need to refresh before the star shows up
        onView(matcher).check(ViewAssertions.doesNotExist())
    }

    /**
     * Asserts that the conversation with the specified subject is displayed.
     *
     * @param subject The subject of the conversation.
     */
    fun assertConversationDisplayed(subject: String) {
        val matcher = withText(subject)
        waitForView(matcher).scrollTo().assertDisplayed()
    }

    /**
     * Asserts that the conversation with the specified subject is not displayed.
     *
     * @param subject The subject of the conversation.
     */
    fun assertConversationNotDisplayed(subject: String) {
        val matcher = withText(subject)
        onView(matcher).check(ViewAssertions.doesNotExist())
    }

    /**
     * Asserts the visibility of the unread marker for the conversation with the specified subject.
     *
     * @param subject The subject of the conversation.
     * @param visibility The expected visibility of the unread marker.
     */
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

    /**
     * Selects the conversation with the specified subject.
     *
     * @param conversationSubject The subject of the conversation to select.
     */
    fun selectConversation(conversationSubject: String) {
        waitForView(withId(R.id.inboxRecyclerView))
        val matcher = withText(conversationSubject)
        onView(matcher).scrollTo().longClick()
    }

    /**
     * Selects the conversation with the specified subject.
     *
     * @param conversation The conversation to select.
     */
    fun selectConversation(conversation: Conversation) {
        selectConversation(conversation.subject!!)
    }

    /**
     * Selects the conversation with the specified subject.
     *
     * @param conversation The conversation to select.
     */
    fun selectConversation(conversation: ConversationApiModel) {
        selectConversation(conversation.subject!!)
    }

    /**
     * Clicks the archive option in the action mode.
     */
    fun clickArchive() {
        waitForViewWithId(R.id.inboxArchiveSelected).click()
    }

    /**
     * Clicks the unarchive option in the action mode.
     */
    fun clickUnArchive() {
        waitForViewWithId(R.id.inboxUnarchiveSelected).click()
    }

    /**
     * Clicks the star option in the action mode.
     */
    fun clickStar() {
        waitForViewWithId(R.id.inboxStarSelected).click()
    }

    /**
     * Clicks the unstar option in the action mode.
     */
    fun clickUnstar() {
        waitForViewWithId(R.id.inboxUnstarSelected).click()
    }

    /**
     * Clicks the mark as read option in the action mode.
     */
    fun clickMarkAsRead() {
        waitForViewWithId(R.id.inboxMarkAsReadSelected).click()
    }

    /**
     * Clicks the mark as unread option in the action mode.
     */
    fun clickMarkAsUnread() {
        waitForViewWithId(R.id.inboxMarkAsUnreadSelected).click()
    }

    /**
     * Clicks the delete option in the action mode.
     */
    fun clickDelete() {
        Espresso.openActionBarOverflowOrOptionsMenu(
            InstrumentationRegistry.getInstrumentation().getTargetContext()
        )
        onView(ViewMatchers.withText("Delete"))
            .perform(ViewActions.click());
    }

    /**
     * Confirms the delete action.
     */
    fun confirmDelete() {
        waitForView(withText("DELETE") + withAncestor(R.id.buttonPanel)).click()
    }

    /**
     * Swipes the conversation with the specified subject to the right.
     *
     * @param conversationSubject The subject of the conversation to swipe.
     */
    fun swipeConversationRight(conversationSubject: String) {
        waitForView(withId(R.id.inboxRecyclerView))
        val matcher = withText(conversationSubject)
        onView(matcher).scrollTo().swipeRight()
    }

    /**
     * Swipes the conversation to the right.
     *
     * @param conversation The conversation to swipe.
     */
    fun swipeConversationRight(conversation: ConversationApiModel) {
        swipeConversationRight(conversation.subject!!)
    }

    /**
     * Swipes the conversation to the right.
     *
     * @param conversation The conversation to swipe.
     */
    fun swipeConversationRight(conversation: Conversation) {
        swipeConversationRight(conversation.subject!!)
    }

    /**
     * Swipes the conversation with the specified subject to the left.
     *
     * @param conversationSubject The subject of the conversation to swipe.
     */
    fun swipeConversationLeft(conversationSubject: String) {
        waitForView(withId(R.id.inboxRecyclerView))
        val matcher = withText(conversationSubject)
        onView(matcher).scrollTo()
        onView(matcher).swipeLeft()
    }

    /**
     * Swipes the conversation to the left.
     *
     * @param conversation The conversation to swipe.
     */
    fun swipeConversationLeft(conversation: Conversation) {
        swipeConversationLeft(conversation.subject!!)
    }

    /**
     * Swipes the conversation to the left.
     *
     * @param conversation The conversation to swipe.
     */
    fun swipeConversationLeft(conversation: ConversationApiModel) {
        swipeConversationLeft(conversation.subject!!)
    }

    /**
     * Selects multiple conversations.
     *
     * @param conversations The list of conversation subjects to select.
     */
    fun selectConversations(conversations: List<String>) {
        for(conversation in conversations) {
            selectConversation(conversation)
        }
    }

    /**
     * Asserts the selected conversation number in the edit toolbar.
     *
     * @param selectedConversationNumber The expected selected conversation number.
     */
    fun assertSelectedConversationNumber(selectedConversationNumber: String) {
        onView(withText(selectedConversationNumber) + withAncestor(R.id.editToolbar))
    }

    /**
     * Asserts the visibility of the edit toolbar.
     *
     * @param visibility The expected visibility of the edit toolbar.
     */
    fun assertEditToolbarIs(visibility: ViewMatchers.Visibility) {
        editToolbar.assertVisibility(visibility)
    }

    /**
     * Asserts that the star icon is displayed.
     */
    fun assertStarDisplayed() {
        waitForViewWithId(R.id.inboxStarSelected).assertDisplayed()
    }

    /**
     * Asserts that the unstar icon is displayed.
     */
    fun assertUnStarDisplayed() {
        waitForViewWithId(R.id.inboxUnstarSelected).assertDisplayed()
    }
}
