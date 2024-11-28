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


import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import com.instructure.canvas.espresso.has
import com.instructure.canvas.espresso.hasTextInputLayoutErrorText
import com.instructure.canvas.espresso.withIndex
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.espresso.ClickUntilMethod
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertVisible
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.onViewWithContentDescription
import com.instructure.espresso.pages.onViewWithId
import com.instructure.espresso.pages.waitForView
import com.instructure.espresso.pages.waitForViewWithClassName
import com.instructure.espresso.pages.waitForViewWithId
import com.instructure.espresso.pages.waitForViewWithText
import com.instructure.espresso.pages.waitScrollClick
import com.instructure.espresso.pages.withId
import com.instructure.espresso.pages.withText
import com.instructure.espresso.randomString
import com.instructure.espresso.replaceText
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R
import com.instructure.teacher.view.AssignmentOverrideView
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matchers
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * The `EditQuizDetailsPage` class represents a page for editing quiz details.
 * It extends the `BasePage` class.
 */
class EditQuizDetailsPage : BasePage() {

    private val quizTitleEditText by OnViewWithId(R.id.editQuizTitle)
    private val publishSwitch by WaitForViewWithId(R.id.publishSwitch)
    private val accessCodeSwitch by WaitForViewWithId(R.id.accessCodeSwitch)
    private val accessCodeEditText by WaitForViewWithId(R.id.editAccessCode)
    private val saveButton by OnViewWithId(R.id.menuSave)
    private val descriptionWebView by OnViewWithId(R.id.descriptionWebView, autoAssert = false)
    private val noDescriptionTextView by OnViewWithId(
        R.id.noDescriptionTextView,
        autoAssert = false
    )

    /**
     * Saves the quiz by clicking the save button.
     */
    fun saveQuiz() {
        saveButton.click()
    }

    /**
     * Edits the quiz title with the specified new name.
     *
     * @param newName The new name to be set as the quiz title.
     */
    fun editQuizTitle(newName: String) {
        quizTitleEditText.replaceText(newName)
        saveQuiz()
    }

    /**
     * Clicks on the access code switch to toggle its state.
     */
    fun clickAccessCode() {
        accessCodeSwitch.scrollTo()
        accessCodeSwitch.click()
    }

    /**
     * Clicks on the access code edit text field.
     */
    fun clickAccessCodeEditText() {
        accessCodeEditText.scrollTo()
        accessCodeEditText.click()
    }

    /**
     * Edits the access code for the quiz and saves it.
     *
     * @return The new access code.
     */
    fun editAccessCode(): String {
        val code = randomString()
        accessCodeEditText.scrollTo()
        accessCodeEditText.replaceText(code)
        saveQuiz()
        return code
    }

    /**
     * Edits the date of the quiz with the specified year, month, and day.
     *
     * @param year The year value.
     * @param month The month value (0-11).
     * @param dayOfMonth The day of the month value.
     */
    fun editDate(year: Int, month: Int, dayOfMonth: Int) {
        waitForViewWithClassName(Matchers.equalTo<String>(DatePicker::class.java.name))
            .perform(PickerActions.setDate(year, month, dayOfMonth))
        onViewWithId(android.R.id.button1).click()
    }

    /**
     * Edits the time of the quiz with the specified hour and minute.
     *
     * @param hour The hour value.
     * @param min The minute value.
     */
    fun editTime(hour: Int, min: Int) {
        waitForViewWithClassName(Matchers.equalTo<String>(TimePicker::class.java.name))
            .perform(PickerActions.setTime(hour, min))
        onViewWithId(android.R.id.button1).click()
    }

    /**
     * Removes the second override for the quiz.
     */
    fun removeSecondOverride() {
        addOverrideButton().scrollTo()
        onViewWithContentDescription("remove_override_button_1").scrollTo()
        ClickUntilMethod.run(
            onView(withContentDescription("remove_override_button_1")),
            onView(withText("Remove Due Date"))
        )
        waitForViewWithText(R.string.removeDueDate).assertVisible()
        waitForViewWithText(R.string.remove).click()
    }

    /**
     * Asserts that the date has changed to the specified year, month, and day.
     *
     * @param year The expected year value.
     * @param month The expected month value (0-11).
     * * @param dayOfMonth The expected day of the month value.
     * @param id The resource ID of the view displaying the date.
     */
    fun assertDateChanged(year: Int, month: Int, dayOfMonth: Int, id: Int) {
        val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
        waitForViewWithId(id).assertHasText(DateHelper.fullMonthNoLeadingZeroDateFormat.format(cal.time))
    }

    /**
     * Asserts that the time has changed to the specified hour and minute.
     *
     * @param hour The expected hour value.
     * @param min The expected minute value.
     * @param id The resource ID of the view displaying the time.
     */
    fun assertTimeChanged(hour: Int, min: Int, id: Int) {
        val cal = Calendar.getInstance().apply { set(0, 0, 0, hour, min) }
        val sdh = SimpleDateFormat("H:mm a", Locale.US)
        waitForViewWithId(id).assertHasText(sdh.format(cal.time))
    }

    /**
     * Asserts that a new override has been created for the quiz.
     */
    fun assertNewOverrideCreated() {
        waitForViewWithId(R.id.overrideContainer).check(
            has(
                2,
                Matchers.instanceOf(AssignmentOverrideView::class.java)
            )
        )
    }

    /**
     * Asserts that an override has been removed from the quiz.
     */
    fun assertOverrideRemoved() {
        waitForViewWithId(R.id.overrideContainer).check(
            has(
                1,
                Matchers.instanceOf(AssignmentOverrideView::class.java)
            )
        )
    }

    /**
     * Asserts that the "Due Date Before Unlock Date" error message is shown.
     */
    fun assertDueDateBeforeUnlockDateErrorShown() {
        waitForViewWithId(R.id.fromDateTextInput).check(
            ViewAssertions.matches(
                hasTextInputLayoutErrorText(R.string.unlock_after_due_date_error)
            )
        )
    }

    /**
     * Asserts that the "Due Date After Lock Date" error message is shown.
     */
    fun assertDueDateAfterLockDateErrorShown() {
        waitForViewWithId(R.id.toDateTextInput).check(
            ViewAssertions.matches(
                hasTextInputLayoutErrorText(R.string.lock_before_due_date_error)
            )
        )
    }

    /**
     * Asserts that the "Lock Date After Unlock Date" error message is shown.
     */
    fun assertLockDateAfterUnlockDateErrorShown() {
        waitForViewWithId(R.id.toDateTextInput).check(
            ViewAssertions.matches(
                hasTextInputLayoutErrorText(R.string.lock_after_unlock_error)
            )
        )
    }

    /**
     * Asserts that the "No Assignees" error message is shown.
     */
    fun assertNoAssigneesErrorShown() {
        Espresso.onView(withIndex(withId(R.id.assignToTextInput), 1))
            .check(ViewAssertions.matches(hasTextInputLayoutErrorText(R.string.assignee_blank_error)))
    }

    private fun addOverrideButton() = waitForView(
        allOf(
            withId(R.id.addOverride),
            withEffectiveVisibility(Visibility.VISIBLE)
        )
    )

    fun editAssignees() = waitScrollClick(R.id.assignTo)
    fun clickEditDueDate() = waitScrollClick(R.id.dueDate)
    fun clickEditDueTime() = waitScrollClick(R.id.dueTime)
    fun clickEditUnlockDate() = waitScrollClick(R.id.fromDate)
    fun clickEditUnlockTime() = waitScrollClick(R.id.fromTime)
    fun clickEditLockDate() = waitScrollClick(R.id.toDate)
    fun clickEditLockTime() = waitScrollClick(R.id.toTime)
    fun clickAddOverride() = addOverrideButton().scrollTo().click()
    fun switchPublish() = waitScrollClick(R.id.publishSwitch)
}
