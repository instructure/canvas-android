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

import androidx.test.espresso.AmbiguousViewMatcherException
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertInvisible
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.student.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not

// Allows us to test paging through modules
class ModuleProgressionPage : BasePage(R.id.moduleProgressionPage) {

    fun assertPreviousButtonDisplayed() {
        onView(withId(R.id.prev_item)).assertDisplayed()
    }

    fun assertPreviousButtonInvisible() {
        onView(withId(R.id.prev_item)).assertInvisible()
    }

    fun clickPreviousButton() {
        onView(withId(R.id.prev_item)).click()
    }

    fun assertNextButtonDisplayed() {
        onView(withId(R.id.next_item)).assertDisplayed()
    }

    fun assertNextButtonInvisible() {
        onView(withId(R.id.next_item)).assertInvisible()
    }

    fun clickNextButton() {
        onView(withId(R.id.next_item)).click()
    }

    fun assertModuleTitle(moduleTitle: String) {
        onView(allOf(withId(R.id.moduleName), isDisplayed()))
                .assertHasText(moduleTitle)
    }

    fun assertModuleItemTitleDisplayed(moduleItemTitle: String) {
        // Arggghh... Depending on the module item type, the item title can show up in the
        // tool bar, in the main area, or both (in the case of files).  So if it turns out to be
        // in both, then catch that and do another check excluding the toolbar instance
        try {
            onView(allOf(withText(moduleItemTitle), isDisplayed())).assertDisplayed()
        }
        catch(e: AmbiguousViewMatcherException) {
            onView(allOf(withText(moduleItemTitle), isDisplayed(), not(withParent(withId(R.id.toolbar))) )).assertDisplayed()
        }
    }
}