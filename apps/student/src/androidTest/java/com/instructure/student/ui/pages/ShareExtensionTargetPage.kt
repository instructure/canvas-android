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
package com.instructure.student.ui.pages

import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.WaitForViewWithStringTextIgnoreCase
import com.instructure.espresso.WaitForViewWithText
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertSelected
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.student.R

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

    fun pressNext() {
        nextButton.click()
    }
}