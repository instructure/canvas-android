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
package com.instructure.parentapp.features.assignments.details

import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.pandautils.utils.studentColor
import com.instructure.parentapp.features.assignment.details.ParentAssignmentDetailsColorProvider
import com.instructure.parentapp.util.ParentPrefs
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test

class ParentAssignmentDetailsColorProviderTest {

    @Before
    fun setUp() {
        mockkObject(ColorKeeper)
        every { ColorKeeper.getOrGenerateUserColor(any()) } returns ThemedColor(0)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `submissionAndRubricLabelColor should return ThemePrefs textButtonColor`() {
        val colorKeeper: ColorKeeper = mockk(relaxed = true)
        val parentPrefs: ParentPrefs = mockk(relaxed = true)
        every { parentPrefs.currentStudent.studentColor } returns 1
        val colorProvider = ParentAssignmentDetailsColorProvider(parentPrefs, colorKeeper)

        assertEquals(parentPrefs.currentStudent.studentColor, colorProvider.submissionAndRubricLabelColor)
    }

    @Test
    fun `getContentColor should return colorKeeper getOrGenerateColor`() {
        val colorKeeper: ColorKeeper = mockk(relaxed = true)
        val parentPrefs: ParentPrefs = mockk(relaxed = true)
        every { parentPrefs.currentStudent.studentColor } returns 1
        val colorProvider = ParentAssignmentDetailsColorProvider(parentPrefs, colorKeeper)

        val course = mockk<Course>()
        val expected = ThemedColor(0)
        val result = colorProvider.getContentColor(course)
        assertEquals(expected, result)
    }
}