/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.student.ui.pages

import androidx.appcompat.widget.AppCompatButton
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import com.instructure.canvas.espresso.CanvasTest
import com.instructure.canvas.espresso.common.pages.AssignmentDetailsPage
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.espresso.ModuleItemInteractions
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.clearText
import com.instructure.espresso.click
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.typeText
import com.instructure.student.R
import org.hamcrest.Matchers.allOf

class StudentAssignmentDetailsPage(moduleItemInteractions: ModuleItemInteractions): AssignmentDetailsPage(moduleItemInteractions) {

    fun addBookmark(bookmarkName: String) {
        openOverflowMenu()
        Espresso.onView(withText("Add Bookmark")).click()
        Espresso.onView(withId(R.id.bookmarkEditText)).clearText()
        Espresso.onView(withId(R.id.bookmarkEditText)).typeText(bookmarkName)
        if(CanvasTest.isLandscapeDevice()) Espresso.pressBack()
        Espresso.onView(allOf(isAssignableFrom(AppCompatButton::class.java), containsTextCaseInsensitive("Save"))).click()
    }

    fun selectSubmissionType(submissionType: SubmissionType) {
        val viewMatcher = when (submissionType) {
            SubmissionType.ONLINE_TEXT_ENTRY -> withId(R.id.submissionEntryText)
            SubmissionType.ONLINE_UPLOAD -> withId(R.id.submissionEntryFile)
            SubmissionType.ONLINE_URL -> withId(R.id.submissionEntryWebsite)
            SubmissionType.MEDIA_RECORDING -> withId(R.id.submissionEntryMedia)

            else -> {withId(R.id.submissionEntryText)}
        }

        onView(viewMatcher).click()
    }

    //OfflineMethod
    fun assertDetailsNotAvailableOffline() {
        onView(withId(R.id.notAvailableIcon) + withAncestor(R.id.moduleProgressionPage)).assertDisplayed()
        onView(withId(R.id.title) + withText(R.string.notAvailableOfflineScreenTitle) + withParent(
            R.id.textViews) + withAncestor(R.id.moduleProgressionPage)).assertDisplayed()
        onView(withId(R.id.description) + withText(R.string.notAvailableOfflineDescriptionForTabs) + withParent(
            R.id.textViews) + withAncestor(R.id.moduleProgressionPage)).assertDisplayed()
    }
}