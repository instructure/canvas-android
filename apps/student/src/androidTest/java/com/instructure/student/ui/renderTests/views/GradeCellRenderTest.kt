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
 */
package com.instructure.student.ui.renderTests.views

import android.view.ViewGroup
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertGone
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.pandautils.features.assignments.details.mobius.gradeCell.GradeCellView
import com.instructure.pandautils.features.assignments.details.mobius.gradeCell.GradeCellViewState
import com.instructure.student.R
import com.instructure.student.espresso.StudentRenderTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GradeCellRenderTest : StudentRenderTest() {

    private val gradeCell = object : BasePage() {
        val root by OnViewWithId(R.id.gradeCell)
        val score by OnViewWithId(R.id.score)
        val submittedTitle by OnViewWithId(R.id.submittedTitle)
        val submittedSubtitle by OnViewWithId(R.id.submittedSubtitle)
        val pointsLabel by OnViewWithId(R.id.pointsLabel)
        val yourGrade by OnViewWithId(R.id.yourGrade)
        val latePenalty by OnViewWithId(R.id.latePenalty)
        val finalGrade by OnViewWithId(R.id.finalGrade)
        val grade by OnViewWithId(R.id.grade)
        val outOf by OnViewWithId(R.id.outOf)
        val gradedStateContainer by OnViewWithId(R.id.gradeState)
        val statsContainer by OnViewWithId(R.id.statsContainer)
        val statsGraph by OnViewWithId(R.id.statisticsView)
        val statsLow by OnViewWithId(R.id.minLabel)
        val statsMean by OnViewWithId(R.id.meanLabel)
        val statsHigh by OnViewWithId(R.id.maxLabel)
    }

    @Test
    fun hidesViewForEmptyState() {
        setupViewWithState(GradeCellViewState.Empty)
        gradeCell.root.assertNotDisplayed()
    }

    @Test
    fun correctlyDisplaysSubmittedState() {
        setupViewWithState(GradeCellViewState.Submitted)
        gradeCell.gradedStateContainer.assertNotDisplayed()
        gradeCell.submittedTitle.assertDisplayed()
        gradeCell.submittedSubtitle.assertDisplayed()
        gradeCell.submittedTitle.assertHasText("Successfully submitted!")
        gradeCell.submittedSubtitle.assertHasText("The submission is now waiting to be graded")
    }

    @Test
    fun correctlyDisplaysNotGradedState() {
        val state = GradeCellViewState.GradeData(
            score = "N/A",
            showPointsLabel = true,
            outOf = "Out of 100 pts"
        )
        setupViewWithState(state)

        // Should show score, type label, and outOf
        with(gradeCell) {
            score.assertDisplayed()
            pointsLabel.assertDisplayed()
            outOf.assertDisplayed()
        }

        // Should populate with correct values
        with(gradeCell) {
            score.assertHasText(state.score)
            outOf.assertHasText(state.outOf)
        }
    }

    @Test
    fun correctlyDisplaysGradedState() {
        val state = GradeCellViewState.GradeData(
            graphPercent = 0.89f,
            score = "89",
            grade = "89%",
            showPointsLabel = true,
            outOf = "Out of 100 pts"
        )
        setupViewWithState(state)

        // Should show the score, type label, grade, and outOf
        with(gradeCell) {
            score.assertDisplayed()
            pointsLabel.assertDisplayed()
            grade.assertDisplayed()
            outOf.assertDisplayed()
        }

        // Should populate with correct values
        with(gradeCell) {
            score.assertHasText(state.score)
            grade.assertHasText(state.grade)
            outOf.assertHasText(state.outOf)
        }
    }

    @Test
    fun correctlyDisplaysGradedStateWithLatePenalty() {
        val state = GradeCellViewState.GradeData(
            graphPercent = 0.89f,
            score = "91",
            showPointsLabel = true,
            outOf = "Out of 100 pts",
            yourGrade = "Your Grade: 91 pts",
            latePenalty = "Late Penalty: -2 pts",
            finalGrade = "Final Grade: 89 pts"
        )
        setupViewWithState(state)

        // Should show the score, typeLabel, outOf, latePenalty, and finalGrade
        with(gradeCell) {
            score.assertDisplayed()
            pointsLabel.assertDisplayed()
            outOf.assertDisplayed()
            yourGrade.assertDisplayed()
            latePenalty.assertDisplayed()
            finalGrade.assertDisplayed()
        }

        // Should populate with correct values
        with(gradeCell) {
            score.assertHasText(state.score)
            outOf.assertHasText(state.outOf)
            yourGrade.assertHasText(state.yourGrade)
            latePenalty.assertHasText(state.latePenalty)
            finalGrade.assertHasText(state.finalGrade)
        }
    }

    @Test
    fun displaysGradeStatsIfPresent() {
        val state = GradeCellViewState.GradeData(
            stats = GradeCellViewState.GradeStats(
                score = 81.0,
                outOf = 100.0,
                min = 38.0,
                max = 97.0,
                mean = 76.0,
                minText = "Low: 38",
                maxText = "High: 97",
                meanText = "Mean: 76"
            )
        )
        setupViewWithState(state)

        // Should show the stats container, graph, and min/mean/max text values
        with(gradeCell) {
            statsContainer.assertDisplayed()
            statsGraph.assertDisplayed()
            statsLow.assertHasText(state.stats!!.minText)
            statsMean.assertHasText(state.stats!!.meanText)
            statsHigh.assertHasText(state.stats!!.maxText)
        }
    }

    @Test
    fun hidesGradeStatsIfNotPresent() {
        val state = GradeCellViewState.GradeData(stats = null)
        setupViewWithState(state)

        // Should show the stats container and min/mean/max text values
        with(gradeCell) {
            statsContainer.assertGone()
            statsGraph.assertGone()
            statsLow.assertGone()
            statsMean.assertGone()
            statsHigh.assertGone()
        }
    }

    private fun setupViewWithState(state: GradeCellViewState) : GradeCellView {
        val view = GradeCellView(activityRule.activity)
        var complete = false
        activityRule.runOnUiThread {
            activityRule.activity.loadView(
                view,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            view.setState(state)
            complete = true
        }
        while (!complete) {}
        return view
    }

}
