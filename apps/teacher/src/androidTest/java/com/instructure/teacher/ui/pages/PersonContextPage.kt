/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
 */
@file:Suppress("unused")

package com.instructure.teacher.ui.pages

import androidx.test.espresso.assertion.ViewAssertions.matches
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertContainsText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.plus
import com.instructure.espresso.pages.withAncestor
import com.instructure.espresso.pages.withParent
import com.instructure.espresso.pages.withText
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not

/**
 * Represents the Person Context Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the elements on the "Person Context" page.
 * It contains properties for accessing various views on the page such as the toolbar, student name, student email, course name, section name, and last activity.
 * Additionally, it provides methods for asserting the display of course information, section name based on the user role, and the person name.
 */
open class PersonContextPage : BasePage(R.id.studentContextPage) {

    val toolbar by WaitForViewWithId(R.id.toolbar)
    val studentName by WaitForViewWithId(R.id.studentNameView)
    val studentEmail by WaitForViewWithId(R.id.studentEmailView)
    val courseName by WaitForViewWithId(R.id.courseNameView)
    val sectionName by WaitForViewWithId(R.id.sectionNameView)
    val lastActivity by WaitForViewWithId(R.id.lastActivityView, autoAssert = false)

    /**
     * Asserts the display of course information.
     *
     * @param course The course to assert.
     */
    fun assertDisplaysCourseInfo(course: CourseApiModel) {
        courseName.assertHasText(course.name)
    }

    /**
     * Asserts the section name view based on the user role.
     *
     * @param userRole The user role.
     */
    fun assertSectionNameView(userRole: UserRole) {
        when (userRole) {
            UserRole.TEACHER -> sectionName.check(matches(containsTextCaseInsensitive("Teacher")))
            UserRole.OBSERVER -> sectionName.check(matches(containsTextCaseInsensitive("Observer")))
            UserRole.STUDENT -> sectionName.check(matches(allOf(not(containsTextCaseInsensitive("Teacher")), not(containsTextCaseInsensitive("Observer")))))
        }
    }

    /**
     * Asserts that the person name is displayed.
     *
     * @param personName The name of the person to assert.
     */
    fun assertPersonNameIsDisplayed(personName: String) {
        studentName.check(matches(withText(personName))).assertDisplayed()
    }

    /**
     * Asserts that the person has the corresponding pronouns.
     *
     * @param pronounString The pronouns of the person to assert.
     */
    fun assertPersonPronouns(pronounString: String) {
        studentName.assertContainsText(pronounString)
    }

    /**
     * Asserts that the person has the corresponding pronouns on the Toolbar.
     *
     * @param personName The person name to assert.
     * @param pronounString The pronouns of the person to assert.
     */
    fun assertPersonToolbarPronouns(personName: String, pronounString: String) {
        onView(withText(personName) + withParent(com.instructure.pandautils.R.id.toolbar) + withAncestor(R.id.studentContextPage))
            .assertDisplayed().
            assertContainsText(pronounString)
    }

    /**
     * Enum class representing the user roles.
     */
    enum class UserRole {
        TEACHER, STUDENT, OBSERVER
    }
}
