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
package com.instructure.student.ui.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.dataseeding.model.ModuleApiModel
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.withAncestor
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
        val matcher = withText(module.name)
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).click()
    }

    /** Asserts that *any* module is locked.  It is exceedingly hard to check that the
     * "locked" icon is present, so this is the consolation prize. */
    fun assertAnyModuleLocked() {
        val matcher = allOf(withId(R.id.titleText), withText("Locked"))
        scrollRecyclerView(R.id.listView, matcher)
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

    /**
     * Opening file modules requires the extra step of clicking the "Open" button,
     * thus the [clickOpenButton] parameter.
     */
    fun clickModuleItem(module: ModuleObject, itemTitle: String, clickOpenButton: Boolean = false) {
        assertAndClickModuleItem(module.name!!, itemTitle, true)
        if(clickOpenButton) {
            onView(withId(R.id.openButton)).click()
        }
    }

    // Assert that a module item is displayed and, optionally, click it
    private fun assertAndClickModuleItem(moduleName: String, itemTitle: String, clickItem: Boolean = false) {
        try {
            scrollRecyclerView(R.id.listView, withText(itemTitle))
            if(clickItem) {
                onView(withText(itemTitle)).click()
            }
        }
        catch(ex: Exception) {
            when(ex) {
                is NoMatchingViewException, is PerformException -> {
                    // Maybe our module hasn't been expanded.  Click the module and try again.
                    val moduleMatcher = withText(moduleName)
                    scrollRecyclerView(R.id.listView, moduleMatcher)
                    onView(moduleMatcher).click()
                    scrollRecyclerView(R.id.listView, withText(itemTitle))
                    if(clickItem) {
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
        onView(allOf(withId(R.id.swipeRefreshLayout),isDisplayed())).perform(withCustomConstraints(swipeDown(), isDisplayingAtLeast(5)))
    }
}