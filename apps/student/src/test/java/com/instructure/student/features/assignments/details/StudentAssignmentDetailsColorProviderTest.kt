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
package com.instructure.student.features.assignments.details

import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ThemedColor
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test

class StudentAssignmentDetailsColorProviderTest {

    @Before
    fun setUp() {
        mockkObject(ThemePrefs)
        every { ThemePrefs.textButtonColor } returns 1

        mockkObject(ColorKeeper)
        every { ColorKeeper.getOrGenerateColor(any()) } returns ThemedColor(0)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `submissionAndRubricLabelColor should return ThemePrefs textButtonColor`() {
        val colorKeeper: ColorKeeper = mockk(relaxed = true)
        val colorProvider = StudentAssignmentDetailsColorProvider(colorKeeper)

        assertEquals(ThemePrefs.textButtonColor, colorProvider.submissionAndRubricLabelColor)
    }

    @Test
    fun `getContentColor should return colorKeeper getOrGenerateColor`() {
        val colorKeeper: ColorKeeper = mockk(relaxed = true)
        val colorProvider = StudentAssignmentDetailsColorProvider(colorKeeper)

        val course = mockk<Course>()
        val expected = ThemedColor(0)
        val result = colorProvider.getContentColor(course)
        assertEquals(expected, result)
    }
}