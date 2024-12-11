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

package com.instructure.pandautils.features.grades

import android.content.Context
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseGrade
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.pandautils.R
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class GradeFormatterTest {

    private val context: Context = mockk(relaxed = true)
    private val gradeFormatter = GradeFormatter(context)

    @Before
    fun setup() {
        every { context.getString(R.string.noGradeText) } returns "N/A"
    }

    @Test
    fun `Final grade maps correctly when grade is null`() = runTest {
        val result = gradeFormatter.getGradeString(course = null, courseGrade = null, isFinal = true)

        Assert.assertEquals("N/A", result)
    }

    @Test
    fun `Final grade maps correctly when no final grade`() = runTest {
        val courseGrade = CourseGrade(noFinalGrade = true)

        val result = gradeFormatter.getGradeString(course = null, courseGrade = courseGrade, isFinal = true)

        Assert.assertEquals("N/A", result)
    }

    @Test
    fun `Final grade maps correctly with grade string and score`() = runTest {
        val courseGrade = CourseGrade(finalScore = 95.0, finalGrade = "A")

        val result = gradeFormatter.getGradeString(course = null, courseGrade = courseGrade, isFinal = true)

        Assert.assertEquals("95% A", result)
    }

    @Test
    fun `Current grade maps correctly when grade is null`() = runTest {
        val result = gradeFormatter.getGradeString(course = null, courseGrade = null, isFinal = false)

        Assert.assertEquals("N/A", result)
    }

    @Test
    fun `Current grade maps correctly when no current grade`() = runTest {
        val courseGrade = CourseGrade(noCurrentGrade = true)

        val result = gradeFormatter.getGradeString(course = null, courseGrade = courseGrade, isFinal = false)

        Assert.assertEquals("N/A", result)
    }

    @Test
    fun `Current grade maps correctly with grade string and score`() = runTest {
        val courseGrade = CourseGrade(currentScore = 88.5, currentGrade = "B+")

        val result = gradeFormatter.getGradeString(course = null, courseGrade = courseGrade, isFinal = false)

        Assert.assertEquals("88.5% B+", result)
    }

    @Test
    fun `Grade maps correctly when restricted and has grade string`() = runTest {
        val course = Course(id = 1L, settings = CourseSettings(restrictQuantitativeData = true))
        val courseGrade = CourseGrade(currentGrade = "B+")

        val result = gradeFormatter.getGradeString(course = course, courseGrade = courseGrade, isFinal = false)

        Assert.assertEquals("B+", result)
    }

    @Test
    fun `Grade maps correctly when restricted without grade string but with grading scheme`() = runTest {
        val course = Course(
            id = 1L,
            settings = CourseSettings(restrictQuantitativeData = true),
            gradingSchemeRaw = listOf(
                listOf("A", 0.9),
                listOf("B", 0.8)
            )
        )
        val courseGrade = CourseGrade(currentScore = 85.0)

        val result = gradeFormatter.getGradeString(course = course, courseGrade = courseGrade, isFinal = false)

        Assert.assertEquals("B", result)
    }

    @Test
    fun `Grade maps correctly when unrestricted`() = runTest {
        val course = Course(id = 1L, settings = CourseSettings(restrictQuantitativeData = false))
        val courseGrade = CourseGrade(currentScore = 92.0, currentGrade = "A")

        val result = gradeFormatter.getGradeString(course = course, courseGrade = courseGrade, isFinal = false)

        Assert.assertEquals("92% A", result)
    }

    @Test
    fun `Point based grades map correctly`() = runTest {
        val course = Course(id = 1L, settings = CourseSettings(restrictQuantitativeData = false), pointsBasedGradingScheme = true, scalingFactor = 10.0)
        val courseGrade = CourseGrade(currentScore = 90.0, currentGrade = "A")

        val result = gradeFormatter.getGradeString(course = course, courseGrade = courseGrade, isFinal = false)

        Assert.assertEquals("9.00 / 10.00 A", result)
    }
}
