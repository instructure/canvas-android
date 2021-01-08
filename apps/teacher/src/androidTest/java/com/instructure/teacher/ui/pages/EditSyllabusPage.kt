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
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TypeInRCETextEditor

class EditSyllabusPage : BasePage(R.id.editSyllabusPage) {

    private val toolbar by WaitForViewWithText(R.string.editSyllabusTitle)
    private val saveButton by OnViewWithId(R.id.menuSaveSyllabus)
    private val contentRceView by WaitForViewWithId(R.id.rce_webView)
    private val editSyllabusShowCourseSummarySwitch by OnViewWithText(R.id.showSummarySwitch)
    private val editSyllabusShowCourseSummaryLabel by OnViewWithId(R.id.showSummaryLabel)

    fun assertToolbarDisplayedWithCorrectTitle() {
        toolbar.assertDisplayed()
    }

    fun editSyllabusBody(text: String) {
        contentRceView.perform(TypeInRCETextEditor(text))
    }

    fun saveSyllabusEdit() {
        saveButton.click()
    }

    fun showSummary() {
        onView(withText("SUMMARY")).click()
    }

    fun editSyllabusToggleShowSummary() {
        editSyllabusShowCourseSummaryLabel.scrollTo()
        onView(withId(R.id.showSummarySwitch)).perform(withCustomConstraints(click(), isDisplayingAtLeast(50)))
    }

    fun assertHasSummaryEntry(calendarTitle: String) {
        onView(withId(R.id.syllabusItemTitle) + withText(calendarTitle)).assertDisplayed()
    }
}