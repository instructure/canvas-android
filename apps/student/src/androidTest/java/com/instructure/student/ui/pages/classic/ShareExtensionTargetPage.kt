/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.ui.pages.classic

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.WaitForViewWithStringTextIgnoreCase
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withText
import com.instructure.student.R
import org.hamcrest.Matchers.anything

class ShareExtensionTargetPage : BasePage() {

    private val avatar by WaitForViewWithId(R.id.avatar)
    private val dialogTitle by WaitForViewWithId(R.id.dialogTitle)
    private val userName by WaitForViewWithId(R.id.userName)
    private val selectionWrapper by WaitForViewWithId(R.id.selectionWrapper)
    private val filesCheckbox by WaitForViewWithId(R.id.filesCheckBox)
    private val assignmentCheckbox by WaitForViewWithId(R.id.assignmentCheckBox)
    private val nextButton by WaitForViewWithStringTextIgnoreCase("next")
    private val cancelButton by WaitForViewWithStringTextIgnoreCase("cancel")

    fun assertFilesCheckboxIsSelected() {
        filesCheckbox.check(ViewAssertions.matches(ViewMatchers.isChecked()))
    }

    fun assertUserName(username: String) {
        userName.assertHasText(username)
    }

    fun assertCourseSelectorDisplayedWithCourse(courseName: String) {
        onViewWithId(R.id.studentCourseSpinner).assertDisplayed()
        onView(withText(courseName) + withAncestor(R.id.studentCourseSpinner)).assertDisplayed()
    }

    fun assertAssignmentSelectorDisplayedWithAssignment(assignmentName: String) {
        onViewWithId(R.id.assignmentSpinner).assertDisplayed()
        onView(withText(assignmentName) + withAncestor(R.id.assignmentSpinner)).assertDisplayed()
    }

    fun assertNoAssignmentSelectedStringDisplayed() {
        onViewWithId(R.id.assignmentSpinner).assertDisplayed()
        onView(withText(R.string.noAssignmentsWithFileUpload) + withAncestor(R.id.assignmentSpinner)).assertDisplayed()
    }

    fun selectAssignment(assignmentName: String) {
        onViewWithId(R.id.assignmentSpinner).click()
        onData(anything()).inRoot(isDialog()).atPosition(1)
    }

    fun selectSubmission() {
        assignmentCheckbox.click()
    }

    fun pressNext() {
        nextButton.click()
    }
}