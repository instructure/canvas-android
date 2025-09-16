/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.ui.pages.classic

import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.ImageViewDrawableMatcher
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.dataseeding.model.ModuleApiModel
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withDescendant
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.waitForCheck
import com.instructure.pandautils.utils.color
import com.instructure.student.R
import org.hamcrest.Matchers.allOf

class ModulesPage : BasePage(R.id.modulesPage) {
    fun assertModuleDisplayed(module: ModuleApiModel) {
        scrollRecyclerView(R.id.listView, withText(module.name))
    }

    fun assertModuleDisplayed(module: ModuleObject) {
        scrollRecyclerView(R.id.listView, withText(module.name))
    }

    fun clickModule(module: ModuleObject) {
        val matcher = allOf(withText(module.name), withAncestor(withId(R.id.listView)))
        onView(matcher).scrollTo().click()
    }

    /** Asserts that *any* module is locked.  It is exceedingly hard to check that the
     * "locked" icon is present, so this is the consolation prize. */
    fun assertAnyModuleLocked() {
        val matcher = allOf(withId(R.id.titleText), withText("Locked"))
        scrollRecyclerView(R.id.listView, matcher)
    }

    // Asserts that an assignment (presumably from a module) is locked
    fun assertAssignmentLocked(assignment: Assignment, course: Course) {
        val matcher = allOf(
            hasSibling(withText(assignment.name)),
            withId(R.id.indicator)
        )

        // Scroll to the assignment
        scrollRecyclerView(R.id.listView, matcher)

        // Make sure that the lock icon is showing, in the proper course color
        val courseColor = course.color
        onView(matcher).check(matches(ImageViewDrawableMatcher(R.drawable.ic_lock, courseColor)))

        // Make sure that clicking on the (locked) assignment does nothing
        onView(allOf(withId(R.id.title), withText(assignment.name))).click()
        onView(matcher).assertDisplayed()
    }

    fun assertModuleItemDisplayed(module: ModuleApiModel, itemTitle: String) {
        assertAndClickModuleItem(module.name, itemTitle)
    }

    fun assertModuleItemDisplayed(module: ModuleObject, itemTitle: String) {
        assertAndClickModuleItem(module.name!!, itemTitle)
    }

    fun assertModuleItemNotDisplayed(itemTitle: String) {
        onView(withText(itemTitle)).check(doesNotExist())
    }

    fun assertPossiblePointsDisplayed(points: String) {
        val matcher = withId(R.id.points) + withText("$points pts")

        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).assertDisplayed()
    }

    fun assertPossiblePointsNotDisplayed(name: String) {
        val matcher = withParent(hasSibling(withChild(withId(R.id.title) + withText(name)))) + withId(R.id.points)

        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).assertNotDisplayed()
    }

    /**
     * It is occasionally the case that we need to click a few extra buttons to get "fully" into
     * the item.  Thus the [extraClickIds] vararg param.
     */
    fun clickModuleItem(module: ModuleObject, itemTitle: String, vararg extraClickIds: Int) {
        assertAndClickModuleItem(module.name!!, itemTitle, true)
        for (extraClickId in extraClickIds) {
            onView(allOf(withId(extraClickId), isDisplayed())).click()
        }
    }

    // Assert that a module item is displayed and, optionally, click it
    fun assertAndClickModuleItem(moduleName: String, itemTitle: String, clickItem: Boolean = false) {
        try {
            scrollRecyclerView(R.id.listView, withText(itemTitle))
            if (clickItem) {
                onView(withText(itemTitle)).click()
            }
        } catch (ex: Exception) {
            when (ex) {
                is NoMatchingViewException, is PerformException -> {
                    // Maybe our module hasn't been expanded.  Click the module and try again.
                    val moduleMatcher = withText(moduleName)
                    scrollRecyclerView(R.id.listView, moduleMatcher)
                    onView(moduleMatcher).click()
                    scrollRecyclerView(R.id.listView, withText(itemTitle))
                    if (clickItem) {
                        onView(withText(itemTitle)).click()
                    }
                }
                else -> throw ex // Some other exception
            }
        }
    }

    fun assertEmptyView() {
        onView(allOf(withId(R.id.emptyView), withAncestor(R.id.modulesPage))).assertDisplayed()
    }

    fun refresh() {
        onView(allOf(withId(R.id.swipeRefreshLayout), isDisplayed())).perform(withCustomConstraints(swipeDown(), isDisplayingAtLeast(5)))
    }

    fun clickOnModuleExpandCollapseIcon(moduleName: String) {
        onView(withId(R.id.expandCollapse) + hasSibling(withChild(withText(moduleName) + withId(R.id.title)))).click()
    }

    fun assertModulesAndItemsCount(expectedCount: Int) {
        onView(withId(R.id.listView) + withDescendant(withId(R.id.title))).waitForCheck(RecyclerViewItemCountAssertion(expectedCount))
    }
}
