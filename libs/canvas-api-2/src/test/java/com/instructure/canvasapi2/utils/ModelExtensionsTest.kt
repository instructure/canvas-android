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
package com.instructure.canvasapi2.utils

import com.instructure.canvasapi2.models.GradingSchemeRow
import com.instructure.canvasapi2.models.Submission
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class ModelExtensionsTest {

    private val gradingSchemes = listOf(
        GradingSchemeRow("A", 0.95),
        GradingSchemeRow("B", 0.9),
        GradingSchemeRow("C", 0.8),
        GradingSchemeRow("D", 0.7),
        GradingSchemeRow("F", 0.6)
    )

    @Test
    fun `Score to letter grade returns empty string when max score is 0`() {
        val result = convertScoreToLetterGrade(1.0, 0.0, gradingSchemes)

        assertEquals("", result)
    }

    @Test
    fun `Score to letter grade returns empty string when grading schemes is empty`() {
        val result = convertScoreToLetterGrade(1.0, 10.0, emptyList())

        assertEquals("", result)
    }

    @Test
    fun `Score to letter grade returns last item when nothing matches`() {
        val result = convertScoreToLetterGrade(1.0, 10.0, gradingSchemes)

        assertEquals("F", result)
    }

    @Test
    fun `Score to letter grade returns last item when zero points`() {
        val result = convertScoreToLetterGrade(0.0, 10.0, gradingSchemes)

        assertEquals("F", result)
    }

    @Test
    fun `Score to letter grade returns first item when overgraded`() {
        val result = convertScoreToLetterGrade(15.0, 10.0, gradingSchemes)

        assertEquals("A", result)
    }

    @Test
    fun `Score to letter grade returns items inclusively`() {
        val result = convertScoreToLetterGrade(95.0, 100.0, gradingSchemes)

        assertEquals("A", result)
    }

    @Test
    fun `Score to letter grade returns correct items with more decimal places`() {
        val result = convertScoreToLetterGrade(94.9999, 100.0, gradingSchemes)

        assertEquals("B", result)
    }

    @Test
    fun `Score to letter grade returns the last value with negatice score`() {
        val result = convertScoreToLetterGrade(-50.0, 100.0, gradingSchemes)

        assertEquals("F", result)
    }

    @Test
    fun `Score to letter grade returns C for 80 percent`() {
        val result = convertScoreToLetterGrade(80.0, 100.0, gradingSchemes)

        assertEquals("C", result)
    }

    @Test
    fun `Score to letter grade returns D for 70 percent`() {
        val result = convertScoreToLetterGrade(70.0, 100.0, gradingSchemes)

        assertEquals("D", result)
    }

    @Test
    fun `Convert percent to letter grade returns empty string when no grading schemes is empty`() {
        val result = convertPercentScoreToLetterGrade(1.0, emptyList())

        assertEquals("", result)
    }

    @Test
    fun `Convert percent to letter grade returns last item when nothing matches`() {
        val result = convertPercentScoreToLetterGrade( 0.1, gradingSchemes)

        assertEquals("F", result)
    }

    @Test
    fun `Score to letter grade handles floating point precision for 1 point assignment with A grade`() {
        val standardGradingScheme = listOf(
            GradingSchemeRow("A", 0.9),
            GradingSchemeRow("B", 0.8),
            GradingSchemeRow("C", 0.7),
            GradingSchemeRow("D", 0.6),
            GradingSchemeRow("F", 0.0)
        )
        val result = convertScoreToLetterGrade(0.9, 1.0, standardGradingScheme)

        assertEquals("A", result)
    }

    @Test
    fun `Score to letter grade handles floating point precision for 1 point assignment with B grade`() {
        val standardGradingScheme = listOf(
            GradingSchemeRow("A", 0.9),
            GradingSchemeRow("B", 0.8),
            GradingSchemeRow("C", 0.7),
            GradingSchemeRow("D", 0.6),
            GradingSchemeRow("F", 0.0)
        )
        val result = convertScoreToLetterGrade(0.8, 1.0, standardGradingScheme)

        assertEquals("B", result)
    }

    @Test
    fun `Score to letter grade handles conversion from percentage to score for 1 point assignment`() {
        val standardGradingScheme = listOf(
            GradingSchemeRow("A", 0.9),
            GradingSchemeRow("B", 0.8),
            GradingSchemeRow("C", 0.7),
            GradingSchemeRow("D", 0.6),
            GradingSchemeRow("F", 0.0)
        )
        val percentage = 90.0f
        val pointsPossible = 1.0
        val score = (percentage / 100) * pointsPossible
        val result = convertScoreToLetterGrade(score, pointsPossible, standardGradingScheme)

        assertEquals("A", result)
    }

    @Test
    fun `Score to letter grade handles exact threshold values`() {
        val standardGradingScheme = listOf(
            GradingSchemeRow("A", 0.9),
            GradingSchemeRow("B", 0.8),
            GradingSchemeRow("C", 0.7),
            GradingSchemeRow("D", 0.6),
            GradingSchemeRow("F", 0.0)
        )
        assertEquals("A", convertScoreToLetterGrade(90.0, 100.0, standardGradingScheme))
        assertEquals("B", convertScoreToLetterGrade(80.0, 100.0, standardGradingScheme))
        assertEquals("C", convertScoreToLetterGrade(70.0, 100.0, standardGradingScheme))
        assertEquals("D", convertScoreToLetterGrade(60.0, 100.0, standardGradingScheme))
    }

    @Test
    fun `Counts matching custom status submissions`() {
        val list = listOf(
            Submission(
                assignmentId = 1,
                excused = false,
                customGradeStatusId = 1L,
                workflowState = "submitted",
                isGradeMatchesCurrentSubmission = false,
                grade = "90"
            ),
            Submission(
                assignmentId = 1,
                excused = false,
                customGradeStatusId = 1L,
                workflowState = "graded",
                isGradeMatchesCurrentSubmission = false,
                grade = "85"
            ),
            Submission(
                assignmentId = 1,
                excused = false,
                customGradeStatusId = 1L,
                workflowState = "missing",
                isGradeMatchesCurrentSubmission = false,
                grade = "70"
            ),
            Submission(
                assignmentId = 1,
                excused = false,
                customGradeStatusId = null,
                workflowState = "submitted",
                isGradeMatchesCurrentSubmission = false,
                grade = "90"
            ),
            Submission(
                assignmentId = 1,
                excused = true,
                customGradeStatusId = 1L,
                workflowState = "submitted",
                isGradeMatchesCurrentSubmission = false,
                grade = "90"
            ),
            Submission(
                assignmentId = 1,
                excused = false,
                customGradeStatusId = 1L,
                workflowState = "submitted",
                isGradeMatchesCurrentSubmission = true,
                grade = "90"
            ),
            Submission(
                assignmentId = 1,
                excused = false,
                customGradeStatusId = 1L,
                workflowState = "submitted",
                isGradeMatchesCurrentSubmission = true,
                grade = null
            )
        )

        val count = list.countCustomGradeStatus("submitted", "graded", "pending_review")
        assertEquals(3, count)
    }

    @Test
    fun `Counts all matching when requireNoGradeMatch is false`() {
        val list = listOf(
            Submission(
                assignmentId = 1,
                excused = false,
                customGradeStatusId = 1L,
                workflowState = "submitted",
                isGradeMatchesCurrentSubmission = true,
                grade = "80"
            ),
            Submission(
                assignmentId = 1,
                excused = false,
                customGradeStatusId = 1L,
                workflowState = "submitted",
                isGradeMatchesCurrentSubmission = false,
                grade = "75"
            ),
            Submission(
                assignmentId = 1,
                excused = false,
                customGradeStatusId = 1L,
                workflowState = "submitted",
                isGradeMatchesCurrentSubmission = true,
                grade = null
            ),
            Submission(
                assignmentId = 1,
                excused = false,
                customGradeStatusId = null,
                workflowState = "submitted",
                isGradeMatchesCurrentSubmission = false,
                grade = "88"
            )
        )

        val count = list.countCustomGradeStatus("submitted", requireNoGradeMatch = false)
        assertEquals(3, count)
    }

    @Test
    fun `Returns 0 for empty list`() {
        val count = emptyList<Submission>().countCustomGradeStatus("submitted")
        assertEquals(0, count)
    }

    @Test
    fun `Returns 0 when no submissions match`() {
        val list = listOf(
            Submission(
                assignmentId = 1,
                excused = true,
                customGradeStatusId = 1L,
                workflowState = "submitted",
                isGradeMatchesCurrentSubmission = false,
                grade = "90"
            ),
            Submission(
                assignmentId = 1,
                excused = false,
                customGradeStatusId = null,
                workflowState = "submitted",
                isGradeMatchesCurrentSubmission = false,
                grade = "90"
            ),
            Submission(
                assignmentId = 1,
                excused = false,
                customGradeStatusId = 1L,
                workflowState = "missing",
                isGradeMatchesCurrentSubmission = false,
                grade = "90"
            ),
            Submission(
                assignmentId = 1,
                excused = false,
                customGradeStatusId = 1L,
                workflowState = "submitted",
                isGradeMatchesCurrentSubmission = true,
                grade = "95"
            )
        )

        val count = list.countCustomGradeStatus("submitted", "graded")
        assertEquals(0, count)
    }

    @Test
    fun `correctAttemptNumbers returns empty list when input is empty`() {
        val result = emptyList<Submission?>().correctAttemptNumbers()

        assertEquals(emptyList<Submission>(), result)
    }

    @Test
    fun `correctAttemptNumbers filters out null submissions`() {
        val submissions = listOf(
            Submission(assignmentId = 1, attempt = 0L, submittedAt = Date(1000)),
            null,
            Submission(assignmentId = 1, attempt = 0L, submittedAt = Date(2000))
        )

        val result = submissions.correctAttemptNumbers()

        assertEquals(2, result.size)
        assertEquals(2L, result[0].attempt) // Newest gets highest number
        assertEquals(1L, result[1].attempt)
    }

    @Test
    fun `correctAttemptNumbers returns original submissions when all have valid attempt numbers`() {
        val submissions = listOf(
            Submission(assignmentId = 1, attempt = 3L, submittedAt = Date(3000)),
            Submission(assignmentId = 1, attempt = 2L, submittedAt = Date(2000)),
            Submission(assignmentId = 1, attempt = 1L, submittedAt = Date(1000))
        )

        val result = submissions.correctAttemptNumbers()

        assertEquals(3, result.size)
        assertEquals(3L, result[0].attempt)
        assertEquals(2L, result[1].attempt)
        assertEquals(1L, result[2].attempt)
    }

    @Test
    fun `correctAttemptNumbers corrects attempt numbers when any submission has attempt 0`() {
        val submissions = listOf(
            Submission(assignmentId = 1, attempt = 0L, submittedAt = Date(3000)),
            Submission(assignmentId = 1, attempt = 0L, submittedAt = Date(2000)),
            Submission(assignmentId = 1, attempt = 0L, submittedAt = Date(1000))
        )

        val result = submissions.correctAttemptNumbers()

        assertEquals(3, result.size)
        assertEquals(3L, result[0].attempt) // Newest (3000) gets attempt 3
        assertEquals(2L, result[1].attempt) // Middle (2000) gets attempt 2
        assertEquals(1L, result[2].attempt) // Oldest (1000) gets attempt 1
    }

    @Test
    fun `correctAttemptNumbers assigns highest attempt number to newest submission`() {
        val oldestDate = Date(1000)
        val middleDate = Date(2000)
        val newestDate = Date(3000)

        val submissions = listOf(
            Submission(assignmentId = 1, attempt = 0L, submittedAt = middleDate),
            Submission(assignmentId = 1, attempt = 0L, submittedAt = oldestDate),
            Submission(assignmentId = 1, attempt = 0L, submittedAt = newestDate)
        )

        val result = submissions.correctAttemptNumbers()

        assertEquals(3, result.size)
        assertEquals(3L, result[0].attempt)
        assertEquals(newestDate, result[0].submittedAt)
        assertEquals(2L, result[1].attempt)
        assertEquals(middleDate, result[1].submittedAt)
        assertEquals(1L, result[2].attempt)
        assertEquals(oldestDate, result[2].submittedAt)
    }

    @Test
    fun `correctAttemptNumbers handles single submission with attempt 0`() {
        val submissions = listOf(
            Submission(assignmentId = 1, attempt = 0L, submittedAt = Date(1000))
        )

        val result = submissions.correctAttemptNumbers()

        assertEquals(1, result.size)
        assertEquals(1L, result[0].attempt)
    }

    @Test
    fun `correctAttemptNumbers corrects when only one submission has invalid attempt`() {
        val submissions = listOf(
            Submission(assignmentId = 1, attempt = 2L, submittedAt = Date(2000)),
            Submission(assignmentId = 1, attempt = 0L, submittedAt = Date(1000))
        )

        val result = submissions.correctAttemptNumbers()

        assertEquals(2, result.size)
        assertEquals(2L, result[0].attempt) // Newest
        assertEquals(1L, result[1].attempt) // Oldest
    }
}