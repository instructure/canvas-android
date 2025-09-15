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
package com.instructure.teacher.ui.renderTests.renderPages

import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R
import org.hamcrest.Matchers.containsString

class EditSyllabusRenderPage : BasePage(R.id.editSyllabusPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val saveButton by OnViewWithId(R.id.menuSaveSyllabus)
    private val savingProgressBar by OnViewWithId(R.id.savingProgressBar)
    private val contentLabel by OnViewWithId(R.id.contentLabel)
    private val contentRceView by WaitForViewWithId(R.id.contentRCEView)
    private val detailsLabel by OnViewWithId(R.id.detailsLabel)
    private val showCourseSummaryLabel by OnViewWithId(R.id.showSummaryLabel)
    private val showCourseSummarySwitch by OnViewWithId(R.id.showSummarySwitch)

    fun assertLoadedStateDisplayed() {
        toolbar.assertDisplayed()
        saveButton.assertDisplayed()
        contentLabel.assertDisplayed()
        contentRceView.assertDisplayed()
        showCourseSummaryLabel.scrollTo()
        detailsLabel.assertDisplayed()
        showCourseSummaryLabel.assertDisplayed()
        showCourseSummarySwitch.assertDisplayed()
    }

    fun assertSavingStateDisplayed() {
        savingProgressBar.assertDisplayed()
    }

    fun assertCorrectDataDisplayed(syllabusBody: String, showSummary: Boolean) {
        val checkedMatcher = if (showSummary) ViewMatchers.isChecked() else ViewMatchers.isNotChecked()
        showCourseSummarySwitch.check(ViewAssertions.matches(checkedMatcher))

        onWebView()
            .withElement(findElement(Locator.TAG_NAME, "html"))
            .check(webMatches(getText(), containsString(syllabusBody)))
    }
}