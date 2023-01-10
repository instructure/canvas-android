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
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R

class StudentContextPage : PersonContextPage() {

    private val messageButton by WaitForViewWithId(R.id.messageButton)

    fun assertDisplaysStudentInfo(student: CanvasUserApiModel) {
        waitForView(withParent(R.id.toolbar) + withText(student.shortName)).assertDisplayed()
        studentName.assertHasText(student.shortName)
        studentEmail.assertHasText(student.loginId)
        onView(withId(R.id.gradeItems)).scrollTo().assertDisplayed()
    }

    fun assertStudentGrade(grade: String) {
        onView(withId(R.id.gradeBeforePosting)).assertHasText(grade)
    }

    fun assertStudentSubmission(submittedCount: String) {
        onView(withId(R.id.submittedCount)).assertHasText(submittedCount)
    }

    fun assertAssignmentListed(assignmentTitle: String) {
        onView(withId(R.id.assignmentTitle) + withText(assignmentTitle)).scrollTo().assertDisplayed()
    }

    fun assertAssignmentSubmitted() {
        onView(withId(R.id.submissionStatus) + withText(R.string.submitted)).assertDisplayed()
    }

    fun clickOnNewMessageButton() {
        messageButton.click()
    }
}
