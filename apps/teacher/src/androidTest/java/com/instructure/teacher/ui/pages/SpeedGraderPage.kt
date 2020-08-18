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
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.SubmissionApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import org.hamcrest.Matchers

@Suppress("unused")
class SpeedGraderPage : BasePage() {

    private val speedGraderActivityToolbar by OnViewWithId(R.id.speedGraderToolbar)
    private val slidingUpPanelLayout by OnViewWithId(R.id.slidingUpPanelLayout)
    private val submissionPager by OnViewWithId(R.id.submissionContentPager)

    private val gradeTab by OnViewWithStringText(getStringFromResource(R.string.sg_tab_grade).toUpperCase())
    private val commentsTab by OnViewWithStringText(getStringFromResource(R.string.sg_tab_comments).toUpperCase())
    private val filesTab by OnViewWithStringText(getStringFromResource(R.string.sg_tab_files).toUpperCase())

    private val submissionDropDown by WaitForViewWithId(R.id.submissionVersionsButton)
    private val submissionVersionDialogTitle by WaitForViewWithText(R.string.submission_versions)

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
        gradeTab.click()
    }

    fun selectCommentsTab() {
        commentsTab.click()
    }

    fun selectFilesTab() {
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

}
