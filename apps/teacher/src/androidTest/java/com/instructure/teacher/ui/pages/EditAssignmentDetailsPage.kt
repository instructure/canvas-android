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
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TypeInRCETextEditor
import com.instructure.teacher.view.AssignmentOverrideView
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matchers
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class EditAssignmentDetailsPage : BasePage() {

    private val assignmentNameEditText by OnViewWithId(R.id.editAssignmentName)
    private val pointsPossibleEditText by WaitForViewWithId(R.id.editGradePoints)
    private val publishSwitch by WaitForViewWithId(R.id.publishSwitch)
    private val saveButton by OnViewWithId(R.id.menuSave)
    private val descriptionWebView by OnViewWithId(R.id.descriptionWebView, autoAssert = false)
    private val noDescriptionTextView by OnViewWithId(R.id.noDescriptionTextView, autoAssert = false)
    private val overlayContainer by OnViewWithId(R.id.overrideContainer, autoAssert = false)
    private val contentRceView by WaitForViewWithId(R.id.rce_webView, autoAssert = false)

    fun saveAssignment() {
        saveButton.click()
    }

    fun clickAssignmentNameEditText() {
        assignmentNameEditText.click()
    }

    fun clickPointsPossibleEditText() {
        scrollTo(R.id.editGradePoints)
        pointsPossibleEditText.click()
    }

    fun editAssignmentName(newName: String) {
        assignmentNameEditText.replaceText(newName)
        Espresso.closeSoftKeyboard()
    }

    fun editAssignmentPoints(newPoints: Double) {
        val df = DecimalFormat("#")
        pointsPossibleEditText.replaceText(df.format(newPoints))
        Espresso.closeSoftKeyboard()
    }

    fun editAssignees() = waitScrollClick(R.id.assignTo)
    fun clickEditDueDate() = waitScrollClick(R.id.dueDate)
    fun clickEditDueTime() = waitScrollClick(R.id.dueTime)
    fun clickEditUnlockDate() = waitScrollClick(R.id.fromDate)
    fun clickEditUnlockTime() = waitScrollClick(R.id.fromTime)
    fun clickEditLockDate() = waitScrollClick(R.id.toDate)
    fun clickEditLockTime() = waitScrollClick(R.id.toTime)
    fun clickPublishSwitch() = waitScrollClick(R.id.publishSwitch)

    fun clickAddOverride() = onView(allOf(withId(R.id.addOverride), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).scrollTo().click()
    fun removeFirstOverride() {
        waitForViewWithContentDescription("remove_override_button_0").scrollTo().click()
        waitForViewWithText(R.string.remove).click()
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

    fun assertDateChanged(year: Int, month: Int, dayOfMonth: Int, id: Int) {
        val cal = Calendar.getInstance().apply {set(year, month, dayOfMonth)}
        waitForViewWithId(id).assertHasText(DateHelper.fullMonthNoLeadingZeroDateFormat.format(cal.time))
    }

    fun assertTimeChanged(hour: Int, min: Int, id: Int) {
        val cal = Calendar.getInstance().apply {set(0, 0, 0, hour, min)}
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
        waitForViewWithId(R.id.fromDateTextInput).check(matches(hasTextInputLayoutErrorText(R.string.unlock_after_due_date_error)))
    }

    fun assertDueDateAfterLockDateErrorShown() {
        waitForViewWithId(R.id.toDateTextInput).check(matches(hasTextInputLayoutErrorText(R.string.lock_before_due_date_error)))
    }

    fun assertLockDateAfterUnlockDateErrorShown() {
        waitForViewWithId(R.id.toDateTextInput).check(matches(hasTextInputLayoutErrorText(R.string.lock_after_unlock_error)))
    }

    fun assertNoAssigneesErrorShown() {
        onView(withIndex(withId(R.id.assignToTextInput), 1)).check(matches(hasTextInputLayoutErrorText(R.string.assignee_blank_error)))
    }

    fun clickOnDisplayGradeAsSpinner() {
        onView(withId(R.id.displayGradeAsSpinner)).scrollTo().click()
    }

    fun selectGradeType(gradeType: String) {
        onView(withText(gradeType)).click()
    }

    fun editDescription(newDescription: String) {
        contentRceView.perform(TypeInRCETextEditor(newDescription))
    }

}
