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

import com.google.android.material.chip.Chip
import com.instructure.canvas.espresso.typedViewCondition
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.teacher.R

class AddMessagePage: BasePage() {

    private val subjectTextView by WaitForViewWithId(R.id.subjectView)
    private val chipsInput by WaitForViewWithId(R.id.chips)
    private val chipGroup by WaitForViewWithId(R.id.chipGroup)
    private val messageEditText by WaitForViewWithId(R.id.message)
    private val sendButton by WaitForViewWithId(R.id.menu_send)
    private val coursesSpinner by WaitForViewWithId(R.id.courseSpinner)
    private val editSubjectEditText by WaitForViewWithId(R.id.editSubject)
    private val addContactsButton by WaitForViewWithId(R.id.contactsImageButton)

    override fun assertPageObjects(duration: Long) {
        subjectTextView.assertDisplayed()
        chipsInput.assertDisplayed()
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

    fun selectCourseFromSpinner(course: Course) {
        selectCourseFromSpinner(course.name)
    }

    fun selectCourseFromSpinner(courseName: String) {
        waitForViewWithText(courseName).click()
    }

    fun clickAddContacts() {
        addContactsButton.click()
    }

    fun assertHasStudentRecipient(student: CanvasUserApiModel) {
        chipGroup.assertHasChild(typedViewCondition<Chip> { it.text.toString() == student.shortName })
    }
    fun assertHasStudentRecipient(student: User) {
        chipGroup.assertHasChild(typedViewCondition<Chip> { it.text.toString() == student.shortName })
    }

    fun addNewMessage() {
        val subject = randomString()
        val message = randomString()
        editSubjectEditText.replaceText(subject)
        messageEditText.replaceText(message)
        sendButton.click()
    }

    fun addSubject(subject: String) {
        editSubjectEditText.replaceText(subject)
    }

    fun addMessage(message: String) {
        messageEditText.scrollTo()
        messageEditText.replaceText(message)
    }

    fun clickSendButton() {
        sendButton.click()
    }
}
