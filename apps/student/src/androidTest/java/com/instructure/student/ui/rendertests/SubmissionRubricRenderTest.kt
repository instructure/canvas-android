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
package com.instructure.student.ui.rendertests

import android.graphics.Color
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvas.espresso.assertFontSizeSP
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricCriterionRating
import com.instructure.canvasapi2.models.Submission
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertGone
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.assertVisible
import com.instructure.espresso.click
import com.instructure.espresso.page.onViewWithText
import com.instructure.pandautils.features.assignments.details.mobius.gradeCell.GradeCellViewState
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.RatingData
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.RubricListData
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricModel
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.SubmissionRubricViewState
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui.SubmissionRubricFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsTabData
import com.instructure.student.ui.rendertests.renderpages.SubmissionRubricRenderPage
import com.instructure.student.ui.utils.StudentRenderTest
import com.spotify.mobius.runners.WorkRunner
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.not
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SubmissionRubricRenderTest : StudentRenderTest() {

    private val page = SubmissionRubricRenderPage()

    private val dataTemplate = RubricListData.Criterion(
        criterionId = "123",
        title = "Criterion title",
        ratingTitle = "Rating Title",
        ratingDescription = "Rating Description",
        ratings = listOf(
            RatingData(
                id = "_id0",
                text = "0",
                isSelected = false,
                isAssessed = false
            ),
            RatingData(
                id = "_id1",
                text = "5",
                isSelected = false,
                isAssessed = false
            ),
            RatingData(
                id = "_id2",
                text = "10",
                isSelected = true,
                isAssessed = true
            ),
            RatingData(
                id = "_id3",
                text = "15",
                isSelected = false,
                isAssessed = false
            )
        ),
        comment = "Test comment",
        showDescriptionButton = true,
        tint = Color.BLUE
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
    fun displaysCriterionTitle() {
        val data = dataTemplate
        loadPageWithViewData(data)
        page.criterionTitle.assertVisible()
        page.criterionTitle.assertHasText(data.title)
    }

    @Test
    fun displaysRatingItems() {
        val data = dataTemplate
        loadPageWithViewData(data)
        page.ratingLayout.check(matches(hasChildCount(data.ratings.size)))
        data.ratings.forEach {
            val view = page.onViewWithText(it.text)

            // Assert displayed
            view.assertDisplayed()

            // Assert selection state
            if (it.isSelected) {
                view.check(matches(isSelected()))
            } else {
                view.check(matches(not(isSelected())))
            }

            // Assert clicking updates rating title and description
            view.click()

            page.onViewWithText(it.text).inRoot(RootMatchers.hasWindowLayoutParams()).assertDisplayed()
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
        page.descriptionButton.assertVisible()
    }

    @Test
    fun hidesLongDescriptionButton() {
        val data = dataTemplate.copy(showDescriptionButton = false)
        loadPageWithViewData(data)
        page.descriptionButton.assertGone()
    }

    @Test
    fun usesLargeFontSize() {
        val data = dataTemplate.copy(
            ratings = listOf(
                RatingData(
                    id = "_id0",
                    text = "Large text",
                    isSelected = false,
                    isAssessed = false,
                    useSmallText = false
                )
            )
        )
        loadPageWithViewData(data)
        page.onViewWithText("Large text").assertFontSizeSP(20f)
    }

    @Test
    fun usesSmallFontSize() {
        val data = dataTemplate.copy(
            ratings = listOf(
                RatingData(
                    id = "_id0",
                    text = "Small text",
                    isSelected = false,
                    isAssessed = false,
                    useSmallText = true
                )
            )
        )
        loadPageWithViewData(data)
        page.onViewWithText("Small text").assertFontSizeSP(16f)
    }

    @Test
    fun hidesRatingContainerIfNoRatings() {
        val data = dataTemplate.copy(ratings = emptyList())
        loadPageWithViewData(data)
        page.ratingLayout.assertGone()
    }

    @Test
    fun populatesRatingTitleAndDescriptionWhenPresent() {
        val data = dataTemplate
        loadPageWithViewData(data)
        page.ratingInfoContainer.assertVisible()
        page.selectedRatingTitle.assertVisible()
        page.selectedRatingTitle.assertHasText(data.ratingTitle!!)
        page.selectedRatingDescription.assertVisible()
        page.selectedRatingDescription.assertHasText(data.ratingDescription!!)
    }

    @Test
    fun updatesRatingInfoOnClick() {
        val assignment = Assignment(
            courseId = 123L,
            rubric = listOf(
                RubricCriterion(
                    id = "123",
                    description = "Criterion description 1",
                    longDescription = "This is a long description for criterion 1",
                    points = 15.0,
                    ratings = mutableListOf(
                        RubricCriterionRating("_id1", "Rating 1 Title", "Rating 1 Description", 5.5),
                        RubricCriterionRating("_id2", "Rating 2 Title", null, 10.0),
                        RubricCriterionRating("_id3", null, null, 15.0)
                    )
                )
            )
        )
        loadPageWithModel(SubmissionRubricModel(assignment, Submission()))

        // No ratings should be selected at this point, so the rating info container should not be showing
        page.ratingInfoContainer.assertGone()

        // Clicking on the "5.5" rating should show the info container with the correct title and description
        page.onViewWithText("5.5").click()
        page.ratingInfoContainer.assertVisible()
        page.selectedRatingTitle.assertVisible()
        page.selectedRatingTitle.assertHasText("Rating 1 Title")
        page.selectedRatingDescription.assertVisible()
        page.selectedRatingDescription.assertHasText("Rating 1 Description")

        // Clicking on the "15" rating should completely hide the info container
        page.onViewWithText("15").click()
        page.ratingInfoContainer.assertGone()

        // Clicking on the "10" rating should show the info container with just the title
        page.onViewWithText("10").click()
        page.ratingInfoContainer.assertVisible()
        page.selectedRatingTitle.assertVisible()
        page.selectedRatingTitle.assertHasText("Rating 2 Title")
        page.selectedRatingDescription.assertGone()
    }

    @Test
    fun hidesRatingTitleAndDescriptionIfNotPresent() {
        val data = dataTemplate.copy(
            ratingTitle = null,
            ratingDescription = null
        )
        loadPageWithViewData(data)
        page.ratingInfoContainer.assertGone()
        page.selectedRatingTitle.assertNotDisplayed()
        page.selectedRatingDescription.assertNotDisplayed()
    }

    @Test
    fun hidesRatingTitleIfNotPresent() {
        val data = dataTemplate.copy(ratingTitle = null)
        loadPageWithViewData(data)
        page.ratingInfoContainer.assertVisible()
        page.selectedRatingTitle.assertNotDisplayed()
        page.selectedRatingDescription.assertVisible()
        page.selectedRatingDescription.assertHasText(data.ratingDescription!!)
    }

    @Test
    fun hidesRatingDescriptionIfNotPresent() {
        val data = dataTemplate.copy(ratingDescription = null)
        loadPageWithViewData(data)
        page.ratingInfoContainer.assertVisible()
        page.selectedRatingTitle.assertVisible()
        page.selectedRatingTitle.assertHasText(data.ratingTitle!!)
        page.selectedRatingDescription.assertNotDisplayed()
    }

    private fun loadPageWithViewData(listData: RubricListData): SubmissionRubricFragment {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val data = SubmissionDetailsTabData.RubricData(
            name = "Rubric",
            assignment = Assignment(),
            submission = Submission()
        )
        val fragment = SubmissionRubricFragment.newInstance(data).apply {
            overrideInitViewState = SubmissionRubricViewState(listOf(listData))
            loopMod = { it.effectRunner { emptyEffectRunner } }

        }
        activityRule.activity.loadFragment(fragment)
        return fragment
    }

    private fun loadPageWithModel(model: SubmissionRubricModel): SubmissionRubricFragment {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val data = SubmissionDetailsTabData.RubricData(
            name = "Rubric",
            assignment = Assignment(),
            submission = Submission()
        )
        val fragment = SubmissionRubricFragment.newInstance(data).apply {
            overrideInitModel = model
            loopMod = { it.effectRunner { emptyEffectRunner } }
        }
        activityRule.activity.loadFragment(fragment)
        return fragment
    }

}
