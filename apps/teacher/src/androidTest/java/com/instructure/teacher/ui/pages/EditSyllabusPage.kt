/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.ui.pages

import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.OnViewWithText
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.WaitForViewWithText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.withId
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TypeInRCETextEditor

/**
 * Represents the Edit Syllabus page.
 *
 * This page extends the BasePage class and provides functionality for editing the syllabus.
 * It contains various view elements such as a toolbar, save button, content RCE view,
 * and toggles for showing the course summary.
 *
 * @property toolbar The toolbar view displaying the title of the page.
 * @property saveButton The save button view for saving the syllabus edit.
 * @property contentRceView The content RCE view used for editing the syllabus body.
 * @property editSyllabusShowCourseSummarySwitch The switch view for toggling the display of the course summary.
 * @property editSyllabusShowCourseSummaryLabel The label view for the course summary toggle.
 */
class EditSyllabusPage : BasePage(R.id.editSyllabusPage) {

    private val toolbar by WaitForViewWithText(R.string.editSyllabusTitle)
    private val saveButton by OnViewWithId(R.id.menuSaveSyllabus)
    private val contentRceView by WaitForViewWithId(R.id.rce_webView)
    private val editSyllabusShowCourseSummarySwitch by OnViewWithText(R.id.showSummarySwitch)
    private val editSyllabusShowCourseSummaryLabel by OnViewWithId(R.id.showSummaryLabel)

    /**
     * Asserts that the toolbar is displayed with the correct title.
     */
    fun assertToolbarDisplayedWithCorrectTitle() {
        toolbar.assertDisplayed()
    }

    /**
     * Edits the syllabus body with the specified text.
     *
     * @param text The text to be entered in the syllabus body.
     */
    fun editSyllabusBody(text: String) {
        contentRceView.perform(TypeInRCETextEditor(text))
    }

    /**
     * Saves the syllabus edit by clicking the save button.
     */
    fun saveSyllabusEdit() {
        saveButton.click()
    }

    /**
     * Toggles the display of the course summary.
     */
    fun editSyllabusToggleShowSummary() {
        editSyllabusShowCourseSummaryLabel.scrollTo()
        onView(withId(R.id.showSummarySwitch)).perform(withCustomConstraints(click(), isDisplayingAtLeast(50)))
    }
}
