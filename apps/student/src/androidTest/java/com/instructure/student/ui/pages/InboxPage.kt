package com.instructure.student.ui.pages

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.ConversationApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.GroupApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.OnViewWithStringText
import com.instructure.espresso.OnViewWithText
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.typeText
import com.instructure.student.R
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf

class InboxPage : BasePage(R.id.inboxPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val createMessageButton by OnViewWithId(R.id.addMessage)

    private fun scrollToRecyclerViewItem(matcher: Matcher<View>) {
        // Scroll RecyclerView item into view, if necessary
        onView(ViewMatchers.withId(R.id.inboxRecyclerView))
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(matcher)))
    }

    fun assertConversationDisplayed(conversation: ConversationApiModel) {
        val matcher = withText(conversation.subject)
        scrollToRecyclerViewItem(matcher)
        onView(matcher).assertDisplayed()
    }

    fun selectConversation(conversation: ConversationApiModel) {
        val matcher = withText(conversation.subject)
        scrollToRecyclerViewItem(matcher)
        onView(matcher).click()
    }

    fun pressNewMessageButton() {
        createMessageButton.clickPartial()
    }

    fun ViewInteraction.clickPartial() {
        perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isEnabled() // no constraints, they are checked above
            }

            override fun getDescription(): String {
                return "hit partially visible view"
            }

            override fun perform(uiController: UiController, view: View) {
                view.performClick()
            }
        })
    }
}