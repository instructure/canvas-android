/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.student.ui.pages.offline

import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.hasCheckedState
import com.instructure.espresso.ConstraintLayoutItemCountAssertion
import com.instructure.espresso.ConstraintLayoutItemCountAssertionWithMatcher
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.actions.ForceClick
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.pandautils.R
import org.hamcrest.CoreMatchers.allOf

class ManageOfflineContentPage : BasePage(R.id.manageOfflineContentPage) {

    private val syncButton by OnViewWithId(R.id.syncButton)
    private val storageInfoContainer by WaitForViewWithId(R.id.storageInfoContainer)

    //OfflineMethod
    fun changeItemSelectionState(courseName: String) {
        onView(withId(R.id.checkbox) + hasSibling(withId(R.id.title) + withText(courseName))).scrollTo().click()
    }

    //OfflineMethod
    fun expandCollapseItem(itemName: String) {
        onView(withId(R.id.arrow) + withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE) + hasSibling(withId(R.id.title) + withText(itemName))).scrollTo().perform(ForceClick())
    }

    //OfflineMethod
    fun expandCollapseFiles() {
        expandCollapseItem("Files")
    }

    //OfflineMethod
    fun clickOnSyncButton() {
        syncButton.click()
        confirmSync()
    }

    //OfflineMethod
    private fun confirmSync() {
        waitForView(withText("Sync") + withAncestor(R.id.buttonPanel)).click()
    }

    //OfflineMethod
    fun confirmDiscardChanges() {
        waitForView(withText("Discard") + withAncestor(R.id.buttonPanel)).click()
    }

    //OfflineMethod
    fun assertStorageInfoDetails() {
        onView(withId(R.id.storageLabel) + withText(R.string.offline_content_storage)).assertDisplayed()
        onView(withId(R.id.storageInfo) + containsTextCaseInsensitive("Used")).assertDisplayed()
        onView(withId(R.id.progress) + withParent(withId(R.id.storageInfoContainer))).assertDisplayed()
        onView(withId(R.id.otherLabel) + withText(R.string.offline_content_other)).assertDisplayed()
        onView(withId(R.id.canvasLabel) + withText(R.string.offline_content_canvas_student)).assertDisplayed()
        onView(withId(R.id.remainingLabel) + withText(R.string.offline_content_remaining)).assertDisplayed()
    }

    //OfflineMethod
    fun assertSelectButtonText(selectAll: Boolean) {
        if(selectAll) waitForView(withId(R.id.menu_select_all) + withText(R.string.offline_content_select_all)).assertDisplayed()
        else waitForView(withId(R.id.menu_select_all) + withText(R.string.offline_content_deselect_all)).assertDisplayed()
    }

    //OfflineMethod
    fun clickOnSelectAllButton() {
        waitForView(withId(R.id.menu_select_all) + withText(R.string.offline_content_select_all)).click()
    }

    //OfflineMethod
    fun clickOnDeselectAllButton() {
        waitForView(withId(R.id.menu_select_all) + withText(R.string.offline_content_deselect_all)).click()
    }

    //OfflineMethod
    fun assertCourseCountWithMatcher(expectedCount: Int) {
        ConstraintLayoutItemCountAssertionWithMatcher((allOf(withId(R.id.arrow), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))), expectedCount)
    }

    //OfflineMethod
    fun assertCourseCount(expectedCount: Int) {
        onView((allOf(withId(R.id.arrow), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))).check(ConstraintLayoutItemCountAssertion(expectedCount))
    }

    //OfflineMethod
    fun assertToolbarTexts(courseName: String) {
        onView(withText(courseName) + withParent(R.id.toolbar) + withAncestor(R.id.manageOfflineContentPage)).assertDisplayed()
        onView(withText(R.string.offline_content_toolbar_title) + withParent(R.id.toolbar) + withAncestor(R.id.manageOfflineContentPage)).assertDisplayed()
    }

    fun assertCheckedStateOfItem(itemName: String, state: Int) {
        onView(withId(R.id.checkbox) + hasSibling(withId(R.id.title) + withText(itemName)) + hasCheckedState(state)).scrollTo().assertDisplayed()
    }

}
