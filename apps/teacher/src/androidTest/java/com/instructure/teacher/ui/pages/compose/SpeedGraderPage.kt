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
package com.instructure.teacher.ui.pages.compose

import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.composetest.hasTestTagThatContains
import com.instructure.dataseeding.model.SubmissionApiModel
import com.instructure.espresso.assertCompletelyDisplayed
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertVisible
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.getStringFromResource
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.waitForViewWithId
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.pandautils.features.speedgrader.grade.comments.CommentIdKey
import com.instructure.teacher.R
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf

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
class SpeedGraderPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    /**
     * Clicks the expand panel button in the Compose UI.
     */
    @OptIn(ExperimentalTestApi::class)
    fun clickExpandPanelButton() {
        composeTestRule
            .waitUntilExactlyOneExists(hasTestTag("expandPanelButton"), timeoutMillis = 10000)
        composeTestRule.onNodeWithTag("expandPanelButton", useUnmergedTree = true)
            .performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Clicks the collapse panel button in the Compose UI.
     */
    fun clickCollapsePanelButton() {
        composeTestRule
            .onNodeWithTag("collapsePanelButton", useUnmergedTree = true)
            .performClick()
        composeTestRule.waitForIdle()
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
        composeTestRule.onNode(
            hasTestTag("selectedAttachmentItem") and hasText(itemName),
            useUnmergedTree = true
        ).assertIsDisplayed()
    }

    /**
     * Clicks the attachment selector to expand the dropdown.
     */
    fun clickAttachmentSelector() {
        composeTestRule.onNodeWithTag("attemptSelector").performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Selects an attachment from the dropdown by name.
     *
     * @param attachmentName The name of the attachment to select.
     */
    fun selectAttachment(attachmentName: String) {
        composeTestRule.onNodeWithText(attachmentName).performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Asserts that the SpeedGrader toolbar is displayed.
     *
     * @param title The expected title of the SpeedGrader toolbar.
     * @param subTitle The expected subtitle of the SpeedGrader toolbar, if any.
     */
    fun assertSpeedGraderToolbarTitle(title: String, subTitle: String? = null) {
        composeTestRule.onNode(hasText(title) and hasAnyAncestor(hasTestTag("speedGraderAppBar")))
            .assertIsDisplayed()
        if (subTitle != null) composeTestRule.onNode(
            hasText(title) and hasText(subTitle) and hasAnyAncestor(
                hasTestTag("speedGraderAppBar")
            )
        ).assertIsDisplayed()
    }

    /**
     * Asserts that the page has the submission drop-down.
     */
    fun assertHasSubmissionDropDown() {
        composeTestRule.onNode(hasTestTag("attemptSelector")).assertIsDisplayed()
    }

    /**
     * Selects the specified tab (e.g., "Grades", "Comments") in the SpeedGrader page.
     * @param tabTitle The tab's name which will be selected.
     */
    fun selectTab(tabTitle: String) {
        composeTestRule.onNode(hasTestTag("speedGraderTab-${tabTitle}"), useUnmergedTree = true)
            .performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Asserts that the comments label with the specified comment count is displayed.
     *
     * @param commentCount The expected comment count to be displayed in the label.
     */
    @OptIn(ExperimentalTestApi::class)
    fun assertCommentsLabelDisplayed(commentCount: Int) {
        composeTestRule.onNodeWithTag("commentsLabel").performScrollTo().assertIsDisplayed()
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag("commentsLabel") and hasText("Comments ($commentCount)"), timeoutMillis = 5000)
        composeTestRule
            .onNodeWithTag("commentsLabel")
            .assertTextContains("Comments ($commentCount)", substring = true)
            .assertIsDisplayed()
    }

    /**
     * Clicks the comment library button in the Compose UI.
     */
    @OptIn(ExperimentalTestApi::class)
    fun clickCommentLibraryButton() {
        composeTestRule.waitUntilExactlyOneExists(hasTestTag("commentLibraryButton"), timeoutMillis = 5000)
        composeTestRule
            .onNodeWithTag("commentLibraryButton")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Clicks the comment attachment button in the Compose UI.
     */
    fun clickCommentAttachmentButton() {
        composeTestRule
            .onNodeWithTag("commentAttachmentButton")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Clicks the "Choose Files" option in the attachment type selection dialog.
     */
    fun clickChooseFilesOption() {
        composeTestRule
            .onNodeWithText(getStringFromResource(R.string.choose_files))
            .performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Types a comment in the main SpeedGrader comment input field.
     *
     * @param comment The comment text to type.
     */
    fun typeCommentInInputField(comment: String) {
        composeTestRule
            .onNodeWithTag("speedGraderCommentInputField")
            .performTextInput(comment)
        closeSoftKeyboard()
        composeTestRule.waitForIdle()
    }

    /**
     * Types the specified comment in the comment library filter input field.
     *
     * @param comment The comment to type.
     */
    fun typeCommentInCommentLibraryInputField(comment: String) {
        composeTestRule
            .onNodeWithTag("commentLibraryFilterInputField")
            .performTextInput(comment)
        closeSoftKeyboard()
        composeTestRule.waitForIdle()
    }

    /**
     * Clears the comment input field.
     */
    fun clearComment() {
        composeTestRule.onNodeWithTag("commentLibraryFilterInputField").performTextClearance()
        composeTestRule.waitForIdle()
    }

    /**
     * Selects a comment from the comment library that contains the specified part of the comment.
     *
     * @param index The index of the comment to select (0-based). If null, selects the first result comment.
     */
    fun selectCommentLibraryResultItem(index: Int? = null) {

        val targetText = composeTestRule
            .onAllNodesWithTag("commentLibraryItem")[index ?: 0]
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .joinToString("") { it.text }
        composeTestRule.onNode(
            hasText(
                targetText,
                substring = true
            ) and (hasTestTagThatContains("ownCommentText").not())
        )
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Sends the comment (if there's any).
     */
    fun clickSendCommentButton(commentLibraryOpened: Boolean = false) {
        if (commentLibraryOpened) {
            composeTestRule
                .onNodeWithTag("commentLibrarySendCommentButton")
                .performClick()
        } else {
            composeTestRule
                .onNodeWithTag("sendCommentButton")
                .performScrollTo()
                .performClick()
        }
        composeTestRule.waitForIdle()
    }

    /**
     * Sends an audio comment.
     */
    fun sendAudioComment() {
        composeTestRule.onNodeWithText("Record Audio", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        swipeUpGradeAndRubric()
        waitForView(withId(R.id.recordAudioButton)).click()
        Thread.sleep(3000) // Let the audio recording go for a bit
        waitForView(withId(R.id.stopButton)).click()
        waitForView(withId(R.id.sendAudioButton)).click()
        composeTestRule.waitForIdle()
    }

    /**
     * Asserts the display of an media comment.
     *
     * @param text The expected text to be displayed.
     */
    fun assertMediaCommentDisplayed(text: String) {
        composeTestRule.onNode(hasText(text), useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Sends a video comment.
     */
    fun sendVideoComment() {
        composeTestRule.onNodeWithText("Record Video", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        swipeUpGradeAndRubric()
        waitForView(withId(R.id.startRecordingButton)).click()
        Thread.sleep(3000) // Let the video recording go for a bit
        waitForView(withId(R.id.endRecordingButton)).click()
        waitForView(withId(R.id.sendButton)).click()
        composeTestRule.waitForIdle()
    }

    /**
     * Asserts the display of a video comment.
     */
    fun assertVideoCommentDisplayed() {
        val videoCommentMatcher = allOf(
            withId(R.id.commentHolder),
            hasDescendant(allOf(containsTextCaseInsensitive("video"), withId(R.id.attachmentNameTextView)))
        )

        waitForView(videoCommentMatcher).scrollTo().assertDisplayed()
    }

    /**
     * Clicks on a media comment with the specified text.
     *
     * @param mediaCommentText The text of the media comment to click.
     */
    fun clickOnMediaComment(mediaCommentText: String) {
        composeTestRule.onNode(hasText(mediaCommentText) and hasAnySibling(hasTestTag("mediaAttachmentBox")), useUnmergedTree = true).performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Asserts the display of the media comment preview.
     */
    fun assertMediaCommentPreviewDisplayed() {
        onView(allOf(withId(R.id.prepareMediaButton), withParent(R.id.mediaPreviewContainer))).assertDisplayed()
    }

    /**
     * Asserts the count of items in the comment library.
     *
     * @param expectedCount The expected count of items in the comment library.
     */
    @OptIn(ExperimentalTestApi::class)
    fun assertCommentLibraryItemCount(expectedCount: Int) {
        composeTestRule.waitUntilExactlyOneExists(hasTestTagThatContains("commentLibraryListColumn"), timeoutMillis = 5000)
        composeTestRule
            .onAllNodesWithTag("commentLibraryItem")
            .assertCountEquals(expectedCount)
    }

    /**
     * Asserts that a comment library item with the specified text is displayed.
     *
     * @param text The text of the comment library item to assert.
     */
    fun assertCommentLibraryItemDisplayed(text: String) {
        composeTestRule.onNode(
            hasTestTag("commentLibraryItem") and hasText(text, substring = true),
            useUnmergedTree = true
        ).assertIsDisplayed()
    }

    /**
     * Asserts that the comment with the specified text is displayed.
     *
     * @param comment The comment text to assert.
     */
    fun assertCommentDisplayed(comment: String, author: String? = null) { // if author is null, that means it's an own comment because in that case we don't display the author name.
        if (author != null) {
            composeTestRule.onNode(hasTestTag("commentAuthorName") and hasText(author), useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            composeTestRule.onNode(hasTestTag("commentCreatedAtDate") and hasAnySibling(hasTestTag("commentAuthorName") and hasText(author)), useUnmergedTree = true).performScrollTo().assertIsDisplayed()
            composeTestRule.onNode(hasTestTag("commentText") and hasAnySibling(hasTestTag("commentAuthorName") and hasText(author)), useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        }
        else composeTestRule.onNode(hasTestTag("ownCommentText") and hasText(comment), useUnmergedTree = true).performScrollTo().assertIsDisplayed()
    }

    /**
     * Asserts that a comment text is displayed without checking for author.
     * This is useful when you have multiple comments from the same author,
     * where sibling matching becomes unreliable.
     *
     * @param comment The comment text to assert.
     * @param commentId The unique ID of the comment.
     * @param isOwnComment If true, looks for "ownCommentText" tag, otherwise "commentText" tag.
     */
    fun assertCommentTextDisplayed(comment: String, commentId: String, isOwnComment: Boolean = false) {
        val tag = if (isOwnComment) "ownCommentText" else "commentText"
        composeTestRule.onNode(hasTestTag(tag) and hasCommentId(commentId) and hasText(comment), useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Asserts that the comment author name is not displayed.
     * This is useful for verifying that own comments (teacher comments) don't show the author name.
     */
    fun assertCommentAuthorNameNotDisplayed() {
        composeTestRule.onNodeWithTag("commentAuthorName").assertDoesNotExist()
    }

    /**
     * Asserts that a comment attachment with the specified name is displayed.
     *
     * @param attachmentName The name of the attachment to assert.
     */
    fun assertCommentAttachmentDisplayed(attachmentName: String) {
        composeTestRule.onNode(hasText(attachmentName), useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    /**
     * Clicks the close button of the comment library in the Compose UI.
     */
    @OptIn(ExperimentalTestApi::class)
    fun clickCloseCommentLibraryButton() {
        composeTestRule.waitUntilExactlyOneExists(hasTestTag("closeCommentLibraryButton"), timeoutMillis = 5000)
        composeTestRule
            .onNodeWithTag("closeCommentLibraryButton")
            .performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Asserts that the title "Comment Library" string is the title of the Comment Library screen.
     */
    fun assertCommentLibraryTitle() {
        composeTestRule.onNodeWithText("Comment Library").assertIsDisplayed()
    }

    /**
     * Types text in the comment library filter input field and closes the keyboard.
     *
     * @param text The text to type in the filter field.
     */
    fun typeInCommentLibraryFilter(text: String) {
        composeTestRule.onNodeWithTag("commentLibraryFilterInputField").performClick().performTextReplacement(text)
        try {
            closeSoftKeyboard()
        } catch (_: Exception) {
            // Ignore if keyboard is already closed
        }
        composeTestRule.waitForIdle()
    }

    /**
     * Asserts that the comment library filter input field contains the specified text.
     *
     * @param text The text that should be contained in the filter field.
     */
    fun assertCommentLibraryFilterContains(text: String) {
        composeTestRule.onNodeWithTag("commentLibraryFilterInputField").assertTextContains(text)
    }

    /**
     * Swipes up the "Grade & Rubric" section to reveal its bottom content.
     * This performs a swipe gesture on the actual scrollable container to scroll down
     * and reveal the bottom of the Grade & Rubric tab.
     */
    fun swipeUpGradeAndRubric() {
        composeTestRule
            .onNodeWithTag("speedGraderGradeScrollContainer", useUnmergedTree = true)
            .performTouchInput { swipeUp() }
        composeTestRule.waitForIdle()
    }

    /**
     * Asserts that the student is being graded.
     *
     * @param student The student to be graded.
     */
    fun assertGradingStudent(student: User) {
        onWebView(withId(R.id.contentWebView))
            .withElement(findElement(Locator.TAG_NAME, "html"))
            .check(webMatches(getText(), Matchers.containsString(student.shortName)))
    }

    /**
     * Asserts the current student name displayed in the SpeedGrader page.
     *
     * @param studentName The expected name of the current student to be displayed.
     */
    fun assertCurrentStudent(studentName: String) {
        composeTestRule.onNode(hasTestTag("speedGraderUserName") and hasText(studentName), useUnmergedTree = true).assertIsDisplayed()
    }

    /**
     * Asserts the current student submission status.
     *
     * @param status The expected submission status to be displayed.
     */
    fun assertCurrentStudentStatus(status: String) {
        composeTestRule.onNode(hasTestTag("submissionStatusLabel") and hasText(status), useUnmergedTree = true).assertIsDisplayed()
    }

    /**
     * Swipes left to navigate to the next student in the SpeedGrader page.
     */
    fun swipeToNextStudent() {
        composeTestRule.onNodeWithTag("speedGraderPager")
            .performTouchInput { swipeLeft() }
        composeTestRule.waitForIdle()
    }

    /**
     * Swipes right to navigate to the previous student in the SpeedGrader page.
     */
    fun swipeToPreviousStudent() {
        composeTestRule.onNodeWithTag("speedGraderPager")
            .performTouchInput { swipeRight() }
        composeTestRule.waitForIdle()
    }

    /**
     * Clicks the back button.
     */
    fun clickBackButton() {
        composeTestRule.onNode(hasTestTag("navigationButton")).performClick()
        composeTestRule.waitForIdle()
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
        waitForViewWithId(R.id.urlTextView).assertCompletelyDisplayed()
            .assertHasText(submission.url!!)
    }

    /**
     * Asserts that the URL submission link is displayed with the specified submission.
     *
     * @param submission The submission with the URL.
     */
    fun assertDisplaysUrlSubmissionLink(submission: Submission) {
        waitForViewWithId(R.id.urlTextView).assertCompletelyDisplayed()
            .assertHasText(submission.url!!)
    }

    /**
     * Asserts that the URL web view is displayed.
     */
    fun assertDisplaysUrlWebView() {
        waitForViewWithId(R.id.urlTextView).click()
        waitForViewWithId(R.id.canvasWebView).assertCompletelyDisplayed()
    }

}

/**
 * Custom semantics matcher for finding comments by their unique ID.
 * Uses the CommentIdKey from production code to match against comment IDs.
 */
fun hasCommentId(expectedId: String): SemanticsMatcher =
    SemanticsMatcher("has commentId=$expectedId") { node ->
        node.config.getOrNull(CommentIdKey) == expectedId
    }
