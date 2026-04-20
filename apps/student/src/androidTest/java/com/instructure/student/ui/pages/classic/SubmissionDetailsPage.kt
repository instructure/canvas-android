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
package com.instructure.student.ui.pages.classic

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isActivated
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.utils.actionWithCustomConstraints
import com.instructure.canvas.espresso.utils.clickCoordinates
import com.instructure.canvas.espresso.utils.containsTextCaseInsensitive
import com.instructure.canvas.espresso.utils.scrollRecyclerView
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.RubricCriterion
import com.instructure.dataseeding.model.RubricCriterionRating
import com.instructure.espresso.OnViewWithStringTextIgnoreCase
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.waitForViewWithId
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.replaceText
import com.instructure.student.R
import com.instructure.student.ui.rendertests.renderpages.SubmissionCommentsRenderPage
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
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

    fun assertTextSubmissionDisplayedAsComment() {
        onView(allOf(withId(R.id.subtitleTextView), withParent(withId(R.id.commentSubmissionAttachmentView)), hasSibling(withId(R.id.titleTextView) + withText("Text Submission")))).assertDisplayed()
    }

    fun assertCommentNotDisplayed(comment: String, user: User) {
        assertCommentNotDisplayed(comment, user.shortName!!)
    }

    fun assertCommentNotDisplayed(comment: String, user: CanvasUserApiModel) {
        assertCommentNotDisplayed(comment, user.shortName)
    }

    fun assertCommentNotDisplayed(comment: String, userShortName: String) {
        val commentMatcher = allOf(
            withId(R.id.commentHolder),
            hasDescendant(allOf(withText(userShortName), withId(R.id.userNameTextView))),
            hasDescendant(allOf(withText(containsString(comment)), anyOf(withId(R.id.titleTextView), withId(R.id.commentTextView))))
        )

        onView(commentMatcher).check(doesNotExist())
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
     * Click on the video comment to open it
     */
    fun clickVideoComment() {
        onView(allOf(withId(R.id.attachmentNameTextView), containsTextCaseInsensitive("video"))).click()
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
     * Click on the audio comment to open it
     */
    fun clickAudioComment() {
        onView(allOf(withId(R.id.attachmentNameTextView), containsTextCaseInsensitive("audio"))).click()
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
                    .perform(actionWithCustomConstraints(click(), isDisplayingAtLeast(5)))
        }

    }

    fun assertFileDisplayed(fileName: String) {
        val matcher = allOf(withId(R.id.fileName),withText(fileName))
        openFiles() // Make sure that the files tab is open
        scrollRecyclerView(R.id.recyclerView, matcher)
        onView(matcher).assertDisplayed()
    }

    /* Grabs the current coordinates of the center of drawerTabLayout */
    private val tabLayoutCoordinates = CoordinatesProvider { view ->
        val tabs = view.findViewById<View>(R.id.drawerTabLayout)
        val xy = IntArray(2).apply { tabs.getLocationOnScreen(this) }
        val x = xy[0] + (tabs.width / 2f)
        val y = xy[1] + (tabs.height / 2f)
        floatArrayOf(x, y)
    }

    fun swipeDrawerTo(location: GeneralLocation) {
        onView(withId(R.id.slidingUpPanelLayout)).perform(
            GeneralSwipeAction(Swipe.FAST, tabLayoutCoordinates, location, Press.FINGER)
        )
    }

    fun collapseSlidingPanel() {
        swipeDrawerTo(GeneralLocation.BOTTOM_CENTER)
    }

    fun expandSlidingPanel() {
        swipeDrawerTo(GeneralLocation.TOP_CENTER)
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

    fun clickOnAddAttachmentButton() {
        submissionCommentsRenderPage.clickOnAddAttachmentButton()
    }

    /**
     * Check that the RubricCriterion is displayed, and clicking on each rating
     * results in its description and longDescription being displayed.
     */
    fun assertRubricCriterionDisplayed(rc: RubricCriterion) {
        rc.ratings.forEach { rating ->
            val matcher = allOf(withParent(withId(R.id.ratingLayout)), withText(rating.points.toInt().toString()))
            onView(matcher).assertDisplayed()
            onView(matcher).click()

            val descriptionMatcher = allOf(withId(R.id.ratingTitle), withText(rating.description))
            onView(descriptionMatcher).check(matches(isDisplayingAtLeast(10)))

            if(rating.longDescription != null) {
                val longDescriptionMatcher = allOf(withId(R.id.ratingDescription), withText(rating.longDescription))
                onView(longDescriptionMatcher).check(matches(isDisplayingAtLeast(10)))
            }
        }
    }

    /**
     * Checks that pressing the "Description" button pops up a webview with the longDescription text
     */
    fun assertRubricDescriptionDisplays(rc: RubricCriterion) {
        val matcher = allOf(
            withId(R.id.descriptionButton),
            withAncestor(allOf(withId(R.id.rubricCriterion), hasDescendant(allOf(withId(R.id.criterionTitle), withText(rc.description)))))
        )
        onView(matcher).click()

        onWebView(withId(R.id.webView))
                .withElement(findElement(Locator.ID, "content"))
                .check(webMatches(getText(), containsString(rc.longDescription)))

        Espresso.pressBack() // return from web page

    }

    fun assertRubricRatingSelected(rc: RubricCriterion, rating: RubricCriterionRating) {
        val criterionAncestor = allOf(withId(R.id.rubricCriterion), hasDescendant(allOf(withId(R.id.criterionTitle), withText(rc.description))))
        onView(allOf(withId(R.id.ratingTitle), withText(rating.description), withAncestor(criterionAncestor)))
            .check(matches(isDisplayingAtLeast(10)))
        if (rating.longDescription != null) {
            onView(allOf(withId(R.id.ratingDescription), withText(rating.longDescription), withAncestor(criterionAncestor)))
                .check(matches(isDisplayingAtLeast(10)))
        }
    }

    fun assertRubricCustomScoreSelected(rc: RubricCriterion) {
        val criterionAncestor = allOf(withId(R.id.rubricCriterion), hasDescendant(allOf(withId(R.id.criterionTitle), withText(rc.description))))
        onView(allOf(withId(R.id.ratingTitle), withText(R.string.rubricCustomScore), withAncestor(criterionAncestor)))
            .check(matches(isDisplayingAtLeast(10)))
    }

    fun clickRubricRating(rc: RubricCriterion, rating: RubricCriterionRating) {
        val criterionAncestor = allOf(withId(R.id.rubricCriterion), hasDescendant(allOf(withId(R.id.criterionTitle), withText(rc.description))))
        onView(allOf(withParent(withId(R.id.ratingLayout)), withText(rating.points.toInt().toString()), withAncestor(criterionAncestor))).click()
    }

    fun assertRubricRatingIsAssessed(rc: RubricCriterion, rating: RubricCriterionRating) {
        val criterionAncestor = allOf(withId(R.id.rubricCriterion), hasDescendant(allOf(withId(R.id.criterionTitle), withText(rc.description))))
        onView(allOf(withParent(withId(R.id.ratingLayout)), withText(rating.points.toInt().toString()), withAncestor(criterionAncestor)))
            .check(matches(isActivated()))
    }

    fun assertRubricRatingIsPreviewSelected(rc: RubricCriterion, rating: RubricCriterionRating) {
        val criterionAncestor = allOf(withId(R.id.rubricCriterion), hasDescendant(allOf(withId(R.id.criterionTitle), withText(rc.description))))
        val ratingMatcher = allOf(withParent(withId(R.id.ratingLayout)), withText(rating.points.toInt().toString()), withAncestor(criterionAncestor))
        onView(ratingMatcher).check(matches(isSelected()))
        onView(ratingMatcher).check(matches(not(isActivated())))
    }

    fun assertNoSubmissionEmptyView() {
        onView(allOf(withId(R.id.title), withText(R.string.submissionDetailsNoSubmissionYet), withAncestor(withId(R.id.submissionDetailsEmptyContent)))).assertDisplayed()
    }

    fun selectAttempt(attemptName: String) {
        onView(withId(R.id.submissionVersionsSpinner)).click()
        waitForView(withId(R.id.attemptTitle) + withText(attemptName)).click()
    }

    fun assertSelectedAttempt(attemptName: String) {
        onView(withId(R.id.attemptTitle) + withText(attemptName) + withAncestor(withId(R.id.slidingUpPanelLayout))).assertDisplayed()
    }

}

