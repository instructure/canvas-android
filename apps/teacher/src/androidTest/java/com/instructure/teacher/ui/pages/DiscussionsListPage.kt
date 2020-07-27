package com.instructure.teacher.ui.pages

import androidx.test.espresso.action.ViewActions
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.dataseeding.model.DiscussionApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.teacher.R

class DiscussionsListPage : BasePage() {

    private val discussionListToolbar by OnViewWithId(R.id.discussionListToolbar)
    private val discussionsFAB by OnViewWithId(R.id.createNewDiscussion)
    private val discussionsRecyclerView by OnViewWithId(R.id.discussionRecyclerView)
    private val searchButton by OnViewWithId(R.id.search)
    private val searchInput by WaitForViewWithId(androidx.appcompat.R.id.search_src_text)

    fun clickDiscussion(discussion: DiscussionApiModel) {
        waitForViewWithText(discussion.title).click()
    }

    fun assertHasDiscussion(discussion: DiscussionApiModel) {
        waitForViewWithText(discussion.title).assertDisplayed()
    }

    fun assertHasDiscussion(discussion: DiscussionTopicHeader) {
        waitForViewWithText(discussion.title!!).assertDisplayed()
    }

    fun openSearch() {
        searchButton.click()
    }

    fun enterSearchQuery(query: String) {
        searchInput.perform(ViewActions.replaceText(query))
    }

    fun assertDiscussionCount(count: Int) {
        discussionsRecyclerView.waitForCheck(RecyclerViewItemCountAssertion(count))
    }
}
