/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.teacher.ui.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import com.instructure.canvas.espresso.checked
import com.instructure.canvas.espresso.matchToolbarText
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertHasText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.replaceText
import com.instructure.teacher.R
import org.hamcrest.Matchers.`is`

class CourseSettingsPage : BasePage() {

    private val courseImage by OnViewWithId(R.id.courseImage)
    private val editCourseNameRootView by OnViewWithId(R.id.renameCourse)
    private val editHomeRootView by OnViewWithId(R.id.editCourseHomepage)
    private val courseNameLabel by OnViewWithId(R.id.courseNameLabel)
    private val courseHomePageText by OnViewWithId(R.id.courseHomePage)
    private val courseNameText by OnViewWithId(R.id.courseName)
    private val toolbar by OnViewWithId(R.id.toolbar)

    fun clickCourseName() {
        editCourseNameRootView.click()
    }

    fun editCourseName(newName: String): String {
        val dialogNameEntry = onViewWithId(R.id.newCourseName)
        val dialogOkButton = onViewWithText(android.R.string.ok)
        dialogNameEntry.replaceText(newName)
        dialogOkButton.click()
        return newName
    }

    fun clickSetHomePage() {
        editHomeRootView.click()
    }

    fun selectNewHomePage(): String {
        var newHomePageString = ""
        val unselectedRadioButton =
                onView(checked(false) { newHomePageString = it })
        val dialogOkButton = onViewWithText(android.R.string.ok)
        unselectedRadioButton.click()
        dialogOkButton.click()

        return newHomePageString
    }

    fun assertHomePageChanged(newHomePage: String) {
        courseHomePageText.assertHasText(newHomePage)
    }

    fun assertCourseNameChanged(newCourseName: String) {
        courseNameText.assertHasText(newCourseName)
        assertToolbarSubtitleHasText(newCourseName)
    }

    fun assertToolbarSubtitleHasText(newCourseName: String) {
        toolbar.check(matches(matchToolbarText(`is`(newCourseName), false)))
    }
}
