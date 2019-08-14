package com.instructure.student.ui.pages

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvasapi2.models.Course
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.withAncestor
import com.instructure.student.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

class EditFavoritesPage : BasePage(R.id.editFavoritesPage) {

    fun assertCourseDisplayed(course: Course) {
        val itemMatcher = allOf(withText(containsString(course.originalName)), withId(R.id.title))
//        onView(allOf(withId(R.id.listView), withAncestor(R.id.editFavoritesPage)))
//                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(itemMatcher)))
        onView(itemMatcher).assertDisplayed()
    }

    fun assertCourseFavorited(course: Course) {
        val itemMatcher = allOf(
                withContentDescription(containsString(", favorite")),
                withText(containsString(course.originalName)),
                withId(R.id.title))
        onView(allOf(withId(R.id.listView), withAncestor(R.id.editFavoritesPage)))
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(itemMatcher)))
        onView(itemMatcher).assertDisplayed()
    }

    fun assertCourseNotFavorited(course: Course) {
        val itemMatcher = allOf(
                withContentDescription(containsString(", not favorite")),
                withText(containsString(course.originalName)),
                withId(R.id.title))
        onView(allOf(withId(R.id.listView), withAncestor(R.id.editFavoritesPage)))
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(itemMatcher)))
        onView(itemMatcher).assertDisplayed()
    }

    fun toggleCourse(course: Course) {
        val itemMatcher = allOf(withId(R.id.star), hasSibling(allOf(withText(containsString(course.originalName)), withId(R.id.title))))
        onView(allOf(withId(R.id.listView), withAncestor(R.id.editFavoritesPage)))
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(itemMatcher)))
                .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(itemMatcher), click()))
    }


}

