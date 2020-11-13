package com.instructure.teacher.ui.pages

import android.view.View
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher

class DashboardPage : BasePage() {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val editFavoriteCourses by WaitForViewWithId(R.id.menu_edit_favorite_courses)
    private val coursesPageLabel by WaitForViewWithStringText("Courses")
    private val emptyView by OnViewWithId(R.id.emptyCoursesView, autoAssert = false)
    private val coursesView by OnViewWithId(R.id.swipeRefreshLayout, autoAssert = false)
    private val coursesHeaderWrapper by OnViewWithId(R.id.coursesHeaderWrapper, autoAssert = false)
    private val courseLabel by WaitForViewWithId(R.id.courseLabel)
    private val seeAllCoursesButton by WaitForViewWithId(R.id.seeAllTextView)
    private val bottomBar by OnViewWithId(R.id.bottomBar)
    private val coursesTab by WaitForViewWithId(R.id.tab_courses)
    private val todoTab by WaitForViewWithId(R.id.tab_todo)
    private val inboxTab by WaitForViewWithId(R.id.tab_inbox)

    private val hamburgerButtonMatcher = allOf(withContentDescription(R.string.navigation_drawer_open), isDisplayed())

    fun assertDisplaysCourse(course: CourseApiModel) {
        val matcher = allOf(withText(course.name), withId(R.id.titleTextView), withAncestor(R.id.swipeRefreshLayout))
        scrollAndAssertDisplayed(matcher)
    }

    fun assertDisplaysCourses() {
        emptyView.assertNotDisplayed()
        onView(withParent(R.id.toolbar) + withText(R.string.courses)).assertDisplayed()
        coursesView.assertDisplayed()
        seeAllCoursesButton.assertDisplayed()
    }

    fun assertOpensCourse(course: CourseApiModel) {
        assertDisplaysCourse(course)
        onView(withText(course.name)).click()
        onView(withId(R.id.courseBrowserTitle)).assertContainsText(course.name)
        onView(withParent(R.id.overlayToolbar) + withContentDescription("Navigate up")).click()
    }

    fun clickCoursesTab() {
        onView(withId(R.id.tab_courses)).click()
        onView(withParent(R.id.toolbar) + withText(R.string.courses)).assertDisplayed()
    }

    fun clickInboxTab() {
        onView(withId(R.id.tab_inbox)).click()
        onView(withParent(R.id.toolbar) + withText(R.string.inbox)).assertDisplayed()
    }

    fun clickTodoTab() {
        onView(withId(R.id.tab_todo)).click()
        onView(withParent(R.id.toDoToolbar) + withText("To Do")).assertDisplayed()
    }

    private fun scrollAndAssertDisplayed(matcher: Matcher<View>) {
        onView(matcher).assertDisplayed()
    }

    fun waitForRender() {
        onView(hamburgerButtonMatcher).waitForCheck(matches(isDisplayed()))
    }
}