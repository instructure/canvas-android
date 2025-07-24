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
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.SubmissionApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.OnViewWithStringText
import com.instructure.espresso.ViewPagerItemCountAssertion
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.WaitForViewWithText
import com.instructure.espresso.assertCompletelyDisplayed
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertVisible
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.getStringFromResource
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForViewWithId
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.pageToItem
import com.instructure.espresso.swipeToTop
import com.instructure.teacher.R
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import java.util.Locale

/**
 * Represents the SpeedGrader page.
 *
 * This page provides functionality for interacting with the elements on the SpeedGrader page. It contains methods for
 * asserting various aspects of the page, such as the submission drop-down, tabs (grades and comments), file tab, grading
 * student, page count, and different submission views. It also provides methods for selecting different tabs, swiping up
 * the comments and grades tabs, navigating to the submission page, clicking the back button, and asserting the visibility
 * of the comment library. This page extends the BasePage class.
 */
@Suppress("unused")
class SpeedGraderPage(private val composeTestRule: ComposeTestRule) : BasePage() { // TODO: YET this is a 'hybrid' page because it's highly used in tests, we'll eliminate the non-compose parts step by step.

    private val speedGraderActivityToolbar by OnViewWithId(R.id.speedGraderToolbar)
    private val slidingUpPanelLayout by OnViewWithId(R.id.slidingUpPanelLayout,false)
    private val submissionPager by OnViewWithId(R.id.submissionContentPager)

    private val gradeTab by OnViewWithStringText(getStringFromResource(R.string.sg_tab_grade).uppercase(Locale.getDefault()))
    private val commentsTab by OnViewWithStringText(getStringFromResource(R.string.sg_tab_comments).uppercase(Locale.getDefault()))

    private val submissionDropDown by WaitForViewWithId(R.id.submissionVersionsSpinner)
    private val submissionVersionDialogTitle by WaitForViewWithText(R.string.submission_versions)
    private val commentLibraryContainer by OnViewWithId(R.id.commentLibraryFragmentContainer)

    /**
     * Assert that the 'Grade' label is displayed on the SpeedGrader page's 'Grade & Rubric' tab.
     */
    fun assertSpeedGraderLabelDisplayed() {
        composeTestRule.onNodeWithTag("speedGraderGradeLabel").assertIsDisplayed()
    }

    /**
     * Clicks the expand panel button in the Compose UI.
     */
    fun clickExpandPanelButton() {
        composeTestRule
            .onNodeWithTag("expandPanelButton", useUnmergedTree = true)
            .performClick()
    }

    /**
     * Clicks the collapse panel button in the Compose UI.
     */
    fun clickCollapsePanelButton() {
        composeTestRule
            .onNodeWithTag("collapsePanelButton", useUnmergedTree = true)
            .performClick()
    }

    /**
     * Enters a new grade in the Compose grade input field.
     *
     * @param grade The grade value to input.
     */
    fun enterNewGrade(grade: String) {
        composeTestRule
            .onNodeWithTag("gradeInputField")
            .performTextInput(grade)
    }

    /**
     * Asserts that the final grade is displayed in the Compose UI.
     *
     * @param grade The expected grade value to be displayed.
     */
    fun assertFinalGradeIsDisplayed(grade: String) {
        composeTestRule
            .onNodeWithTag("finalGradeDisplay")
            .assertTextContains(grade, substring = true)
            .assertIsDisplayed()
    }

    /**
     * Asserts that the empty view (No Submission) is displayed on the SpeedGrader page.
     */
    fun assertEmptyViewDisplayed() {
        onView(withId(R.id.titleTextView) + withText(R.string.noSubmission)).assertDisplayed()
        onView(withId(R.id.messageTextView) + withText(R.string.noSubmissionTeacher)).assertDisplayed()
    }

    /**
     * Asserts that the attachment with the corresponding name is displayed in the SpeedGrader page.
     *
     * @param itemName The name of the attachment to assert.
     */
    fun assertSelectedAttachmentItemDisplayed(itemName: String) {
        composeTestRule.onNode(hasTestTag("selectedAttachmentItem") and hasText(itemName), useUnmergedTree = true).assertIsDisplayed()
    }

    /**
     * Asserts that the SpeedGrader toolbar is displayed.
     *
     * @param title The expected title of the SpeedGrader toolbar.
     * @param subTitle The expected subtitle of the SpeedGrader toolbar, if any.
     */
    fun assertSpeedGraderToolbarTitle(title: String, subTitle: String? = null) {
        composeTestRule.onNode(hasText(title) and hasAnyAncestor(hasTestTag("speedGraderAppBar"))).assertIsDisplayed()
        if(subTitle != null) composeTestRule.onNode(hasText(title) and hasText(subTitle) and hasAnyAncestor(hasTestTag("speedGraderAppBar"))).assertIsDisplayed()
    }

    /**
     * Asserts that the page has the submission drop-down.
     */
    fun assertHasSubmissionDropDown() {
        submissionDropDown.assertDisplayed()
    }

    /**
     * Selects the "Grades" tab.
     */
    fun selectGradesTab() {
        val gradesTabText = getStringFromResource(R.string.sg_tab_grade).uppercase(Locale.getDefault())
        onView(allOf((withText(gradesTabText)), isDisplayed())).click()
    }

    /**
     * Selects the "Comments" tab.
     */
    fun selectCommentsTab() {
        commentsTab.click()
    }

    /**
     * Swipes up the "Comments" tab.
     */
    fun swipeUpCommentsTab() {
        commentsTab.swipeToTop()
    }

    /**
     * Swipes up the "Grades" tab.
     */
    fun swipeUpGradesTab() {
        gradeTab.swipeToTop()
    }

    /**
     * Selects the "Files" tab with the specified file count.
     */
    fun selectFilesTab(fileCount: Int) {
        val filesTab = waitForViewWithText(getStringFromResource(R.string.sg_tab_files_w_counter, fileCount).toUpperCase())
        filesTab.click()
    }

    /**
     * Asserts that the student is being graded.
     *
     * @param student The student to be graded.
     */
    fun assertGradingStudent(student: CanvasUserApiModel) {
        onViewWithText(student.name).assertCompletelyDisplayed()
    }

    /**
     * Asserts that the student is being graded.
     *
     * @param student The student to be graded.
     */
    fun assertGradingStudent(student: User) {
        onViewWithText(student.name).assertCompletelyDisplayed()
    }

    /**
     * Navigates to the submission page at the specified index.
     *
     * @param index The index of the submission page to navigate to.
     */
    fun goToSubmissionPage(index: Int) {
        submissionPager.pageToItem(index)
    }

    /**
     * Clicks the back button.
     */
    fun clickBackButton() {
        try {
            Espresso.onView(
                Matchers.allOf(
                    ViewMatchers.withContentDescription(androidx.appcompat.R.string.abc_action_bar_up_description),
                    ViewMatchers.isCompletelyDisplayed(),
                    ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.gradingToolbar))
                )
            ).click()
        } catch (e: NoMatchingViewException) {}
    }

    /**
     * Asserts the page count of the submission pager.
     *
     * @param count The expected page count.
     */
    fun assertPageCount(count: Int) {
        submissionPager.check(ViewPagerItemCountAssertion(count))
    }

    /**
     * Asserts that the text submission view is displayed.
     */
    fun assertDisplaysTextSubmissionView() {
        waitForViewWithId(R.id.contentWebView).assertVisible()
    }

    /**
     * Asserts that the text submission view with the student's name is displayed.
     *
     * @param studentName The name of the student.
     */
    fun assertDisplaysTextSubmissionViewWithStudentName(studentName: String) {
        onView(allOf(withText(studentName), isDisplayed()))
    }

    /**
     * Asserts that the empty state with the specified string resource is displayed.
     *
     * @param stringRes The string resource of the empty state.
     */
    fun assertDisplaysEmptyState(@StringRes stringRes: Int) {
        waitForViewWithText(stringRes).assertCompletelyDisplayed()
    }

    /**
     * Asserts that the URL submission link is displayed with the specified submission.
     *
     * @param submission The submission with the URL.
     */
    fun assertDisplaysUrlSubmissionLink(submission: SubmissionApiModel) {
        waitForViewWithId(R.id.urlTextView).assertCompletelyDisplayed().assertHasText(submission.url!!)
    }

    /**
     * Asserts that the URL submission link is displayed with the specified submission.
     *
     * @param submission The submission with the URL.
     */
    fun assertDisplaysUrlSubmissionLink(submission: Submission) {
        waitForViewWithId(R.id.urlTextView).assertCompletelyDisplayed().assertHasText(submission.url!!)
    }

    /**
     * Asserts that the URL web view is displayed.
     */
    fun assertDisplaysUrlWebView() {
        waitForViewWithId(R.id.urlTextView).click()
        waitForViewWithId(R.id.canvasWebView).assertCompletelyDisplayed()
    }

    /**
     * Asserts that the comment library is not visible.
     */
    fun assertCommentLibraryNotVisible() {
        commentLibraryContainer.check(ViewAssertions.matches(ViewMatchers.hasChildCount(0)))
    }

    /**
     *
     * Asserts that the file with the given filename is displayed.
     * @param fileName The name of the file.
     */
    fun assertFileDisplayed(fileName: String) {
        val matcher =
            Matchers.allOf(ViewMatchers.withId(R.id.fileNameText), ViewMatchers.withText(fileName))
        Espresso.onView(matcher).assertDisplayed()
    }
    /**

    Asserts that the comment attachment with the given filename and display name is displayed.
    @param fileName The name of the attachment file.
    @param displayName The display name of the attachment.
     */
    fun assertCommentAttachmentDisplayedCommon(fileName: String, displayName: String) {
        val commentMatcher = Matchers.allOf(
            ViewMatchers.withId(R.id.commentHolder),
            ViewMatchers.hasDescendant(
                Matchers.allOf(
                    ViewMatchers.withText(displayName),
                    ViewMatchers.withId(R.id.userNameTextView)
                )
            ),
            ViewMatchers.hasDescendant(
                Matchers.allOf(
                    ViewMatchers.withText(fileName),
                    ViewMatchers.withId(R.id.attachmentNameTextView)
                )
            )
        )
        onView(commentMatcher).assertDisplayed()
    }
}
