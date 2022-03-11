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

import androidx.annotation.StringRes
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.SubmissionApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import java.util.Locale

@Suppress("unused")
class SpeedGraderPage : BasePage() {

    private val speedGraderActivityToolbar by OnViewWithId(R.id.speedGraderToolbar)
    private val slidingUpPanelLayout by OnViewWithId(R.id.slidingUpPanelLayout,false)
    private val submissionPager by OnViewWithId(R.id.submissionContentPager)

    private val gradeTab by OnViewWithStringText(getStringFromResource(R.string.sg_tab_grade).uppercase(Locale.getDefault()))
    private val commentsTab by OnViewWithStringText(getStringFromResource(R.string.sg_tab_comments).uppercase(Locale.getDefault()))

    private val submissionDropDown by WaitForViewWithId(R.id.submissionVersionsButton)
    private val submissionVersionDialogTitle by WaitForViewWithText(R.string.submission_versions)
    private val commentLibraryContainer by OnViewWithId(R.id.commentLibraryFragmentContainer)

    fun assertHasSubmissionDropDown() {
        submissionDropDown.assertDisplayed()
    }

    fun assertSubmissionDialogDisplayed() {
        submissionVersionDialogTitle.assertDisplayed()
    }

    fun openSubmissionsDialog() {
        ClickUntilMethod.run(
                onView(withId(R.id.submissionVersionsButton)),
                onView(withText(R.string.submission_versions))
        )
    }

    fun selectGradesTab() {
        val gradesTabText = getStringFromResource(R.string.sg_tab_grade).uppercase(Locale.getDefault())
        onView(allOf((withText(gradesTabText)), isDisplayed())).click()
    }

    fun selectCommentsTab() {
        commentsTab.click()
    }

    fun swipeUpCommentsTab() {
        commentsTab.swipeToTop()
    }

    fun swipeUpGradesTab() {
        gradeTab.swipeToTop()
    }

    fun selectFilesTab(fileCount: Int) {
        val filesTab = waitForViewWithText(getStringFromResource(R.string.sg_tab_files_w_counter, fileCount).uppercase(Locale.getDefault()))
        filesTab.click()
    }

    fun assertGradingStudent(student: CanvasUserApiModel) {
        onViewWithText(student.name).assertCompletelyDisplayed()
    }

    fun assertGradingStudent(student: User) {
        onViewWithText(student.name).assertCompletelyDisplayed()
    }

    fun goToSubmissionPage(index: Int) {
        submissionPager.pageToItem(index)
    }

    fun clickBackButton() {
        try {
            Espresso.onView(
                Matchers.allOf(
                    ViewMatchers.withContentDescription(androidx.appcompat.R.string.abc_action_bar_up_description),
                    ViewMatchers.isCompletelyDisplayed(),
                    ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.speedGraderToolbar))
                )
            ).click()
        } catch (e: NoMatchingViewException) {}
    }

    fun assertPageCount(count: Int) {
        submissionPager.check(ViewPagerItemCountAssertion(count))
    }

    fun assertDisplaysTextSubmissionView() {
        waitForViewWithId(R.id.textSubmissionWebView).assertVisible()
    }

    fun assertDisplaysTextSubmissionViewWithStudentName(studentName: String) {
        onView(allOf(withText(studentName), isDisplayed()))
    }

    fun assertDisplaysEmptyState(@StringRes stringRes: Int) {
        waitForViewWithText(stringRes).assertCompletelyDisplayed()
    }

    fun assertDisplaysUrlSubmissionLink(submission: SubmissionApiModel) {
        waitForViewWithId(R.id.urlTextView).assertCompletelyDisplayed().assertHasText(submission.url!!)
    }

    fun assertDisplaysUrlSubmissionLink(submission: Submission) {
        waitForViewWithId(R.id.urlTextView).assertCompletelyDisplayed().assertHasText(submission.url!!)
    }

    fun assertDisplaysUrlWebView() {
        waitForViewWithId(R.id.urlTextView).click()
        waitForViewWithId(R.id.canvasWebView).assertCompletelyDisplayed()
    }

    fun assertCommentLibraryNotVisible() {
        commentLibraryContainer.check(ViewAssertions.matches(ViewMatchers.hasChildCount(0)))
    }
}
