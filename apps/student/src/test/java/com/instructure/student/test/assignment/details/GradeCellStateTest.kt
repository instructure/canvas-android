/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.test.assignment.details

import android.content.Context
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentScoreStatistics
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.features.assignments.details.mobius.gradeCell.GradeCellViewState
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GradeCellStateTest : Assert() {

    private val courseColor = Color.RED

    private lateinit var context: Context
    private lateinit var baseAssignment: Assignment
    private lateinit var baseSubmission: Submission
    private lateinit var baseGradedState: GradeCellViewState.GradeData

    @Before
    fun setup() {
        mockkObject(ColorKeeper)
        every { ColorKeeper.getOrGenerateColor(CanvasContext.emptyCourseContext(123)) } returns ThemedColor(courseColor)
        context = ApplicationProvider.getApplicationContext()
        baseAssignment = Assignment(
            courseId = 123,
            pointsPossible = 100.0,
            gradingType = Assignment.POINTS_TYPE
        )

        baseSubmission = Submission(
            attempt = 1L,
            submittedAt = DateHelper.makeDate(2017, 6, 27, 18, 47, 0),
            workflowState = "graded",
            enteredGrade = "85",
            enteredScore = 85.0,
            grade = "85",
            score = 85.0
        )

        baseGradedState = GradeCellViewState.GradeData(
            accentColor = courseColor,
            outOf = "Out of 100 pts",
            outOfContentDescription = "Out of 100 points",
            gradeCellContentDescription = "85 Out of 100 points"
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Returns Empty state for null submission`() {
        val expected = GradeCellViewState.Empty
        val actual = GradeCellViewState.fromSubmission(context, baseAssignment, null)
        assertEquals(expected, actual)
    }

    @Test
    fun `Returns Graded state if graded but not submitted`() {
        val submission = baseSubmission.copy(submittedAt = null)
        val expected = baseGradedState.copy(
            graphPercent = 0.85f,
            score = "85",
            showPointsLabel = true
        )
        val actual = GradeCellViewState.fromSubmission(context, baseAssignment, submission)
        assertEquals(expected, actual)
    }

    @Test
    fun `Returns Empty state if graded, not submitted, and grade is hidden`() {
        val submission = baseSubmission.copy(
            submittedAt = null,
            enteredGrade = null,
            enteredScore = 0.0,
            grade = null,
            score = 0.0
        )
        val expected = GradeCellViewState.Empty
        val actual = GradeCellViewState.fromSubmission(context, baseAssignment, submission)
        assertEquals(expected, actual)
    }

    @Test
    fun `Returns Submitted state if submitted but not graded`() {
        val submission = Submission(
            attempt = 1L,
            submittedAt = DateHelper.makeDate(2017, 6, 27, 18, 47, 0),
            workflowState = "submitted"
        )
        val expected = GradeCellViewState.Submitted
        val actual = GradeCellViewState.fromSubmission(context, baseAssignment, submission)
        assertEquals(expected, actual)
    }

    @Test
    fun `Returns Empty state when not submitted and ungraded`() {
        val submission = Submission()
        val expected = GradeCellViewState.Empty
        val actual = GradeCellViewState.fromSubmission(context, baseAssignment, submission)
        assertEquals(expected, actual)
    }

    @Test
    fun `Returns correct graded state for excused`() {
        val submission = baseSubmission.copy(
            excused = true
        )
        val expected = baseGradedState.copy(
            graphPercent = 1.0f,
            showCompleteIcon = true,
            grade = "Excused",
            outOf = "Out of 100 pts",
            outOfContentDescription = "Out of 100 points",
            gradeCellContentDescription = ""
        )
        val actual = GradeCellViewState.fromSubmission(context, baseAssignment, submission)
        assertEquals(expected, actual)
    }

    @Test
    fun `Returns correct state for 'Percentage' grading type`() {
        val assignment = baseAssignment.copy(
            gradingType = Assignment.PERCENT_TYPE
        )
        val submission = baseSubmission.copy(
            grade = "85%"
        )
        val expected = baseGradedState.copy(
            graphPercent = 0.85f,
            score = "85",
            showPointsLabel = true,
            grade = "85%",
            gradeContentDescription = "85%",
            gradeCellContentDescription = "85 Out of 100 points, 85%"
        )
        val actual = GradeCellViewState.fromSubmission(context, assignment, submission)
        assertEquals(expected, actual)
    }

    @Test
    fun `Returns correct state for 'Complete-Incomplete' grading type if Complete`() {
        val assignment = baseAssignment.copy(
            gradingType = Assignment.PASS_FAIL_TYPE
        )
        val submission = baseSubmission.copy(
            grade = "complete"
        )
        val expected = baseGradedState.copy(
            graphPercent = 1.0f,
            showCompleteIcon = true,
            grade = "Complete",
            gradeCellContentDescription = ""
        )
        val actual = GradeCellViewState.fromSubmission(context, assignment, submission)
        assertEquals(expected, actual)
    }

    @Test
    fun `Returns correct state for 'Complete-Incomplete' grading type if Incomplete`() {
        val assignment = baseAssignment.copy(
            gradingType = Assignment.PASS_FAIL_TYPE
        )
        val submission = baseSubmission.copy(
            grade = "incomplete",
            score = 0.0
        )
        val expected = baseGradedState.copy(
            accentColor = 0xFF66717C.toInt(),
            graphPercent = 1.0f,
            showIncompleteIcon = true,
            grade = "Incomplete",
            gradeCellContentDescription = ""
        )
        val actual = GradeCellViewState.fromSubmission(context, assignment, submission)
        assertEquals(expected, actual)
    }

    @Test
    fun `Returns correct state for 'Complete-Incomplete' grading type if not graded`() {
        val assignment = baseAssignment.copy(
            gradingType = Assignment.PASS_FAIL_TYPE
        )
        val submission = baseSubmission.copy(
            grade = null,
            score = 0.0
        )
        val expected = GradeCellViewState.Submitted
        val actual = GradeCellViewState.fromSubmission(context, assignment, submission)
        assertEquals(expected, actual)
    }

    @Test
    fun `Returns correct state for 'Points' grading type`() {
        val expected = baseGradedState.copy(
            graphPercent = 0.85f,
            score = "85",
            showPointsLabel = true
        )
        val actual = GradeCellViewState.fromSubmission(context, baseAssignment, baseSubmission)
        assertEquals(expected, actual)
    }

    @Test
    fun `Returns correct state for 'Letter Grade' grading type`() {
        val assignment = baseAssignment.copy(
            gradingType = Assignment.LETTER_GRADE_TYPE
        )
        val submission = baseSubmission.copy(
            grade = "B+"
        )
        val expected = baseGradedState.copy(
            graphPercent = 0.85f,
            score = "85",
            showPointsLabel = true,
            grade = "B+",
            gradeContentDescription = "B+",
            outOf = "Out of 100 pts",
            outOfContentDescription = "Out of 100 points",
            gradeCellContentDescription = "85 Out of 100 points, B+"
        )
        val actual = GradeCellViewState.fromSubmission(context, assignment, submission)
        assertEquals(expected, actual)
    }

    @Test
    fun `Returns correct state for 'Letter Grade' grading type with minus`() {
        val assignment = baseAssignment.copy(
            gradingType = Assignment.LETTER_GRADE_TYPE
        )
        val submission = baseSubmission.copy(
            grade = "A-",
            enteredGrade = "A-",
            enteredScore = 91.0,
            score = 91.0
        )
        val expected = baseGradedState.copy(
            graphPercent = 0.91f,
            score = "91",
            showPointsLabel = true,
            grade = "A-",
            gradeContentDescription = "A. minus",
            gradeCellContentDescription = "91 Out of 100 points, A. minus"
        )
        val actual = GradeCellViewState.fromSubmission(context, assignment, submission)
        assertEquals(expected, actual)
    }

    @Test
    fun `Returns correct state for 'GPA Scale' grading type`() {
        val assignment = baseAssignment.copy(
            gradingType = Assignment.GPA_SCALE_TYPE
        )
        val submission = baseSubmission.copy(
            grade = "3.8 GPA"
        )
        val expected = baseGradedState.copy(
            graphPercent = 0.85f,
            score = "85",
            showPointsLabel = true,
            grade = "3.8 GPA",
            gradeContentDescription = "3.8 GPA",
            gradeCellContentDescription = "85 Out of 100 points, 3.8 GPA"
        )
        val actual = GradeCellViewState.fromSubmission(context, assignment, submission)
        assertEquals(expected, actual)
    }

    @Test
    fun `Returns empty state for 'Not Graded' grading type`() {
        val assignment = baseAssignment.copy(
            gradingType = Assignment.NOT_GRADED_TYPE
        )
        val expected = GradeCellViewState.Empty
        val actual = GradeCellViewState.fromSubmission(context, assignment, Submission())
        assertEquals(expected, actual)
    }

    @Test
    fun `Returns correct state for late penalty`() {
        val submission = baseSubmission.copy(
            pointsDeducted = 6.0,
            grade = "79",
            score = 79.0
        )
        val expected = baseGradedState.copy(
            graphPercent = 0.79f,
            score = "79",
            showPointsLabel = true,
            yourGrade = "Your Grade: 85 pts",
            latePenalty = "Late Penalty: -6 pts",
            finalGrade = "Final Grade: 79",
            gradeCellContentDescription = "79 Out of 100 points"
        )
        val actual = GradeCellViewState.fromSubmission(context, baseAssignment, submission)
        assertEquals(expected, actual)
    }

    @Test
    fun `Includes content descriptions for letter grade with minus`() {
        val assignment = baseAssignment.copy(
                gradingType = Assignment.LETTER_GRADE_TYPE
        )
        val submission = baseSubmission.copy(
                grade = "B-"
        )
        val expected = baseGradedState.copy(
                graphPercent = 0.85f,
                score = "85",
                showPointsLabel = true,
                grade = "B-",
                gradeContentDescription = "B. minus",
                gradeCellContentDescription = "85 Out of 100 points, B. minus"
        )
        val actual = GradeCellViewState.fromSubmission(context, assignment, submission)
        assertEquals(expected, actual)
    }

    @Test
    fun `Includes content descriptions for grades`() {
        val assignment = baseAssignment.copy(
                gradingType = Assignment.LETTER_GRADE_TYPE
        )
        val submission = baseSubmission.copy(
                grade = "B",
                enteredGrade = "88",
                enteredScore = 88.0,
                score = 88.0
        )
        val expected = baseGradedState.copy(
                graphPercent = 0.88f,
                score = "88",
                showPointsLabel = true,
                grade = "B",
                gradeContentDescription = "B",
                gradeCellContentDescription = "88 Out of 100 points, B"
        )
        val actual = GradeCellViewState.fromSubmission(context, assignment, submission)
        assertEquals(expected, actual)
    }

    @Test
    fun `Includes grade statistics if provided by the assignment`() {
        val assignment = baseAssignment.copy(
            scoreStatistics = AssignmentScoreStatistics(mean = 76.85, min = 56.0, max = 97.0)
        )
        val submission = baseSubmission.copy(
            grade = "B",
            enteredGrade = "A",
            enteredScore = 95.0,
            score = 88.0
        )
        val expected = GradeCellViewState.GradeStats(
            score = 88.0, // Should use score, not enteredScore
            outOf = 100.0,
            min = 56.0,
            max = 97.0,
            mean = 76.85,
            minText = "Low: 56",
            maxText = "High: 97",
            meanText = "Mean: 76.8"
        )
        val actual = GradeCellViewState.fromSubmission(context, assignment, submission) as GradeCellViewState.GradeData
        assertEquals(expected, actual.stats)
    }

    @Test
    fun `Returns empty state when assignment is quantitative and quantitative data is restricted`() {
        val assignment = baseAssignment.copy(
            gradingType = Assignment.POINTS_TYPE
        )
        val expected = GradeCellViewState.Empty
        val actual = GradeCellViewState.fromSubmission(context, assignment, Submission(), true)
        assertEquals(expected, actual)
    }

    @Test
    fun `Create excused grade cell without points when assignment is quantitative and quantitative data is restricted`() {
        val submission = baseSubmission.copy(
            excused = true
        )
        val expected = baseGradedState.copy(
            graphPercent = 1.0f,
            showCompleteIcon = true,
            grade = "Excused",
            outOf = "",
            outOfContentDescription = "",
            gradeCellContentDescription = ""
        )
        val actual = GradeCellViewState.fromSubmission(context, baseAssignment, submission, true)
        assertEquals(expected, actual)
    }

    @Test
    fun `Create letter grade cell without points when quantitative data is restricted`() {
        val assignment = baseAssignment.copy(
            gradingType = Assignment.LETTER_GRADE_TYPE
        )
        val submission = baseSubmission.copy(
            grade = "B+"
        )
        val expected = baseGradedState.copy(
            graphPercent = 1.0f,
            score = "",
            showCompleteIcon = true,
            grade = "B+",
            gradeContentDescription = "B+",
            outOf = "",
            outOfContentDescription = "",
            gradeCellContentDescription = "Grade: B+"
        )
        val actual = GradeCellViewState.fromSubmission(context, assignment, submission, true)
        assertEquals(expected, actual)
    }
}
