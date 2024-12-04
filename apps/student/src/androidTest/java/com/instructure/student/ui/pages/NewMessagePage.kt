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

import android.view.View
import android.widget.TextView
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.GroupApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.onViewWithId
import com.instructure.espresso.pages.plus
import com.instructure.espresso.pages.withAncestor
import com.instructure.espresso.pages.withDescendant
import com.instructure.espresso.pages.withId
import com.instructure.espresso.pages.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.typeText
import com.instructure.student.R
import junit.framework.Assert.assertTrue
import org.hamcrest.Matchers.allOf

class NewMessagePage : BasePage() {

    private val subjectTextView by OnViewWithId(R.id.subjectView)
    private val chipGroup by OnViewWithId(R.id.chipGroup, autoAssert = false)
    private val sendButton by OnViewWithId(R.id.menu_send)
    private val coursesSpinner by OnViewWithId(R.id.courseSpinner)
    private val editSubjectEditText by OnViewWithId(R.id.editSubject)
    private val addContactsButton by OnViewWithId(R.id.contactsImageButton)
    private val sendIndividualMessageSwitch by OnViewWithId(R.id.sendIndividualSwitch)

    fun selectCourse(course: CourseApiModel) {
        coursesSpinner.assertDisplayed()
        coursesSpinner.click()
        onView(withText(course.name)).click()
    }

    fun selectCourse(course: Course) {
        coursesSpinner.assertDisplayed()
        coursesSpinner.click()
        onView(withText(course.name)).click()
    }

    fun selectGroup(group: GroupApiModel) {
        coursesSpinner.assertDisplayed()
        coursesSpinner.click()
        onView(ViewMatchers.withText(group.name)).click()
    }

    fun setRecipient(user: CanvasUserApiModel, isGroupRecipient: Boolean = false) {
        addContactsButton.click()
        if(!isGroupRecipient) onView(withText("Students")).click()
        onView(withId(R.id.title) + withText(user.shortName)).click()
        onView(withText(R.string.done)).click()
    }

    fun setRecipient(user: User, userType: String, isGroupRecipient: Boolean = false) {
        addContactsButton.click()
        if(!isGroupRecipient) onView(withText(userType)).click()
        onView(withText(user.shortName)).click()
        onView(withText(R.string.done)).click()
    }

    fun setRecipients(users: List<User>, userType: String, isGroupRecipient: Boolean = false) {
        addContactsButton.click()
        if(!isGroupRecipient) onView(withText(userType)).click()
        users.forEach {
            onView(withText(it.shortName)).click()
        }
        onView(withText(R.string.done)).click()
    }

    fun setRecipientGroup(userType: String) {
        addContactsButton.click()
        val itemMatcher = allOf(
                hasSibling(withChild(withText(userType))),
                withId(R.id.checkBox)
        )
        onView(itemMatcher).perform(click())
        onView(withText(R.string.done)).click()
    }

    fun selectAllRecipients(userTypes: List<String>) {
        addContactsButton.click()
        userTypes.forEach {
            val itemMatcher = allOf(
                    hasSibling(withChild(withText(it))),
                    withId(R.id.checkBox)
            )
            onView(itemMatcher).click()
        }
        onView(withText(R.string.done)).click()
    }

    fun assertRecipientGroupsNotDisplayed(userType: String) {
        addContactsButton.click()
        val itemMatcher = allOf(
                hasSibling(withChild(withText(userType))),
                withId(R.id.checkBox)
        )
        onView(itemMatcher).assertNotDisplayed()
    }

    fun assertRecipientGroupContains(userType: String, userCount: Int = 1) {
        addContactsButton.click()
        onView(withText(userType)).click()
        onViewWithId(R.id.recipientRecyclerView).check(RecyclerViewItemCountAssertion(userCount))
    }

    fun setSubject(subject: String) {
        editSubjectEditText.typeText(subject)
    }

    fun setMessage(messageText: String) {
        Espresso.closeSoftKeyboard()
        onView(allOf(withId(R.id.message), withAncestor(R.id.messageContainer)))
                .scrollTo()
                .typeText(messageText)
    }

    fun clickSend() {
        Espresso.closeSoftKeyboard()
        sendButton.click()
    }

    /**
     * Populate a normal non-group message
     * Fills in [course], [toUser], [subject] and [message] (content) for the message.
     * If [recipientPopulated] is true, then toUser is ignored.  If false, then [toUser] is put into the recipients section.
     */
    fun populateMessage(course: CourseApiModel, toUser: CanvasUserApiModel, subject: String, message: String, recipientPopulated: Boolean = false) {
        selectCourse(course)
        if(recipientPopulated) {
            chipGroup.check(matches(hasChildCount(1)))
        }
        else {
            chipGroup.check(matches(hasChildCount(0)))
            setRecipient(toUser)
        }
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

    fun assertToolbarTitleNewMessage() {
        onView(withId(R.id.toolbar) + withDescendant(withText(R.string.newMessage))).assertDisplayed()
    }

    fun assertCourseSelectorNotShown() {
        coursesSpinner.assertNotDisplayed()
    }

    fun assertRecipientsNotShown() {
        onViewWithId(R.id.recipientWrapper).assertNotDisplayed()
    }

    fun assertSendIndividualMessagesNotShown() {
        sendIndividualMessageSwitch.assertNotDisplayed()
    }

    fun assertSubjectViewShown() {
        onViewWithId(R.id.editSubject).assertDisplayed()
    }

    fun assertMessageViewShown() {
        onViewWithId(R.id.message)
    }
}

/** Custom ViewAssertion to make sure that a TextBox is empty */
class AssertTextEmpty : ViewAssertion {
    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        when(view) {
            is TextView -> assertTrue(view.text.isNullOrEmpty())
        }
    }
}

/** Custom ViewAssertion to make sure that a TextBox is populated with something */
class AssertTextPopulated : ViewAssertion {
    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        when(view) {
            is TextView -> assertTrue(view.text != null && view.text.length > 0)
        }
    }
}
