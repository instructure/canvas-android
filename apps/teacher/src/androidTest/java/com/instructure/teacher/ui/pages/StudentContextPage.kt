/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.click
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R

/**
 * Represents the Student Context Page.
 *
 * This page extends the PersonContextPage class and provides functionality for interacting with the elements on the
 * Student Context page. It contains methods for asserting the display of student information, student grade, student
 * submission, assignment listing, and assignment submission. It also provides a method for clicking on the new message
 * button.
 */
class StudentContextPage : PersonContextPage() {

    private val messageButton by WaitForViewWithId(R.id.messageButton)

    /**
     * Asserts the display of student information on the Student Context page.
     *
     * @param student The student for which the information should be displayed.
     */
    fun assertDisplaysStudentInfo(student: CanvasUserApiModel) {
        waitForView(withParent(R.id.toolbar) + withText(student.shortName)).assertDisplayed()
        studentName.assertHasText(student.shortName)
        studentEmail.assertHasText(student.loginId)
        onView(withId(R.id.gradeItems)).scrollTo().assertDisplayed()
    }

    /**
     * Asserts the student's grade on the Student Context page.
     *
     * @param grade The expected grade of the student.
     */
    fun assertStudentGrade(grade: String) {
        onView(withId(R.id.gradeBeforePosting)).assertHasText(grade)
    }

    /**
     * Asserts the student's submission count on the Student Context page.
     *
     * @param submittedCount The expected number of submitted items.
     */
    fun assertStudentSubmission(submittedCount: String) {
        onView(withId(R.id.submittedCount)).assertHasText(submittedCount)
    }

    /**
     * Asserts the listing of an assignment on the Student Context page.
     *
     * @param assignmentTitle The title of the assignment to be listed.
     */
    fun assertAssignmentListed(assignmentTitle: String) {
        onView(withId(R.id.assignmentTitle) + withText(assignmentTitle)).scrollTo().assertDisplayed()
    }

    /**
     * Asserts the submission status of an assignment on the Student Context page.
     */
    fun assertAssignmentSubmitted() {
        onView(withId(R.id.submissionStatus) + withText(R.string.submitted)).assertDisplayed()
    }

    /**
     * Clicks on the new message button on the Student Context page.
     */
    fun clickOnNewMessageButton() {
        messageButton.click()
    }
}
