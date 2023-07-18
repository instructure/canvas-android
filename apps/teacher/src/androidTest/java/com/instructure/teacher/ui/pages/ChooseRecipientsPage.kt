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

import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.teacher.R

/**
 * Represents a page for choosing recipients.
 *
 * This class extends the `BasePage` class and provides methods for asserting page objects, checking for the presence of students,
 * and interacting with the page by clicking on various elements.
 *
 * @constructor Creates an instance of the `ChooseRecipientsPage` class.
 */
class ChooseRecipientsPage : BasePage() {

    private val toolbar by WaitForViewWithId(R.id.toolbar)
    private val recyclerView by WaitForViewWithId(R.id.recyclerView)
    private val menuDone by WaitForViewWithId(R.id.menuDone)
    private val checkBox by WaitForViewWithId(R.id.checkBox)

    /**
     * Asserts the presence of page objects within a specified duration.
     *
     * @param duration The duration to wait for the page objects to appear.
     * @throws AssertionError if any of the page objects fail to appear within the specified duration.
     */
    override fun assertPageObjects(duration: Long) {
        toolbar.assertDisplayed()
        recyclerView.assertDisplayed()
        menuDone.assertDisplayed()
    }

    /**
     * Asserts that the page has the "Students" category displayed.
     *
     * @throws AssertionError if the "Students" category is not displayed.
     */
    fun assertHasStudent() {
        waitForViewWithText("Students").assertDisplayed()
    }

    /**
     * Clicks the "Done" menu item.
     */
    fun clickDone() {
        menuDone.click()
    }

    /**
     * Clicks the "Students" category.
     */
    fun clickStudentCategory() {
        waitForViewWithText("Students").click()
    }

    /**
     * Clicks on a student with the provided User object.
     *
     * @param student The User object representing the student to click.
     */
    fun clickStudent(student: User) {
        waitForViewWithText(student.shortName!!).click()
    }

    /**
     * Clicks on a student with the provided CanvasUserApiModel object.
     *
     * @param student The CanvasUserApiModel object representing the student to click.
     */
    fun clickStudent(student: CanvasUserApiModel) {
        waitForViewWithText(student.shortName).click()
    }
}

