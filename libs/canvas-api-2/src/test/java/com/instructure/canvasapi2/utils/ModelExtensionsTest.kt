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
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test

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
}