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
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.withText
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not

open class PersonContextPage : BasePage(R.id.studentContextPage) {

    val toolbar by WaitForViewWithId(R.id.toolbar)
    val studentName by WaitForViewWithId(R.id.studentNameView)
    val studentEmail by WaitForViewWithId(R.id.studentEmailView)
    val courseName by WaitForViewWithId(R.id.courseNameView)
    val sectionName by WaitForViewWithId(R.id.sectionNameView)
    val lastActivity by WaitForViewWithId(R.id.lastActivityView, autoAssert = false)

    fun assertDisplaysCourseInfo(course: CourseApiModel) {
        courseName.assertHasText(course.name)
    }

    fun assertSectionNameView(userRole: UserRole) {
        when (userRole) {
            UserRole.TEACHER -> sectionName.check(matches(containsTextCaseInsensitive("Teacher")))
            UserRole.OBSERVER -> sectionName.check(matches(containsTextCaseInsensitive("Observer")))
            UserRole.STUDENT -> sectionName.check(matches(allOf(not(containsTextCaseInsensitive("Teacher")), not(containsTextCaseInsensitive("Observer")))))
        }
    }

    fun assertPersonNameIsDisplayed(personName: String) {
        studentName.check(matches(withText(personName))).assertDisplayed()
    }

    enum class UserRole {
        TEACHER, STUDENT, OBSERVER
    }
}
