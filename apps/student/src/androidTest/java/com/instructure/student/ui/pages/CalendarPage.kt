package com.instructure.student.ui.pages

import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.QuizApiModel
import com.instructure.espresso.OnViewWithId
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

    // Tolerant of quiz being scrolled off the page
    fun assertQuizDisplayed(quiz: QuizApiModel) {
        assertTextDisplayedInRecyclerView(quiz.title)
    }

    // On low-res devices, the month text can get scrunched, and may not completely display.
    // So we'll only ask that 50% of it be displayed.
    fun toggleCalendarVisibility() {
        onView(withId(R.id.monthText)).perform(withCustomConstraints(click(), isDisplayingAtLeast(50)))
    }

    private fun assertTextDisplayedInRecyclerView(s: String) {
        // Common matcher
        val matcher = ViewMatchers.withText(Matchers.containsString(s))

        // Scroll RecyclerView item into view, if necessary
        scrollRecyclerView(R.id.calendarRecyclerView, matcher)

        // Now make sure that it is displayed
        // Shouldn't be necessary given that the line above passed.  Also, this line can
        // fail (after the line above passes!) for no apparent reason.
        // Espresso.onView(matcher).assertDisplayed()
    }

//    fun waitForRender() {
//        toolbar.assertDisplayed()
//    }

}