package com.instructure.student.ui.pages

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.QuizApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.student.R
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf

class CalendarPage: BasePage(R.id.calendarPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val listview by OnViewWithId(R.id.listview, autoAssert = false)

    fun selectDesiredCalendarsAndDismiss(vararg courseNames: String) {
        for(courseName in courseNames) {
            onView(allOf(withText(courseName), isDisplayed(), withId(R.id.courseName))).click()
        }
        onView(withText(R.string.done)).click()
    }

    // Tolerant of assignment being scrolled off the page
    fun assertAssignmentDisplayed(assignment: AssignmentApiModel) {
        assertTextDisplayedInRecyclerView(assignment.name)
    }

    // Tolerant of quizResult being scrolled off the page
    fun assertQuizDisplayed(quiz: QuizApiModel) {
        assertTextDisplayedInRecyclerView(quiz.title)
    }

    private fun assertTextDisplayedInRecyclerView(s: String) {
        // Common matcher
        val matcher = ViewMatchers.withText(Matchers.containsString(s))

        // Scroll RecyclerView item into view, if necessary
        onView(withId(R.id.calendarRecyclerView)) // The drawer (not displayed) also has a listView
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(matcher)))

        // Now make sure that it is displayed
        Espresso.onView(matcher).assertDisplayed()
    }

//    fun waitForRender() {
//        toolbar.assertDisplayed()
//    }

}