package com.instructure.student.ui.pages

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.QuizApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.student.R
import org.hamcrest.Matchers

class TodoPage: BasePage(R.id.todoPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val listview by OnViewWithId(R.id.listview, autoAssert = false)

    fun assertAssignmentDisplayed(assignment: AssignmentApiModel) {
        assertTextDisplayedInRecyclerView(assignment.name)
    }

    fun assertQuizDisplayed(quiz: QuizApiModel) {
        assertTextDisplayedInRecyclerView(quiz.title)
    }

    // Assert that a string is displayed somewhere in the RecyclerView
    private fun assertTextDisplayedInRecyclerView(s: String) {
        // Common matcher
        val matcher = ViewMatchers.withText(Matchers.containsString(s))

        // Scroll RecyclerView item into view, if necessary
        scrollRecyclerView(R.id.listView, matcher)

        // Now make sure that it is displayed
        Espresso.onView(matcher).assertDisplayed()
    }
}