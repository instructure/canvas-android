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
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasChild
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.waitForViewWithText
import com.instructure.espresso.replaceText
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R

/**
 * Add message page
 *
 * @constructor Create empty Add message page
 */
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

    /**
     * Add a (reply) message to an existing conversation.
     *
     * @param message text itself.
     */
    fun addReply(message: String) {
        messageEditText.replaceText(message)
        sendButton.click()
    }

    /**
     * Assert compose new message objects displayed
     *
     */
    fun assertComposeNewMessageObjectsDisplayed() {
        coursesSpinner.assertDisplayed()
        editSubjectEditText.assertDisplayed()
    }

    /**
     * Click course spinner
     *
     */
    fun clickCourseSpinner() {
        coursesSpinner.click()
    }

    /**
     * Select course from spinner
     *
     * @param course: Object that we would like to select based on it's name.
     */
    fun selectCourseFromSpinner(course: Course) {
        selectCourseFromSpinner(course.name)
    }

    /**
     * Select course from spinner
     *
     * @param courseName: That we would like to select
     */
    fun selectCourseFromSpinner(courseName: String) {
        waitForViewWithText(courseName).click()
    }

    /**
     * Click add contacts button
     *
     */
    fun clickAddContacts() {
        addContactsButton.click()
    }

    /**
     * Assert has student recipient
     *
     * @param student: The student object parameter.
     */
    fun assertHasStudentRecipient(student: User) {
        chipGroup.assertHasChild(typedViewCondition<Chip> { it.text.toString() == student.shortName })
    }

    /**
     * Replace the message subject with the given parameter.
     *
     * @param subject: New subject parameter.
     */
    private fun addSubject(subject: String) {
        editSubjectEditText.replaceText(subject)
    }

    /**
     * Replace the message body with the given parameter.
     *
     * @param message: New message body parameter.
     */
    private fun addMessage(message: String) {
        messageEditText.scrollTo()
        messageEditText.replaceText(message)
    }

    /**
     * Compose a message with subject
     *
     * @param subject: Subject of the message.
     * @param message: Message body.
     */
    fun composeMessageWithSubject(subject: String, message: String) {
        addSubject(subject)
        addMessage(message)
    }

    /**
     * Click send button.
     *
     */
    fun clickSendButton() {
        sendButton.click()
    }
}
