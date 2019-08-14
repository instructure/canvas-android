/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.pages

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.GroupApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.typeText
import com.instructure.student.R
import org.hamcrest.Matchers.allOf

class NewMessagePage : BasePage() {

    private val subjectTextView by OnViewWithId(R.id.subjectView)
    private val recipientsEditTextView by OnViewWithId(R.id.recipientsView, autoAssert = false)
    private val sendButton by OnViewWithId(R.id.menu_send)
    private val coursesSpinner by OnViewWithId(R.id.courseSpinner)
    private val editSubjectEditText by OnViewWithId(R.id.editSubject)
    private val addContactsButton by OnViewWithId(R.id.contactsImageButton)
    private val sendIndividualMessageSwitch by OnViewWithId(R.id.sendIndividualSwitch)

    fun selectCourse(course: CourseApiModel) {
        coursesSpinner.assertDisplayed()
        coursesSpinner.click()
        onView(ViewMatchers.withText(course.name)).click()
    }

    fun selectGroup(group: GroupApiModel) {
        coursesSpinner.assertDisplayed()
        coursesSpinner.click()
        onView(ViewMatchers.withText(group.name)).click()
    }

    fun setRecipient(user: CanvasUserApiModel, isGroupRecipient: Boolean = false) {
        addContactsButton.click()
        if(!isGroupRecipient) onView(withText("Students")).click()
        onView(withText(user.shortName)).click()
        onView(withText(R.string.done)).click()
    }

    fun setSubject(subject: String) {
        editSubjectEditText.typeText(subject)
    }

    fun setMessage(messageText: String) {
        onView(allOf(withId(R.id.message), isDisplayed())).typeText(messageText)
        //messageEditText.typeText(messageText)
    }

    fun hitSend() {
        sendButton.click()
    }

    fun populateMessage(course: CourseApiModel, toUser: CanvasUserApiModel, subject: String, message: String) {
        selectCourse(course)
        setRecipient(toUser)
        setSubject(subject)
        Espresso.closeSoftKeyboard()
        setMessage(message)
        Espresso.closeSoftKeyboard()
    }

    fun populateGroupMessage(group: GroupApiModel, toUser: CanvasUserApiModel, subject: String, message: String) {
        selectGroup(group)
        setRecipient(toUser, true)
        setSubject(subject)
        Espresso.closeSoftKeyboard()
        setMessage(message)
        Espresso.closeSoftKeyboard()
    }
}