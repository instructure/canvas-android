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

import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertHasText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withId
import com.instructure.student.R

class ShareExtensionStatusPage : BasePage() {

    private val dialogTitle by WaitForViewWithId(R.id.dialogTitle)
    private val subtitle by WaitForViewWithId(R.id.subtitle)
    private val description by WaitForViewWithId(R.id.description)

    fun assertAssignmentSubmissionSuccess() {
        dialogTitle.assertHasText(R.string.submission)
        subtitle.assertHasText(R.string.submissionSuccessTitle)
        description.assertHasText(R.string.submissionSuccessMessage)
    }

    fun assertFileUploadSuccess() {
        dialogTitle.assertHasText(R.string.fileUpload)
        subtitle.assertHasText(R.string.fileUploadSuccess)
        description.assertHasText(R.string.filesUploadedSuccessfully)
    }

    fun clickOnDone() {
        onView(withId(R.id.doneButton)).click()
    }

}