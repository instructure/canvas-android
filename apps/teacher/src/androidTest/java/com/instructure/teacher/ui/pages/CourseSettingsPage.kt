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
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onViewWithId
import com.instructure.espresso.pages.onViewWithText
import com.instructure.espresso.replaceText
import com.instructure.teacher.R
import org.hamcrest.Matchers.`is`

/**
 * Represents a page for the course settings.
 *
 * This class extends the `BasePage` class and provides methods for interacting with the course settings,
 * such as clicking on the course name, editing the course name, clicking on the set home page option,
 * selecting a new home page, and asserting the changes in the home page and course name.
 *
 * @constructor Creates an instance of the `CourseSettingsPage` class.
 */
class CourseSettingsPage : BasePage() {

    private val courseImage by OnViewWithId(R.id.courseImage)
    private val editCourseNameRootView by OnViewWithId(R.id.renameCourse)
    private val editHomeRootView by OnViewWithId(R.id.editCourseHomepage)
    private val courseNameLabel by OnViewWithId(R.id.courseNameLabel)
    private val courseHomePageText by OnViewWithId(R.id.courseHomePage)
    private val courseNameText by OnViewWithId(R.id.courseName)
    private val toolbar by OnViewWithId(R.id.toolbar)

    /**
     * Clicks on the course name.
     */
    fun clickCourseName() {
        editCourseNameRootView.click()
    }

    /**
     * Edits the course name with a new name.
     *
     * @param newName The new name for the course.
     * @return The new name for the course.
     */
    fun editCourseName(newName: String): String {
        val dialogNameEntry = onViewWithId(R.id.newCourseName)
        val dialogOkButton = onViewWithText(android.R.string.ok)
        dialogNameEntry.replaceText(newName)
        dialogOkButton.click()
        return newName
    }

    /**
     * Clicks on the set home page option.
     */
    fun clickSetHomePage() {
        editHomeRootView.click()
    }

    /**
     * Selects a new home page and returns its string representation.
     *
     * @return The string representation of the new home page.
     */
    fun selectNewHomePage(): String {
        var newHomePageString = ""
        val unselectedRadioButton =
            onView(checked(false) { newHomePageString = it })
        val dialogOkButton = onViewWithText(android.R.string.ok)
        unselectedRadioButton.click()
        dialogOkButton.click()

        return newHomePageString
    }

    /**
     * Asserts that the home page has been changed to the specified value.
     *
     * @param newHomePage The expected new home page value.
     * @throws AssertionError if the home page does not match the expected value.
     */
    fun assertHomePageChanged(newHomePage: String) {
        courseHomePageText.assertHasText(newHomePage)
    }

    /**
     * Asserts that the course name has been changed to the specified value.
     *
     * @param newCourseName The expected new course name.
     * @throws AssertionError if the course name does not match the expected value.
     */
    fun assertCourseNameChanged(newCourseName: String) {
        courseNameText.assertHasText(newCourseName)
        assertToolbarSubtitleHasText(newCourseName)
    }

    /**
     * Asserts that the toolbar subtitle has the specified text.
     *
     * @param newCourseName The expected text for the toolbar subtitle.
     * @throws AssertionError if the toolbar subtitle text does not match the expected value.
     */
    private fun assertToolbarSubtitleHasText(newCourseName: String) {
        toolbar.check(matches(matchToolbarText(`is`(newCourseName), false)))
    }
}

