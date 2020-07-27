package com.instructure.teacher.ui.pages

import com.instructure.canvasapi2.models.Conversation
import com.instructure.dataseeding.model.ConversationApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.WaitForToolbarTitle

class InboxPage: BasePage() {

    private val toolbarTitle by WaitForToolbarTitle(R.string.tab_inbox)

    private val inboxRecyclerView by WaitForViewWithId(R.id.inboxRecyclerView)

    private val addMessageFAB by WaitForViewWithId(R.id.addMessage)

    //Only displayed when inbox is empty
    private val emptyPandaView by WaitForViewWithId(R.id.emptyPandaView)
    private val filterText by OnViewWithId(R.id.filterText)

    override fun assertPageObjects() {
        toolbarTitle.assertDisplayed()
    }

    fun assertHasConversation() {
        inboxRecyclerView.check(RecyclerViewItemCountAssertion(1))
    }

    fun clickConversation(conversation: ConversationApiModel) {
        waitForViewWithText(conversation.subject).click()
    }

    fun clickConversation(conversation: Conversation) {
        waitForViewWithText(conversation.subject!!).click()
    }

    fun clickAddMessageFAB() {
        addMessageFAB.click()
    }
}
