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
package com.instructure.student.ui.renderTests

import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.espresso.*
import com.instructure.espresso.page.onViewWithText
import com.instructure.student.espresso.StudentRenderTest
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.RatingData
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.RubricListData
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricViewState
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui.SubmissionRubricFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsTabData
import com.instructure.student.mobius.assignmentDetails.ui.gradeCell.GradeCellViewState
import com.instructure.student.ui.pages.renderPages.SubmissionRubricRenderPage
import com.spotify.mobius.runners.WorkRunner
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubmissionRubricRenderTest : StudentRenderTest() {

    private val page = SubmissionRubricRenderPage()

    private val dataTemplate = RubricListData.Criterion(
        criterionId = "123",
        description = "Criterion description",
        ratingDescription = "Selected rating description",
        ratings = listOf(
            RatingData(
                points = "0",
                description = "No Effort",
                isSelected = false
            ),
            RatingData(
                points = "5",
                description = "Bad Job",
                isSelected = false
            ),
            RatingData(
                points = "10",
                description = "Could be better",
                isSelected = true
            ),
            RatingData(
                points = "15",
                description = "Top notch",
                isSelected = false
            )
        ),
        comment = "Test comment",
        showLongDescriptionButton = true
    )

    @Test
    fun displaysEmptyState() {
        loadPageWithViewData(RubricListData.Empty)
        page.emptyView.assertVisible()
    }

    @Test
    fun displaysGrade() {
        loadPageWithViewData(RubricListData.Grade(GradeCellViewState.Empty))
        page.gradeView.assertVisible()
    }

    @Test
    fun displaysCriterionDescription() {
        val data = dataTemplate
        loadPageWithViewData(data)
        page.criterionDescription.assertVisible()
        page.criterionDescription.assertHasText(data.description)
    }

    @Test
    fun displaysSelectedRatingDescription() {
        val data = dataTemplate
        loadPageWithViewData(data)
        page.selectedRatingDescription.assertVisible()
        page.selectedRatingDescription.assertHasText(data.ratingDescription!!)
    }

    @Test
    fun hidesSelectedRatingDescription() {
        val data = dataTemplate.copy(ratingDescription = null)
        loadPageWithViewData(data)
        page.selectedRatingDescription.assertGone()
    }

    @Test
    fun displaysRatingItems() {
        val data = dataTemplate
        loadPageWithViewData(data)
        page.ratingLayout.check(matches(hasChildCount(data.ratings.size)))
        data.ratings.forEach {
            val view = page.onViewWithText(it.points)

            // Assert displayed
            view.assertDisplayed()

            // Assert selection state
            if (it.isSelected) {
                view.check(matches(isSelected()))
            } else {
                view.check(matches(not(isSelected())))
            }

            // Assert tooltip shows description
            view.click()
            page.onViewWithText(it.description!!).inRoot(RootMatchers.hasWindowLayoutParams()).assertDisplayed()
        }
    }

    @Test
    fun displaysComment() {
        val data = dataTemplate
        loadPageWithViewData(data)
        page.commentContainer.assertVisible()
        page.comment.assertHasText(data.comment!!)
    }

    @Test
    fun hidesComment() {
        val data = dataTemplate.copy(comment = null)
        loadPageWithViewData(data)
        page.commentContainer.assertGone()
    }

    @Test
    fun displaysLongDescriptionButton() {
        val data = dataTemplate
        loadPageWithViewData(data)
        page.longDescriptionButton.assertVisible()
        page.bottomPadding.assertGone()
    }

    @Test
    fun hidesLongDescriptionButton() {
        val data = dataTemplate.copy(showLongDescriptionButton = false)
        loadPageWithViewData(data)
        page.longDescriptionButton.assertGone()
        page.bottomPadding.assertVisible()
    }

    @Test
    fun displaysLongDescriptionDialog() {
        val description = "This is a description"
        val longDescription = "Long Description ".repeat(20).trim()
        val data = dataTemplate.copy(description = "")
        val fragment = loadPageWithViewData(data)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val view = fragment.getMobiusView()
        view.displayLongDescription(description, """<p>$longDescription</p>""")
        page.onViewWithText(description).assertDisplayed()
        Web.onWebView()
            .withElement(DriverAtoms.findElement(Locator.TAG_NAME, "p"))
            .check(
                WebViewAssertions.webMatches(
                    DriverAtoms.getText(),
                    Matchers.comparesEqualTo(longDescription)
                )
            )
    }

    @Test
    fun doesNotDisplayTooltipForEmptyDescription() {
        val ratingData = RatingData(
            points = "10 / 15 pts",
            description = null,
            isSelected = true
        )
        val data = dataTemplate.copy(ratings = listOf(ratingData))
        loadPageWithViewData(data)
        page.onViewWithText(ratingData.points).click()
        page.tooltip.inRoot(RootMatchers.hasWindowLayoutParams()).check(doesNotExist())
    }

    @Test
    fun hidesRatingContainerIfNoRatings() {
        val data = dataTemplate.copy(ratings = emptyList())
        loadPageWithViewData(data)
        page.ratingLayout.assertGone()
    }

    private fun loadPageWithViewData(listData: RubricListData): SubmissionRubricFragment {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val fragment = SubmissionRubricFragment().apply {
            overrideInitViewState = SubmissionRubricViewState(listOf(listData))
            loopMod = { it.effectRunner { emptyEffectRunner } }
            data = SubmissionDetailsTabData.RubricData(
                name = "Rubric",
                assignment = Assignment(),
                submission = Submission()
            )
        }
        activityRule.activity.loadFragment(fragment)
        return fragment
    }

}
