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

import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.teacher.R
import com.instructure.canvasapi2.models.User

class AddMessagePage: BasePage() {

    private val subjectTextView by WaitForViewWithId(R.id.subjectView)
    private val recipientsEditTextView by WaitForViewWithId(R.id.recipient)
    private val messageEditText by WaitForViewWithId(R.id.message)
    private val sendButton by WaitForViewWithId(R.id.menu_send)
    private val coursesSpinner by WaitForViewWithId(R.id.courseSpinner)
    private val editSubjectEditText by WaitForViewWithId(R.id.editSubject)
    private val addContactsButton by WaitForViewWithId(R.id.contactsImageButton)

    override fun assertPageObjects() {
        subjectTextView.assertDisplayed()
        recipientsEditTextView.assertDisplayed()
    }

    fun addReply(message: String) {
        messageEditText.replaceText(message)
        sendButton.click()
    }

    fun assertComposeNewMessageObjectsDisplayed() {
        coursesSpinner.assertDisplayed()
        editSubjectEditText.assertDisplayed()
    }

    fun clickCourseSpinner() {
        coursesSpinner.click()
    }

    fun selectCourseFromSpinner(course: CourseApiModel) {
        waitForViewWithText(course.name).click()
    }

    fun clickAddContacts() {
        addContactsButton.click()
    }

    fun assertHasStudentRecipient(student: CanvasUserApiModel) {
        recipientsEditTextView.assertContainsText(student.shortName)
    }
    fun assertHasStudentRecipient(student: User) {
        recipientsEditTextView.assertContainsText(student.shortName!!)
    }

    fun addNewMessage() {
        val subject = randomString()
        val message = randomString()
        editSubjectEditText.replaceText(subject)
        messageEditText.replaceText(message)
        sendButton.click()
    }
}
