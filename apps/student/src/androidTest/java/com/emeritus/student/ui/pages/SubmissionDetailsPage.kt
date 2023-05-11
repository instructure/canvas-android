/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 *
 */
package com.emeritus.student.ui.pages

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.clickCoordinates
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.waitForViewWithId
import com.emeritus.student.ui.pages.renderPages.SubmissionCommentsRenderPage
import org.hamcrest.Matchers.*
import java.lang.Thread.sleep

open class SubmissionDetailsPage : BasePage(R.id.submissionDetails) {
    private val commentsButton by OnViewWithStringTextIgnoreCase("comments")

    private val submissionCommentsRenderPage = SubmissionCommentsRenderPage()

    fun assertPdfAnnotationSelected() {
        waitForViewWithId(R.id.commentsButton).assertDisplayed()
    }

    fun clickSubmissionContentAtPosition(percentX: Float, percentY: Float) {
        sleep(1000) // Sometimes the pdf/annotations aren't fully ready when we click
        onViewWithId(R.id.submissionContent).perform(clickCoordinates(percentX, percentY))
    }

    fun openPdfComments() {
        waitForViewWithId(R.id.commentsButton).click()
    }

    fun addFirstAnnotationComment(text: String) {
        waitForViewWithId(R.id.commentsButton).click()
        waitForViewWithId(R.id.freeTextInput).replaceText(text)
        onView(containsTextCaseInsensitive("OK")).click()
    }

    fun openComments() {
        commentsButton.click()
    }

    fun openFiles() {
        onView(allOf(containsTextCaseInsensitive("files"), isDisplayed())).click()
    }

    fun openRubric() {
        onView(allOf(containsTextCaseInsensitive("rubric"), isDisplayed())).click()
    }

    /**
     * Assert that a comment is displayed
     * [description] contains some text that is in the comment
     * [user] is the author of the comment
     */
    fun assertCommentDisplayed(description: String, user: CanvasUserApiModel) {
        assertCommentDisplayedCommon(description, user.shortName)
    }

    /**
     * Assert that a comment is displayed
     * [description] contains some text that is in the comment
     * [user] is the author of the comment
     */
    fun assertCommentDisplayed(description: String, user: User) {
        assertCommentDisplayedCommon(description, user.shortName!!)
    }

    private fun assertCommentDisplayedCommon(description: String, shortUserName: String) {
        val commentMatcher = allOf(
                withId(R.id.commentHolder),
                hasDescendant(allOf(withText(shortUserName), withId(R.id.userNameTextView))),
                hasDescendant(allOf(withText(containsString(description)), anyOf(withId(R.id.titleTextView), withId(R.id.commentTextView))))
        )

        submissionCommentsRenderPage.scrollAndAssertDisplayed(commentMatcher)

    }

    /**
     * Assert that the comment stream contains a video comment
     */
    fun assertVideoCommentDisplayed() {
        val commentMatcher = allOf(
                withId(R.id.commentHolder),
                hasDescendant(allOf(containsTextCaseInsensitive("video"), withId(R.id.attachmentNameTextView)))
        )

        submissionCommentsRenderPage.scrollAndAssertDisplayed(commentMatcher)
    }

    /**
     * Assert that the comment stream contains an audio comment
     */
    fun assertAudioCommentDisplayed() {
        val commentMatcher = allOf(
                withId(R.id.commentHolder),
                hasDescendant(allOf(containsTextCaseInsensitive("audio"), withId(R.id.attachmentNameTextView)))
        )

        submissionCommentsRenderPage.scrollAndAssertDisplayed(commentMatcher)
    }

    /**
     * Assert that a comment is displayed
     * [fileName] is the name of the attached file
     * [user] is the author of the comment
     */
    fun assertCommentAttachmentDisplayed(fileName: String, user: CanvasUserApiModel) {
        assertCommentAttachmentDisplayedCommon(fileName, user.shortName, false)
    }

    /**
     * Assert that a comment is displayed
     * [fileName] is the name of the attached file
     * [user] is the author of the comment
     */
    fun assertCommentAttachmentDisplayed(fileName: String, user: User) {
        assertCommentAttachmentDisplayedCommon(fileName, user.shortName!!, false)
    }

    /**
     * Open a comment attachment
     */
    fun openCommentAttachment(fileName: String, user: User) {
        assertCommentAttachmentDisplayedCommon(fileName, user.shortName!!, true)
    }

    /**
     * Utility method to scroll to (and optionally click) a comment attachment
     */
    private fun assertCommentAttachmentDisplayedCommon(fileName: String, displayName: String, click:Boolean = false) {
        val commentMatcher = allOf(
                withId(R.id.commentHolder),
                hasDescendant(allOf(withText(displayName), withId(R.id.userNameTextView))),
                hasDescendant(allOf(withText(fileName), withId(R.id.attachmentNameTextView)))
        )

        submissionCommentsRenderPage.scrollAndAssertDisplayed(commentMatcher)
        if(click) {
            //onView(commentMatcher).click()
            onView(allOf(withId(R.id.attachmentNameTextView), withText(fileName)))
                    .perform(withCustomConstraints(click(), isDisplayingAtLeast(5)))
        }

    }

    fun assertFileDisplayed(fileName: String) {
        val matcher = allOf(withId(R.id.fileName),withText(fileName))
        openFiles() // Make sure that the files tab is open
        scrollRecyclerView(R.id.recyclerView, matcher)
        onView(matcher).assertDisplayed()
    }

    fun addAndSendComment(comment: String) {
        submissionCommentsRenderPage.addAndSendComment(comment)
    }

    fun addAndSendVideoComment() {
        submissionCommentsRenderPage.addAndSendVideoComment()
    }

    fun addAndSendAudioComment() {
        submissionCommentsRenderPage.addAndSendAudioComment()
    }

    /**
     * Check that the RubricCriterion is displayed, and clicking on each rating
     * results in its description and longDescription being displayed.
     */
    fun assertRubricCriterionDisplayed(rc: RubricCriterion) {
        rc.ratings.forEach { rating ->
            val matcher = allOf(withParent(withId(R.id.ratingLayout)), withText(rating.points.toInt().toString()))
            scrollRecyclerView(R.id.recyclerView, matcher)
            onView(matcher).assertDisplayed()
            onView(matcher).perform(withCustomConstraints(click(), isDisplayingAtLeast(10))) // click on rating

            val descriptionMatcher = allOf(withId(R.id.ratingTitle), withText(rating.description))
            scrollRecyclerView(R.id.recyclerView, descriptionMatcher)
            onView(descriptionMatcher).check(matches(isDisplayingAtLeast(10)))

            if(rating.longDescription != null) {
                val longDescriptionMatcher = allOf(withId(R.id.ratingDescription), withText(rating.longDescription))
                scrollRecyclerView(R.id.recyclerView, longDescriptionMatcher)
                onView(longDescriptionMatcher).check(matches(isDisplayingAtLeast(10)))
            }
        }
    }

    /**
     * Checks that pressing the "Description" button pops up a webview with the longDescription text
     */
    fun assertRubricDescriptionDisplays(rc: RubricCriterion) {
        val matcher = allOf(withId(R.id.descriptionButton), containsTextCaseInsensitive("description"))
        scrollRecyclerView(R.id.recyclerView, matcher)
        onView(matcher).assertDisplayed() // probably unnecessary
        onView(matcher).click()

        onWebView(withId(R.id.webView))
                .withElement(findElement(Locator.ID, "content"))
                .check(webMatches(getText(), containsString(rc.longDescription)))

        Espresso.pressBack() // return from web page

    }


}

