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


import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.*
import android.widget.DatePicker
import android.widget.TimePicker
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.espresso.*
import com.instructure.canvas.espresso.has
import com.instructure.canvas.espresso.hasTextInputLayoutErrorText
import com.instructure.canvas.espresso.withIndex
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import com.instructure.teacher.view.AssignmentOverrideView
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matchers
import java.text.SimpleDateFormat
import java.util.*

class EditQuizDetailsPage : BasePage() {

    private val quizTitleEditText by OnViewWithId(R.id.editQuizTitle)
    private val publishSwitch by WaitForViewWithId(R.id.publishSwitch)
    private val accessCodeSwitch by WaitForViewWithId(R.id.accessCodeSwitch)
    private val accessCodeEditText by WaitForViewWithId(R.id.editAccessCode)
    private val saveButton by OnViewWithId(R.id.menuSave)
    private val descriptionWebView by OnViewWithId(R.id.descriptionWebView, autoAssert = false)
    private val noDescriptionTextView by OnViewWithId(R.id.noDescriptionTextView, autoAssert = false)

    fun saveQuiz() {
        saveButton.click()
    }

    fun editQuizTitle(newName: String) {
        // Combination of scroll and click randomly selects the text sometimes.
        // This opens a contextual menu and blocks the background view to receive focus and fails the test.
        // That is why, `quizTitleEditText.scrollTo()` is not needed.
        quizTitleEditText.replaceText(newName)
        saveQuiz()
    }

    fun clickAccessCode() {
        accessCodeSwitch.scrollTo()
        accessCodeSwitch.click()
    }

    fun clickAccessCodeEditText() {
        accessCodeEditText.scrollTo()
        accessCodeEditText.click()
    }

    fun editAccessCode(): String {
        val code = randomString()
        accessCodeEditText.scrollTo()
        accessCodeEditText.replaceText(code)
        saveQuiz()
        return code
    }

    fun editDate(year: Int, month: Int, dayOfMonth: Int) {
        waitForViewWithClassName(Matchers.equalTo<String>(DatePicker::class.java.name))
                .perform(PickerActions.setDate(year, month, dayOfMonth))
        onViewWithId(android.R.id.button1).click()
    }

    fun editTime(hour: Int, min: Int) {
        waitForViewWithClassName(Matchers.equalTo<String>(TimePicker::class.java.name))
                .perform(PickerActions.setTime(hour, min))
        onViewWithId(android.R.id.button1).click()
    }

    fun removeSecondOverride() {
        // scroll to bottom to make the 2nd override button visible
        addOverrideButton().scrollTo()

        ClickUntilMethod.run(
                onView(withContentDescription("remove_override_button_1")),
                onView(withText("Remove Due Date"))
        )

        // Wait for alert dialog to display before clicking "Remove"
        waitForViewWithText(R.string.removeDueDate).assertVisible()
        waitForViewWithText(R.string.remove).click()
    }

    fun assertDateChanged(year: Int, month: Int, dayOfMonth: Int, id: Int) {
        val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
        waitForViewWithId(id).assertHasText(DateHelper.fullMonthNoLeadingZeroDateFormat.format(cal.time))
    }

    fun assertTimeChanged(hour: Int, min: Int, id: Int) {
        val cal = Calendar.getInstance().apply { set(0, 0, 0, hour, min) }
        val sdh = SimpleDateFormat("H:mm a", Locale.US)
        waitForViewWithId(id).assertHasText(sdh.format(cal.time))
    }

    fun assertNewOverrideCreated() {
        waitForViewWithId(R.id.overrideContainer).check(has(2, Matchers.instanceOf(AssignmentOverrideView::class.java)))
    }

    fun assertOverrideRemoved() {
        waitForViewWithId(R.id.overrideContainer).check(has(1, Matchers.instanceOf(AssignmentOverrideView::class.java)))
    }

    fun assertDueDateBeforeUnlockDateErrorShown() {
        waitForViewWithId(R.id.fromDateTextInput).check(ViewAssertions.matches(hasTextInputLayoutErrorText(R.string.unlock_after_due_date_error)))
    }

    fun assertDueDateAfterLockDateErrorShown() {
        waitForViewWithId(R.id.toDateTextInput).check(ViewAssertions.matches(hasTextInputLayoutErrorText(R.string.lock_before_due_date_error)))
    }

    fun assertLockDateAfterUnlockDateErrorShown() {
        waitForViewWithId(R.id.toDateTextInput).check(ViewAssertions.matches(hasTextInputLayoutErrorText(R.string.lock_after_unlock_error)))
    }

    fun assertNoAssigneesErrorShown() {
        Espresso.onView(withIndex(withId(R.id.assignToTextInput), 1)).check(ViewAssertions.matches(hasTextInputLayoutErrorText(R.string.assignee_blank_error)))
    }

    private fun addOverrideButton() = waitForView(allOf(withId(R.id.addOverride),
            withEffectiveVisibility(Visibility.VISIBLE)))

    fun editAssignees() = waitScrollClick(R.id.assignTo)
    fun clickEditDueDate() = waitScrollClick(R.id.dueDate)
    fun clickEditDueTime() = waitScrollClick(R.id.dueTime)
    fun clickEditUnlockDate() = waitScrollClick(R.id.fromDate)
    fun clickEditUnlockTime() = waitScrollClick(R.id.fromTime)
    fun clickEditLockDate() = waitScrollClick(R.id.toDate)
    fun clickEditLockTime() = waitScrollClick(R.id.toTime)
    fun clickAddOverride() = addOverrideButton().scrollTo().click()
}
