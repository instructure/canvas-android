/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.pandautils.features.assignments.details.gradecellview

import android.content.res.Resources
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingSchemeRow
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ColorKeeper
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test
import java.util.Date

class GradeCellViewDataTest {

    private val resources: Resources = mockk(relaxed = true)
    private val colorKeeper: ColorKeeper = mockk(relaxed = true)

    @Test
    fun `Map empty grade cell`() {
        val gradeCell = GradeCellViewData.fromSubmission(
            resources,
            colorKeeper.getOrGenerateColor(Course()),
            Assignment(),
            Submission()
        )

        Assert.assertEquals(GradeCellViewData.State.EMPTY, gradeCell.state)
    }

    @Test
    fun `Map submitted grade cell`() {
        val gradeCell = GradeCellViewData.fromSubmission(
            resources,
            colorKeeper.getOrGenerateColor(Course()),
            Assignment(),
            Submission(submittedAt = Date(), workflowState = "submitted")
        )

        Assert.assertEquals(GradeCellViewData.State.SUBMITTED, gradeCell.state)
    }

    @Test
    fun `Map graded grade cell`() {
        val gradeCell = GradeCellViewData.fromSubmission(
            resources,
            colorKeeper.getOrGenerateColor(Course()),
            Assignment(),
            Submission(submittedAt = Date(), grade = "A")
        )

        Assert.assertEquals(GradeCellViewData.State.GRADED, gradeCell.state)
    }

    @Test
    fun `Create empty grade cell when assignment is quantitative and quantitative data is restricted and there is no grading scheme`() {
        val gradeCell = GradeCellViewData.fromSubmission(
            resources,
            colorKeeper.getOrGenerateColor(Course()),
            Assignment(gradingType = Assignment.POINTS_TYPE),
            Submission(submittedAt = Date(), grade = "10", score = 10.0),
            restrictQuantitativeData = true
        )

        Assert.assertEquals(GradeCellViewData.State.EMPTY, gradeCell.state)
    }

    @Test
    fun `Create cell with converted grade when assignment is quantitative and quantitative data is restricted`() {
        val gradeCell = GradeCellViewData.fromSubmission(
            resources,
            colorKeeper.getOrGenerateColor(Course()),
            Assignment(gradingType = Assignment.POINTS_TYPE, pointsPossible = 20.0),
            Submission(submittedAt = Date(), grade = "10", score = 17.0),
            restrictQuantitativeData = true,
            gradingScheme = listOf(
                GradingSchemeRow("A", 0.9),
                GradingSchemeRow("B", 0.8),
                GradingSchemeRow("F", 0.0)
            )
        )

        Assert.assertEquals(GradeCellViewData.State.GRADED, gradeCell.state)
        Assert.assertEquals("B", gradeCell.grade)
    }

    @Test
    fun `Create excused grade cell without points when assignment is quantitative and quantitative data is restricted`() {
        every { resources.getString(R.string.excused) } returns "EX"
        every { resources.getString(R.string.outOfPointsAbbreviatedFormatted, any()) } returns "out of 10"

        val gradeCell = GradeCellViewData.fromSubmission(
            resources,
            colorKeeper.getOrGenerateColor(Course()),
            Assignment(gradingType = Assignment.POINTS_TYPE),
            Submission(submittedAt = Date(), grade = "10", score = 10.0, excused = true),
            restrictQuantitativeData = true
        )

        Assert.assertEquals(GradeCellViewData.State.GRADED, gradeCell.state)
        Assert.assertEquals("EX", gradeCell.grade)
        Assert.assertEquals("", gradeCell.outOf)
    }

    @Test
    fun `Create excused grade cell with points when assignment is quantitative and quantitative data is not restricted`() {
        every { resources.getString(R.string.excused) } returns "EX"
        every { resources.getString(R.string.outOfPointsAbbreviatedFormatted, any()) } returns "out of 10"

        val gradeCell = GradeCellViewData.fromSubmission(
            resources,
            colorKeeper.getOrGenerateColor(Course()),
            Assignment(gradingType = Assignment.POINTS_TYPE),
            Submission(submittedAt = Date(), grade = "10", score = 10.0, excused = true),
            restrictQuantitativeData = false
        )

        Assert.assertEquals(GradeCellViewData.State.GRADED, gradeCell.state)
        Assert.assertEquals("EX", gradeCell.grade)
        Assert.assertEquals("out of 10", gradeCell.outOf)
    }

    @Test
    fun `Create letter grade cell with points when quantitative data is not restricted`() {
        every { resources.getString(R.string.outOfPointsAbbreviatedFormatted, any()) } returns "out of 10"

        val gradeCell = GradeCellViewData.fromSubmission(
            resources,
            colorKeeper.getOrGenerateColor(Course()),
            Assignment(gradingType = Assignment.LETTER_GRADE_TYPE, pointsPossible = 10.0),
            Submission(submittedAt = Date(), grade = "A", score = 10.0, enteredScore = 10.0),
            restrictQuantitativeData = false
        )

        Assert.assertEquals(GradeCellViewData.State.GRADED, gradeCell.state)
        Assert.assertEquals("A", gradeCell.grade)
        Assert.assertEquals("10", gradeCell.score)
        Assert.assertEquals(1.0f, gradeCell.chartPercent)
        Assert.assertEquals("out of 10", gradeCell.outOf)
    }

    @Test
    fun `Create letter grade cell without points when quantitative data is restricted`() {
        every { resources.getString(R.string.outOfPointsAbbreviatedFormatted, any()) } returns "out of 10"

        val gradeCell = GradeCellViewData.fromSubmission(
            resources,
            colorKeeper.getOrGenerateColor(Course()),
            Assignment(gradingType = Assignment.LETTER_GRADE_TYPE, pointsPossible = 10.0),
            Submission(submittedAt = Date(), grade = "A", score = 10.0, enteredScore = 10.0),
            restrictQuantitativeData = true
        )

        Assert.assertEquals(GradeCellViewData.State.GRADED, gradeCell.state)
        Assert.assertEquals("A", gradeCell.grade)
        Assert.assertEquals("", gradeCell.score)
        Assert.assertEquals(1.0f, gradeCell.chartPercent)
        Assert.assertEquals("", gradeCell.outOf)
    }
}
