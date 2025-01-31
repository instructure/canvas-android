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
 */
package com.instructure.teacher.ui.pages


import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import com.instructure.canvas.espresso.has
import com.instructure.canvas.espresso.hasTextInputLayoutErrorText
import com.instructure.canvas.espresso.withIndex
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertHasText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.scrollTo
import com.instructure.espresso.page.waitForViewWithClassName
import com.instructure.espresso.page.waitForViewWithContentDescription
import com.instructure.espresso.page.waitForViewWithId
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.waitScrollClick
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.replaceText
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TypeInRCETextEditor
import com.instructure.teacher.view.AssignmentOverrideView
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matchers
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Represents the Edit Assignment Details page.
 */
class EditAssignmentDetailsPage : BasePage() {

    private val assignmentNameEditText by OnViewWithId(R.id.editAssignmentName)
    private val pointsPossibleEditText by WaitForViewWithId(R.id.editGradePoints)
    private val publishSwitch by WaitForViewWithId(R.id.publishSwitch)
    private val saveButton by OnViewWithId(R.id.menuSave)
    private val descriptionWebView by OnViewWithId(R.id.descriptionWebView, autoAssert = false)
    private val noDescriptionTextView by OnViewWithId(R.id.noDescriptionTextView, autoAssert = false)
    private val overlayContainer by OnViewWithId(R.id.overrideContainer, autoAssert = false)
    private val contentRceView by WaitForViewWithId(R.id.rce_webView, autoAssert = false)

    /**
     * Saves the assignment after making changes.
     */
    fun saveAssignment() {
        saveButton.click()
    }

    /**
     * Clicks on the assignment name EditText field.
     */
    fun clickAssignmentNameEditText() {
        assignmentNameEditText.click()
    }

    /**
     * Clicks on the points possible EditText field.
     */
    fun clickPointsPossibleEditText() {
        scrollTo(R.id.editGradePoints)
        pointsPossibleEditText.click()
    }

    /**
     * Edits the name of the assignment with the specified [newName].
     *
     * @param newName The new name for the assignment.
     */
    fun editAssignmentName(newName: String) {
        assignmentNameEditText.replaceText(newName)
        Espresso.closeSoftKeyboard()
    }

    /**
     * Edits the points possible for the assignment with the specified [newPoints].
     *
     * @param newPoints The new points possible for the assignment.
     */
    fun editAssignmentPoints(newPoints: Double) {
        val df = DecimalFormat("#")
        pointsPossibleEditText.replaceText(df.format(newPoints))
        Espresso.closeSoftKeyboard()
    }

    /**
     * Edits the assignees for the assignment.
     */
    fun editAssignees() = waitScrollClick(R.id.assignTo)

    /**
     * Clicks on the edit due date field.
     */
    fun clickEditDueDate() = waitScrollClick(R.id.dueDate)

    /**
     * Clicks on the edit due time field.
     */
    fun clickEditDueTime() = waitScrollClick(R.id.dueTime)

    /**
     * Clicks on the edit unlock date field.
     */
    fun clickEditUnlockDate() = waitScrollClick(R.id.fromDate)

    /**
     * Clicks on the edit unlock time field.
     */
    fun clickEditUnlockTime() = waitScrollClick(R.id.fromTime)

    /**
     * Clicks on the edit lock date field.
     */
    fun clickEditLockDate() = waitScrollClick(R.id.toDate)

    /**
     * Clicks on the edit lock time field.
     */
    fun clickEditLockTime() = waitScrollClick(R.id.toTime)

    /**
     * Clicks on the publish switch to toggle its state.
     */
    fun clickPublishSwitch() = waitScrollClick(R.id.publishSwitch)

    /**
     * Clicks on the add override button.
     */
    fun clickAddOverride() =
        onView(allOf(withId(R.id.addOverride), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).scrollTo().click()

    /**
     * Removes the first override from the list.
     */
    fun removeFirstOverride() {
        waitForViewWithContentDescription("remove_override_button_0").scrollTo().click()
        waitForViewWithText(R.string.remove).click()
    }

    /**
     * Edits the date with the specified [year], [month], and [dayOfMonth].
     *
     * @param year The year.
     * @param month The month (0-11).
     * @param dayOfMonth The day of the month (1-31).
     */
    fun editDate(year: Int, month: Int, dayOfMonth: Int) {
        waitForViewWithClassName(Matchers.equalTo<String>(DatePicker::class.java.name))
            .perform(PickerActions.setDate(year, month, dayOfMonth))
        onViewWithId(android.R.id.button1).click()
    }

    /**
     * Edits the time with the specified [hour] and [min].
     *
     * @param hour The hour (0-23).
     * @param min The minute (0-59).
     */
    fun editTime(hour: Int, min: Int) {
        waitForViewWithClassName(Matchers.equalTo<String>(TimePicker::class.java.name))
            .perform(PickerActions.setTime(hour, min))
        onViewWithId(android.R.id.button1).click()
    }

    /**
     * Asserts that the date with the specified [year], [month], and [dayOfMonth] has been changed.
     *
     * @param year The expected year.
     * @param month The expected month (0-11).
     * @param dayOfMonth The expected day of the month (1-31).
     * @param id The resource ID of the view to assert the date change.
     */
    fun assertDateChanged(year: Int, month: Int, dayOfMonth: Int, id: Int) {
        val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
        waitForViewWithId(id).assertHasText(DateHelper.fullMonthNoLeadingZeroDateFormat.format(cal.time))
    }

    /**
     * Asserts that the time with the specified [hour] and [min] has been changed.
     *
     * @param hour The expected hour (0-23).
     * @param min The expected minute (0-59).
     * @param id The resource ID of the view to assert the time change.
     */
    fun assertTimeChanged(hour: Int, min: Int, id: Int) {
        val cal = Calendar.getInstance().apply { set(0, 0, 0, hour, min) }
        val sdh = SimpleDateFormat("H:mm a", Locale.US)
        waitForViewWithId(id).assertHasText(sdh.format(cal.time))
    }

    /**
     * Asserts that a new override has been created.
     */
    fun assertNewOverrideCreated() {
        waitForViewWithId(R.id.overrideContainer).check(has(2, Matchers.instanceOf(AssignmentOverrideView::class.java)))
    }

    /**
     * Asserts that an override has been removed.
     */
    fun assertOverrideRemoved() {
        waitForViewWithId(R.id.overrideContainer).check(has(1, Matchers.instanceOf(AssignmentOverrideView::class.java)))
    }

    /**
     * Asserts that an error indicating that the due date is before the unlock date is shown.
     */
    fun assertDueDateBeforeUnlockDateErrorShown() {
        waitForViewWithId(R.id.fromDateTextInput).check(matches(hasTextInputLayoutErrorText(R.string.unlock_after_due_date_error)))
    }

    /**
     * Asserts that an error indicating that the due date is after the lock date is shown.
     */
    fun assertDueDateAfterLockDateErrorShown() {
        waitForViewWithId(R.id.toDateTextInput).check(matches(hasTextInputLayoutErrorText(R.string.lock_before_due_date_error)))
    }

    /**
     * Asserts that an error indicating that the lock date is after the unlock date is shown.
     */
    fun assertLockDateAfterUnlockDateErrorShown() {
        waitForViewWithId(R.id.toDateTextInput).check(matches(hasTextInputLayoutErrorText(R.string.lock_after_unlock_error)))
    }

    /**
     * Asserts that an error indicating that no assignees are selected is shown.
     */
    fun assertNoAssigneesErrorShown() {
        onView(withIndex(withId(R.id.assignToTextInput), 1)).check(matches(hasTextInputLayoutErrorText(R.string.assignee_blank_error)))
    }

    /**
     * Clicks on the display grade as spinner.
     */
    fun clickOnDisplayGradeAsSpinner() {
        onView(withId(R.id.displayGradeAsSpinner)).scrollTo().click()
    }

    /**
     * Selects the grade type with the specified [gradeType].
     *
     * @param gradeType The grade type to select.
     */
    fun selectGradeType(gradeType: String) {
        onView(withText(gradeType)).click()
    }

    /**
     * Edits the description of the assignment with the specified [newDescription].
     *
     * @param newDescription The new description for the assignment.
     */
    fun editDescription(newDescription: String) {
        contentRceView.perform(TypeInRCETextEditor(newDescription))
    }
}

