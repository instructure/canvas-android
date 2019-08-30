package com.instructure.student.ui.pages

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.student.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf

class DiscussionListPage : BasePage(R.id.discussionListPage) {

    fun assertTopicDisplayed(topicTitle: String) {
        val matcher = allOf(withText(topicTitle), withId(R.id.discussionTitle))
        scrollToItem(matcher)
        onView(matcher).assertDisplayed()
    }

    fun selectTopic(topicTitle: String) {
        val matcher = allOf(withText(topicTitle), withId(R.id.discussionTitle))
        scrollToItem(matcher)
        onView(matcher).click()
    }

    private fun scrollToItem(matcher: Matcher<View>) {
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.discussionRecyclerView), ViewMatchers.isDisplayed()))
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(matcher)))
    }

}