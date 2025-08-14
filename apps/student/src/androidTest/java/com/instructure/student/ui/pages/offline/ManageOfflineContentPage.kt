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

import androidx.test.espresso.Espresso
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.hasCheckedState
import com.instructure.canvas.espresso.withRotation
import com.instructure.espresso.ConstraintLayoutItemCountAssertion
import com.instructure.espresso.ConstraintLayoutItemCountAssertionWithMatcher
import com.instructure.espresso.DoesNotExistAssertion
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.actions.ForceClick
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.matchers.WaitForViewMatcher
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
import com.instructure.pandautils.binding.BindableViewHolder
import org.hamcrest.CoreMatchers.allOf

class ManageOfflineContentPage : BasePage(R.id.manageOfflineContentPage) {

    private val syncButton by OnViewWithId(R.id.syncButton)
    private val storageInfoContainer by WaitForViewWithId(R.id.storageInfoContainer)

    fun changeItemSelectionState(itemName: String) {
        onView(withId(R.id.offlineContentRecyclerView))
            .perform(RecyclerViewActions.scrollTo<BindableViewHolder>(hasDescendant(withText(itemName))))
        onView(withId(R.id.checkbox) + hasSibling(withId(R.id.title) + withText(itemName))).scrollTo().click()
    }

    fun expandCollapseItem(itemName: String) {
        onView(withId(R.id.arrow) + withEffectiveVisibility(Visibility.VISIBLE) + hasSibling(withId(R.id.title) + withText(itemName))).scrollTo().perform(ForceClick())
    }

    fun expandCollapseFiles() {
        expandCollapseItem("Files")
    }

    fun clickOnSyncButton() {
        syncButton.click()
    }

    fun clickOnSyncButtonAndConfirm() {
        clickOnSyncButton()
        confirmSync()
    }

    private fun confirmSync() {
        waitForView(withText("Sync") + withAncestor(R.id.buttonPanel)).click()
    }

    fun confirmDiscardChanges() {
        waitForView(withText("Discard") + withAncestor(R.id.buttonPanel)).click()
    }

    fun assertStorageInfoDetails() {
        onView(withId(R.id.storageLabel) + withText(R.string.offline_content_storage)).assertDisplayed()
        onView(withId(R.id.storageInfo) + containsTextCaseInsensitive("Used")).assertDisplayed()
        onView(withId(R.id.progress) + withParent(withId(R.id.storageInfoContainer))).assertDisplayed()
        onView(withId(R.id.otherLabel) + withText(R.string.offline_content_other)).assertDisplayed()
        onView(withId(R.id.canvasLabel) + withText(R.string.offline_content_canvas)).assertDisplayed()
        onView(withId(R.id.remainingLabel) + withText(R.string.offline_content_remaining)).assertDisplayed()
    }

    fun assertSelectButtonText(selectAll: Boolean) {
        if (selectAll) waitForView(withId(R.id.menu_select_all) + withText(R.string.offline_content_select_all)).assertDisplayed()
        else waitForView(withId(R.id.menu_select_all) + withText(R.string.offline_content_deselect_all)).assertDisplayed()
    }

    fun clickOnSelectAllButton() {
        waitForView(withId(R.id.menu_select_all) + withText(R.string.offline_content_select_all)).click()
    }

    fun clickOnDeselectAllButton() {
        waitForView(withId(R.id.menu_select_all) + withText(R.string.offline_content_deselect_all)).click()
    }

    fun assertCourseCountWithMatcher(expectedCount: Int) {
        ConstraintLayoutItemCountAssertionWithMatcher((allOf(withId(R.id.arrow), withEffectiveVisibility(Visibility.VISIBLE))), expectedCount)
    }

    fun assertCourseCount(expectedCount: Int) {
        onView((allOf(withId(R.id.arrow), withEffectiveVisibility(Visibility.VISIBLE)))).check(ConstraintLayoutItemCountAssertion(expectedCount))
    }

    fun assertToolbarTexts(courseName: String) {
        onView(withText(courseName) + withParent(R.id.toolbar) + withAncestor(R.id.manageOfflineContentPage)).assertDisplayed()
        onView(withText(R.string.offline_content_toolbar_title) + withParent(R.id.toolbar) + withAncestor(R.id.manageOfflineContentPage)).assertDisplayed()
    }

    fun assertCheckedStateOfItem(itemName: String, state: Int) {
        val matcher = withId(R.id.checkbox) + hasSibling(withId(R.id.title) + withText(itemName)) + hasCheckedState(state)
        onView(withId(R.id.offlineContentRecyclerView))
            .perform(RecyclerViewActions.scrollTo<BindableViewHolder>(hasDescendant(withText(itemName))))
        onView(matcher).scrollTo().assertDisplayed()
    }

    fun waitForItemDisappear(itemName: String) {
        waitForView(withId(R.id.checkbox) + hasSibling(withId(R.id.title) + withText(itemName))).check(DoesNotExistAssertion(5))
    }

    fun assertDisplaysNoCourses() {
        Espresso.onView(ViewMatchers.withText(R.string.offline_content_empty_message)).assertDisplayed()
    }

    fun assertDisplaysEmptyCourse() {
        Espresso.onView(ViewMatchers.withText(R.string.offline_content_empty_course_message)).scrollTo().assertDisplayed()
    }

    fun assertDisplaysItemWithExpandedState(title: String, expanded: Boolean) {
        Espresso.onView(
            ViewMatchers.withId(R.id.arrow)
                    + withRotation(if (expanded) 180f else 0f)
                    + withEffectiveVisibility(Visibility.VISIBLE)
                    + hasSibling(ViewMatchers.withId(R.id.title) + ViewMatchers.withText(title))
        ).scrollTo().assertDisplayed()
    }

    fun assertItemDisplayed(title: String) {
        val matcher = withId(R.id.title) + withText(title)
        onView(withId(R.id.offlineContentRecyclerView))
            .perform(RecyclerViewActions.scrollTo<BindableViewHolder>(hasDescendant(matcher)))
        onView(matcher).scrollTo().assertDisplayed()
    }

    fun assertDiscardDialogDisplayed() {
        WaitForViewMatcher.waitForView(ViewMatchers.withText(R.string.offline_content_discard_dialog_title))
            .assertDisplayed()
    }

    fun assertSyncDialogDisplayed(text: String) {
        WaitForViewMatcher.waitForView(ViewMatchers.withText(text)).assertDisplayed()
    }

    fun assertStorageInfoText(storageInfoText: String) {
        Espresso.onView(
            ViewMatchers.withId(R.id.storageInfo) + ViewMatchers.withText(
                storageInfoText
            )
        ).assertDisplayed()
    }
}



