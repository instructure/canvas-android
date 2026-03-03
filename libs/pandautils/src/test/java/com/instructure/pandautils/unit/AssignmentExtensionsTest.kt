/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.unit

import android.content.res.Resources
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.GradingSchemeRow
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.utils.getGrade
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AssignmentExtensionsTest {

    private lateinit var resources: Resources
    private lateinit var gradingScheme: List<GradingSchemeRow>

    @Before
    fun setup() {
        resources = mockk(relaxed = true)

        // Standard grading scheme: A+ (97-100), A (93-96), A- (90-92), B+ (87-89), etc.
        gradingScheme = listOf(
            GradingSchemeRow(name = "A+", value = 0.97),
            GradingSchemeRow(name = "A", value = 0.93),
            GradingSchemeRow(name = "A-", value = 0.90),
            GradingSchemeRow(name = "B+", value = 0.87),
            GradingSchemeRow(name = "B", value = 0.83),
            GradingSchemeRow(name = "B-", value = 0.80),
            GradingSchemeRow(name = "C+", value = 0.77),
            GradingSchemeRow(name = "C", value = 0.73),
            GradingSchemeRow(name = "C-", value = 0.70),
            GradingSchemeRow(name = "D", value = 0.60),
            GradingSchemeRow(name = "F", value = 0.0)
        )
    }

    @Test
    fun getGrade_letterGradeWithRestrictQuantitativeData_actualLetterGrade_returnsLetterGrade() {
        val assignment = Assignment(
            pointsPossible = 100.0,
            gradingType = Assignment.LETTER_GRADE_TYPE
        )
        val submission = Submission(
            grade = "A-",
            score = 91.0
        )

        val result = assignment.getGrade(
            submission = submission,
            resources = resources,
            restrictQuantitativeData = true,
            gradingScheme = gradingScheme
        )

        assertEquals("A-", result.text)
    }

    @Test
    fun getGrade_letterGradeWithRestrictQuantitativeData_numericGradeWithPoints_convertsToLetterGrade() {
        val assignment = Assignment(
            pointsPossible = 100.0,
            gradingType = Assignment.LETTER_GRADE_TYPE
        )
        val submission = Submission(
            grade = "91",  // Numeric grade instead of letter grade
            score = 91.0
        )

        val result = assignment.getGrade(
            submission = submission,
            resources = resources,
            restrictQuantitativeData = true,
            gradingScheme = gradingScheme
        )

        assertEquals("A-", result.text)
    }

    @Test
    fun getGrade_letterGradeWithRestrictQuantitativeData_numericGradeZeroPoints_convertsToLetterGrade() {
        val assignment = Assignment(
            pointsPossible = 0.0,
            gradingType = Assignment.LETTER_GRADE_TYPE
        )
        val submission = Submission(
            grade = "90",  // Numeric grade representing 90%
            score = 90.0
        )

        val result = assignment.getGrade(
            submission = submission,
            resources = resources,
            restrictQuantitativeData = true,
            gradingScheme = gradingScheme
        )

        assertEquals("A-", result.text)
    }

    @Test
    fun getGrade_letterGradeWithRestrictQuantitativeData_highNumericGradeZeroPoints_convertsToHighLetterGrade() {
        val assignment = Assignment(
            pointsPossible = 0.0,
            gradingType = Assignment.LETTER_GRADE_TYPE
        )
        val submission = Submission(
            grade = "98",  // Should convert to A+
            score = 98.0
        )

        val result = assignment.getGrade(
            submission = submission,
            resources = resources,
            restrictQuantitativeData = true,
            gradingScheme = gradingScheme
        )

        assertEquals("A+", result.text)
    }

    @Test
    fun getGrade_letterGradeWithRestrictQuantitativeData_lowNumericGradeZeroPoints_convertsToLowLetterGrade() {
        val assignment = Assignment(
            pointsPossible = 0.0,
            gradingType = Assignment.LETTER_GRADE_TYPE
        )
        val submission = Submission(
            grade = "75",  // Should convert to C
            score = 75.0
        )

        val result = assignment.getGrade(
            submission = submission,
            resources = resources,
            restrictQuantitativeData = true,
            gradingScheme = gradingScheme
        )

        assertEquals("C", result.text)
    }

    @Test
    fun getGrade_letterGradeWithRestrictQuantitativeData_numericGradeNoGradingScheme_returnsNumericGrade() {
        val assignment = Assignment(
            pointsPossible = 100.0,
            gradingType = Assignment.LETTER_GRADE_TYPE
        )
        val submission = Submission(
            grade = "91",
            score = 91.0
        )

        val result = assignment.getGrade(
            submission = submission,
            resources = resources,
            restrictQuantitativeData = true,
            gradingScheme = emptyList()
        )

        assertEquals("91", result.text)
    }

    @Test
    fun getGrade_letterGradeWithoutRestrictQuantitativeData_showsScoreAndGrade() {
        val assignment = Assignment(
            pointsPossible = 100.0,
            gradingType = Assignment.LETTER_GRADE_TYPE
        )
        val submission = Submission(
            grade = "A-",
            score = 91.0
        )

        every {
            resources.getString(any(), any(), any(), any())
        } returns "91 / 100 (A-)"

        val result = assignment.getGrade(
            submission = submission,
            resources = resources,
            restrictQuantitativeData = false,
            gradingScheme = gradingScheme
        )

        assertEquals("91 / 100 (A-)", result.text)
    }

    @Test
    fun getGrade_gpaScaleWithRestrictQuantitativeData_numericGrade_convertsToLetterGrade() {
        val assignment = Assignment(
            pointsPossible = 100.0,
            gradingType = Assignment.GPA_SCALE_TYPE
        )
        val submission = Submission(
            grade = "85",  // Numeric grade
            score = 85.0
        )

        val result = assignment.getGrade(
            submission = submission,
            resources = resources,
            restrictQuantitativeData = true,
            gradingScheme = gradingScheme
        )

        assertEquals("B", result.text)
    }

    @Test
    fun getGrade_letterGradeWithRestrictQuantitativeData_excusedSubmission_returnsExcused() {
        val assignment = Assignment(
            pointsPossible = 100.0,
            gradingType = Assignment.LETTER_GRADE_TYPE
        )
        val submission = Submission(
            excused = true,
            grade = "90",
            score = 90.0
        )

        every { resources.getString(any()) } returns "Excused"

        val result = assignment.getGrade(
            submission = submission,
            resources = resources,
            restrictQuantitativeData = true,
            gradingScheme = gradingScheme
        )

        assertEquals("Excused", result.text)
    }

    @Test
    fun getGrade_letterGradeWithRestrictQuantitativeData_noSubmission_returnsNoGrade() {
        val assignment = Assignment(
            pointsPossible = 100.0,
            gradingType = Assignment.LETTER_GRADE_TYPE
        )

        val result = assignment.getGrade(
            submission = null,
            resources = resources,
            restrictQuantitativeData = true,
            gradingScheme = gradingScheme
        )

        assertEquals("-", result.text)
    }
}
